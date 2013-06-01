package jmini3d.android.compat;

import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Functions since API level 5
 *
 * @author rui
 */
public class CompatibilityWrapper5 {

	public static int getPointerId(MotionEvent ev, int pointerIndex) {
		return ev.getPointerId(pointerIndex);
	}

	public static int getPointerCount(MotionEvent ev) {
		return ev.getPointerCount();
	}

	public static float getX(MotionEvent ev, int pointerIndex) {
		return ev.getX(pointerIndex);
	}

	public static float getY(MotionEvent ev, int pointerIndex) {
		return ev.getY(pointerIndex);
	}

	public static void setZOrderOnTop(GLSurfaceView view) {
		view.setZOrderOnTop(true);
	}
}