package jmini3d.gwt;

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.DOM;

public class ResourceLoader {

	String imagePath;

	/**
	 * 
	 * @param imagePath
	 *            Path where images are located, with the trailing slash i.e.
	 *            "img/"
	 */
	public ResourceLoader(String imagePath) {
		this.imagePath = imagePath;
	}

	public ImageElement getImage(String image) {
		ImageElement img = ImageElement.as(DOM.createImg());
		img.setSrc(imagePath + image + ".png");

		return img;
	}

}
