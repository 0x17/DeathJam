package com.andreschnabel.deathjam;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class CollectAnim {
	private long creatTime;
	private final TextureRegion region;
	private final static long ANIM_DURATION = 1000;
	private final Vector2 centerPos;

	public CollectAnim(Vector2 centerPos, TextureRegion textureRegion) {
		creatTime = Utils.getTicks();
		this.region = textureRegion;
		this.centerPos = centerPos;
	}

	public boolean render(SpriteBatch sb) {
		long curT = System.currentTimeMillis();
		long deltaT = (curT  - creatTime);

		float fraction = (deltaT / (float)ANIM_DURATION);

		float scaleX = ( 1 + fraction * 1.5f );
		float scaleY = ( 1 + fraction * 1.5f );
		float w = region.getRegionWidth() * scaleX;
		float h = region.getRegionHeight() * scaleY;
		float x = centerPos.x - w / 2.0f;
		float y = centerPos.y - h / 2.0f;

		//sb.draw(region, x, y, w, h);

		// This is not what I intended but it looks nice anyways.
		sb.draw(region, x, y, centerPos.x, centerPos.y, w, h, scaleX, scaleY, 0.0f);

		return deltaT > ANIM_DURATION;
	}
}
