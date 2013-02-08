package com.andreschnabel.deathjam;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Matrix4;

public class DeathGame implements ApplicationListener {
	private SpriteCache backCache;
	private int backCacheId;
	private SpriteBatch sb, fsb;
	private BitmapFont hugeFont;
	private BitmapFont smallFont, bigFont;

	private World world;
	private Player player;
	private Scroller scroller;

	@Override
	public void create() {
		sb = new SpriteBatch();
		fsb = new SpriteBatch();

		initBackground();

		initFonts();

		float lum = 0.3f;
		Gdx.gl.glClearColor(lum, lum, lum, 1.0f);

		world = new World();
		player = new Player(world);
		scroller = new Scroller(player);
	}

	private void initBackground() {
		backCache = new SpriteCache();
		backCache.beginCache();

		TextureRegion backRegion = Globals.atlas.findRegion("back");

		float w = backRegion.getRegionWidth()*2;
		float h = backRegion.getRegionWidth()*2;

		for(int y=0; y<Globals.PSCR_H; y+=h)
			for(int x=0; x<Globals.PSCR_W; x+=w)
				backCache.add(backRegion, x, y, w, h);

		backCacheId = backCache.endCache();
	}

	private void initFonts() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Utils.assetHandle("font.ttf"));
		hugeFont = generator.generateFont(72);
		bigFont = generator.generateFont(60);
		smallFont = generator.generateFont(30);
		generator.dispose();
	}

	@Override
	public void dispose() {
		smallFont.dispose();
		bigFont.dispose();
		hugeFont.dispose();
		sb.dispose();
		world.dispose();
		player.dispose();
		Globals.atlas.dispose();
	}

	@Override
	public void render() {
		processInput();
		update();
		renderScene();
	}

	private void update() {
		player.updatePlayerPos();
		scroller.updateScrolling();
		player.updateInertia();
		scroller.updateCamera();
		world.update();
	}

	private void processInput() {
		if(Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			Gdx.app.exit();
		}

		if(player.gameover) return;

		if(Gdx.input.isKeyPressed(Keys.LEFT)) {
			player.move(-1, 0);
		}
		if(Gdx.input.isKeyPressed(Keys.RIGHT)) {
			player.move(1, 0);
		}
		if(Gdx.input.isKeyPressed(Keys.UP)) {
			player.move(0, 1);
		}
		if(Gdx.input.isKeyPressed(Keys.DOWN)) {
			player.move(0, -1);
		}
	}

	private void renderScene() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		backCache.begin();
		backCache.draw(backCacheId);
		backCache.end();

		Matrix4 mviewmx = scroller.cam.view;
		
		world.render(mviewmx);
		renderPlayer(mviewmx);

		renderTextOverlay();
	}

	private void renderPlayer(Matrix4 mviewmx) {
		sb.setTransformMatrix(mviewmx);
		sb.begin();
		player.draw(sb);
		sb.end();
	}

	private void renderTextOverlay() {
		fsb.begin();

		String curStr = player.alive ? "ALIVE" : "DEAD";
		TextBounds txtBounds = bigFont.getBounds(curStr);

		bigFont.setColor(Color.WHITE);
		bigFont.draw(fsb, "Score " + player.score, 40, Globals.PSCR_H - txtBounds.height);

		bigFont.setColor(player.alive ? Color.GREEN : Color.RED);
		bigFont.draw(fsb, curStr, Globals.PSCR_W - txtBounds.width, txtBounds.height + 40);

		if(player.gameover) {
			txtBounds = hugeFont.getBounds("GAME OVER!");
			float gx = (Globals.PSCR_W - txtBounds.width) / 2.0f;
			float gy = (Globals.PSCR_H - txtBounds.height) / 2.0f;
			hugeFont.draw(fsb, "GAME OVER!", gx, gy);
		}

		fsb.end();
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
