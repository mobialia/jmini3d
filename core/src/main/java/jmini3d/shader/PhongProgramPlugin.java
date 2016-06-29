package jmini3d.shader;

import jmini3d.Color4;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.light.AmbientLight;
import jmini3d.light.DirectionalLight;
import jmini3d.light.Light;
import jmini3d.light.PointLight;
import jmini3d.material.Material;
import jmini3d.material.PhongMaterial;

public class PhongProgramPlugin extends ProgramPlugin {

	boolean useLighting = false;
	boolean useAmbientLight = false;
	boolean usePointLight = false;
	boolean useDirLight = false;

	int maxPointLights;
	int maxDirLights;

	int ambientColorUniform;
	int diffuseColorUniform;
	int specularColorUniform;
	int shininessUniform;
	int ambientLightColorUniform;
	int pointLightPositionUniform;
	int pointLightColorUniform;
	int dirLightDirectionUniform;
	int dirLightColorUniform;

	float pointLightPositions[];
	float pointLightColors[];
	float dirLightDirections[];
	float dirLightColors[];

	float shininess = 0;
	Color4 ambientColor = Color4.fromFloat(-1, -1, -1, -1);
	Color4 diffuseColor = Color4.fromFloat(-1, -1, -1, -1);
	Color4 specularColor = Color4.fromFloat(-1, -1, -1, -1);
	Color4 ambientLightColor = Color4.fromFloat(-1, -1, -1, -1);

	public PhongProgramPlugin(Program program) {
		super(program);
	}

	@Override
	public void prepareShader(Scene scene, Material material) {
		maxPointLights = 0;
		maxDirLights = 0;

		if (scene.lights.size() > 0) {
			program.shaderDefines.put("USE_PHONG_LIGHTING", null);
			useLighting = true;
			program.useNormals = true;

			for (Light light : scene.lights) {

				if (light instanceof AmbientLight) {
					useAmbientLight = true;
					program.shaderDefines.put("USE_AMBIENT_LIGHT", null);
				}

				if (light instanceof PointLight) {
					maxPointLights++;
				}

				if (light instanceof DirectionalLight) {
					maxDirLights++;
				}
			}

			if (maxPointLights > 0) {
				program.shaderDefines.put("USE_POINT_LIGHT", null);
				usePointLight = true;
				program.useCameraPosition = true;

				pointLightPositions = new float[maxPointLights * 3];
				pointLightColors = new float[maxPointLights * 4];
			}

			if (maxDirLights > 0) {
				program.shaderDefines.put("USE_DIR_LIGHT", null);
				useDirLight = true;
				program.useCameraPosition = true;

				dirLightDirections = new float[maxDirLights * 3];
				dirLightColors = new float[maxDirLights * 4];
			}
		}

		program.shaderDefines.put("MAX_POINT_LIGHTS", String.valueOf(maxPointLights));
		program.shaderDefines.put("MAX_DIR_LIGHTS", String.valueOf(maxDirLights));
	}

	@Override
	public void onShaderLoaded() {
		if (useLighting) {
			ambientColorUniform = program.getUniformLocation("ambientColor");
			diffuseColorUniform = program.getUniformLocation("diffuseColor");
			specularColorUniform = program.getUniformLocation("specularColor");
			shininessUniform = program.getUniformLocation("shininess");

			if (useAmbientLight) {
				ambientLightColorUniform = program.getUniformLocation("ambientLightColor");
			}
			if (usePointLight) {
				pointLightPositionUniform = program.getUniformLocation("pointLightPosition");
				pointLightColorUniform = program.getUniformLocation("pointLightColor");
			}
			if (useDirLight) {
				dirLightDirectionUniform = program.getUniformLocation("dirLightDirection");
				dirLightColorUniform = program.getUniformLocation("dirLightColor");
			}
		}
	}

	@Override
	public void onSetSceneUniforms(Scene scene) {
		if (useLighting) {
			int pointLightIndex = 0;
			int dirLightIndex = 0;

			for (int i = 0; i < scene.lights.size(); i++) {
				Light light = scene.lights.get(i);
				if (light instanceof AmbientLight) {
					program.setColorUniformIfChanged(ambientLightColorUniform, light.color, ambientLightColor);
				}

				if (light instanceof PointLight) {
					pointLightPositions[pointLightIndex * 3] = ((PointLight) light).position.x;
					pointLightPositions[pointLightIndex * 3 + 1] = ((PointLight) light).position.y;
					pointLightPositions[pointLightIndex * 3 + 2] = ((PointLight) light).position.z;

					pointLightColors[pointLightIndex * 4] = light.color.r;
					pointLightColors[pointLightIndex * 4 + 1] = light.color.g;
					pointLightColors[pointLightIndex * 4 + 2] = light.color.b;
					pointLightColors[pointLightIndex * 4 + 3] = light.color.a;

					pointLightIndex++;
				}

				if (light instanceof DirectionalLight) {
					dirLightDirections[dirLightIndex * 3] = ((DirectionalLight) light).direction.x;
					dirLightDirections[dirLightIndex * 3 + 1] = ((DirectionalLight) light).direction.y;
					dirLightDirections[dirLightIndex * 3 + 2] = ((DirectionalLight) light).direction.z;

					dirLightColors[dirLightIndex * 4] = light.color.r;
					dirLightColors[dirLightIndex * 4 + 1] = light.color.g;
					dirLightColors[dirLightIndex * 4 + 2] = light.color.b;
					dirLightColors[dirLightIndex * 4 + 3] = light.color.a;

					dirLightIndex++;
				}
			}
			if (maxPointLights > 0) {
				program.setUniform3fv(pointLightPositionUniform, maxPointLights, pointLightPositions);
				program.setUniform4fv(pointLightColorUniform, maxPointLights, pointLightColors);
			}
			if (maxDirLights > 0) {
				program.setUniform3fv(dirLightDirectionUniform, maxDirLights, dirLightDirections);
				program.setUniform4fv(dirLightColorUniform, maxDirLights, dirLightColors);
			}
		}
	}

	public void onDrawObject(Object3d o3d) {
		if (useLighting) {
			program.setColorUniformIfChanged(ambientColorUniform, ((PhongMaterial) o3d.material).ambient, ambientColor);
			program.setColorUniformIfChanged(diffuseColorUniform, ((PhongMaterial) o3d.material).diffuse, diffuseColor);
			program.setColorUniformIfChanged(specularColorUniform, ((PhongMaterial) o3d.material).specular, specularColor);
			shininess = program.setFloatUniformIfValueChanged(shininessUniform, ((PhongMaterial) o3d.material).shininess, shininess);
		}
	}
}