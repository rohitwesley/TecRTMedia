package com.tecrt.rohitthomas.tecrtmedia;

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

    //TODO Priority: add filters : VHS,NoisyTV
    //TODO Priority: fix filters : cartoon,edges,intro

    public static final String vertexShaderCode
            = "uniform mat4 uMVPMatrix;\n"
            + "uniform mat4 uTexMatrix;\n"
            + "attribute highp vec4 aPosition;\n"
            + "attribute highp vec4 aTextureCoord;\n"
            + "varying highp vec2 vTextureCoord;\n"
            + "\n"
            + "void main() {\n"
            + "	gl_Position = uMVPMatrix * aPosition;\n"
            + "	vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n"
            + "}\n";

    public static final String fragmentShaderCode
            = "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            + "uniform samplerExternalOES sTexture;\n"
            + "varying highp vec2 vTextureCoord;\n"
            + "void main() {\n"
            + "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n"
            + "}";

    public static final String introVShaderCode
            = "uniform mat4 uMVPMatrix;\n"
            + "uniform mat4 uTexMatrix;\n"
            + "attribute highp vec4 aPosition;\n"
            + "attribute highp vec4 aTextureCoord;\n"
            + "varying highp vec2 vTextureCoord;\n"
            + "\n"
            + "void main() {\n"
            + "	gl_Position = uMVPMatrix * aPosition;\n"
            + "	vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n"
            + "}\n";

    public static final String introFShaderCode
            =  "void main () {\n" +
            "    vec3 color = texture2D(sTexture, vTextureCoord).rgb;\n" +

            "    color = filter(color, sTexture, vTextureCoord);\n" +
            "    color = brightness(color, uBrightness);\n" +
            "    color = contrast(color, uContrast);\n" +
            "    color = saturation(color, uSaturation);\n" +

            "    const float sqrt2 = 1.414213562373;\n" +
            "    float len = distance(vTextureCoord, vec2(0.5)) * sqrt2;\n" +
            "    len = smoothstep(1.0 - uCornerRadius, 1.0, len);\n" +
            "    color *= mix(0.5, 1.0, 1.0 - len);\n" +
            "    gl_FragColor = vec4(color, 1.0);\n" +
            "}\n";

