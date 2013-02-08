package com.andreschnabel.deathjam;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Globals {
	
	public final static int SCR_W = 640;
	public final static int SCR_H = 480;

	public static TextureAtlas atlas;

	static {
		atlas = new TextureAtlas(Utils.assetHandle("atlas.pack"));
	}

}
