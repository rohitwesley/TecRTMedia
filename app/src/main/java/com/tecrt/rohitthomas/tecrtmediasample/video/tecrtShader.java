package com.tecrt.rohitthomas.tecrtmediasample.video;

import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * Created by rohitthomas on 10/06/16.
 */
public class tecrtShader {

    private int vertexShaderHandle;
    private int fragmentShaderHandle;
    private int shaderProgram;

    //TODO add filters : ansel,blackNwhite,cartoon,edges,georgia,poloroid,retro,sahara,sepia,vhs

    public static final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "attribute vec4 vTexCoordinate;" +
                    "uniform mat4 textureTransform;" +
                    "varying vec2 v_TexCoordinate;" +
                    "void main() {" +
                    "   v_TexCoordinate = (textureTransform * vTexCoordinate).xy;" +
                    "   gl_Position = vPosition;" +
                    "}";

    public static final String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;" +
                    "uniform samplerExternalOES texture;" +
                    "varying vec2 v_TexCoordinate;" +
                    "void main () {" +
                    "    vec4 color = texture2D(texture, v_TexCoordinate);" +
                    "    gl_FragColor = color;" +
                    "}";

    public static final String filterVShaderCode =
            "attribute vec4 vPosition;" +
                    "attribute vec4 vTexCoordinate;" +
                    "uniform mat4 textureTransform;" +
                    "varying vec2 v_TexCoordinate;" +
                    "void main() {" +
                    "   v_TexCoordinate = (textureTransform * vTexCoordinate).xy;" +
                    "   gl_Position = vPosition;" +
                    "}";

    public static final String variableF =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;" +
                    "uniform float uBrightness;" +
                    "uniform float uContrast;" +
                    "uniform float uSaturation;" +
                    "uniform float uCornerRadius;" +
                    "uniform samplerExternalOES texture;" +
                    "varying vec2 v_TexCoordinate;";
//    "uniform vec2 uPixelSize;" +
    public static final String filterFShaderCode =
                    "vec3 brightness(vec3 color, float brightness) {" +
                    "    float scaled = brightness / 2.0;" +
                    "    if (scaled < 0.0) {" +
                    "    return color * (1.0 + scaled);" +
                    "    } else {" +
                    "    return color + ((1.0 - color) * scaled);" +
                    "    }" +
                    "}" +
                    "vec3 contrast(vec3 color, float contrast) {" +
                    "    const float PI = 3.14159265;" +
                    "    return min(vec3(1.0), ((color - 0.5) * (tan((contrast + 1.0) * PI / 4.0) ) + 0.5));" +
                    "}" +
                    "vec3 saturation(vec3 color, float sat) {" +
                    "    const float lumaR = 0.212671;" +
                    "    const float lumaG = 0.715160;" +
                    "    const float lumaB = 0.072169;" +
                    "    float v = sat + 1.0;" +
                    "    float i = 1.0 - v;" +
                    "    float r = i * lumaR;" +
                    "    float g = i * lumaG;" +
                    "    float b = i * lumaB;" +
                    "    mat3 mat = mat3(r + v, r, r, g, g + v, g, b, b, b + v);" +
                    "    return mat * color;" +
                    "}" +

                    "vec3 overlay(vec3 overlayComponent, vec3 underlayComponent, float alpha) {" +
                    "    vec3 underlay = underlayComponent * alpha;" +
                    "    return underlay * (underlay + (2.0 * overlayComponent * (1.0 - underlay)));" +
                    "}" +

                    "vec3 multiplyWithAlpha(vec3 overlayComponent, float alpha, vec3 underlayComponent) {" +
                    "    return underlayComponent * overlayComponent * alpha;" +
                    "}" +

                    "vec3 screenPixelComponent(vec3 maskPixelComponent, float alpha, vec3 imagePixelComponent) {" +
                    "    return 1.0 - (1.0 - (maskPixelComponent * alpha)) * (1.0 - imagePixelComponent);" +
                    "    }" +

                    "vec3 rgbToHsv(vec3 color) {" +
                    "    vec3 hsv;" +

                    "    float mmin = min(color.r, min(color.g, color.b));" +
                    "    float mmax = max(color.r, max(color.g, color.b));" +
                    "    float delta = mmax - mmin;" +

