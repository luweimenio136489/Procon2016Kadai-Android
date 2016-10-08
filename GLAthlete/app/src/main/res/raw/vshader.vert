/*
 * 頂点シェーダー
 * テクスチャ座標の計算をGPUで行う
 * シェーダーで条件分岐するのちょっとイヤなのでそのうち実装を変えるかも
 */

uniform mat4 mvpMat;
attribute vec3 position;

varying vec2 texCoord;

// uv空間の値
// 中心を画像から見てやや内側に補正
const vec2 fCenter = vec2(0.25, 0.4444);
//const vec2 fCenter = vec2(0.255, 0.4444);
const vec2 rCenter = vec2(0.75, 0.4444);
//const vec2 rCenter = vec2(0.745, 0.4444);
// 半径(？)
const vec2 fLen = vec2(0.25, 0.4444) * 0.87;
const vec2 rLen = vec2(0.25, 0.4444) * 0.87;

void main() {
    //vColor = vec4(abs(position.x), abs(position.y), abs(position.z), 1.0);
    if (position.z > 0.0) {         // front
        texCoord = vec2(fCenter.x - position.y * fLen.x, fCenter.y + position.x * fLen.y);
    } else {                        // rear
        texCoord = vec2(rCenter.x + position.y * rLen.x, rCenter.y + position.x * rLen.y);
    }
	gl_Position = mvpMat * vec4(position, 1.0);
}
