package jmini3d.gwt.input;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

import java.util.HashMap;

import jmini3d.input.KeyListener;
import jmini3d.input.TouchListener;
import jmini3d.input.TouchPointer;

public class InputController implements EventListener {

	static final Integer MOUSE_POINTER_ID = 666;

	TouchListener touchListener;
	KeyListener keyListener;

	TouchPointer mousePointer;

	HashMap<Integer, TouchPointer> pointers = new HashMap<Integer, TouchPointer>();
	HashMap<Integer, TouchPointer> pointersAux = new HashMap<Integer, TouchPointer>();

	Element element;
	int eventBits;

	public float scale = 1;

	public InputController(final Element element) {
		this.element = element;
		Event.setEventListener(element, this);

		mousePointer = new TouchPointer();
		eventBits = 0;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public void setTouchListener(TouchListener touchListener) {
		int mask = Event.ONMOUSEDOWN | Event.ONMOUSEUP | Event.ONMOUSEMOVE | Event.ONMOUSEOUT | //
				Event.ONTOUCHSTART | Event.ONTOUCHMOVE | Event.ONTOUCHEND | Event.ONTOUCHCANCEL;

		if (touchListener != null) {
			eventBits |= mask;
		} else {
			eventBits &= ~mask;
		}
		Event.sinkEvents(element, eventBits);

		this.touchListener = touchListener;
	}

	public void setKeyListener(KeyListener keyListener) {
		int mask = Event.ONKEYDOWN | Event.ONKEYUP | Event.ONMOUSEWHEEL;

		if (keyListener != null) {
			eventBits |= mask;
		} else {
			eventBits &= ~mask;
		}
		Event.sinkEvents(element, eventBits);

		this.keyListener = keyListener;
	}

	@Override
	public void onBrowserEvent(Event event) {
		if ((event.getTypeInt() & Event.ONMOUSEDOWN) != 0) {
			mousePointer.x = (int) (scale * (event.getClientX() - element.getAbsoluteLeft()));
			mousePointer.y = (int) (scale * (event.getClientY() - element.getAbsoluteTop()));
			mousePointer.status = TouchPointer.TOUCH_DOWN;
			pointers.put(MOUSE_POINTER_ID, mousePointer);
			touchListener.onTouch(pointers);
			event.preventDefault();
		}
		if ((event.getTypeInt() & Event.ONMOUSEDOWN) != 0) {
			mousePointer.x = (int) (scale * (event.getClientX() - element.getAbsoluteLeft()));
			mousePointer.y = (int) (scale * (event.getClientY() - element.getAbsoluteTop()));
			mousePointer.status = TouchPointer.TOUCH_MOVE;

			if (pointers.containsKey(MOUSE_POINTER_ID)) {
				touchListener.onTouch(pointers);
			}
			event.preventDefault();
		}
		if ((event.getTypeInt() & Event.ONMOUSEMOVE) != 0) {
			mousePointer.x = (int) (scale * (event.getClientX() - element.getAbsoluteLeft()));
			mousePointer.y = (int) (scale * (event.getClientY() - element.getAbsoluteTop()));
			mousePointer.status = TouchPointer.TOUCH_MOVE;

			if (pointers.containsKey(MOUSE_POINTER_ID)) {
				touchListener.onTouch(pointers);
			}
			event.preventDefault();
		}
		if ((event.getTypeInt() & Event.ONMOUSEUP) != 0) {
			mousePointer.x = (int) (scale * (event.getClientX() - element.getAbsoluteLeft()));
			mousePointer.y = (int) (scale * (event.getClientY() - element.getAbsoluteTop()));
			mousePointer.status = TouchPointer.TOUCH_UP;

			if (pointers.containsKey(MOUSE_POINTER_ID)) {
				touchListener.onTouch(pointers);
				pointers.remove(MOUSE_POINTER_ID);
			}
			event.preventDefault();
		}
		if ((event.getTypeInt() & Event.ONMOUSEOUT) != 0) {
			mousePointer.x = (int) (scale * (event.getClientX() - element.getAbsoluteLeft()));
			mousePointer.y = (int) (scale * (event.getClientY() - element.getAbsoluteTop()));
			mousePointer.status = TouchPointer.TOUCH_UP;

			if (pointers.containsKey(MOUSE_POINTER_ID)) {
				touchListener.onTouch(pointers);
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
				pointer.x = (int) (scale * (touch.getClientX() - element.getAbsoluteLeft()));
				pointer.y = (int) (scale * (touch.getClientY() - element.getAbsoluteTop()));
				pointer.status = TouchPointer.TOUCH_DOWN;
				pointers.put(touch.getIdentifier(), pointer);
			}
			touchListener.onTouch(pointers);

			for (TouchPointer pointer : pointers.values()) {
				pointer.status = TouchPointer.TOUCH_MOVE;
			}
			event.preventDefault();
		}
		if ((event.getTypeInt() & Event.ONTOUCHMOVE) != 0) {
			for (int i = 0; i < event.getChangedTouches().length(); i++) {
				Touch touch = event.getChangedTouches().get(i);
				TouchPointer pointer = pointers.get(touch.getIdentifier());
				pointer.x = (int) (scale * (touch.getClientX() - element.getAbsoluteLeft()));
				pointer.y = (int) (scale * (touch.getClientY() - element.getAbsoluteTop()));
				pointer.status = TouchPointer.TOUCH_MOVE;
			}
			touchListener.onTouch(pointers);
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
			touchListener.onTouch(pointers);

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
			touchListener.onTouch(pointers);
			event.preventDefault();
		}
		if ((event.getTypeInt() & Event.ONKEYDOWN) != 0 || (event.getTypeInt() & Event.ONKEYUP) != 0) {
			int key = 0;
			switch (event.getKeyCode()) {
				case KeyCodes.KEY_UP:
					key = KeyListener.KEY_UP;
					break;
				case KeyCodes.KEY_DOWN:
					key = KeyListener.KEY_DOWN;
					break;
				case KeyCodes.KEY_LEFT:
					key = KeyListener.KEY_LEFT;
					break;
				case KeyCodes.KEY_RIGHT:
					key = KeyListener.KEY_RIGHT;
					break;
				case KeyCodes.KEY_ENTER:
					key = KeyListener.KEY_ENTER;
					break;
				case KeyCodes.KEY_ESCAPE:
					key = KeyListener.KEY_BACK;
					break;
				case KeyCodes.KEY_BACKSPACE:
					key = KeyListener.KEY_BACKSPACE;
					break;
				case KeyCodes.KEY_PAGEUP:
					key = KeyListener.KEY_PAGEUP;
					break;
				case KeyCodes.KEY_PAGEDOWN:
					key = KeyListener.KEY_PAGEDOWN;
					break;
			}
			if (key != 0) {
				boolean managed = false;
				if ((event.getTypeInt() & Event.ONKEYDOWN) != 0) {
					managed = keyListener.onKeyDown(key);
				}
				if ((event.getTypeInt() & Event.ONKEYUP) != 0) {
					managed = keyListener.onKeyUp(key);
				}
				if (managed) {
					event.preventDefault();
				}
			}
		}
		if ((event.getTypeInt() & Event.ONMOUSEWHEEL) != 0) {
			boolean managed = false;
			if (event.getMouseWheelVelocityY() > 0) {
				managed |= keyListener.onKeyDown(KeyListener.KEY_ZOOM_OUT);
				managed |= keyListener.onKeyUp(KeyListener.KEY_ZOOM_OUT);
			} else {
				managed |= keyListener.onKeyDown(KeyListener.KEY_ZOOM_IN);
				managed |= keyListener.onKeyUp(KeyListener.KEY_ZOOM_IN);
			}
			if (managed) {
				event.preventDefault();
			}
		}
	}

}