package mini3d.gwt;

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.DOM;

public class ResourceLoader {

	public ImageElement getImage(String image) {
		ImageElement img = ImageElement.as(DOM.createImg());
		img.setSrc(image + ".png");

		return img;
	}

}