//    + "vec3 mainImage(samplerExternalOES iChannel0, vec2 iResolution, float iGlobalTime, vec2 fragCoord) {\n"
//            + " return texture2D(sTexture, vTextureCoord);\n"
//            + "}\n"
//            + "void main() {\n"
//            + " vec4 color = mainImage(sTexture, uPixelSize, uTime, vTextureCoord);\n"
//            + "  gl_FragColor = color;\n"
//            + "}";
//
//    + "vec3 mainImage(samplerExternalOES iChannel0, vec2 iResolution, float iGlobalTime, vec2 fragCoord) {\n"
//            + " vec2 R = iResolution.xy;\n"
//            + " U = (fragCoord+fragCoord-R)/R.y;\n"
//            + " O = texture2D(iChannel0,.5+.5*U);\n"
//            + " float N = 12., c = cos(6.28/N),s=sin(6.28/N),"
//            + " a = 3.14/4.*(.5+.5*sin(iGlobalTime)),d,A;\n"
//            + " for (int i=0; i<20; i++) {\n"
//            + "  d = -dot(U-vec2(-1,1),vec2(sin(a),cos(a)));\n"
//            + "  A = smoothstep(.01,0.,d);\n"
//            + "  O.rgb += (1.-O.w) * A * vec3(1.-4.*smoothstep(.01,0.,abs(d)));\n"
//            + "  O.w = A;\n"
//            + "  U *= mat2(c,-s,s,c);\n"
//            + " }\n"
//            + " O *= smoothstep(1.,.99,length(U));\n"
//            + " return O;\n"
//            + "}\n"
//            + "void main() {\n"
//            + " vec4 color = mainImage(sTexture, uPixelSize, uTime, vTextureCoord);\n"
//            + "  gl_FragColor = color;\n"
//            + "}";
    public static final String filterVShaderCode
        = "uniform mat4 uMVPMatrix;\n"
        + "uniform mat4 uTexMatrix;\n"
        + "attribute highp vec4 aPosition;\n"
        + "attribute highp vec4 aTextureCoord;\n"
        + "uniform highp vec2 iResolution;\n"
        + "varying highp vec2 uPixelSize;\n"
        + "varying highp vec2 vTextureCoord;\n"
        + "\n"
        + "void main() {\n"
        + "	gl_Position = uMVPMatrix * aPosition;\n"
        + "	uPixelSize = iResolution;\n"
        + "	vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n"
        + "}\n";

    public static final String variableF
            = "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            + "uniform samplerExternalOES sTexture;\n"
            + "varying highp vec2 vTextureCoord;\n"
            + "uniform float uTime;\n"
            + "uniform float uBrightness;\n"
            + "uniform float uContrast;\n"
            + "uniform float uSaturation;\n"
            + "uniform float uCornerRadius;\n"
            + "varying highp vec2 uPixelSize;\n";

    public static final String filterFShaderCode =
                    "vec3 brightness(vec3 color, float brightness) {\n" +
                    "    float scaled = brightness / 2.0;\n" +
                    "    if (scaled < 0.0) {\n" +
                    "    return color * (1.0 + scaled);\n" +
                    "    } else {\n" +
                    "    return color + ((1.0 - color) * scaled);\n" +
                    "    }\n" +
                    "}\n" +
                    "vec3 contrast(vec3 color, float contrast) {\n" +
                    "    const float PI = 3.14159265;\n" +
                    "    return min(vec3(1.0), ((color - 0.5) * (tan((contrast + 1.0) * PI / 4.0) ) + 0.5));\n" +
                    "}\n" +
                    "vec3 saturation(vec3 color, float sat) {\n" +
                    "    const float lumaR = 0.212671;\n" +
                    "    const float lumaG = 0.715160;\n" +
                    "    const float lumaB = 0.072169;\n" +
                    "    float v = sat + 1.0;\n" +
                    "    float i = 1.0 - v;\n" +
                    "    float r = i * lumaR;\n" +
                    "    float g = i * lumaG;\n" +
                    "    float b = i * lumaB;\n" +
                    "    mat3 mat = mat3(r + v, r, r, g, g + v, g, b, b, b + v);\n" +
                    "    return mat * color;\n" +
                    "}\n" +

                    "vec3 overlay(vec3 overlayComponent, vec3 underlayComponent, float alpha) {\n" +
                    "    vec3 underlay = underlayComponent * alpha;\n" +
                    "    return underlay * (underlay + (2.0 * overlayComponent * (1.0 - underlay)));\n" +
                    "}\n" +

                    "vec3 multiplyWithAlpha(vec3 overlayComponent, float alpha, vec3 underlayComponent) {\n" +
                    "    return underlayComponent * overlayComponent * alpha;\n" +
                    "}\n" +

                    "vec3 screenPixelComponent(vec3 maskPixelComponent, float alpha, vec3 imagePixelComponent) {\n" +
                    "    return 1.0 - (1.0 - (maskPixelComponent * alpha)) * (1.0 - imagePixelComponent);\n" +
                    "    }\n" +

                    "vec3 rgbToHsv(vec3 color) {\n" +
                    "    vec3 hsv;\n" +

                    "    float mmin = min(color.r, min(color.g, color.b));\n" +
                    "    float mmax = max(color.r, max(color.g, color.b));\n" +
                    "    float delta = mmax - mmin;\n" +

                    "    hsv.z = mmax;\n" +
                    "    hsv.y = delta / mmax;\n" +

                    "    if (color.r == mmax) {\n" +
                    "    hsv.x = (color.g - color.b) / delta;\n" +
                    "    } else if (color.g == mmax) {\n" +
                    "    hsv.x = 2.0 + (color.b - color.r) / delta;\n" +
                    "    } else {\n" +
                    "    hsv.x = 4.0 + (color.r - color.g) / delta;\n" +
                    "    }\n" +

                    "    hsv.x *= 0.166667;\n" +
                    "    if (hsv.x < 0.0) {\n" +
                    "    hsv.x += 1.0;\n" +
                    "    }\n" +

                    "    return hsv;\n" +
                    "}\n" +

                    "vec3 hsvToRgb(vec3 hsv) {\n" +
                    "    if (hsv.y == 0.0) {\n" +
                    "    return vec3(hsv.z);\n" +
                    "    } else {\n" +
                    "    float i;\n" +
                    "    float aa, bb, cc, f;\n" +

                    "    float h = hsv.x;\n" +
                    "    float s = hsv.y;\n" +
                    "    float b = hsv.z;\n" +

                    "    if (h == 1.0) {\n" +
                    "    h = 0.0;\n" +
                    "    }\n" +

                    "    h *= 6.0;\n" +
                    "    i = floor(h);\n" +
                    "    f = h - i;\n" +
                    "    aa = b * (1.0 - s);\n" +
                    "    bb = b * (1.0 - (s * f));\n" +
                    "    cc = b * (1.0 - (s * (1.0 - f)));\n" +

                    "    if (i == 0.0) return vec3(b, cc, aa);\n" +
                    "    if (i == 1.0) return vec3(bb, b, aa);\n" +
                    "    if (i == 2.0) return vec3(aa, b, cc);\n" +
                    "    if (i == 3.0) return vec3(aa, bb, b);\n" +
                    "    if (i == 4.0) return vec3(cc, aa, b);\n" +
                    "    if (i == 5.0) return vec3(b, aa, bb);\n" +
                    "    }\n" +
                    "}\n";
    public static final String mainFShaderCode =
                    "void main () {\n" +
                    "    vec3 color = texture2D(sTexture, vTextureCoord).rgb;\n" +

                    "    color = filter(color, sTexture, vTextureCoord);\n" +
                    "    color = brightness(color, uBrightness);\n" +
                    "    color = contrast(color, uContrast);\n" +
                    "    color = saturation(color, uSaturation);\n" +

                    "    const float sqrt2 = 1.414213562373;\n" +
                    "    float len = distance(vTextureCoord, vec2(0.5)) * sqrt2;\n" +
                    "    len = smoothstep(1.0 - uCornerRadius, 1.0, len);\n" +
                    "    color *= mix(0.5, 1.0, 1.0 - len);\n" +
                    "    gl_FragColor = vec4(color, 1.0);\n" +
                    "}\n";

    public static final String introfilter
            = "void main () {\n"
            + " vec3 color = texture2D(sTexture, vTextureCoord).rgb;\n"
            + " color = filter(color, sTexture, vTextureCoord);\n"
            + " color = brightness(color, uBrightness);\n"
            + " color = contrast(color, uContrast);\n"
            + " color = saturation(color, uSaturation);\n"
            + " const float sqrt2 = 1.414213562373;\n"
            + " float len = distance(vTextureCoord, vec2(0.5)) * sqrt2;\n"
            + " len = smoothstep(1.0 - uCornerRadius, 1.0, len);\n"
            + " color *= mix(0.5, 1.0, 1.0 - len);\n"

            + " vec2 p = vTextureCoord.xy   ;\n"

            + " vec4 fragColor = vec4(vec3(0.0), 0.);\n"
            + " float focus = sin(uTime*0.1)*.35+.5;\n"
            + " float blur = 2.*sqrt(abs(p.y - focus));\n"
            + " vec2 v_TexCoor = vec2( vTextureCoord.x,vTextureCoord.y);\n"
            + " float blurSizeH = blur * 1.0 / 30.0;\n"
            + " float blurSizeV = blur * 1.0 / 20.0;\n"
            + " vec4 sum = vec4(0.0);\n"
            + " for (int x = -2; x <= 2; x++)\n"
            + " for (int y = -2; y <= 2; y++)\n"
            + "   sum += texture2D(sTexture, vec2(v_TexCoor.x + float(x) * blurSizeH, v_TexCoor.y + float(y) * blurSizeV)) / 81.0;\n"

            + " gl_FragColor = sum;\n"
            + " }\n";



