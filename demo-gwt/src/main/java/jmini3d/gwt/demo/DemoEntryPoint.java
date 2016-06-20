package jmini3d.gwt.demo;

import jmini3d.demo.DemoScreenController;
import jmini3d.gwt.EntryPoint3d;
import jmini3d.gwt.input.InputController;

public class DemoEntryPoint extends EntryPoint3d {

	InputController inputController;

	@Override
	public void onModuleLoad() {
		resourceDir = "./resources/";
		super.onModuleLoad();
		DemoScreenController screenController = new DemoScreenController();
		canvas3d.setScreenController(screenController);
		canvas3d.setLogFps(true);
		inputController = new InputController(canvas3d.getElement());
		inputController.setTouchListener(screenController);
		inputController.setKeyListener(screenController);
		inputController.scale = canvas3d.getScale();
		canvas3d.getElement().focus();
	}

}