package jmini3d.input;


public interface KeyListener {

	int KEY_UP = 1;
	int KEY_DOWN = 2;
	int KEY_LEFT = 3;
	int KEY_RIGHT = 4;
	int KEY_CENTER = 5;
	int KEY_ZOOM_IN = 6;
	int KEY_ZOOM_OUT = 7;
	int KEY_BACKSPACE = 8;
	int KEY_BACK = 9;
	int KEY_ENTER = 13;
	int KEY_PAGEUP = 33;
	int KEY_PAGEDOWN = 34;

	boolean onKeyDown(int key);

	boolean onKeyUp(int key);

}