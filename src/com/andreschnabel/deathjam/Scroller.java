package com.andreschnabel.deathjam;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;

public class Scroller {

	private static final float SCROLL_SPEED = 5.0f;
	private final float SCROLL_WINDOW_HORIZONTAL = Globals.VSCR_W / 2.1f;
	private final float SCROLL_WINDOW_VERTICAL = Globals.VSCR_H / 2.1f;
	private final World world;
	private final Player player;

	public float xOffset, yOffset;

	public OrthographicCamera cam;

	public Scroller(World world, Player player) {
		cam = new OrthographicCamera(Globals.VSCR_W, Globals.VSCR_H);
		this.world = world;
		this.player = player;
	}

	public void updateCamera() {
		float maxInertia = player.getMaxInertia();
		float zoomFactor = 2.0f - 0.25f * (maxInertia / 20.0f);

		cam.position.set(xOffset* zoomFactor, yOffset* zoomFactor, 0.0f);
		cam.update();
		cam.view.scale(zoomFactor, zoomFactor, 1.0f);
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

		if(Globals.VSCR_W - playerScrX <= SCROLL_WINDOW_HORIZONTAL) {
			float scrollSpeed = determineScrollSpeed(playerScrX + playerRect.width < Globals.VSCR_W);
			scroll(scrollSpeed, 0);
		}

		if(Globals.VSCR_H - playerScrY <= SCROLL_WINDOW_VERTICAL) {
			float scrollSpeed = determineScrollSpeed(playerScrY + playerRect.height < Globals.VSCR_H);
			scroll(0, scrollSpeed);
		}

		if(playerScrY <= SCROLL_WINDOW_VERTICAL) {
			float scrollSpeed = determineScrollSpeed(playerScrY >= 0);
			scroll(0, -scrollSpeed);
		}
	}

}