                    "    hsv.z = mmax;" +
                    "    hsv.y = delta / mmax;" +

                    "    if (color.r == mmax) {" +
                    "    hsv.x = (color.g - color.b) / delta;" +
                    "    } else if (color.g == mmax) {" +
                    "    hsv.x = 2.0 + (color.b - color.r) / delta;" +
                    "    } else {" +
                    "    hsv.x = 4.0 + (color.r - color.g) / delta;" +
                    "    }" +

                    "    hsv.x *= 0.166667;" +
                    "    if (hsv.x < 0.0) {" +
                    "    hsv.x += 1.0;" +
                    "    }" +

                    "    return hsv;" +
                    "}" +

                    "vec3 hsvToRgb(vec3 hsv) {" +
                    "    if (hsv.y == 0.0) {" +
                    "    return vec3(hsv.z);" +
                    "    } else {" +
                    "    float i;" +
                    "    float aa, bb, cc, f;" +

                    "    float h = hsv.x;" +
                    "    float s = hsv.y;" +
                    "    float b = hsv.z;" +

                    "    if (h == 1.0) {" +
                    "    h = 0.0;" +
                    "    }" +

                    "    h *= 6.0;" +
                    "    i = floor(h);" +
                    "    f = h - i;" +
                    "    aa = b * (1.0 - s);" +
                    "    bb = b * (1.0 - (s * f));" +
                    "    cc = b * (1.0 - (s * (1.0 - f)));" +

                    "    if (i == 0.0) return vec3(b, cc, aa);" +
                    "    if (i == 1.0) return vec3(bb, b, aa);" +
                    "    if (i == 2.0) return vec3(aa, b, cc);" +
                    "    if (i == 3.0) return vec3(aa, bb, b);" +
                    "    if (i == 4.0) return vec3(cc, aa, b);" +
                    "    if (i == 5.0) return vec3(b, aa, bb);" +
                    "    }" +
                    "}";
    public static final String mainFShaderCode =
                    "void main () {" +
                    "    vec3 color = texture2D(texture, v_TexCoordinate).rgb;" +

                    "    color = filter(color, texture, v_TexCoordinate);" +
                    "    color = brightness(color, uBrightness);" +
                    "    color = contrast(color, uContrast);" +
                    "    color = saturation(color, uSaturation);" +

                    "    const float sqrt2 = 1.414213562373;" +
                    "    float len = distance(v_TexCoordinate, vec2(0.5)) * sqrt2;" +
                    "    len = smoothstep(1.0 - uCornerRadius, 1.0, len);" +
                    "    color *= mix(0.5, 1.0, 1.0 - len);" +
                    "    gl_FragColor = vec4(color, 1.0);" +
                    "}";

    public static final String defaultfilter =
            "    vec3 filter(vec3 color, sampler2D texture, vec2 texturePos) {" +
                    "    return color;" +
                    "    }";

    public static final String anselfilter =
            "    vec3 filter(vec3 color, sampler2D texture, vec2 texturePos) {" +
                    "    float gray = dot(color, vec3(0.299, 0.587, 0.114));" +
                    "    if (gray > 0.5) {" +
                    "    return vec3(1.0 - (1.0 - 2.0 * (gray - 0.5)) * (1.0 - gray));" +
                    "    } else {" +
                    "    return vec3(2.0 * gray * gray);" +
                    "    }" +
                    "    }";

    public static final String bnwfilter =
            "    vec3 filter(vec3 color, sampler2D texture, vec2 texturePos) {" +
                    "    float gray = dot(color, vec3(0.299, 0.587, 0.114));" +
                    "    return vec3(gray);" +
                    "    }";

    public static final String cartoonfilter =

                    "    vec3 filter(vec3 color, sampler2D texture, vec2 texturePos) {" +
                    "    vec3 border;" +

