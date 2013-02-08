package com.andreschnabel.deathjam;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class Player {

	private static final float MOV_SPEED = 5.0f;
	private static final float MAX_MOV_SPEED = 20.0f;

	public Vector2 inertia = Vector2.Zero.cpy();

	private Sprite playerSpr;
	private List<TextureRegion> playerRegions = new ArrayList<TextureRegion>();
	private final World world;

	public boolean alive = true;
	public int score;
	public boolean gameover;

	public Player(World world) {
		this.world = world;

		ArrayList<TextureAtlas.AtlasRegion> regions = new ArrayList<TextureAtlas.AtlasRegion>();
		for(TextureAtlas.AtlasRegion region : Globals.atlas.getRegions()) {
			if(region.name.matches("player\\d+"))
				playerRegions.add(region);
		}
		playerSpr = new Sprite(playerRegions.get(0));
		playerSpr.setPosition(world.playerStart.x, world.playerStart.y);
	}

	public void move(float dx, float dy) {
		dx *= MOV_SPEED;
		dy *= MOV_SPEED;

		inertia.x += dx;
		inertia.y += dy;

		if(inertia.x >= MAX_MOV_SPEED) inertia.x = MAX_MOV_SPEED;
		if(inertia.y >= MAX_MOV_SPEED) inertia.y = MAX_MOV_SPEED;
	}

	private final float RECT_MINIFICATION_FACTOR = 0.9f;

	private void minificateRect(Rectangle rect) {
		rect.x += (1 - RECT_MINIFICATION_FACTOR) * rect.width;
		rect.y += (1 - RECT_MINIFICATION_FACTOR) * rect.height;
		rect.width *= RECT_MINIFICATION_FACTOR;
		rect.height *= RECT_MINIFICATION_FACTOR;
	}

	public void updatePlayerPos() {
		playerSpr.setX(playerSpr.getX()+inertia.x);
		playerSpr.setY(playerSpr.getY()+inertia.y);

		Rectangle playerRect = playerSpr.getBoundingRectangle();
		minificateRect(playerRect);

		if(world.inTile(playerRect)) {
			playerSpr.setX(playerSpr.getX()-inertia.x);
			playerSpr.setY(playerSpr.getY()-inertia.y);
			inertia.x *= -1.0f;
			inertia.y *= -1.0f;

			if(world.inTileOfType(playerRect, 'X')) {
				kill();
				return;
			}

			if(world.inTileOfType(playerRect, 'Y')) {
				revive();
				return;
			}
		}

		List<Enemy> enemies = world.getEnemies();
		playerRect = playerSpr.getBoundingRectangle();
		minificateRect(playerRect);

		for(Enemy enemy : enemies) {
			if(Intersector.overlapRectangles(playerRect, enemy.getRect())) {
				kill();
				return;
			}
		}
	}

	public void kill() {
		if(alive) {
			alive = false;
			world.loadFromFile("deathworld1.txt", true);
			playerSpr.setPosition(world.playerStart.x, world.playerStart.y);
			inertia.set(0.0f, 0.0f);
		} else {
			gameover = true;
		}
	}

	public void revive() {
		alive = true;
		world.loadFromFile("world1.txt", true);
		playerSpr.setPosition(world.playerStart.x, world.playerStart.y);
		inertia.set(0.0f, 0.0f);
	}

	public float getMaxInertia() {
		return Math.max(Math.abs(inertia.x), Math.abs(inertia.y));
	}

	public void updateInertia() {
		//float delta = Gdx.graphics.getDeltaTime();
		inertia.x *= 0.85f;
		inertia.y *= 0.85f;

		playerSpr.setRotation((float) (MathUtils.radiansToDegrees * Math.atan2(inertia.y, inertia.x)));

		float maxInertia = getMaxInertia();
		int regionIndex;
		if(maxInertia <= 0.01f) {
			regionIndex = 0;
		} else if(maxInertia <= 5.0f) {
			regionIndex = 1;
		} else if(maxInertia <= 15.0f) {
			regionIndex = 2;
		} else {
			regionIndex = 3;
		}
		playerSpr.setRegion(playerRegions.get(regionIndex));

		score += world.tryCollectCoin(playerSpr.getBoundingRectangle());
	}

	public Rectangle getRect() {
		return playerSpr.getBoundingRectangle();
	}

	public void draw(SpriteBatch sb) {
		playerSpr.draw(sb);
	}
}
