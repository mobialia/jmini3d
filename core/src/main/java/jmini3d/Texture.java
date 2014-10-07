package jmini3d;

public class Texture {
	public String image;
	public int status = 0;

	public Texture(String image) {
		this.image = image;
	}

	// TODO unload previous image
	public void setImage(String image) {
		if (this.image == null || !this.image.equals(image)) {
			this.image = image;
			status = 0;
		}
	}
}