                    "    float dx = uPixelSize.x * 1.5;" +
                    "    float dy = uPixelSize.y * 1.5;" +
                    "    vec3 sample0 = texture2D(sTexture, vec2(texturePos.x - dx, texturePos.y + dy)).rgb;" +
                    "    vec3 sample1 = texture2D(sTexture, vec2(texturePos.x - dx, texturePos.y)).rgb;" +
                    "    vec3 sample2 = texture2D(sTexture, vec2(texturePos.x - dx, texturePos.y - dy)).rgb;" +
                    "    vec3 sample3 = texture2D(sTexture, vec2(texturePos.x, texturePos.y + dy)).rgb;" +
                    "    vec3 sample4 = texture2D(sTexture, vec2(texturePos.x, texturePos.y)).rgb;" +
                    "    vec3 sample5 = texture2D(sTexture, vec2(texturePos.x, texturePos.y - dy)).rgb;" +
                    "    vec3 sample6 = texture2D(sTexture, vec2(texturePos.x + dx, texturePos.y + dy)).rgb;" +
                    "    vec3 sample7 = texture2D(sTexture, vec2(texturePos.x + dx, texturePos.y)).rgb;" +
                    "    vec3 sample8 = texture2D(sTexture, vec2(texturePos.x + dx, texturePos.y - dy)).rgb;" +

                    "    color = (sample0 + sample1 + sample2 + sample3 + sample4 + sample5 + sample6 + sample7 + sample8) / 9.0;" +

                    "    vec3 horizEdge = sample2 + sample5 + sample8 - (sample0 + sample3 + sample6);" +
                    "    vec3 vertEdge = sample0 + sample1 + sample2 - (sample6 + sample7 + sample8);" +

                    "    border = sqrt((horizEdge * horizEdge) + (vertEdge * vertEdge));" +

                    "    if (border.r > 0.3 || border.g > 0.3 || border.b > 0.3){" +
                    "    color *= 1.0 - dot(border, border);" +
                    "    }" +

                    "    const vec3 colorRed = vec3(1.0, 0.3, 0.3);" +
                    "    const vec3 colorGreen = vec3(0.3, 1.0, 0.3);" +
                    "    const vec3 colorBlue = vec3(0.3, 0.3, 1.0);" +

                    "    color = floor(color * 8.0) * 0.125;" +
                    "    color = colorRed * color.r + colorBlue * color.b + colorGreen * color.g;" +

                    "    return color;" +
                    "    }";

    public static final String edgefilter =

                    "    vec3 filter(vec3 color, sampler2D texture, vec2 texturePos) {" +
                    "    float dx = uPixelSize.x;" +
                    "    float dy = uPixelSize.y;" +
                    "    float len0 = length(texture2D(texture, vec2(texturePos.x - dx, texturePos.y - dy)).rgb);" +
                    "    float len1 = length(texture2D(texture, vec2(texturePos.x - dx, texturePos.y)).rgb);" +
                    "    float len2 = length(texture2D(texture, vec2(texturePos.x - dx, texturePos.y + dy)).rgb);" +
                    "    float len3 = length(texture2D(texture, vec2(texturePos.x, texturePos.y - dy)).rgb);" +
                    "    float len4 = length(texture2D(texture, vec2(texturePos.x, texturePos.y)).rgb);" +
                    "    float len5 = length(texture2D(texture, vec2(texturePos.x, texturePos.y + dy)).rgb);" +
                    "    float len6 = length(texture2D(texture, vec2(texturePos.x + dx, texturePos.y - dy)).rgb);" +
                    "    float len7 = length(texture2D(texture, vec2(texturePos.x + dx, texturePos.y)).rgb);" +
                    "    float len8 = length(texture2D(texture, vec2(texturePos.x + dx, texturePos.y + dy)).rgb);" +

                    "    float delta = (" +
                    "    abs(len1 - len7) +" +
                    "    abs(len5 - len3) +" +
                    "    abs(len0 - len8) +" +
                    "    abs(len2 - len6) ) * 0.25;" +

                    "    return vec3(0.8 * delta, 1.2 * delta, 2.0 * delta);" +
                    "    }";

    public static final String georgiafilter =
            "    vec3 filter(vec3 color, sampler2D texture, vec2 texturePos) {" +
                    "    color = brightness(color, 0.4724);" +
                    "    color = contrast(color, 0.3149);" +

                    "    color.g = color.g * 0.87 + 0.13;" +
                    "    color.b = color.b * 0.439 + 0.561;" +

                    "    color *= vec3(0.981, 0.862, 0.686);" +

