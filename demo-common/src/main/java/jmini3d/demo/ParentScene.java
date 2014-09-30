package jmini3d.demo;

import jmini3d.Object3d;
import jmini3d.Rect;
import jmini3d.Scene;
import jmini3d.Texture;
import jmini3d.geometry.SpriteGeometry;
import jmini3d.material.SpriteMaterial;

public class ParentScene extends Scene {

	ArialFont font = new ArialFont();

	String title;

	Object3d titleObject3d;
	Rect fm = new Rect();

	Object3d buttonRight;
	Object3d buttonLeft;

	public ParentScene(String title) {
		this.title = title;

		titleObject3d = font.getTextLine(title, fm);
		addHudElement(titleObject3d);

		SpriteMaterial buttonRightMaterial = new SpriteMaterial(new Texture("arrow_right.png"));
		SpriteGeometry buttonRightGeometry = new SpriteGeometry(1);
		buttonRightGeometry.addSprite(0, 0, 0, 0);
		buttonRight = new Object3d(buttonRightGeometry, buttonRightMaterial);
		addHudElement(buttonRight);

		SpriteMaterial buttonLeftMaterial = new SpriteMaterial(new Texture("arrow_left.png"));
		SpriteGeometry buttonLeftGeometry = new SpriteGeometry(1);
		buttonLeftGeometry.addSprite(0, 0, 0, 0);
		buttonLeft = new Object3d(buttonLeftGeometry, buttonLeftMaterial);
		addHudElement(buttonLeft);
	}

	/**
	 * Hud elements need viewport size, this method is also called each time that the viewport is changed
	 */
	@Override
	public void onViewPortChanged(int width, int height) {
		// DO not exceed screen width
		float titleScale = ((float) width) / ((float) fm.right);
		if (titleScale > 1.5f) {
			titleScale = 1.5f;
		}
		titleObject3d.setScale(titleScale);
		// center in screen
		titleObject3d.setPosition((width - (((float) fm.right) * titleScale)) / 2, fm.bottom * titleScale * 0.5f, 0);

		float buttonWidth = Math.min(width, height) / 5;
		((SpriteGeometry) buttonLeft.getGeometry3d()).setSpritePosition(0, 0, height - buttonWidth, buttonWidth, height);
		((SpriteGeometry) buttonRight.getGeometry3d()).setSpritePosition(0, width - buttonWidth, height - buttonWidth, width, height);
	}
}
