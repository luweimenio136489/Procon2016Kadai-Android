package nittcprocon.glathlete

/**
 * わざわざ自分で作りたくないシリーズ
 * static importを乱用していくスタイル
 */

internal class Types {
    // float2要素のベクトル
    class Vec2f internal constructor(internal var x: Float, internal var y: Float) {

        internal fun asArray(): Array<Float> {
            return arrayOf(x, y)
        }

        internal fun asList(): List<Float> {
            return java.util.Arrays.asList(*asArray())
        }
    }

    // float3要素のベクトル
    internal class Vec3f(var x: Float, var y: Float, var z: Float) {

        fun asArray(): Array<Float> {
            return arrayOf(x, y, z)
        }

        fun asList(): List<Float> {
            return java.util.Arrays.asList(*asArray())
        }

        // dedupに使う
        override fun equals(obj: Any?): Boolean {
            if (obj == null)
                return false

            if (this === obj)
                return true

            if (javaClass == obj.javaClass) {
                val other = obj as Vec3f?
                if (x == other!!.x && y == other.y && z == other.z)
                    return true
            }

            return false
        }
    }

    // 3頂点からなる三角形
    // a, b, c の順に右回り
    internal class Tri(var a: Vec3f, var b: Vec3f, var c: Vec3f) {

        fun asArray(): Array<Vec3f> {
            return arrayOf(a, b, c)
        }

        fun asList(): List<Vec3f> {
            return java.util.Arrays.asList(*asArray())
        }
    }

    // 4頂点からなる四角形
    // a, b, c, d の順に右回り
    internal class Quad(var a: Vec3f, var b: Vec3f, var c: Vec3f, var d: Vec3f) {

        fun asArray(): Array<Vec3f> {
            return arrayOf(a, b, c, d)
        }

        fun asList(): List<Vec3f> {
            return java.util.Arrays.asList(*asArray())
        }
    }
}