                    "    return color;" +
                    "    }";

    public static final String polaroidfilter =
            "    vec3 filter(vec3 color, sampler2D texture, vec2 texturePos) {" +
                    "    const mat4 mat = mat4(1.438, -0.062, -0.062, 0.0," +
                    "    -0.122, 1.378, -0.122, 0.0," +
                    "    -0.016, -0.016, 1.483, 0.0," +
                    "     -0.03, 0.05, -0.02, 0.0);" +
                    "    return (mat * vec4(color, 1.0)).rgb;" +
                    "    }";

    public static final String retrofilter =
            "    vec3 filter(vec3 color, sampler2D texture, vec2 texturePos) {" +
                    "    float gray = dot(color, vec3(0.299, 0.587, 0.114));" +
                    "    color = overlay(vec3(gray), color, 1.0);" +
                    "    color = multiplyWithAlpha(vec3(0.984, 0.949, 0.639), 0.588235, color);" +
                    "    color = screenPixelComponent(vec3(0.909, 0.396, 0.702), 0.2, color);" +
                    "    color = screenPixelComponent(vec3(0.035, 0.286, 0.914), 0.168627, color);" +
                    "    return color;" +
                    "    }";

    public static final String saharafilter =
            "    vec3 filter(vec3 color, sampler2D texture, vec2 texturePos) {" +
                    "    color.r = color.r * 0.843 + 0.157;" +
                    "    color.b = color.b * 0.882 + 0.118;" +

                    "    vec3 hsv = rgbToHsv(color);" +
                    "    hsv.y = hsv.y * 0.55;" +
                    "    color = hsvToRgb(hsv);" +

                    "    color = saturation(color, 0.65);" +
                    "    color *= vec3(1.0, 0.891, 0.733);" +

                    "    return color;" +
                    "    }";

    public static final String sepiafilter =
            "    vec3 filter(vec3 color, sampler2D texture, vec2 texturePos) {" +
                    "    float luminosity = dot(color, vec3(0.21, 0.72, 0.07));" +
                    "    float brightGray = brightness(vec3(luminosity), 0.234375).r;" +

                    "    vec3 tinted = overlay(vec3(0.419, 0.259, 0.047), vec3(brightGray), 1.0);" +

                    "    float invertMask = 1.0 - luminosity;" +
                    "    float luminosity3 = pow(luminosity, 3.0);" +

                    "    return vec3(luminosity3) + (tinted * invertMask * (luminosity + 1.0));" +
                    "    }";

