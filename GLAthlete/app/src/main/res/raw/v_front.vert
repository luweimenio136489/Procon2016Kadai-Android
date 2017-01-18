/*
 * 頂点シェーダー(前面用)
 * テクスチャ座標の計算をGPUで行う
 */

precision mediump float;

uniform mat4 mvpMat;                // Model, View, Projection 変換行列
uniform mat4 stTransform;           // テクスチャからゴミを取り除く変換行列
attribute vec3 position;            // 処理するxyz空間上の座標

uniform vec2 fCenter;
uniform vec2 fLen;

varying vec2 texCoord;              // フラグメントシェーダに渡すテクスチャ座標(uv)

const float PI = 3.1415926535897932384626433832795;

// External_OESのゴミを取り除く変換行列を適用する
vec2 apply_transform(in float u, in float v)
{
    return (stTransform * vec4(u, v, 0, 1)).xy;
}

vec2 calcFTexCoord(void)
{
    float r = abs(acos(abs(position.z))) / (PI / 2.0);
    float t;

    if (position.x == 0.0)
        t = 0.0;
    else
        t = atan(position.y, position.x);

    float a = r * cos(t), b = r * sin(t);

    float fCu = fCenter.x, fCv = fCenter.y, fLu = fLen.x, fLv = fLen.y;

    return apply_transform(fCu - fLu * b, fCv + fLv * a);
}

void main()
{
    texCoord = calcFTexCoord();
	gl_Position = mvpMat * vec4(position, 1.0);
}
