package jmini3d;

import java.util.ArrayList;
import java.util.HashMap;

import jmini3d.geometry.SpriteGeometry;
import jmini3d.material.SpriteMaterial;

/**
 * http://www.angelcode.com/products/bmfont/doc/file_format.html
 */
public class Font {

	public class CharSprite {
		int x;
		int y;
		int width;
		int height;
		int xoffset;
		int yoffset;
		int xadvance;
		int page;
		int chnl;
	}

	public class Kerning {
		char first;
		char second;
		int amount;
	}

	int lineHeight, base, scaleW, scaleH, pages, packed, alphaChnl, redChnl, greenChnl, blueChnl;
	ArrayList<Texture> textures = new ArrayList<Texture>();

	HashMap<Character, CharSprite> chars = new HashMap<Character, CharSprite>();
	ArrayList<Kerning> kernings = new ArrayList<Kerning>();

	public Font() {

	}

	public void setCommon(int lineHeight, int base, int scaleW, int scaleH, int pages, int packed, int alphaChnl, int redChnl, int greenChnl, int blueChnl) {
		this.lineHeight = lineHeight;
		this.base = base;
		this.scaleW = scaleW;
		this.scaleH = scaleH;
		this.pages = pages;
		this.packed = packed;
		this.alphaChnl = alphaChnl;
		this.redChnl = redChnl;
		this.greenChnl = greenChnl;
		this.blueChnl = blueChnl;
	}

	public void addPage(int id, String file) {
		Texture texture = new Texture(file);
		textures.add(texture);
	}

	public void addChar(int id, int x, int y, int width, int height, int xoffset, int yoffset, int xadvance, int page, int chnl) {
		CharSprite cs = new CharSprite();
		char theChar = Character.toChars(id)[0];
		cs.x = x;
		cs.y = y;
		cs.width = width;
		cs.height = height;
		cs.xoffset = xoffset;
		cs.yoffset = yoffset;
		cs.xadvance = xadvance;
		cs.page = page;
		cs.chnl = chnl;
		chars.put(theChar, cs);
	}

	public void addKerning(int first, int second, int amount) {
		Kerning kerning = new Kerning();
		kerning.first = Character.toChars(Integer.valueOf(first))[0];
		kerning.second = Character.toChars(Integer.valueOf(second))[0];
		kerning.amount = Integer.valueOf(amount);
		kernings.add(kerning);
	}

	public Object3d getTextLine(String text) {
		return getTextLine(text, null);
	}

	/**
	 * Returns a new Object3D with a text line
	 *
	 * @param text
	 * @param fontMetrics This object is going to be modified with the text line size
	 * @return
	 */
	public Object3d getTextLine(String text, Rect fontMetrics) {
		int length = text.length();
		SpriteGeometry geometry = new SpriteGeometry(text.length());

		int x = 0;
		int y = 0;
		char oldChar = (char) -1;
		for (int i = 0; i < length; i++) {
			char c = text.charAt(i);
			CharSprite cs = chars.get(c);
			if (cs == null) {
				System.out.print("Character " + c + " not found in bitmap texture");
				continue;
			}
			for (Kerning k : kernings) {
				if (k.first == oldChar && k.second == c) {
					x += k.amount;
				}
			}

			geometry.addSprite(x + cs.xoffset, y + cs.yoffset, x + cs.xoffset + cs.width, y + cs.yoffset + cs.height, //
					Utils.p(cs.x + 1, scaleW), Utils.p(cs.y + 1, scaleH), Utils.p(cs.x + cs.width + 1, scaleW), Utils.p(cs.y + cs.height + 1, scaleH));
			x += cs.xadvance;
			oldChar = c;
		}
		if (fontMetrics != null) {
			fontMetrics.right = x;
			fontMetrics.bottom = lineHeight;
		}
		return new Object3d(geometry, new SpriteMaterial(textures.get(0)));
	}

	/**
	 * Modifies an Object3D with a SpriteGeometry to put a text
	 *
	 * @param object3d
	 * @param text
	 * @param fontMetrics
	 */
	public void setTextLine(Object3d object3d, String text, Rect fontMetrics) {
		int length = text.length();
		SpriteGeometry geometry = (SpriteGeometry) object3d.geometry3d;

		int i;
		int x = 0;
		int y = 0;
		char oldChar = (char) -1;
		for (i = 0; i < length; i++) {
			char c = text.charAt(i);
			CharSprite cs = chars.get(c);
			if (cs == null) {
				System.out.print("Character " + c + " not found in bitmap texture");
				continue;
			}
			for (Kerning k : kernings) {
				if (k.first == oldChar && k.second == c) {
					x += k.amount;
				}
			}

			geometry.setSprite(i, x + cs.xoffset, y + cs.yoffset, x + cs.xoffset + cs.width, y + cs.yoffset + cs.height, //
					Utils.p(cs.x + 1, scaleW), Utils.p(cs.y + 1, scaleH), Utils.p(cs.x + cs.width + 1, scaleW), Utils.p(cs.y + cs.height + 1, scaleH));
			x += cs.xadvance;
			oldChar = c;
		}
		if (fontMetrics != null) {
			fontMetrics.right = x;
			fontMetrics.bottom = lineHeight;
		}

		while (i < geometry.spriteCount) {
			geometry.hideSprite(i);
			i++;
		}
	}

	public void clear() {
		textures.clear();
		chars.clear();
		kernings.clear();
	}
}
