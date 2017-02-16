/*
 * 頂点シェーダー(板ポリ)
 */

precision mediump float;

uniform mat4 stTransform;
attribute vec3 position;
varying vec2 texCoord;

// External_OESのゴミを取り除く変換行列を適用する
vec2 apply_transform(in vec2 uv)
{
    return (stTransform * vec4(uv, 0, 1)).xy;
}

vec2 calc_texCoord()
{
    return apply_transform(position.xy / 2.0 + vec2(0.5, 0.5));
}

void main() {
	gl_Position = vec4(position, 1.0);
	texCoord = calc_texCoord();
}
