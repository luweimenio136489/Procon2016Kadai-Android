package nittcprocon.glathlete;

import static nittcprocon.glathlete.Types.*;

interface Model {
    Model addTri(Tri tri);
    Model addQuad(Quad quad);
    void drawWithShader(ShaderProgram shader);
}
