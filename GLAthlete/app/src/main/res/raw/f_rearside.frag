/*
 * フラグメントシェーダ(側面の後側用)
 * 頂点シェーダから受け取ったテクスチャ座標の色を割合に従って混ぜて出力する
 */

#extension GL_OES_EGL_image_external : require

precision mediump float;
//uniform sampler2D texture;
uniform samplerExternalOES texture;
varying vec2 fTexCoord, rTexCoord;
varying float rRate;

void main(void)
{
    float fRate = 1.0 - rRate;
    gl_FragColor = texture2D(texture, fTexCoord) * fRate + texture2D(texture, rTexCoord) * rRate;
}
