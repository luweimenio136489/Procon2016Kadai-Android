/*
 * フラグメントシェーダ(板ポリ)
 */

#extension GL_OES_EGL_image_external : require

precision mediump float;

uniform mat4 stTransform;
uniform samplerExternalOES texture;
varying vec2 texCoord;

// External_OESのゴミを取り除く変換行列を適用する
vec2 apply_transform(in vec2 uv)
{
    return (stTransform * vec4(uv, 0, 1)).xy;
}

void main(void) {
	gl_FragColor = texture2D(texture, texCoord);
	//gl_FragColor = texture2D(texture, apply_transform(gl_FragCoord.xy));
}
