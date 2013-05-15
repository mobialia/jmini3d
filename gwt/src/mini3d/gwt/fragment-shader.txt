precision mediump float;

varying vec2 vTextureCoord;
varying vec3 vLighting;
varying vec3 vNormal;
varying vec4 vPosition;

varying vec3 vObjectColor;
varying float vObjectColorTrans;

uniform sampler2D sampler;

uniform float reflectivity;
uniform samplerCube envMap;

void main(void) {
    vec4 fragmentColor = texture2D(sampler, vec2(vTextureCoord.s, vTextureCoord.t));
    gl_FragColor = vec4(mix(fragmentColor.rgb, vObjectColor, vObjectColorTrans) * vLighting, fragmentColor.a);

	if (reflectivity > 0.0) {
    	vec3 eyeDirection = normalize(-vPosition.xyz);
    	vec3 lookup = reflect(eyeDirection, vNormal);
    	vec4 cubeColor = textureCube(envMap, -lookup);
    	
		gl_FragColor.rgb = mix(gl_FragColor.rgb, cubeColor.rgb, reflectivity);
	}
}