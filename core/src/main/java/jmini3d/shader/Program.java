package jmini3d.shader;

import java.util.HashMap;

import jmini3d.Color4;
import jmini3d.Scene;
import jmini3d.Vector3;
import jmini3d.material.Material;

/**
 * Abstract class to be used from the Shader plugins indepently of the Android or GWT rendering
 * architecture
 */
public abstract class Program {

	public static String DEFAULT_VERTEX_SHADER = "vertex_shader.glsl";
	public static String DEFAULT_FRAGMENT_SHADER = "fragment_shader.glsl";

	public static final int TEXTURE_CUBE_MAP_NEGATIVE_X = 0x8516;
	public static final int TEXTURE_CUBE_MAP_NEGATIVE_Y = 0x8518;
	public static final int TEXTURE_CUBE_MAP_NEGATIVE_Z = 0x851A;
	public static final int TEXTURE_CUBE_MAP_POSITIVE_X = 0x8515;
	public static final int TEXTURE_CUBE_MAP_POSITIVE_Y = 0x8517;
	public static final int TEXTURE_CUBE_MAP_POSITIVE_Z = 0x8519;

	// Relative to the axis system
	public static int[] CUBE_MAP_SIDES = {TEXTURE_CUBE_MAP_POSITIVE_X, TEXTURE_CUBE_MAP_NEGATIVE_X,
			TEXTURE_CUBE_MAP_POSITIVE_Z, TEXTURE_CUBE_MAP_NEGATIVE_Z,
			TEXTURE_CUBE_MAP_POSITIVE_Y, TEXTURE_CUBE_MAP_NEGATIVE_Y};

	public int shaderKey = -1;

	// The shader program can be changed by a shader plugin
	public String vertexShaderName = DEFAULT_VERTEX_SHADER;
	public String fragmentShaderName = DEFAULT_FRAGMENT_SHADER;
	// A list of "#define key value" or "#define key" (if value == null) to be appended at the beginning of the shader
	public HashMap<String, String> shaderDefines = new HashMap<>();

	public boolean useNormals = false;
	public boolean useMap = false;
	public boolean useEnvMap = false;
	public boolean useNormalMap = false;
	public boolean useVertexColors = false;
	public boolean useCameraPosition = false;
	public boolean useShaderPlugins = false;

	public int vertexPositionAttribLocation = -1;
	public int vertexNormalAttribLocation = -1;
	public int textureCoordAttribLocation = -1;
	public int vertexColorAttribLocation = -1;

	// Cached values to avoid setting buffers with an already existing value
	public Integer activeVertexPosition = null;
	public Integer activeVertexNormal = null;
	public Integer activeTextureCoord = null;
	public Integer activeVertexColor = null;
	public Integer activeFacesBuffer = null;

	public int projectionMatrixUniform;
	public int modelMatrixUniform;
	public int viewMatrixUniform;
	public int normalMatrixUniform;

	public int mapUniform;
	public int reflectivityUniform;
	public int envMapUniform;
	public int normalMapUniform;
	public int cameraPositionUniform;
	public int objectColorUniform;

	// Cached values to avoid setting uniforms two times
	public int mapLast = -1;
	public int envMapLast = -1;
	public int normalMapLast = -1;
	public float projectionMatrixLast[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	public float viewMatrixLast[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	public float modelMatrixLast[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	public float normalMatrixLast[] = {0, 0, 0, 0, 0, 0, 0, 0, 0};
	public Color4 objectColorLast = Color4.fromFloat(-1, -1, -1, -1);
	public Vector3 cameraPositionLast = new Vector3(0, 0, 0);
	public float reflectivityLast;

	public ProgramPlugin sceneProgramPlugin;
	public ProgramPlugin materialProgramPlugin;

	public void prepareShader(Scene scene, Material material) {
		useShaderPlugins = (scene.shaderKey & material.shaderKey & ShaderKey.SHADER_PLUGIN_MASK) != 0;

		if (useShaderPlugins) {
			if (scene.shaderPlugin != null) {
				sceneProgramPlugin = scene.shaderPlugin.getProgramPlugin(this);
				sceneProgramPlugin.prepareShader(scene, material);
			}
			if (material.shaderPlugin != null) {
				materialProgramPlugin = material.shaderPlugin.getProgramPlugin(this);
				materialProgramPlugin.prepareShader(scene, material);
			}
		}

		if (material.map != null) {
			shaderDefines.put("USE_MAP", null);
			useMap = true;
		}

		if (material.envMap != null) {
			shaderDefines.put("USE_ENVMAP", null);
			useNormals = true;
			useEnvMap = true;
			useCameraPosition = true;
		}

		if (material.useEnvMapAsMap) {
			shaderDefines.put("USE_ENVMAP_AS_MAP", null);
		}

		if (material.applyColorToAlpha) {
			shaderDefines.put("APPLY_COLOR_TO_ALPHA", null);
		}

		if (material.useVertexColors) {
			useVertexColors = true;
			shaderDefines.put("USE_VERTEX_COLORS", null);
		}

		if (useCameraPosition) {
			shaderDefines.put("USE_CAMERA_POSITION", null);
		}

		if (useNormals) {
			if (material.normalMap != null) {
				shaderDefines.put("USE_NORMAL_MAP", null);
				useNormalMap = true;
				useNormals = false;
			} else {
				shaderDefines.put("USE_NORMALS", null);
			}
		}
	}

	public void onShaderLoaded() {
		if (sceneProgramPlugin != null) {
			sceneProgramPlugin.onShaderLoaded();
		}
		if (materialProgramPlugin != null) {
			materialProgramPlugin.onShaderLoaded();
		}

		projectionMatrixUniform = getUniformLocation("projectionMatrix");
		viewMatrixUniform = getUniformLocation("viewMatrix");
		modelMatrixUniform = getUniformLocation("modelMatrix");
		objectColorUniform = getUniformLocation("objectColor");

		if (useNormals || useNormalMap) {
			normalMatrixUniform = getUniformLocation("normalMatrix");
		}
		if (useNormalMap) {
			normalMapUniform = getUniformLocation("normalMap");
		}
		if (useMap) {
			mapUniform = getUniformLocation("map");
		}
		if (useEnvMap) {
			reflectivityUniform = getUniformLocation("reflectivity");
			envMapUniform = getUniformLocation("envMap");
		}
		if (useCameraPosition) {
			cameraPositionUniform = getUniformLocation("cameraPosition");
		}

		// Initialize attrib locations: be careful, they are also enabled, so it
		// cannot be called before the value is going to be set
		vertexPositionAttribLocation = getAndEnableAttribLocation("vertexPosition");

		// Save memory
		shaderDefines = null;
	}

	public abstract int getAndEnableAttribLocation(String attribName);

	public abstract int getUniformLocation(String uniformName);

	/**
	 * @returns uniformValue
	 */
	public abstract float setFloatUniformIfValueChanged(int uniform, float uniformValue, float lastValue);

	public abstract void setColorUniformIfChanged(int colorUniform, Color4 newColor, Color4 lastColor);

	public abstract void setUniform3fv(int location, int count, float[] v);

	public abstract void setUniform4fv(int location, int count, float[] v);

}
