package com.example.ui.glass

import android.content.Context
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.PowerManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DesignTokens
import com.example.ui.theme.GlassPalette
import com.example.ui.theme.GlassVariant
import com.example.ui.theme.LocalGlassConfig
import kotlin.math.max

/**
 * Holds the shared backdrop GraphicsLayer captured from the background content
 * (source node) so floating Liquid Glass controls (effect nodes) can sample, blur,
 * and refract what is underneath without infinite draw recursion.
 */
@Stable
class GlassBackdropState(
    val graphicsLayer: GraphicsLayer
) {
    var rootBounds by mutableStateOf(Rect.Zero)
    var averageLuminance by mutableFloatStateOf(0.45f)

    // Plain non-snapshot flags to prevent circular RenderNode recording on RenderThread
    var isRecordingSource: Boolean = false
    var hasRecordedContent: Boolean = false
}

val LocalGlassBackdrop = staticCompositionLocalOf<GlassBackdropState?> { null }

@Composable
fun rememberGlassBackdropState(): GlassBackdropState {
    val layer = rememberGraphicsLayer()
    return remember(layer) { GlassBackdropState(layer) }
}

/**
 * Source-node modifier: records the underlying background/photo content into
 * [GlassBackdropState.graphicsLayer] AND draws it to the screen.
 * Guards against circular RenderNode references with [GlassBackdropState.isRecordingSource].
 */
fun Modifier.glassBackdropSource(
    backdropState: GlassBackdropState?,
    estimatedLuminance: Float = 0.45f
): Modifier = if (backdropState == null) {
    this
} else {
    this
        .onGloballyPositioned { coords ->
            backdropState.rootBounds = coords.boundsInRoot()
            backdropState.averageLuminance = estimatedLuminance
        }
        .drawWithContent {
            if (!backdropState.isRecordingSource && size.width > 1f && size.height > 1f) {
                backdropState.isRecordingSource = true
                try {
                    backdropState.graphicsLayer.record(
                        size = IntSize(size.width.toInt(), size.height.toInt())
                    ) {
                        this@drawWithContent.drawContent()
                    }
                    backdropState.hasRecordedContent = true
                } finally {
                    backdropState.isRecordingSource = false
                }
            }
            this@drawWithContent.drawContent()
        }
}

/**
 * Real-time device tilt sensor state (smoothed accelerometer / gravity vector)
 * so the specular highlight bevel on Liquid Glass controls shifts naturally as you tilt the phone.
 */
