package jmini3d;

public class CubeMapTexture {

	public String[] images;
	public int status = 0;
	public int loadCount = 0;

	public CubeMapTexture(String[] images) {
		this.images = images;
	}

}