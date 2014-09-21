package jmini3d.input;

public class TouchPointer {

	public final static int TOUCH_DOWN = 0;
	public final static int TOUCH_MOVE = 1;
	public final static int TOUCH_UP = 2;

	public int x;
	public int y;

	public int status;

	@Override
	public String toString() {
		return "TouchPointer [x=" + x + ", y=" + y + ", status=" + status + "]";
	}
}