package jmini3d.gwt;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;

public class ResourceLoader {

	String resourcePath;
	String shaderPath;

	public static interface OnTextResourceLoaded {
		public void onResourceLoaded(String text);
	}

	/**
	 * @param resourcePath Path where images are located, with the trailing slash i.e. "img/"
	 * @param shaderPath Path where the shaders can be found (caution! they must be copied manually)
	 */
	public ResourceLoader(String resourcePath, String shaderPath) {
		this.resourcePath = resourcePath;
		this.shaderPath = shaderPath;
	}

	public ImageElement getImage(String image) {
		ImageElement img = ImageElement.as(DOM.createImg());
		img.setSrc(resourcePath + image);

		return img;
	}

	/**
	 * The GWT shader load is async
	 *
	 * @param file
	 */
	public void loadShader(String file, OnTextResourceLoaded listener) {
		XMLHttpRequest request = XMLHttpRequest.create();

		request.setOnReadyStateChange(new ReadyStateChangeHandler() {

			@Override
			public void onReadyStateChange(XMLHttpRequest xhr) {
				if (xhr.getReadyState() == XMLHttpRequest.DONE) {
					// ASYNC
					listener.onResourceLoaded(xhr.getResponseText());
				}
			}
		});

		request.open("GET", GWT.getHostPageBaseURL() + shaderPath + file);
		request.send();
	}

}
