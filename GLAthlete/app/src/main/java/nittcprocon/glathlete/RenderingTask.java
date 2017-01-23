package nittcprocon.glathlete;

class RenderingTask {
    private Model model;
    private ShaderProgram shader;

    RenderingTask(Model model, ShaderProgram shader) {
        model(model).shader(shader);
    }

    void render() {
        model.drawWithShader(shader);
    }

    /*
     * スクストに倣ったGetter/Setterのスタイル
     */

    RenderingTask model(Model model) {
        this.model = model;
        return this;
    }

    RenderingTask shader(ShaderProgram shader) {
        this.shader = shader;
        return this;
    }

    Model model() {
        return model;
    }

    ShaderProgram shader() {
        return shader;
    }
}
