package com.andreschnabel.deathjam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class World {

	private final static int TILE_W = 64;
	private final static int TILE_H = 64;
	private static final int COIN_VALUE = 100;
	private char[][] grid;
	public int gridW;
	public int gridH;
	
	private List<AtlasRegion> tileRegions;
	private AtlasRegion coinRegion;
	private HashMap<Character, Integer> charToRegionMap;
	public Vector2 playerStart = new Vector2();
	private List<Enemy> enemies = new ArrayList<Enemy>();
	private AtlasRegion enemyRegion;

	private float rotAlpha;
	private List<Rectangle> coinRects = new ArrayList<Rectangle>();

	private SpriteCache sc;
	private int cacheId;
	private final SpriteBatch sb;

	private TextureRegion floorRegion;
	private boolean inDeathWorld;
	private Color brightRed = new Color(1.0f, 0.4f, 0.4f, 1.0f);
	private final Sound coindSound;
	private final Music aliveLoop;
	private final Music deadLoop;

	public World() {
		initCharToRegionMap();

		tileRegions = new ArrayList<AtlasRegion>();
		for(AtlasRegion region : Globals.atlas.getRegions()) {
			if(region.name.startsWith("tile"))
				tileRegions.add(region);
		}
		coinRegion = Globals.atlas.findRegion("coin");
		enemyRegion = Globals.atlas.findRegion("enemy");

		floorRegion = Globals.atlas.findRegion("floor");

		deadLoop = Gdx.audio.newMusic(Utils.assetHandle("deadloop.mp3"));
		aliveLoop = Gdx.audio.newMusic(Utils.assetHandle("aliveloop.mp3"));

		loadFromFile("world1.txt", false);

		sb = new SpriteBatch();

		coindSound = Gdx.audio.newSound(Utils.assetHandle("coin.wav"));
	}

	private void setupGridCache() {
		sc = new SpriteCache();

		sc.beginCache();
		if(inDeathWorld)
			sc.setColor(Color.RED);

		for(int y=0; y<gridH; y++) {
			for(int x=0; x<gridW; x++) {
				char c = grid[y][x];
				float xpos = x * TILE_W;
				float ypos = y * TILE_H;
				if(c != ' ') {
					int regionIndex = charToRegionMap.get(c);
					TextureRegion region = tileRegions.get(regionIndex);
					sc.add(region, xpos, ypos);
				} else {
					sc.add(floorRegion, xpos, ypos);
				}
			}
		}

		cacheId = sc.endCache();
	}

	public void dispose() {
		deadLoop.dispose();
		aliveLoop.dispose();
		coindSound.dispose();
		sc.dispose();
	}

	private void initCharToRegionMap() {
		charToRegionMap = new HashMap<Character, Integer>();
		charToRegionMap.put('#', 0);
		charToRegionMap.put('A', 1);
		charToRegionMap.put('B', 2);
		charToRegionMap.put('C', 3);
		charToRegionMap.put('X', 4);
		charToRegionMap.put('Y', 5);
	}

	public void loadFromFile(String filename, boolean deathWorld) {
		this.inDeathWorld = deathWorld;
		coinRects.clear();
		enemies.clear();

		String worldStr = Utils.assetHandle(filename).readString();
		String[] lines = worldStr.split("\n");

		gridW = maxLineLength(lines);
		gridH = lines.length;

		grid = new char[gridH][gridW];
		fillWhitespace();

		int xCounter, yCounter;
		xCounter = yCounter = 0;
		for(int j=0; j<lines.length; j++) {
			String line = lines[j /*lines.length-j-1*/];

			for(int i=0; i<line.length(); i++) {
				char c = line.charAt(i);

				int xPosition, yPosition;
				xPosition = xCounter * TILE_W;
				yPosition = yCounter * TILE_H;

				if(c == 's') {
					c = ' ';
					playerStart.x = xPosition;
					playerStart.y = yPosition;
					Utils.debug("Start pos = " + playerStart);
				} else if(c == 'c') {
					c = ' ';
					Rectangle ncrect = new Rectangle(
							xPosition + (TILE_W - coinRegion.getRegionWidth()) / 2.0f,
							yPosition + (TILE_H - coinRegion.getRegionHeight()) / 2.0f,
							coinRegion.getRegionWidth(),
							coinRegion.getRegionHeight());
					coinRects.add(ncrect);
				} else if(c == 'e') {
					c = ' ';
					enemies.add(new Enemy(xPosition, yPosition));
				}

				grid[yCounter][xCounter] = c;
				xCounter++;
			}
			xCounter = 0;
			yCounter++;
		}

		setupGridCache();

		if(inDeathWorld) {
			aliveLoop.stop();
			deadLoop.play();
		} else {
			deadLoop.stop();
			aliveLoop.play();
		}
	}

	private void fillWhitespace() {
		for(int y = 0; y<gridH; y++)
			for(int x = 0; x<gridW; x++)
				grid[y][x] = ' ';
	}

	private int maxLineLength(String[] lines) {
		int maxLength = 0;
		for(String line : lines) {
			maxLength = line.length() > maxLength ? line.length() : maxLength;
		}
		return maxLength;
	}

	public void update() {
		Enemy.updateAlpha();
		for(Enemy enemy : enemies)
			enemy.updatePos();
	}

	public void render(Matrix4 mviewmx) {
		sc.setTransformMatrix(mviewmx);
		sc.begin();
		sc.draw(cacheId);
		sc.end();

		sb.setTransformMatrix(mviewmx);
		sb.begin();
		if(inDeathWorld) sb.setColor(brightRed);
		for(Rectangle coinRect : coinRects) {
			sb.draw(coinRegion, coinRect.x, coinRect.y);
		}

		for(Enemy enemy : enemies) {
			sb.draw(enemyRegion, enemy.pos.x, enemy.pos.y);
		}
		sb.end();
	}

	public Vector2[] calcCorners(Rectangle rect) {
		Vector2[] corners = new Vector2[4];
		corners[0] = new Vector2(rect.x, rect.y+rect.height);
		corners[1] = new Vector2(rect.x+rect.width, rect.y+rect.height);
		corners[2] = new Vector2(rect.x, rect.y);
		corners[3] = new Vector2(rect.x+rect.width, rect.y);
		return corners;
	}

	public boolean inTileOfType(Rectangle rect, char type) {
		Vector2[] corners = calcCorners(rect);
		for(Vector2 corner : corners) {
			if(isOfType(corner, type))
				return true;
		}
		return false;
	}

	private boolean isOfType(Vector2 pos, char type) {
		return grid[(int)(pos.y / TILE_H)][(int)(pos.x / TILE_W)] == type;
	}

	public boolean inTile(Rectangle rect) {
		Vector2[] corners = calcCorners(rect);
		for(Vector2 corner : corners) {
			if(!isEmpty(corner))
				return true;
		}
		return false;
	}

	public boolean isEmpty(Vector2 pos) {
		return grid[(int)(pos.y / TILE_H)][(int)(pos.x / TILE_W)] == ' ';
	}

	public int tryCollectCoin(Rectangle playerRect) {
		int collectedAmount = 0;
		List<Rectangle> toDel = new LinkedList<Rectangle>();
		for(Rectangle coinRect : coinRects) {
			if(Intersector.overlapRectangles(coinRect, playerRect)) {
				toDel.add(coinRect);
				collectedAmount += COIN_VALUE;
				coindSound.play();
			}
		}
		coinRects.removeAll(toDel);
		return collectedAmount;
	}

	public List<Enemy> getEnemies() {
		return enemies;
	}
}
