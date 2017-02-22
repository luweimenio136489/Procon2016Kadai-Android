package nittcprocon.glathlete

internal class RenderingTask(var model: Model, var shader: ShaderProgram) {
    fun render() {
        model.drawWithShader(shader)
    }

    /*
     * スクストに倣ったGetter/Setterのスタイル
     */

    fun model(model: Model): RenderingTask {
        this.model = model
        return this
    }

    fun shader(shader: ShaderProgram): RenderingTask {
        this.shader = shader
        return this
    }

    fun model(): Model {
        return model
    }

    fun shader(): ShaderProgram {
        return shader
    }
}
