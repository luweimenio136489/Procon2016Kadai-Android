/*
 * フラグメントシェーダ(前面・背面用)
 * 頂点シェーダから受け取ったテクスチャ座標の色を素直に出力する
 */

#extension GL_OES_EGL_image_external : require

precision mediump float;
//uniform sampler2D texture;
uniform samplerExternalOES texture;
varying vec2 texCoord;

void main(void)
{
    gl_FragColor = texture2D(texture, texCoord);
}
