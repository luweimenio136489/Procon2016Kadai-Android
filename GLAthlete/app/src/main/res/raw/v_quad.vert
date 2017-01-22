/*
 * 頂点シェーダー(板ポリ)
 */

precision mediump float;

uniform mat4 stTransform;
attribute vec3 position;
varying vec2 texCoord;

void main() {
	gl_Position = vec4(position, 1.0);
}