    public void setProgram(String program) {
        switch (program) {
            case "basic":
                vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                GLES20.glShaderSource(vertexShaderHandle, vertexShaderCode);
                GLES20.glCompileShader(vertexShaderHandle);
                checkGlError("Vertex shader compile");

                fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
                GLES20.glShaderSource(fragmentShaderHandle, fragmentShaderCode);
                GLES20.glCompileShader(fragmentShaderHandle);
                checkGlError("Pixel shader compile");

                shaderProgram = GLES20.glCreateProgram();
                GLES20.glAttachShader(shaderProgram, vertexShaderHandle);
                GLES20.glAttachShader(shaderProgram, fragmentShaderHandle);
                GLES20.glLinkProgram(shaderProgram);
                checkGlError("Shader program compile");
                break;

            case "adjustment":
                vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                GLES20.glShaderSource(vertexShaderHandle, filterVShaderCode);
                GLES20.glCompileShader(vertexShaderHandle);
                checkGlError("Vertex shader compile");

                fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
                GLES20.glShaderSource(fragmentShaderHandle, variableF + defaultfilter + filterFShaderCode + mainFShaderCode);
                GLES20.glCompileShader(fragmentShaderHandle);
                checkGlError("Pixel shader compile");

                shaderProgram = GLES20.glCreateProgram();
                GLES20.glAttachShader(shaderProgram, vertexShaderHandle);
                GLES20.glAttachShader(shaderProgram, fragmentShaderHandle);
                GLES20.glLinkProgram(shaderProgram);
                checkGlError("Shader program compile");
                break;

            case "blacknwhite":
                vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                GLES20.glShaderSource(vertexShaderHandle, filterVShaderCode);
                GLES20.glCompileShader(vertexShaderHandle);
                checkGlError("Vertex shader compile");

                fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
                GLES20.glShaderSource(fragmentShaderHandle, variableF + bnwfilter + filterFShaderCode + mainFShaderCode);
                GLES20.glCompileShader(fragmentShaderHandle);
                checkGlError("Pixel shader compile");

                shaderProgram = GLES20.glCreateProgram();
                GLES20.glAttachShader(shaderProgram, vertexShaderHandle);
                GLES20.glAttachShader(shaderProgram, fragmentShaderHandle);
                GLES20.glLinkProgram(shaderProgram);
                checkGlError("Shader program compile");
                break;

            case "ansel":
                vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                GLES20.glShaderSource(vertexShaderHandle, filterVShaderCode);
                GLES20.glCompileShader(vertexShaderHandle);
                checkGlError("Vertex shader compile");

                fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
                GLES20.glShaderSource(fragmentShaderHandle, variableF + anselfilter + filterFShaderCode + mainFShaderCode);
                GLES20.glCompileShader(fragmentShaderHandle);
                checkGlError("Pixel shader compile");

                shaderProgram = GLES20.glCreateProgram();
                GLES20.glAttachShader(shaderProgram, vertexShaderHandle);
                GLES20.glAttachShader(shaderProgram, fragmentShaderHandle);
                GLES20.glLinkProgram(shaderProgram);
                checkGlError("Shader program compile");
                break;

            case "cartoon":
                vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                GLES20.glShaderSource(vertexShaderHandle, filterVShaderCode);
                GLES20.glCompileShader(vertexShaderHandle);
                checkGlError("Vertex shader compile");

                fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
                GLES20.glShaderSource(fragmentShaderHandle, variableF + filterFShaderCode + cartoonfilter + mainFShaderCode);
                GLES20.glCompileShader(fragmentShaderHandle);
                checkGlError("Pixel shader compile");

                shaderProgram = GLES20.glCreateProgram();
                GLES20.glAttachShader(shaderProgram, vertexShaderHandle);
                GLES20.glAttachShader(shaderProgram, fragmentShaderHandle);
                GLES20.glLinkProgram(shaderProgram);
                checkGlError("Shader program compile");
                break;

            case "default":
                vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                GLES20.glShaderSource(vertexShaderHandle, filterVShaderCode);
                GLES20.glCompileShader(vertexShaderHandle);
                checkGlError("Vertex shader compile");

                fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
                GLES20.glShaderSource(fragmentShaderHandle, variableF + defaultfilter + filterFShaderCode + mainFShaderCode);
                GLES20.glCompileShader(fragmentShaderHandle);
                checkGlError("Pixel shader compile");

                shaderProgram = GLES20.glCreateProgram();
                GLES20.glAttachShader(shaderProgram, vertexShaderHandle);
                GLES20.glAttachShader(shaderProgram, fragmentShaderHandle);
                GLES20.glLinkProgram(shaderProgram);
                checkGlError("Shader program compile");
                break;

            case "edges":
                vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                GLES20.glShaderSource(vertexShaderHandle, filterVShaderCode);
                GLES20.glCompileShader(vertexShaderHandle);
                checkGlError("Vertex shader compile");

                fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
                GLES20.glShaderSource(fragmentShaderHandle, variableF + filterFShaderCode + edgefilter + mainFShaderCode);
                GLES20.glCompileShader(fragmentShaderHandle);
                checkGlError("Pixel shader compile");

                shaderProgram = GLES20.glCreateProgram();
                GLES20.glAttachShader(shaderProgram, vertexShaderHandle);
                GLES20.glAttachShader(shaderProgram, fragmentShaderHandle);
                GLES20.glLinkProgram(shaderProgram);
                checkGlError("Shader program compile");
                break;

            case "georgia":
                vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                GLES20.glShaderSource(vertexShaderHandle, filterVShaderCode);
                GLES20.glCompileShader(vertexShaderHandle);
                checkGlError("Vertex shader compile");

                fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
                GLES20.glShaderSource(fragmentShaderHandle, variableF + filterFShaderCode + georgiafilter + mainFShaderCode);
                GLES20.glCompileShader(fragmentShaderHandle);
                checkGlError("Pixel shader compile");

                shaderProgram = GLES20.glCreateProgram();
                GLES20.glAttachShader(shaderProgram, vertexShaderHandle);
                GLES20.glAttachShader(shaderProgram, fragmentShaderHandle);
                GLES20.glLinkProgram(shaderProgram);
                checkGlError("Shader program compile");
                break;

            case "polaroid":
                vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                GLES20.glShaderSource(vertexShaderHandle, filterVShaderCode);
                GLES20.glCompileShader(vertexShaderHandle);
                checkGlError("Vertex shader compile");

                fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
                GLES20.glShaderSource(fragmentShaderHandle, variableF + polaroidfilter + filterFShaderCode + mainFShaderCode);
                GLES20.glCompileShader(fragmentShaderHandle);
                checkGlError("Pixel shader compile");

                shaderProgram = GLES20.glCreateProgram();
                GLES20.glAttachShader(shaderProgram, vertexShaderHandle);
                GLES20.glAttachShader(shaderProgram, fragmentShaderHandle);
                GLES20.glLinkProgram(shaderProgram);
                checkGlError("Shader program compile");
                break;

            case "retro":
                vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                GLES20.glShaderSource(vertexShaderHandle, filterVShaderCode);
                GLES20.glCompileShader(vertexShaderHandle);
                checkGlError("Vertex shader compile");

                fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
                GLES20.glShaderSource(fragmentShaderHandle, variableF + filterFShaderCode + retrofilter + mainFShaderCode);
                GLES20.glCompileShader(fragmentShaderHandle);
                checkGlError("Pixel shader compile");

                shaderProgram = GLES20.glCreateProgram();
                GLES20.glAttachShader(shaderProgram, vertexShaderHandle);
                GLES20.glAttachShader(shaderProgram, fragmentShaderHandle);
                GLES20.glLinkProgram(shaderProgram);
                checkGlError("Shader program compile");
                break;

            case "sahara":
                vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                GLES20.glShaderSource(vertexShaderHandle, filterVShaderCode);
                GLES20.glCompileShader(vertexShaderHandle);
                checkGlError("Vertex shader compile");

                fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
                GLES20.glShaderSource(fragmentShaderHandle, variableF + filterFShaderCode + saharafilter + mainFShaderCode);
                GLES20.glCompileShader(fragmentShaderHandle);
                checkGlError("Pixel shader compile");

                shaderProgram = GLES20.glCreateProgram();
                GLES20.glAttachShader(shaderProgram, vertexShaderHandle);
                GLES20.glAttachShader(shaderProgram, fragmentShaderHandle);
                GLES20.glLinkProgram(shaderProgram);
                checkGlError("Shader program compile");
                break;

            case "sepia":
                vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                GLES20.glShaderSource(vertexShaderHandle, filterVShaderCode);
                GLES20.glCompileShader(vertexShaderHandle);
                checkGlError("Vertex shader compile");

                fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
                GLES20.glShaderSource(fragmentShaderHandle, variableF + filterFShaderCode + sepiafilter + mainFShaderCode);
                GLES20.glCompileShader(fragmentShaderHandle);
                checkGlError("Pixel shader compile");

                shaderProgram = GLES20.glCreateProgram();
                GLES20.glAttachShader(shaderProgram, vertexShaderHandle);
                GLES20.glAttachShader(shaderProgram, fragmentShaderHandle);
                GLES20.glLinkProgram(shaderProgram);
                checkGlError("Shader program compile");
                break;
        }
        int[] status = new int[1];
        GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            String error = GLES20.glGetProgramInfoLog(shaderProgram);
            Log.e("SurfaceTest", "Error while linking program:\n" + error);
        }
    }

    public void useProgram() {
        GLES20.glUseProgram(shaderProgram);
    }

    public int getUHandle(String program) {
        return GLES20.glGetUniformLocation(shaderProgram, program);
    }

    public int getAHandle(String program) {
        return GLES20.glGetAttribLocation(shaderProgram, program);
    }

    public void checkGlError(String op)
    {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("SurfaceTest", op + ": glError " + GLUtils.getEGLErrorString(error));
        }
    }

    public void release() {
        GLES20.glDeleteProgram(shaderProgram);
    }
}
