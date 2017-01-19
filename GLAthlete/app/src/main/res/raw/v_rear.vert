/*
 * 頂点シェーダー(背面用)
 * テクスチャ座標の計算をGPUで行う
 */

precision mediump float;

uniform mat4 mvpMat;                // Model, View, Projection 変換行列
uniform mat4 stTransform;           // テクスチャからゴミを取り除く変換行列
attribute vec3 position;            // 処理するxyz空間上の座標

uniform vec2 rCenter;
uniform vec2 rLen;

varying vec2 texCoord;              // フラグメントシェーダに渡すテクスチャ座標(uv)

const float PI = 3.1415926535897932384626433832795;

// External_OESのゴミを取り除く変換行列を適用する
vec2 apply_transform(in float u, in float v)
{
    return (stTransform * vec4(u, v, 0, 1)).xy;
}

vec2 calcRTexCoord(void)
{
    float r = abs(acos(abs(position.z))) / (PI / 2.0);
    float t;

    if (position.x == 0.0)
        t = 0.0;
    else
        t = atan(position.y, position.x);

    float a = -r * cos(t), b = r * sin(t);

    float rCu = rCenter.x, rCv = rCenter.y, rLu = rLen.x, rLv = rLen.y;

    return apply_transform(rCu + rLu * b, rCv + rLv * a);
}

void main()
{
    texCoord = calcRTexCoord();
	gl_Position = mvpMat * vec4(position, 1.0);
}
