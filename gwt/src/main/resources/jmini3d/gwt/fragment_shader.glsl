precision mediump float;

uniform vec4 objectColor;

varying vec4 vPosition;

#ifdef USE_NORMALS
    varying vec3 vNormal;
#endif

#ifdef USE_MAP
    varying vec2 vTextureCoord;
    uniform sampler2D map;
#endif

#ifdef USE_ENVMAP
    uniform vec3 cameraPosition;
    uniform float reflectivity;
    uniform samplerCube envMap;
#endif

#ifdef USE_ENVMAP_AS_MAP
    varying vec4 vPositionEnvMap;
#endif

#ifdef USE_NORMAL_MAP
    uniform sampler2D normalMap;
#endif

#ifdef USE_LIGHTING

    #ifdef USE_AMBIENT_LIGHT
        uniform vec3 ambientColor;
    #endif

    #if MAX_POINT_LIGHTS > 0
        uniform vec3 pointLightPosition[MAX_POINT_LIGHTS];
        uniform vec3 pointLightColor[MAX_POINT_LIGHTS];
    #endif

    #if MAX_DIR_LIGHTS > 0
        uniform vec3 dirLightDirection[MAX_DIR_LIGHTS];
        uniform vec3 dirLightColor[MAX_DIR_LIGHTS];
    #endif
#endif

void main(void) {
    vec4 fragmentColor = vec4(0, 0, 0, 0);

    #ifdef USE_MAP
        fragmentColor = texture2D(map, vTextureCoord);
    #endif

    #ifdef USE_ENVMAP_AS_MAP
        fragmentColor = textureCube(envMap, vec3(vPositionEnvMap.x, vPositionEnvMap.z, vPositionEnvMap.y));
    #endif

    fragmentColor.rgb = mix(fragmentColor.rgb, objectColor.rgb, objectColor.a);

    #ifdef USE_ENVMAP
        if (reflectivity > 0.0) {
            vec3 eyeDirection = normalize(cameraPosition - vPosition.xyz);
            vec3 lookup = reflect(eyeDirection, normalize(vNormal.xyz));
            vec4 cubeColor = textureCube(envMap, vec3(-lookup.x, -lookup.z, -lookup.y));

            fragmentColor.rgb = mix(fragmentColor.rgb, cubeColor.rgb, reflectivity);
        }
    #endif

    #ifdef USE_LIGHTING
        vec3 lighting = vec3(0,0,0);

        #ifdef USE_AMBIENT_LIGHT
            lighting = lighting + ambientColor;
        #endif

        #ifdef USE_NORMAL_MAP
            vec3 normal = normalize(texture2D(normalMap, vTextureCoord).rgb * 2.0 - 1.0);
        #else
            vec3 normal = vNormal;
        #endif

        #if MAX_POINT_LIGHTS > 0
            for (int i = 0 ; i < MAX_POINT_LIGHTS ; i++) {
                vec3 vertexToLight = normalize(pointLightPosition[i] - vPosition.xyz);
                float weight = max(dot(normalize(normal.xyz), vertexToLight), 0.0);
                lighting = lighting + pointLightColor[i] * weight;
            }
        #endif

        #if MAX_DIR_LIGHTS > 0
            for (int i = 0 ; i < MAX_DIR_LIGHTS ; i++) {
                float weight = max(dot(normalize(normal.xyz), -normalize(dirLightDirection[i])), 0.0);
                lighting = lighting + dirLightColor[i] * weight;
            }
        #endif

        fragmentColor.rgb = fragmentColor.rgb * lighting;
    #endif

    gl_FragColor = fragmentColor;
}