@Composable
fun rememberDeviceTiltOffset(): Offset {
    val context = LocalContext.current
    var tiltX by remember { mutableFloatStateOf(-0.65f) }
    var tiltY by remember { mutableFloatStateOf(-0.75f) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                val values = event?.values ?: return
                if (values.size >= 2) {
                    val targetX = (-values[0] / 9.81f).coerceIn(-1f, 1f)
                    val targetY = (-values[1] / 9.81f).coerceIn(-1f, 1f)
                    tiltX = tiltX * 0.85f + targetX * 0.15f
                    tiltY = tiltY * 0.85f + targetY * 0.15f
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        if (sensor != null) {
            sensorManager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    return Offset(tiltX, tiltY)
}

/**
 * Custom AGSL RuntimeShader for API 33+ implementing:
 * 1. Rounded-box Signed Distance Field (SDF) edge lensing & caustic rim profile
 * 2. Chromatic dispersion (RGB channel separation along the curved bevel rim)
 * 3. Top-left bevel specular highlight reacting to smoothed device tilt & touch point
 * 4. Fine optical noise grain & soft inner glow
 */
const val LIQUID_GLASS_AGSL = """
    uniform float2 resolution;
    uniform float cornerRadius;
    uniform float rimWidth;
    uniform float refractionStrength;
    uniform float dispersion;
    uniform float specularIntensity;
    uniform float noiseAlpha;
    uniform float2 lightDir;
    uniform float4 tintColor;

    float sdRoundedBox(float2 p, float2 b, float r) {
        float2 q = abs(p) - b + r;
        return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
    }

    float hash(float2 p) {
        return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453123);
    }

    half4 main(float2 fragCoord) {
        float2 halfSize = max(resolution * 0.5, float2(1.0));
        float r = min(cornerRadius, min(halfSize.x, halfSize.y));
        float2 p = fragCoord - halfSize;
        float d = sdRoundedBox(p, halfSize, r);

        // Rim factor: 1.0 right at the outer edge, smoothly decaying inward over rimWidth
        float rim = clamp(1.0 + d / max(rimWidth, 1.0), 0.0, 1.0);
        float lensCurve = rim * rim * (3.0 - 2.0 * rim);

        // Outward normal approximation from rounded box center
        float2 norm = length(p) > 0.001 ? normalize(p) : float2(0.0, -1.0);

        // Specular bevel highlight reacting to lightDir (top-left bevel + opposite rim catchlight)
        float2 safeLight = length(lightDir) > 0.001 ? normalize(lightDir) : float2(-0.65, -0.75);
        float dirDot = dot(norm, safeLight);
        float primarySpec = pow(max(dirDot, 0.0), 2.6) * lensCurve * specularIntensity;
        float secondarySpec = pow(max(-dirDot, 0.0), 4.2) * lensCurve * (specularIntensity * 0.45);

        // Chromatic dispersion along the curved rim (prismatic RGB fringe)
        float prismShift = lensCurve * dispersion * (0.45 + 0.55 * abs(dirDot));
        half3 chromaticRim = half3(
            primarySpec + prismShift * 0.85,
            primarySpec + secondarySpec * 0.5,
            primarySpec + secondarySpec + prismShift * 1.15
        );

        // Optical caustic inner glow from SDF curvature
        float innerCaustic = (1.0 - lensCurve) * 0.12 * specularIntensity;

        // Fine optical noise grain
        float grain = (hash(fragCoord) - 0.5) * noiseAlpha;

        half3 baseRgb = half3(tintColor.rgb) + half3(innerCaustic) + chromaticRim + half(grain);
        float finalAlpha = clamp(tintColor.a + lensCurve * 0.28 * specularIntensity, 0.0, 0.96);
        return half4(clamp(baseRgb, half3(0.0), half3(1.0)) * half(finalAlpha), half(finalAlpha));
    }
"""

/**
 * Applies the complete Apple iOS 26 Liquid Glass material to any floating control:
 * - Samples, blurs, and refracts the captured [LocalGlassBackdrop] layer (zero circular recursion)
 * - Applies AGSL rounded-box SDF lensing + chromatic dispersion + tilt specular bevel on API 33+
 * - Applies RenderEffect blur on API 31+
 * - Automatically falls back when Reduce Glass or Battery Saver is active
 * - Increases refraction and drop shadow thickness when [expansionProgress] > 0f (e.g. button -> menu)
 */
fun Modifier.liquidGlass(
    shape: Shape = DesignTokens.ShapeCard,
    cornerRadius: Dp = DesignTokens.RadiusCard,
    variantOverride: GlassVariant? = null,
    tintOverride: Color? = null,
    expansionProgress: Float = 0f,
    interactiveTouchPoint: Offset? = null,
    elevationOverride: Dp? = null
): Modifier = composed {
    val config = LocalGlassConfig.current
    val backdropState = LocalGlassBackdrop.current
    val context = LocalContext.current
    val density = LocalDensity.current

    val isBatterySaver = remember(context) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        pm?.isPowerSaveMode == true
    }

    val effectiveReduceGlass = config.reduceGlass || isBatterySaver
    val variant = variantOverride ?: config.variant
    val intensity = config.intensity.coerceIn(0f, 1f)

    var itemBoundsInRoot by remember { mutableStateOf(Rect.Zero) }

    val cornerPx = with(density) { cornerRadius.toPx() }
    val rimWidthPx = with(density) { (config.rimWidthDp * (1f + 0.35f * expansionProgress)).dp.toPx() }
    val refractionPx = config.refractionStrength * intensity * (1f + 0.5f * expansionProgress)
    val blurPx = with(density) { (config.blurRadiusDp * intensity * (1f + 0.25f * expansionProgress)).dp.toPx() }
    val shadowDp = elevationOverride ?: ((config.shadowElevationDp * (1f + 0.45f * expansionProgress)).dp)

    val adaptiveTint = remember(config, variant, tintOverride, backdropState?.averageLuminance) {
        if (tintOverride != null) {
            tintOverride
        } else {
            val lum = backdropState?.averageLuminance ?: config.backdropLuminance
            val autoDarkGlass = config.isDarkTheme || lum < 0.32f
            val baseAlphaMultiplier = if (config.highContrastGlass) 1.35f else 1f
            when (variant) {
                GlassVariant.REGULAR -> if (autoDarkGlass) {
                    GlassPalette.GlassRegularDark.copy(
                        alpha = (0.54f * intensity * baseAlphaMultiplier).coerceIn(0.25f, 0.88f)
                    )
                } else {
                    GlassPalette.GlassRegularLight.copy(
                        alpha = (0.60f * intensity * baseAlphaMultiplier).coerceIn(0.28f, 0.90f)
                    )
                }
                GlassVariant.CLEAR -> if (autoDarkGlass) {
                    GlassPalette.GlassClearDark.copy(
                        alpha = (0.36f * intensity * baseAlphaMultiplier).coerceIn(0.18f, 0.72f)
                    )
                } else {
                    GlassPalette.GlassClearLight.copy(
                        alpha = (0.38f * intensity * baseAlphaMultiplier).coerceIn(0.18f, 0.72f)
                    )
                }
                GlassVariant.TINTED -> config.accentColor.copy(
                    alpha = (0.58f * intensity * baseAlphaMultiplier).coerceIn(0.32f, 0.88f)
                )
            }
        }
    }

    val backdropRenderLayer = if (!effectiveReduceGlass && backdropState != null) {
        rememberGraphicsLayer()
    } else {
        null
    }

    val agslShader = remember(effectiveReduceGlass) {
        if (!effectiveReduceGlass && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runCatching { RuntimeShader(LIQUID_GLASS_AGSL) }.getOrNull()
        } else {
            null
        }
    }

    this
        .onGloballyPositioned { coords ->
            itemBoundsInRoot = coords.boundsInRoot()
        }
        .drawWithCache {
            val outline = shape.createOutline(size, layoutDirection, this)
            val clipPath = Path().apply {
                when (outline) {
                    is Outline.Rounded -> addRoundRect(outline.roundRect)
                    is Outline.Rectangle -> addRect(outline.rect)
                    is Outline.Generic -> addPath(outline.path)
                }
            }

            val lightVec = if (interactiveTouchPoint != null && size.width > 0f && size.height > 0f) {
                val dx = (interactiveTouchPoint.x - size.width * 0.5f) / (size.width * 0.5f)
                val dy = (interactiveTouchPoint.y - size.height * 0.5f) / (size.height * 0.5f)
                Offset(dx.coerceIn(-1f, 1f), dy.coerceIn(-1f, 1f))
            } else {
                Offset(config.lightDirectionX, config.lightDirectionY)
            }

            if (backdropRenderLayer != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val safeBlur = blurPx.coerceIn(1f, 60f)
                backdropRenderLayer.renderEffect = RenderEffect.createBlurEffect(
                    safeBlur,
                    safeBlur,
                    Shader.TileMode.CLAMP
                ).asComposeRenderEffect()
            }

            val agslBrush = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && agslShader != null) {
                agslShader.setFloatUniform("resolution", size.width.coerceAtLeast(1f), size.height.coerceAtLeast(1f))
                agslShader.setFloatUniform("cornerRadius", cornerPx)
                agslShader.setFloatUniform("rimWidth", rimWidthPx)
                agslShader.setFloatUniform("refractionStrength", refractionPx)
                agslShader.setFloatUniform("dispersion", config.chromaticDispersion * intensity)
                agslShader.setFloatUniform("specularIntensity", config.specularIntensity * intensity)
                agslShader.setFloatUniform("noiseAlpha", config.noiseGrainAlpha * intensity)
                agslShader.setFloatUniform("lightDir", lightVec.x, lightVec.y)
                agslShader.setFloatUniform(
                    "tintColor",
                    adaptiveTint.red,
                    adaptiveTint.green,
                    adaptiveTint.blue,
                    adaptiveTint.alpha
                )
                ShaderBrush(agslShader)
            } else {
                null
            }

            onDrawWithContent {
                // Never recurse if this control was accidentally placed inside a backdrop source tree
                val canSampleBackdrop = !effectiveReduceGlass &&
                    backdropState != null &&
                    !backdropState.isRecordingSource &&
                    backdropState.hasRecordedContent &&
                    backdropRenderLayer != null &&
                    size.width > 1f &&
                    size.height > 1f

                // 1. Layered soft drop shadow outside the glass rim
                val shadowPx = shadowDp.toPx()
                if (shadowPx > 0.5f) {
                    drawRoundRect(
                        color = Color.Black.copy(alpha = if (config.isDarkTheme) 0.42f else 0.14f),
                        topLeft = Offset(0f, shadowPx * 0.28f),
                        size = size,
                        cornerRadius = CornerRadius(cornerPx, cornerPx)
                    )
                    drawRoundRect(
                        color = Color.Black.copy(alpha = if (config.isDarkTheme) 0.24f else 0.07f),
                        topLeft = Offset(0f, shadowPx * 0.55f),
                        size = size,
                        cornerRadius = CornerRadius(cornerPx * 1.05f, cornerPx * 1.05f)
                    )
                }

                clipPath(clipPath) {
                    if (effectiveReduceGlass) {
                        val fallbackColor = if (config.isDarkTheme) {
                            Color(0xEE161F30)
                        } else {
                            Color(0xF2FFFFFF)
                        }
                        drawRect(color = fallbackColor)
                        if (variant == GlassVariant.TINTED) {
                            drawRect(color = config.accentColor.copy(alpha = 0.28f))
                        }
                    } else {
                        // 2. Sample, refract (convex lens scale), and blur the captured backdrop slice
                        if (canSampleBackdrop && backdropState != null && backdropRenderLayer != null) {
                            val offsetX = itemBoundsInRoot.left - backdropState.rootBounds.left
                            val offsetY = itemBoundsInRoot.top - backdropState.rootBounds.top
                            val lensScale = 1f + (0.045f * intensity * (1f + 0.3f * expansionProgress))
                            backdropRenderLayer.record(
                                size = IntSize(
                                    size.width.toInt().coerceAtLeast(1),
                                    size.height.toInt().coerceAtLeast(1)
                                )
                            ) {
                                scale(
                                    scaleX = lensScale,
                                    scaleY = lensScale,
                                    pivot = Offset(size.width * 0.5f, size.height * 0.5f)
                                ) {
                                    translate(left = -offsetX, top = -offsetY) {
                                        drawLayer(backdropState.graphicsLayer)
                                    }
                                }
                            }
                            drawLayer(backdropRenderLayer)
                        }

                        // 3. Apply AGSL RuntimeShader (API 33+) or optical fallback tint
                        if (agslBrush != null) {
                            drawRect(brush = agslBrush)
                        } else {
                            drawRect(color = adaptiveTint)
                        }

                        // Clear glass legibility scrim
                        if (variant == GlassVariant.CLEAR) {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.18f),
                                        Color.Black.copy(alpha = 0.34f)
                                    )
                                )
                            )
                        }

                        // 4. Inner Edge Glow & Convex Lens Rim Refraction Gradient
                        val glowAlpha = (config.innerGlowAlpha * intensity).coerceIn(0f, 1f)
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = glowAlpha * 0.65f),
                                    config.accentColor.copy(alpha = glowAlpha * 0.35f)
                                ),
                                center = Offset(
                                    size.width * (0.5f + lightVec.x * 0.2f),
                                    size.height * (0.5f + lightVec.y * 0.2f)
                                ),
                                radius = max(size.width, size.height) * 0.72f
                            ),
                            size = size,
                            cornerRadius = CornerRadius(cornerPx, cornerPx)
                        )
                    }

                    // 5. Top-Left Bevel Specular Highlight & Rim Dispersion Border
                    val specAlpha = if (effectiveReduceGlass) {
                        0.32f
                    } else {
                        (config.specularIntensity * intensity).coerceIn(0.15f, 0.98f)
                    }
                    val rimBrush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = specAlpha),
                            config.accentColor.copy(alpha = specAlpha * 0.45f * config.chromaticDispersion),
                            Color.White.copy(alpha = specAlpha * 0.14f),
                            Color(0xFF38BDF8).copy(alpha = specAlpha * 0.35f * config.chromaticDispersion),
                            Color.White.copy(alpha = specAlpha * 0.62f)
                        ),
                        start = Offset(
                            x = if (lightVec.x <= 0f) 0f else size.width,
                            y = if (lightVec.y <= 0f) 0f else size.height
                        ),
                        end = Offset(
                            x = if (lightVec.x <= 0f) size.width else 0f,
                            y = if (lightVec.y <= 0f) size.height else 0f
                        )
                    )
                    drawRoundRect(
                        brush = rimBrush,
                        size = size,
                        cornerRadius = CornerRadius(cornerPx, cornerPx),
                        style = Stroke(width = if (config.highContrastGlass) 2.2.dp.toPx() else 1.35.dp.toPx())
                    )
                }

                // Draw child content (icons, labels) crisply above the glass
                drawContent()
            }
        }
}

