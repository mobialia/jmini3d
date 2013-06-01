package jmini3d.gwt.input;

import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchCancelHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.user.client.ui.FocusWidget;

import java.util.HashMap;

import jmini3d.input.TouchListener;
import jmini3d.input.TouchPointer;

public class TouchController implements MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseOutHandler, TouchStartHandler, TouchMoveHandler, TouchEndHandler, TouchCancelHandler {

	static final Integer MOUSE_POINTER_ID = 666;

	TouchListener listener;
	TouchPointer mousePointer;

	int eventX;
	int eventY;

	HashMap<Integer, TouchPointer> pointers = new HashMap<Integer, TouchPointer>();
	HashMap<Integer, TouchPointer> pointersAux = new HashMap<Integer, TouchPointer>();

	FocusWidget widget;

	public TouchController(FocusWidget widget) {
		this.widget = widget;

		widget.addMouseDownHandler(this);
		widget.addMouseUpHandler(this);
		widget.addMouseMoveHandler(this);
		widget.addMouseOutHandler(this);

		widget.addTouchStartHandler(this);
		widget.addTouchMoveHandler(this);
		widget.addTouchEndHandler(this);
		widget.addTouchCancelHandler(this);

		mousePointer = new TouchPointer();
	}

	public void setListener(TouchListener listener) {
		this.listener = listener;
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
		mousePointer.x = event.getX();
		mousePointer.y = event.getY();
		mousePointer.status = TouchPointer.TOUCH_DOWN;

		pointers.put(MOUSE_POINTER_ID, mousePointer);
		listener.onTouch(pointers);
		event.preventDefault();
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		mousePointer.x = event.getX();
		mousePointer.y = event.getY();
		mousePointer.status = TouchPointer.TOUCH_MOVE;

		if (pointers.containsKey(MOUSE_POINTER_ID)) {
			listener.onTouch(pointers);
		}
		event.preventDefault();
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		mousePointer.x = event.getX();
		mousePointer.y = event.getY();
		mousePointer.status = TouchPointer.TOUCH_UP;

		if (pointers.containsKey(MOUSE_POINTER_ID)) {
			listener.onTouch(pointers);
			pointers.remove(MOUSE_POINTER_ID);
		}
		event.preventDefault();
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		mousePointer.x = event.getX();
		mousePointer.y = event.getY();
		mousePointer.status = TouchPointer.TOUCH_UP;

		if (pointers.containsKey(MOUSE_POINTER_ID)) {
			listener.onTouch(pointers);
			pointers.remove(MOUSE_POINTER_ID);
		}
		event.preventDefault();
	}

	@Override
	public void onTouchStart(TouchStartEvent event) {
		for (int i = 0; i < event.getChangedTouches().length(); i++) {
			Touch touch = event.getChangedTouches().get(i);
			TouchPointer pointer = pointersAux.get(touch.getIdentifier());
			if (pointer == null) {
				pointer = new TouchPointer();
			}
			pointer.x = touch.getRelativeX(widget.getElement());
			pointer.y = touch.getRelativeY(widget.getElement());
			pointer.status = TouchPointer.TOUCH_DOWN;
			pointers.put(touch.getIdentifier(), pointer);
		}
		listener.onTouch(pointers);

		for (TouchPointer pointer : pointers.values()) {
			pointer.status = TouchPointer.TOUCH_MOVE;
		}

		event.preventDefault();
	}

	@Override
	public void onTouchMove(TouchMoveEvent event) {
		for (int i = 0; i < event.getChangedTouches().length(); i++) {
			Touch touch = event.getChangedTouches().get(i);
			TouchPointer pointer = pointers.get(touch.getIdentifier());
			pointer.x = touch.getRelativeX(widget.getElement());
			pointer.y = touch.getRelativeY(widget.getElement());
			pointer.status = TouchPointer.TOUCH_MOVE;
		}
		listener.onTouch(pointers);
		event.preventDefault();
	}

	@Override
	public void onTouchEnd(TouchEndEvent event) {
		for (Integer key : pointers.keySet()) {
			if (!MOUSE_POINTER_ID.equals(key)) {
				TouchPointer pointer = pointers.get(key);
				pointer.status = TouchPointer.TOUCH_UP;
			}
		}

		for (int i = 0; i < event.getTouches().length(); i++) {
			Touch touch = event.getTouches().get(i);
			TouchPointer pointer = pointers.get(touch.getIdentifier());
			pointer.status = TouchPointer.TOUCH_MOVE;
		}
		listener.onTouch(pointers);

		for (Integer key : pointers.keySet()) {
			if (!MOUSE_POINTER_ID.equals(key)) {
				TouchPointer pointer = pointers.get(key);
				if (pointer.status == TouchPointer.TOUCH_UP) {
					pointers.remove(key);
				}
			}
		}

		event.preventDefault();
	}

	@Override
	public void onTouchCancel(TouchCancelEvent event) {
		mousePointer.status = TouchPointer.TOUCH_UP;
		listener.onTouch(pointers);
		event.preventDefault();
	}
}