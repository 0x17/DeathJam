package com.andreschnabel.deathjam;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Enemy {

	private static final float ENEMY_RADIUS = 64.0f;

	public Vector2 center = new Vector2();
	public Vector2 pos = new Vector2();

	private static float rotAlpha;

	private static int w, h;

	static {
		TextureAtlas.AtlasRegion enemyRegion = Globals.atlas.findRegion("enemy");
		w = enemyRegion.getRegionWidth();
		h = enemyRegion.getRegionHeight();
	}

	public Enemy(float cx, float cy) {
		center.x = cx;
		center.y = cy;
	}

	public static void updateAlpha() {
		rotAlpha += 0.05f; //delta * 0.01f;
	}

	public void updatePos() {
		pos.x = (float) (center.x + Math.cos(rotAlpha) * ENEMY_RADIUS);
		pos.y = (float) (center.y + Math.sin(rotAlpha) * ENEMY_RADIUS);
	}

	public Rectangle getRect() {
		return new Rectangle(pos.x, pos.y, w, h);
	}
}
