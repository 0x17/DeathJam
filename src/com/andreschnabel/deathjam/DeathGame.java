package com.andreschnabel.deathjam;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class DeathGame implements ApplicationListener {
	
	//region Rendering
	private SpriteBatch sb;
	private BitmapFont smallFont, bigFont;
	//endregion
	
	//region Textures

	//private AtlasRegion playerRegion;
	//endregion
	
	private static final float MOV_SPEED = 5.0f;
	private static final float MAX_MOV_SPEED = 20.0f;
	private static final float SCROLL_SPEED = 5.0f;
	private Vector2 inertia = Vector2.Zero.cpy();
	private boolean alive = true;
	private int score = 0;
	
	private World world;

	private final float SCROLL_WINDOW = 100.0f;
	private Sprite playerSpr;

	@Override
	public void create() {

		sb = new SpriteBatch();

		playerSpr = new Sprite(Globals.atlas.findRegion("player"));

		initFonts();

		float lum = 0.3f;
		Gdx.gl.glClearColor(lum, lum, lum, 1.0f);

		world = new World();
		playerSpr.setPosition(world.playerStart.x, world.playerStart.y);
	}

	private void initFonts() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Utils.assetHandle("Gentium.ttf"));
		bigFont = generator.generateFont(30);
		smallFont = generator.generateFont(15);
		generator.dispose();
	}

	@Override
	public void dispose() {
		smallFont.dispose();
		bigFont.dispose();
		sb.dispose();
		Globals.atlas.dispose();
	}

	@Override
	public void render() {
		processInput();
		update();
		renderScene();
	}

	private void update() {
		updatePlayerPos();
		updateScrolling();
		updateInertia();
		score += world.tryCollectCoin(calcPlayerRect());
	}

	private void updateInertia() {
		//float delta = Gdx.graphics.getDeltaTime();
		inertia.x *= 0.85f;
		inertia.y *= 0.85f;

		playerSpr.setRotation(MathUtils.radiansToDegrees * MathUtils.atan2(inertia.y, inertia.x));
	}

	private void updatePlayerPos() {
		playerSpr.setX(playerSpr.getX()+inertia.x);
		playerSpr.setY(playerSpr.getY()+inertia.y);

		Rectangle playerRect = calcPlayerRect();
		if(playerInTile(playerRect)) {
			playerSpr.setX(playerSpr.getX()-inertia.x);
			playerSpr.setY(playerSpr.getY()-inertia.y);
			inertia.x *= -1.5f;
			inertia.y *= -1.5f;

			if(world.inTileOfType(playerRect, 'X')) {
				alive = false;
				world.loadFromFile("deathworld1.txt");
				playerSpr.setPosition(world.playerStart.x, world.playerStart.y);
				inertia.set(0.0f, 0.0f);
			}

			if(world.inTileOfType(playerRect, 'Y')) {
				alive = true;
				world.loadFromFile("world1.txt");
				playerSpr.setPosition(world.playerStart.x, world.playerStart.y);
				inertia.set(0.0f, 0.0f);
			}
		}
	}

	private Rectangle calcPlayerRect() {
		Rectangle rect = playerSpr.getBoundingRectangle();
		rect.x += world.xOffset;
		rect.y += world.yOffset;
		return rect;
	}

	private float determineScrollSpeed(boolean inFrame) {
		return inFrame ? SCROLL_SPEED :  Math.max(-playerSpr.getX() * SCROLL_SPEED, MAX_MOV_SPEED);
	}

	private void updateScrolling() {
		if(playerSpr.getX() <= SCROLL_WINDOW) {
			float scrollSpeed = determineScrollSpeed(playerSpr.getX() >= 0);
			world.scroll(-scrollSpeed, 0);
			playerSpr.setX(playerSpr.getX() + scrollSpeed);
		}

		if(Globals.SCR_W - playerSpr.getX() <= SCROLL_WINDOW) {
			float scrollSpeed = determineScrollSpeed(playerSpr.getX() + playerSpr.getWidth() < Globals.SCR_W);
			world.scroll(scrollSpeed, 0);
			playerSpr.setX(playerSpr.getX() - scrollSpeed);
		}

		if(Globals.SCR_H - playerSpr.getY() <= SCROLL_WINDOW) {
			float scrollSpeed = determineScrollSpeed(playerSpr.getY() + playerSpr.getHeight() < Globals.SCR_H);
			world.scroll(0, scrollSpeed);
			playerSpr.setY(playerSpr.getY() - scrollSpeed);
		}

		if(playerSpr.getY() <= SCROLL_WINDOW) {
			float scrollSpeed = determineScrollSpeed(playerSpr.getY() >= 0);
			world.scroll(0, -scrollSpeed);
			playerSpr.setY(playerSpr.getY() + scrollSpeed);
		}
	}

	private void processInput() {
		if(Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			Gdx.app.exit();
		}

		// horizontal
		if(Gdx.input.isKeyPressed(Keys.LEFT)) {
			move(-MOV_SPEED, 0);
		}
		if(Gdx.input.isKeyPressed(Keys.RIGHT)) {
			move(MOV_SPEED, 0);
		}

		// vertical
		if(Gdx.input.isKeyPressed(Keys.UP)) {
			move(0, MOV_SPEED);
		}
		if(Gdx.input.isKeyPressed(Keys.DOWN)) {
			move(0, -MOV_SPEED);
		}
	}

	private void move(float dx, float dy) {
		inertia.x += dx;
		inertia.y += dy;
		
		if(inertia.x >= MAX_MOV_SPEED) inertia.x = MAX_MOV_SPEED;
		if(inertia.y >= MAX_MOV_SPEED) inertia.y = MAX_MOV_SPEED;
	}

	private boolean playerInTile(Rectangle playerRect) {
		return world.inTile(playerRect);
	}

	private void renderScene() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		sb.begin();
		
		world.render(sb);
		playerSpr.draw(sb);
		
		String curStr = alive ? "ALIVE" : "DEAD";
		TextBounds txtBounds = bigFont.getBounds(curStr);
		bigFont.draw(sb, curStr, 10, Globals.SCR_H - txtBounds.height);
		bigFont.draw(sb, "Score: " + score, txtBounds.width + 10 + 10, Globals.SCR_H - txtBounds.height);
		 
		sb.end();
	}

	@Override
	public void resize(int width, int height) {
	}	

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

}
