package mini3d.gwt;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FocusWidget;
import com.googlecode.gwtgl.binding.WebGLRenderingContext;

public class IeWebGLWidget extends FocusWidget {
	Element element;
	
	public IeWebGLWidget() {
		element = DOM.createElement("object");
		DOM.setElementProperty(element, "type", "application/x-webgl");

		setElement(element);
		setStylePrimaryName("object");
	}

	public void setWidh(String width) {
        DOM.setElementProperty(element, "width", width);
	}

	public void setHeight(String height) {
        DOM.setElementProperty(element, "height", height);
	}
	
	public native WebGLRenderingContext getContext(String context) /*-{
		var element = this.@mini3d.gwt.IeWebGLWidget::element;
		return element.getContext(context);
	}-*/;
}