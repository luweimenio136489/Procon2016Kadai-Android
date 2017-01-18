/*
 * 頂点シェーダー(側面用)
 * テクスチャ座標の計算をGPUで行う
 */

precision mediump float;

uniform mat4 mvpMat;                // Model, View, Projection 変換行列
uniform mat4 stTransform;           // テクスチャからゴミを取り除く変換行列
attribute vec3 position;            // 処理するxyz空間上の座標

uniform vec2 fCenter, rCenter;
uniform vec2 fLen, rLen;

varying vec2 fTexCoord, rTexCoord;  // フラグメントシェーダに渡すテクスチャ座標(uv)

// External_OESのゴミを取り除く変換行列を適用する
vec2 apply_transform(in float u, in float v)
{
    return (stTransform * vec4(u, v, 0, 1)).xy;
}

void main()
{
    fTexCoord = apply_transform(fCenter.x - position.y * fLen.x, fCenter.y + position.x * fLen.y);
    rTexCoord = apply_transform(rCenter.x + position.y * rLen.x, rCenter.y + position.x * rLen.y);

	gl_Position = mvpMat * vec4(position, 1.0);
}
