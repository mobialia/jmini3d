package jmini3d.input;


public interface KeyListener {

	final static int KEY_UP = 1;
	final static int KEY_DOWN = 2;
	final static int KEY_LEFT = 3;
	final static int KEY_RIGHT = 4;
	final static int KEY_CENTER = 5;
	final static int KEY_ZOOM_IN = 6;
	final static int KEY_ZOOM_OUT = 7;
	final static int KEY_BACKSPACE = 8;
	final static int KEY_BACK = 9;
	final static int KEY_ENTER = 13;

	public boolean onKeyDown(int key);

	public boolean onKeyUp(int key);

}