/**
 * Mandatory Liquid Glass press feedback modifier:
 * - Spring scale bounce (1.0 -> 1.06x) with subtle vertical squash (0.97x)
 * - Shimmer sweep across the bevel on press
 * - Haptic tick on press down
 */
fun Modifier.glassPressBounce(
    enabled: Boolean = true,
    scaleTarget: Float = 1.06f,
    onPressChanged: ((Boolean) -> Unit)? = null
): Modifier = composed {
    val config = LocalGlassConfig.current
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val scaleX by animateFloatAsState(
        targetValue = if (isPressed && enabled) scaleTarget else 1f,
        animationSpec = DesignTokens.pressBounceSpring(config.reduceMotion),
        label = "glassScaleX"
    )
    val scaleY by animateFloatAsState(
        targetValue = if (isPressed && enabled) (scaleTarget * 0.97f) else 1f,
        animationSpec = DesignTokens.pressBounceSpring(config.reduceMotion),
        label = "glassScaleY"
    )
    val shimmerProgress by animateFloatAsState(
        targetValue = if (isPressed && enabled) 1f else 0f,
        animationSpec = DesignTokens.pressBounceSpring(config.reduceMotion),
        label = "glassShimmer"
    )

    this
        .graphicsLayer {
            this.scaleX = scaleX
            this.scaleY = scaleY
        }
        .drawWithContent {
            drawContent()
            if (shimmerProgress > 0.01f && !config.reduceMotion) {
                val sweepX = size.width * (-0.3f + shimmerProgress * 1.6f)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.28f * shimmerProgress),
                            Color.Transparent
                        ),
                        start = Offset(sweepX - size.width * 0.35f, 0f),
                        end = Offset(sweepX + size.width * 0.35f, size.height)
                    )
                )
            }
        }
        .pointerInput(enabled, config.hapticsEnabled) {
            if (!enabled) return@pointerInput
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                onPressChanged?.invoke(true)
                if (config.hapticsEnabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                waitForUpOrCancellation()
                isPressed = false
                onPressChanged?.invoke(false)
            }
        }
}
