precision mediump float;

uniform vec4 objectColor;

varying vec4 vPosition;

#ifdef USE_NORMALS
    varying vec3 vNormal;
#endif

#ifdef USE_NORMAL_MAP
    uniform sampler2D normalMap;
    uniform mat3 normalMatrix;
#endif

#ifdef USE_MAP
    varying vec2 vTextureCoord;
    uniform sampler2D map;
#endif

#ifdef USE_VERTEX_COLORS
    varying vec4 vVertexColor;
#endif

#ifdef USE_ENVMAP
    uniform float reflectivity;
    uniform samplerCube envMap;
#endif

#ifdef USE_ENVMAP_AS_MAP
    varying vec4 vPositionEnvMap;
#endif

#ifdef USE_CAMERA_POSITION
    uniform vec3 cameraPosition;
#endif

#ifdef USE_PHONG_LIGHTING
    uniform vec4 ambientColor;
    uniform vec4 diffuseColor;
    uniform vec4 specularColor;
    uniform float shininess;

    #ifdef USE_AMBIENT_LIGHT
        uniform vec4 ambientLightColor;
    #endif

    #if MAX_POINT_LIGHTS > 0
        uniform vec3 pointLightPosition[MAX_POINT_LIGHTS];
        uniform vec4 pointLightColor[MAX_POINT_LIGHTS];
    #endif

    #if MAX_DIR_LIGHTS > 0
        uniform vec3 dirLightDirection[MAX_DIR_LIGHTS];
        uniform vec4 dirLightColor[MAX_DIR_LIGHTS];
    #endif
#endif

void main(void) {
    #ifdef USE_ENVMAP_AS_MAP
        #ifdef USE_VERTEX_COLORS
            vec4 fragmentColor = vVertexColor * textureCube(envMap, vec3(vPositionEnvMap.x, vPositionEnvMap.y, vPositionEnvMap.z));
        #else
            vec4 fragmentColor = textureCube(envMap, vec3(vPositionEnvMap.x, vPositionEnvMap.y, vPositionEnvMap.z));
        #endif
        fragmentColor.rgb = mix(fragmentColor.rgb, objectColor.rgb, objectColor.a);
    #else
        #ifdef USE_MAP
            #ifdef USE_VERTEX_COLORS
                vec4 fragmentColor = vVertexColor * texture2D(map, vTextureCoord);
            #else
                vec4 fragmentColor = texture2D(map, vTextureCoord);
            #endif
            #ifdef APPLY_COLOR_TO_ALPHA
                if (fragmentColor.a < 1.0) {
                     fragmentColor = vec4(fragmentColor.r * objectColor.r, fragmentColor.g * objectColor.g, fragmentColor.b * objectColor.b, 1);
                }
            #else
                fragmentColor.rgb = mix(fragmentColor.rgb, objectColor.rgb, objectColor.a);
            #endif
        #else
            #ifdef USE_VERTEX_COLORS
                vec4 fragmentColor = vVertexColor * objectColor;
            #else
                vec4 fragmentColor = objectColor;
            #endif
        #endif
    #endif

    #ifdef USE_ENVMAP
        if (reflectivity > 0.0) {
            vec3 eyeDirection = normalize(cameraPosition - vPosition.xyz);
            vec3 lookup = reflect(eyeDirection, normalize(vNormal.xyz));
            vec4 cubeColor = textureCube(envMap, vec3(-lookup.x, -lookup.y, -lookup.z));

            fragmentColor.rgb = mix(fragmentColor.rgb, cubeColor.rgb, reflectivity);
        }
    #endif

    #ifdef USE_PHONG_LIGHTING
        vec3 diffuse = vec3(0,0,0);
        vec3 specular = vec3(0,0,0);

        #ifdef USE_AMBIENT_LIGHT
            diffuse = diffuse + ambientColor.rgb * ambientColor.a * ambientLightColor.rgb * ambientLightColor.a;
        #endif

        #ifdef USE_NORMAL_MAP
            vec3 normal = normalize(normalMatrix * (texture2D(normalMap, vTextureCoord).rgb * 2.0 - 1.0));
        #else
            vec3 normal = vNormal;
        #endif

        #if MAX_POINT_LIGHTS > 0
            for (int i = 0 ; i < MAX_POINT_LIGHTS ; i++) {
                vec3 positionToLight = pointLightPosition[i] - vPosition.xyz;
                vec3 positionToCamera = cameraPosition - vPosition.xyz;
                float diffuseWeight = diffuseColor.a * pointLightColor[i].a * max(dot(normalize(normal.xyz), normalize(positionToLight)), 0.0);
                diffuse = diffuse + diffuseColor.rgb * pointLightColor[i].rgb * diffuseWeight;

                if (dot(normalize(positionToLight), normal.xyz) > 0.0) {
                    float specularWeight = specularColor.a * pointLightColor[i].a * pow(max(dot(normalize(positionToCamera), reflect(-normalize(positionToLight), normal.xyz)), 0.0), shininess);
                    specular = specular + specularColor.rgb * pointLightColor[i].rgb * specularWeight;
                }
            }
        #endif

        #if MAX_DIR_LIGHTS > 0
            for (int i = 0 ; i < MAX_DIR_LIGHTS ; i++) {
                vec3 positionToCamera = cameraPosition - vPosition.xyz;
                float diffuseWeight = diffuseColor.a * dirLightColor[i].a * max(dot(normalize(normal.xyz), -normalize(dirLightDirection[i])), 0.0);
                diffuse = diffuse + diffuseColor.rgb * dirLightColor[i].rgb * diffuseWeight;

                if (dot(normalize(-dirLightDirection[i]), normal.xyz) > 0.0) {
                    float specularWeight = specularColor.a * dirLightColor[i].a * pow(max(dot(normalize(positionToCamera), reflect(normalize(dirLightDirection[i]), normal.xyz)), 0.0), shininess);
                    specular = specular + specularColor.rgb * dirLightColor[i].rgb * specularWeight;
                }
            }
        #endif

        fragmentColor.rgb = fragmentColor.rgb * diffuse + specular;
    #endif

    gl_FragColor = fragmentColor;
}