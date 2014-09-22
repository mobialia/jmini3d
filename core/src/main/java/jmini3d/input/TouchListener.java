package jmini3d.input;

import java.util.HashMap;

public interface TouchListener {

	public boolean onTouch(HashMap<Integer, TouchPointer> pointers);

}