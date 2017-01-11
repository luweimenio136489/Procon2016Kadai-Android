/*
 * フラグメントシェーダ(側面用)(stub)
 * 頂点シェーダから受け取ったテクスチャ座標の色を出力する
 */

#extension GL_OES_EGL_image_external : require

precision mediump float;
//uniform sampler2D texture;
uniform samplerExternalOES texture;
varying vec2 fTexCoord, rTexCoord;

void main(void)
{
    gl_FragColor = (texture2D(texture, fTexCoord) + texture2D(texture, rTexCoord)) * 0.5;
}