//    + " vec2 p2 = -p;\n"
//            + " float focus = sin(uTime*2.)*.35+.5;\n"
//            + " float blur = 7.*sqrt(abs(p.y - focus));\n"
//            + " vec4 colorTex = texture2D(sTexture, p, blur);\n"
//    + " vec2 p = vec2((1.0/1280.0),(1.0/720.0));\n"
//            + " p = (vTextureCoord+vTextureCoord-p)/p.y;\n"
//            + " float focus = sin(uTime*2.)*.35+.5;\n"
//            + " float blur = 7.*sqrt(abs(p.y - focus));\n"
//            + " float perspective = 0.3;\n"
//            + " vec2 p2 = -p;\n"
//            + " gl_FragColor = texture2D(sTexture, p2, blur);\n"
//            + " }\n";

    public static final String defaultfilter =
            "    vec3 filter(vec3 color, samplerExternalOES sTexture, vec2 texturePos) {\n" +
                    "    return color;\n" +
                    "    }\n";

    public static final String anselfilter =
            "    vec3 filter(vec3 color, samplerExternalOES sTexture, vec2 texturePos) {\n" +
                    "    float gray = dot(color, vec3(0.299, 0.587, 0.114));\n" +
                    "    if (gray > 0.5) {\n" +
                    "    return vec3(1.0 - (1.0 - 2.0 * (gray - 0.5)) * (1.0 - gray));\n" +
                    "    } else {\n" +
                    "    return vec3(2.0 * gray * gray);\n" +
                    "    }\n" +
                    "    }\n";

    public static final String bnwfilter =
            "    vec3 filter(vec3 color, samplerExternalOES sTexture, vec2 texturePos) {\n" +
                    "    float gray = dot(color, vec3(0.299, 0.587, 0.114));\n" +
                    "    return vec3(gray);\n" +
                    "    }\n ";

    public static final String cartoonfilter =

                    "    vec3 filter(vec3 color, samplerExternalOES sTexture, vec2 texturePos) {\n" +
                    "    vec3 border;\n" +

                    "    float dx = uPixelSize.x * 1.5;\n" +
                    "    float dy = uPixelSize.y * 1.5;\n" +
                    "    vec3 sample0 = texture2D(sTexture, vec2(texturePos.x - dx, texturePos.y + dy)).rgb;\n" +
                    "    vec3 sample1 = texture2D(sTexture, vec2(texturePos.x - dx, texturePos.y)).rgb;\n" +
                    "    vec3 sample2 = texture2D(sTexture, vec2(texturePos.x - dx, texturePos.y - dy)).rgb;\n" +
                    "    vec3 sample3 = texture2D(sTexture, vec2(texturePos.x, texturePos.y + dy)).rgb;\n" +
                    "    vec3 sample4 = texture2D(sTexture, vec2(texturePos.x, texturePos.y)).rgb;\n" +
                    "    vec3 sample5 = texture2D(sTexture, vec2(texturePos.x, texturePos.y - dy)).rgb;\n" +
                    "    vec3 sample6 = texture2D(sTexture, vec2(texturePos.x + dx, texturePos.y + dy)).rgb;\n" +
                    "    vec3 sample7 = texture2D(sTexture, vec2(texturePos.x + dx, texturePos.y)).rgb;\n" +
                    "    vec3 sample8 = texture2D(sTexture, vec2(texturePos.x + dx, texturePos.y - dy)).rgb;\n" +

                    "    color = (sample0 + sample1 + sample2 + sample3 + sample4 + sample5 + sample6 + sample7 + sample8) / 9.0;\n" +

                    "    vec3 horizEdge = sample2 + sample5 + sample8 - (sample0 + sample3 + sample6);\n" +
                    "    vec3 vertEdge = sample0 + sample1 + sample2 - (sample6 + sample7 + sample8);\n" +

                    "    border = sqrt((horizEdge * horizEdge) + (vertEdge * vertEdge));\n" +

                    "    if (border.r > 0.3 || border.g > 0.3 || border.b > 0.3){\n" +
                    "    color *= 1.0 - dot(border, border);\n" +
                    "    }\n" +

                    "    const vec3 colorRed = vec3(1.0, 0.3, 0.3);\n" +
                    "    const vec3 colorGreen = vec3(0.3, 1.0, 0.3);\n" +
                    "    const vec3 colorBlue = vec3(0.3, 0.3, 1.0);\n" +

                    "    color = floor(color * 8.0) * 0.125;\n" +
                    "    color = colorRed * color.r + colorBlue * color.b + colorGreen * color.g;\n" +

                    "    return color;\n" +
                    "    }\n ";

    public static final String edgefilter =

                    "    vec3 filter(vec3 color, samplerExternalOES sTexture, vec2 texturePos) {\n" +
                    "    float dx = uPixelSize.x;\n" +
                    "    float dy = uPixelSize.y;\n" +
                    "    float len0 = length(texture2D(sTexture, vec2(texturePos.x - dx, texturePos.y - dy)).rgb);\n" +
                    "    float len1 = length(texture2D(sTexture, vec2(texturePos.x - dx, texturePos.y)).rgb);\n" +
                    "    float len2 = length(texture2D(sTexture, vec2(texturePos.x - dx, texturePos.y + dy)).rgb);\n" +
                    "    float len3 = length(texture2D(sTexture, vec2(texturePos.x, texturePos.y - dy)).rgb);\n" +
                    "    float len4 = length(texture2D(sTexture, vec2(texturePos.x, texturePos.y)).rgb);\n" +
                    "    float len5 = length(texture2D(sTexture, vec2(texturePos.x, texturePos.y + dy)).rgb);\n" +
                    "    float len6 = length(texture2D(sTexture, vec2(texturePos.x + dx, texturePos.y - dy)).rgb);\n" +
                    "    float len7 = length(texture2D(sTexture, vec2(texturePos.x + dx, texturePos.y)).rgb);\n" +
                    "    float len8 = length(texture2D(sTexture, vec2(texturePos.x + dx, texturePos.y + dy)).rgb);\n" +

                    "    float delta = (\n" +
                    "    abs(len1 - len7) +\n" +
                    "    abs(len5 - len3) +\n" +
                    "    abs(len0 - len8) +\n" +
                    "    abs(len2 - len6) ) * 0.25;\n" +

                    "    return vec3(0.8 * delta, 1.2 * delta, 2.0 * delta);\n" +
                    "    }\n ";

    public static final String georgiafilter =
            "    vec3 filter(vec3 color, samplerExternalOES sTexture, vec2 texturePos) {\n" +
                    "    color = brightness(color, 0.4724);\n" +
                    "    color = contrast(color, 0.3149);\n" +

                    "    color.g = color.g * 0.87 + 0.13;\n" +
                    "    color.b = color.b * 0.439 + 0.561;\n" +

                    "    color *= vec3(0.981, 0.862, 0.686);\n" +

                    "    return color;\n" +
                    "    }\n ";

    public static final String polaroidfilter =
            "    vec3 filter(vec3 color, samplerExternalOES sTexture, vec2 texturePos) {\n" +
                    "    const mat4 mat = mat4(1.438, -0.062, -0.062, 0.0,\n" +
                    "    -0.122, 1.378, -0.122, 0.0,\n" +
                    "    -0.016, -0.016, 1.483, 0.0,\n" +
                    "     -0.03, 0.05, -0.02, 0.0);\n" +
                    "    return (mat * vec4(color, 1.0)).rgb;\n" +
                    "    }\n ";

    public static final String retrofilter =
            "    vec3 filter(vec3 color, samplerExternalOES sTexture, vec2 texturePos) {\n" +
                    "    float gray = dot(color, vec3(0.299, 0.587, 0.114));\n" +
                    "    color = overlay(vec3(gray), color, 1.0);\n" +
                    "    color = multiplyWithAlpha(vec3(0.984, 0.949, 0.639), 0.588235, color);\n" +
                    "    color = screenPixelComponent(vec3(0.909, 0.396, 0.702), 0.2, color);\n" +
                    "    color = screenPixelComponent(vec3(0.035, 0.286, 0.914), 0.168627, color);\n" +
                    "    return color;\n" +
                    "    }\n ";

    public static final String saharafilter =
            "    vec3 filter(vec3 color, samplerExternalOES sTexture, vec2 texturePos) {\n" +
                    "    color.r = color.r * 0.843 + 0.157;\n" +
                    "    color.b = color.b * 0.882 + 0.118;\n" +

                    "    vec3 hsv = rgbToHsv(color);\n" +
                    "    hsv.y = hsv.y * 0.55;\n" +
                    "    color = hsvToRgb(hsv);\n" +

                    "    color = saturation(color, 0.65);\n" +
                    "    color *= vec3(1.0, 0.891, 0.733);\n" +

                    "    return color;\n" +
                    "    }\n ";

    public static final String sepiafilter =
            "    vec3 filter(vec3 color, samplerExternalOES sTexture, vec2 texturePos) {\n" +
                    "    float luminosity = dot(color, vec3(0.21, 0.72, 0.07));\n" +
                    "    float brightGray = brightness(vec3(luminosity), 0.234375).r;\n" +

                    "    vec3 tinted = overlay(vec3(0.419, 0.259, 0.047), vec3(brightGray), 1.0);\n" +

                    "    float invertMask = 1.0 - luminosity;\n" +
                    "    float luminosity3 = pow(luminosity, 3.0);\n" +

                    "    return vec3(luminosity3) + (tinted * invertMask * (luminosity + 1.0));\n" +
                    "    }\n ";

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

            case "intro":
                vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                GLES20.glShaderSource(vertexShaderHandle, filterVShaderCode);
                GLES20.glCompileShader(vertexShaderHandle);
                checkGlError("Vertex shader compile");

                fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
                GLES20.glShaderSource(fragmentShaderHandle, variableF + defaultfilter + filterFShaderCode + introfilter);
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
            if(GLES20.glGetShaderInfoLog(vertexShaderHandle) !="")Log.e("SurfaceTest", "Vertex Shader glError:" + GLES20.glGetShaderInfoLog(vertexShaderHandle));
            if(GLES20.glGetShaderInfoLog(fragmentShaderHandle) !="")Log.e("SurfaceTest", variableF + defaultfilter + filterFShaderCode + mainFShaderCode+ "\nFragment Shader glError:" + GLES20.glGetShaderInfoLog(fragmentShaderHandle));
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

    public int getProgram() {
        return shaderProgram;
    }
}
