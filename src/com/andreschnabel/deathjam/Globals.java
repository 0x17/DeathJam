package com.andreschnabel.deathjam;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Globals {
	
	public final static int VSCR_W = 640;
	public final static int VSCR_H = 360;
	public final static int PSCR_W = 1280;
	public final static int PSCR_H = 720;

	public final static boolean NO_SOUND = true;

	public static TextureAtlas atlas;

	static {
		atlas = new TextureAtlas(Utils.assetHandle("atlas.pack"));
	}

}
