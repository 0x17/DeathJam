package com.andreschnabel.deathjam;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;

public class Scroller {

	private static final float SCROLL_SPEED = 5.0f;
	private final float SCROLL_WINDOW_HORIZONTAL = Globals.SCR_W / 2.1f;
	private final float SCROLL_WINDOW_VERTICAL = Globals.SCR_H / 2.1f;
	private final World world;
	private final Player player;

	public float xOffset, yOffset;

	public OrthographicCamera cam;

	public Scroller(World world, Player player) {
		cam = new OrthographicCamera(Globals.SCR_W, Globals.SCR_H);
		this.world = world;
		this.player = player;
	}

	public void updateCamera() {
		cam.position.set(xOffset, yOffset, 0.0f);
		cam.update();
	}

	public void scroll(float dx, float dy) {
		xOffset += dx;
		yOffset += dy;
	}

	private float determineScrollSpeed(boolean inFrame) {
		return inFrame ? SCROLL_SPEED : SCROLL_SPEED * 2;
	}

	public void updateScrolling() {
		Rectangle playerRect = player.getRect();
		float playerScrX = playerRect.x - xOffset;
		float playerScrY = playerRect.y - yOffset;

		if(playerScrX <= SCROLL_WINDOW_HORIZONTAL) {
			float scrollSpeed = determineScrollSpeed(playerScrX >= 0);
			scroll(-scrollSpeed, 0);
		}

		if(Globals.SCR_W - playerScrX <= SCROLL_WINDOW_HORIZONTAL) {
			float scrollSpeed = determineScrollSpeed(playerScrX + playerRect.width < Globals.SCR_W);
			scroll(scrollSpeed, 0);
		}

		if(Globals.SCR_H - playerScrY <= SCROLL_WINDOW_VERTICAL) {
			float scrollSpeed = determineScrollSpeed(playerScrY + playerRect.height < Globals.SCR_H);
			scroll(0, scrollSpeed);
		}

		if(playerScrY <= SCROLL_WINDOW_VERTICAL) {
			float scrollSpeed = determineScrollSpeed(playerScrY >= 0);
			scroll(0, -scrollSpeed);
		}
	}

}
