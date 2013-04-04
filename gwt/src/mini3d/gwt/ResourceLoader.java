package mini3d.gwt;

import com.google.gwt.user.client.ui.Image;

public class ResourceLoader {

	public Image getImage(String image) {
		return new Image(image + ".png");
	}

}
