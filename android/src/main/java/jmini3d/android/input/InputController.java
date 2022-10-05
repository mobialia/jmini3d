package jmini3d.android.input;

import android.os.Build;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import java.util.HashMap;

import jmini3d.android.compat.CompatibilityWrapper5;
import jmini3d.input.KeyListener;
import jmini3d.input.TouchListener;
import jmini3d.input.TouchPointer;

public class InputController implements OnTouchListener, View.OnKeyListener {

	TouchListener touchListener;
	KeyListener keyListener;

	HashMap<Integer, TouchPointer> pointers = new HashMap<Integer, TouchPointer>();
	HashMap<Integer, TouchPointer> pointersAux = new HashMap<Integer, TouchPointer>();

	long onTouchSleepPeriod = 16;

	View view;

	public InputController(View view) {
		this.view = view;
	}

	public void setOnTouchSleepPeriod(long onTouchSleepPeriod) {
		this.onTouchSleepPeriod = onTouchSleepPeriod;
	}

	public void setTouchListener(TouchListener touchListener) {
		this.touchListener = touchListener;
		view.setOnTouchListener(this);
	}

	public void setKeyListener(KeyListener keyListener) {
		this.keyListener = keyListener;
		view.setOnKeyListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (touchListener == null) {
			return false;
		}
		// = event.getActionMasked();
		int action = event.getAction();
		// = event.getActionIndex();
		int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		int pointerId = 0;
		if (Build.VERSION.SDK_INT >= 5) {
			pointerId = CompatibilityWrapper5.getPointerId(event, pointerIndex);
		}

		switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				TouchPointer touchPointer = pointersAux.get(Integer.valueOf(pointerId));
				if (touchPointer == null) {
					touchPointer = new TouchPointer();
				}

				if (Build.VERSION.SDK_INT >= 5) {
					touchPointer.x = (int) CompatibilityWrapper5.getX(event, pointerIndex);
					touchPointer.y = (int) CompatibilityWrapper5.getY(event, pointerIndex);
				} else {
					touchPointer.x = (int) event.getX();
					touchPointer.y = (int) event.getY();
				}
				touchPointer.status = TouchPointer.TOUCH_DOWN;
				pointers.put(pointerId, touchPointer);
				if (touchListener != null) {
					touchListener.onTouch(pointers);
				}
				touchPointer.status = TouchPointer.TOUCH_MOVE;
				break;
			case MotionEvent.ACTION_MOVE:
				for (int i = 0; i < event.getPointerCount(); i++) {
					if (Build.VERSION.SDK_INT >= 5) {
						int curPointerId = CompatibilityWrapper5.getPointerId(event, i);
						if (pointers.containsKey(Integer.valueOf(curPointerId))) {
							TouchPointer movePointer = pointers.get(Integer.valueOf(curPointerId));
							movePointer.x = (int) CompatibilityWrapper5.getX(event, i);
							movePointer.y = (int) CompatibilityWrapper5.getY(event, i);
							movePointer.status = TouchPointer.TOUCH_MOVE;
						}
					} else {
						TouchPointer movePointer = pointers.get(Integer.valueOf(0));
						movePointer.x = (int) event.getX();
						movePointer.y = (int) event.getY();
						movePointer.status = TouchPointer.TOUCH_MOVE;
					}
				}
				if (touchListener != null) {
					touchListener.onTouch(pointers);
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				TouchPointer upPointer = pointers.get(Integer.valueOf(pointerId));
				upPointer.status = TouchPointer.TOUCH_UP;
				if (touchListener != null) {
					touchListener.onTouch(pointers);
				}
				pointers.remove(Integer.valueOf(pointerId));
				pointersAux.put(pointerId, upPointer);
				break;
			case MotionEvent.ACTION_OUTSIDE:
				break;
		}

		if (onTouchSleepPeriod > 0) {
			try {
				// Avoid event flood
				Thread.sleep(onTouchSleepPeriod);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (keyListener == null) {
			return false;
		}

		int key;

		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_UP:
				key = KeyListener.KEY_UP;
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				key = KeyListener.KEY_DOWN;
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				key = KeyListener.KEY_LEFT;
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				key = KeyListener.KEY_RIGHT;
				break;
			case KeyEvent.KEYCODE_DPAD_CENTER:
				key = KeyListener.KEY_CENTER;
				break;
			case KeyEvent.KEYCODE_BACK:
				key = KeyListener.KEY_BACK;
				break;
			case KeyEvent.KEYCODE_ENTER:
				key = KeyListener.KEY_ENTER;
				break;
			default:
				return false;
		}

		boolean managed = false;
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			managed = keyListener.onKeyDown(key);
		}

		if (event.getAction() == KeyEvent.ACTION_UP) {
			managed = keyListener.onKeyUp(key);
		}
		return managed;
	}

}