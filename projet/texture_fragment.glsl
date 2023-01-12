#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;

smooth in vec4 vertColor;
smooth in vec4 vertTexCoord;
smooth in vec2 vertHeat;

void main() {
    gl_FragColor = texture2D(texture, vertTexCoord.st) * vertColor;
    // Si les distances sont suffisament petites, on modifie la couleur du point
    if (vertHeat[0]<100){
        gl_FragColor.r += (100-vertHeat[0])/100;
    }
    if (vertHeat[1]<100){
        gl_FragColor.g += (100-vertHeat[1])/100;
    }
}
