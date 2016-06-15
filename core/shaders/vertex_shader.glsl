attribute vec3 vertexPosition;

uniform mat4 perspectiveMatrix;
uniform mat4 cameraModelViewMatrix;
uniform mat4 modelViewMatrix;

varying vec4 vPosition;

#ifdef USE_NORMALS
    attribute vec3 vertexNormal;
    uniform mat3 normalMatrix;
    varying vec3 vNormal;
#endif

#ifdef USE_MAP
    attribute vec2 textureCoord;
    varying vec2 vTextureCoord;
#endif

#ifdef USE_ENVMAP_AS_MAP
    varying vec4 vPositionEnvMap;
#endif

#ifdef USE_VERTEX_COLORS
    attribute vec4 vertexColor;
    varying vec4 vVertexColor;
#endif

void main(void) {
    vPosition = modelViewMatrix * vec4(vertexPosition, 1.0);
    gl_Position = perspectiveMatrix * cameraModelViewMatrix * vPosition;

    #ifdef USE_NORMALS
        vNormal = normalize(normalMatrix * vertexNormal);
    #endif

    #ifdef USE_MAP
        vTextureCoord = textureCoord;
    #endif

    #ifdef USE_ENVMAP_AS_MAP
        vPositionEnvMap = vec4(vertexPosition, 1.0);
    #endif

    #ifdef USE_VERTEX_COLORS
        vVertexColor = vertexColor;
    #endif
}