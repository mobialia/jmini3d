package jmini3d.input;

import java.util.HashMap;

public interface TouchListener {

	boolean onTouch(HashMap<Integer, TouchPointer> pointers);

}