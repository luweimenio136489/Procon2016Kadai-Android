/*
 * フラグメントシェーダ(側面の前側用)
 * 頂点シェーダから受け取ったテクスチャ座標の色を割合に従って混ぜて出力する
 */

#extension GL_OES_EGL_image_external : require

precision mediump float;
//uniform sampler2D texture;
uniform samplerExternalOES texture;
varying vec2 fTexCoord, rTexCoord;
varying float fRate;

void main(void)
{
    float rRate = 1.0 - fRate;
    gl_FragColor = texture2D(texture, fTexCoord) * fRate + texture2D(texture, rTexCoord) * rRate;
}
