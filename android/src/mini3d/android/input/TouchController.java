package mini3d.android.input;

import java.util.HashMap;

import mini3d.android.compat.CompatibilityWrapper5;
import mini3d.input.TouchListener;
import mini3d.input.TouchPointer;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class TouchController implements OnTouchListener {

	TouchListener listener;

	HashMap<Integer, TouchPointer> pointers = new HashMap<Integer, TouchPointer>();
	HashMap<Integer, TouchPointer> pointersAux = new HashMap<Integer, TouchPointer>();

	View view;

	public TouchController(View view) {
		this.view = view;

		view.setOnTouchListener(this);
	}

	public void setListener(TouchListener listener) {
		this.listener = listener;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
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

			new TouchPointer();
			if (Build.VERSION.SDK_INT >= 5) {
				touchPointer.x = (int) CompatibilityWrapper5.getX(event, pointerIndex);
				touchPointer.y = (int) CompatibilityWrapper5.getY(event, pointerIndex);
			} else {
				touchPointer.x = (int) event.getX();
				touchPointer.y = (int) event.getY();
			}
			touchPointer.status = TouchPointer.TOUCH_DOWN;
			pointers.put(Integer.valueOf(pointerId), touchPointer);
			if (listener != null) {
				listener.onTouch(pointers);
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
			if (listener != null) {
				listener.onTouch(pointers);
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			TouchPointer upPointer = pointers.get(Integer.valueOf(pointerId));
			upPointer.status = TouchPointer.TOUCH_UP;
			if (listener != null) {
				listener.onTouch(pointers);
			}
			pointers.remove(Integer.valueOf(pointerId));
			pointersAux.put(Integer.valueOf(pointerId), upPointer);
			break;
		case MotionEvent.ACTION_OUTSIDE:
			break;
		}

		try {
			// Avoid event flood
			Thread.sleep(16);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}
}