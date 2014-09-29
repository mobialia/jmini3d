package jmini3d.gwt;

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.DOM;

public class ResourceLoader {

	String resourcePath;

	/**
	 * @param resourcePath Path where images are located, with the trailing slash i.e.
	 *                     "img/"
	 */
	public ResourceLoader(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public ImageElement getImage(String image) {
		ImageElement img = ImageElement.as(DOM.createImg());
		img.setSrc(resourcePath + image);

		return img;
	}

}
