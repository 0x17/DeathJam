package com.andreschnabel.deathjam;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;

public class DeathGame implements ApplicationListener {
	
	//region Rendering
	private SpriteBatch sb;
	private BitmapFont smallFont, bigFont;
	//endregion
	
	//region Textures
	private TextureAtlas atlas;
	private AtlasRegion sprRegion, enemyRegion;
	//endregion
	
	private static final float MOV_SPEED = 5.0f;
	private static final float MAX_MOV_SPEED = 20.0f;
	private static final float SCROLL_SPEED = 5.0f;
	private Vector2 playerPos;
	private Vector2 inertia = Vector2.Zero.cpy();
	private boolean alive = true;
	private int score = 0;
	
	private World world;

	@Override
	public void create() {
		
		sb = new SpriteBatch();
		
		initTextures();
		initFonts();
		
		playerPos = new Vector2(Globals.SCR_W / 2.0f, Globals.SCR_H / 2.0f);
		
		float lum = 0.3f;
		Gdx.gl.glClearColor(lum, lum, lum, 1.0f);
		
		Array<AtlasRegion> regions = atlas.getRegions();
		List<AtlasRegion> tileRegions = new ArrayList<AtlasRegion>();
		for(AtlasRegion region : regions) {
			if(region.name.startsWith("tile"))
				tileRegions.add(region);
		}
		AtlasRegion coinRegion = atlas.findRegion("coin");
		world = new World(tileRegions, coinRegion, enemyRegion);
		world.printGrid();
		playerPos.set(world.playerStart);
	}

	private void initTextures() {
		atlas = new TextureAtlas(Utils.assetHandle("atlas.pack"));
		sprRegion = atlas.findRegion("spr");
		enemyRegion = atlas.findRegion("enemy");
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
		atlas.dispose();
	}
	
	@Override
	public void render() {
		processInput();
		update();
		renderScene(Gdx.graphics.getDeltaTime());
	}

	private void update() {
		if(!updatePlayerPos()) {
			updateScrolling();
			updateInertia();
			score += world.tryCollectCoin(calcPlayerRect());
		}
	}

	private void updateInertia() {
		inertia.x *= 0.75f;
		inertia.y *= 0.75f;
	}

	private boolean updatePlayerPos() {
		playerPos.x += inertia.x;
		playerPos.y += inertia.y;
		
		Rectangle playerRect = calcPlayerRect();
		if(playerInTile(playerRect)) {
			playerPos.x -= inertia.x;
			playerPos.y -= inertia.y;
			inertia.x *= -1.5f;
			inertia.y *= -1.5f;
			
			if(world.inTileOfType(playerRect, 'X')) {
				alive = false;
				world.loadFromFile("deathworld1.txt");
				playerPos.set(world.playerStart);
				return true;
			}
			
			if(world.inTileOfType(playerRect, 'Y')) {
				alive = true;
				world.loadFromFile("world1.txt");
				playerPos.set(world.playerStart);
				return true;
			}
		}
		
		return false;
	}
	
	private float determineScrollSpeed(boolean inframe) {
		return inframe ? SCROLL_SPEED :  Math.max(-playerPos.x * SCROLL_SPEED, MAX_MOV_SPEED);
	}

	private void updateScrolling() {
		if(playerPos.x <= SCROLL_WINDOW) {
			float scrollSpeed = determineScrollSpeed(playerPos.x >= 0);
			world.scroll(-scrollSpeed, 0);
			playerPos.x += scrollSpeed;
		}
		
		if(Globals.SCR_W - playerPos.x <= SCROLL_WINDOW) {
			float scrollSpeed = determineScrollSpeed(playerPos.x + sprRegion.getRegionWidth() < Globals.SCR_W);
			world.scroll(scrollSpeed, 0);
			playerPos.x -= scrollSpeed;
		}
		
		if(Globals.SCR_H - playerPos.y <= SCROLL_WINDOW) {
			float scrollSpeed = determineScrollSpeed(playerPos.y + sprRegion.getRegionHeight() < Globals.SCR_H);
			world.scroll(0, scrollSpeed);
			playerPos.y -= scrollSpeed;
		}
		
		if(playerPos.y <= SCROLL_WINDOW) {
			float scrollSpeed = determineScrollSpeed(playerPos.y >= 0);
			world.scroll(0, -scrollSpeed);
			playerPos.y += scrollSpeed;
		}
	}

	private void processInput() {
		processKeyboard();
		processMouse();
	}

	private void processMouse() {
		if(Gdx.input.isButtonPressed(Buttons.LEFT)) {
			Vector2 oldPos = playerPos.cpy();
			playerPos.x = Gdx.input.getX();
			playerPos.y = Globals.SCR_H - Gdx.input.getY();
			if(playerInTile(calcPlayerRect()))
				playerPos = oldPos;
		}
	}
	
	private final float SCROLL_WINDOW = 100.0f;
	
	private void move(float dx, float dy) {
		inertia.x += dx;
		inertia.y += dy;
		
		if(inertia.x >= MAX_MOV_SPEED) inertia.x = MAX_MOV_SPEED;
		if(inertia.y >= MAX_MOV_SPEED) inertia.y = MAX_MOV_SPEED;
	}

	private void processKeyboard() {
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

	private Rectangle calcPlayerRect() {
		Rectangle playerRect = new Rectangle(
				playerPos.x+world.xOffset,
				playerPos.y+world.yOffset,
				sprRegion.getRegionWidth(),
				sprRegion.getRegionHeight());
		return playerRect;
	}
	
	private boolean playerInTile(Rectangle playerRect) {
		return world.inTile(playerRect);
	}

	private void renderScene(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		sb.begin();
		
		world.render(sb, delta);
		sb.draw(sprRegion, playerPos.x, playerPos.y);
		
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
