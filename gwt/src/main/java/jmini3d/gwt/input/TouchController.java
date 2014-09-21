package jmini3d.gwt.input;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

import java.util.HashMap;

import jmini3d.input.TouchListener;
import jmini3d.input.TouchPointer;

public class TouchController {

	static final Integer MOUSE_POINTER_ID = 666;

	TouchListener listener;
	TouchPointer mousePointer;

	HashMap<Integer, TouchPointer> pointers = new HashMap<Integer, TouchPointer>();
	HashMap<Integer, TouchPointer> pointersAux = new HashMap<Integer, TouchPointer>();

	Element element;

	public TouchController(final Element element) {
		this.element = element;
		Event.setEventListener(element, new EventListener() {
			@Override
			public void onBrowserEvent(Event event) {
				if ((event.getTypeInt() & Event.ONMOUSEDOWN) != 0) {
					mousePointer.x = event.getClientX() - element.getAbsoluteLeft();
					mousePointer.y = event.getClientY() - element.getAbsoluteTop();
					mousePointer.status = TouchPointer.TOUCH_DOWN;
					pointers.put(MOUSE_POINTER_ID, mousePointer);
					listener.onTouch(pointers);
					event.preventDefault();
				}
				if ((event.getTypeInt() & Event.ONMOUSEDOWN) != 0) {
					mousePointer.x = event.getClientX() - element.getAbsoluteLeft();
					mousePointer.y = event.getClientY() - element.getAbsoluteTop();
					mousePointer.status = TouchPointer.TOUCH_MOVE;

					if (pointers.containsKey(MOUSE_POINTER_ID)) {
						listener.onTouch(pointers);
					}
					event.preventDefault();
				}
				if ((event.getTypeInt() & Event.ONMOUSEMOVE) != 0) {
					mousePointer.x = event.getClientX() - element.getAbsoluteLeft();
					mousePointer.y = event.getClientY() - element.getAbsoluteTop();
					mousePointer.status = TouchPointer.TOUCH_MOVE;

					if (pointers.containsKey(MOUSE_POINTER_ID)) {
						listener.onTouch(pointers);
					}
					event.preventDefault();
				}
				if ((event.getTypeInt() & Event.ONMOUSEUP) != 0) {
					mousePointer.x = event.getClientX() - element.getAbsoluteLeft();
					mousePointer.y = event.getClientY() - element.getAbsoluteTop();
					mousePointer.status = TouchPointer.TOUCH_UP;

					if (pointers.containsKey(MOUSE_POINTER_ID)) {
						listener.onTouch(pointers);
						pointers.remove(MOUSE_POINTER_ID);
					}
					event.preventDefault();
				}
				if ((event.getTypeInt() & Event.ONMOUSEOUT) != 0) {
					mousePointer.x = event.getClientX() - element.getAbsoluteLeft();
					mousePointer.y = event.getClientY() - element.getAbsoluteTop();
					mousePointer.status = TouchPointer.TOUCH_UP;

					if (pointers.containsKey(MOUSE_POINTER_ID)) {
						listener.onTouch(pointers);
						pointers.remove(MOUSE_POINTER_ID);
					}
					event.preventDefault();
				}
				if ((event.getTypeInt() & Event.ONTOUCHSTART) != 0) {
					for (int i = 0; i < event.getChangedTouches().length(); i++) {
						Touch touch = event.getChangedTouches().get(i);
						TouchPointer pointer = pointersAux.get(touch.getIdentifier());
						if (pointer == null) {
							pointer = new TouchPointer();
						}
						pointer.x = touch.getRelativeX(element);
						pointer.y = touch.getRelativeY(element);
						pointer.status = TouchPointer.TOUCH_DOWN;
						pointers.put(touch.getIdentifier(), pointer);
					}
					listener.onTouch(pointers);

					for (TouchPointer pointer : pointers.values()) {
						pointer.status = TouchPointer.TOUCH_MOVE;
					}
					event.preventDefault();
				}
				if ((event.getTypeInt() & Event.ONTOUCHMOVE) != 0) {
					for (int i = 0; i < event.getChangedTouches().length(); i++) {
						Touch touch = event.getChangedTouches().get(i);
						TouchPointer pointer = pointers.get(touch.getIdentifier());
						pointer.x = touch.getRelativeX(element);
						pointer.y = touch.getRelativeY(element);
						pointer.status = TouchPointer.TOUCH_MOVE;
					}
					listener.onTouch(pointers);
					event.preventDefault();
				}
				if ((event.getTypeInt() & Event.ONTOUCHEND) != 0) {
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
				if ((event.getTypeInt() & Event.ONTOUCHCANCEL) != 0) {
					mousePointer.status = TouchPointer.TOUCH_UP;
					listener.onTouch(pointers);
					event.preventDefault();
				}
			}
		});
		Event.sinkEvents(element, Event.ONMOUSEDOWN | Event.ONMOUSEUP | Event.ONMOUSEMOVE | Event.ONMOUSEOUT | //
				Event.ONTOUCHSTART | Event.ONTOUCHMOVE | Event.ONTOUCHEND | Event.ONTOUCHCANCEL);

		mousePointer = new TouchPointer();
	}

	public void setListener(TouchListener listener) {
		this.listener = listener;
	}
}