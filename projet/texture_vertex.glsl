uniform mat4 transform;
uniform mat4 texMatrix;
uniform bool on_off;

attribute vec4 position;
attribute vec4 color;
attribute vec2 texCoord;
attribute vec2 heat;

smooth out vec4 vertColor;
smooth out vec4 vertTexCoord;
smooth out vec2 vertHeat;

void main() {
    gl_Position = transform * position;

    vertColor = color;
    vertTexCoord = texMatrix * vec4(texCoord, 1.0, 1.0);
    // On vérifie que les shader sont activés
    if (on_off){
        vertHeat = heat;
    }else{
        // sinon on simule que tous les points sont trop ""éloignés"
        vertHeat = vec2(200,200);
    }
}
