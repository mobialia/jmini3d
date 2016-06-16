attribute vec3 vertexPosition;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

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

uniform float lensDistortionC1;
uniform float lensDistortionC2;
uniform float lensDistortionC3;
uniform float lensDistortionC4;
uniform float lensDistortionC5;
uniform float lensDistortionC6;
uniform float lensDistortionMaxRadiusSQ;

float distortionFactor(float rSquared) {
    float ret = 0.0;
    ret = rSquared * (ret + lensDistortionC6);
    ret = rSquared * (ret + lensDistortionC5);
    ret = rSquared * (ret + lensDistortionC4);
    ret = rSquared * (ret + lensDistortionC3);
    ret = rSquared * (ret + lensDistortionC2);
    ret = rSquared * (ret + lensDistortionC1);
    return ret + 1.0;
}

vec4 distortVertex(vec4 pos) {
    float r2 = clamp(dot(pos.xy, pos.xy) / (pos.z * pos.z), 0.0, lensDistortionMaxRadiusSQ);
    pos.xy *= distortionFactor(r2);
    return pos;
}

void main(void) {
    vPosition = modelMatrix * vec4(vertexPosition, 1.0);
    gl_Position = projectionMatrix * distortVertex(viewMatrix * vPosition);

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