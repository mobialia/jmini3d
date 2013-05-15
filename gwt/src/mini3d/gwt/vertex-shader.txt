attribute vec3 cameraPosition;

attribute vec3 vertexPosition;
attribute vec3 vertexNormal;
attribute vec2 textureCoord;

uniform mat4 modelViewMatrix;
uniform mat4 perspectiveMatrix;
uniform mat3 normalMatrix;

uniform bool useLighting;
uniform vec3 ambientColor;

uniform vec3 pointLightingLocation;
uniform vec3 pointLightingColor;

uniform float objectColorTrans;
uniform vec3 objectColor;

varying vec2 vTextureCoord;
varying vec3 vLighting;
varying vec3 vNormal;
varying vec4 vPosition;

varying vec3 vObjectColor;
varying float vObjectColorTrans;

void main(void) {
	vNormal = normalize(normalMatrix * vertexNormal);
    vPosition = modelViewMatrix * vec4(vertexPosition, 1.0);
    vObjectColor = objectColor;
    vObjectColorTrans = objectColorTrans;
 
    gl_Position = perspectiveMatrix * vPosition;
    vTextureCoord = textureCoord;

    if (!useLighting) {
        vLighting = vec3(1.0, 1.0, 1.0);
    } else {
        vec3 lightDirection = normalize(pointLightingLocation - vPosition.xyz);

        float directionalLightWeighting = max(dot(vNormal, lightDirection), 0.0);
        vLighting = ambientColor + pointLightingColor * directionalLightWeighting;
    }
}