package jmini3d.gwt.demo;

import jmini3d.demo.DemoSceneController;
import jmini3d.gwt.EntryPoint3d;
import jmini3d.gwt.input.InputController;

public class DemoEntryPoint extends EntryPoint3d {

	InputController inputController;

	@Override
	public void onModuleLoad() {
		resourceDir = "./resources/";
		super.onModuleLoad();
		DemoSceneController sceneController = new DemoSceneController();
		canvas3d.setSceneController(sceneController);
		canvas3d.getRenderer3d().setLogFps(true);
		inputController = new InputController(canvas3d.getElement());
		inputController.setTouchListener(sceneController);
		inputController.setKeyListener(sceneController);
		inputController.scale = canvas3d.getScale();
		canvas3d.getElement().focus();
	}

}