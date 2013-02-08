package com.andreschnabel.deathjam;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class World {
	
	//region Grid
	private final static int TILE_W = 64;
	private final static int TILE_H = 64;
	private static final int COIN_VALUE = 100;
	private static final float ENEMY_RADIUS = 64.0f;
	private char[][] grid;
	public int gridW;
	public int gridH;
	
	public float xOffset, yOffset;
	//endregion
	
	private List<AtlasRegion> tileRegions;
	private AtlasRegion coinRegion;
	private HashMap<Character, Integer> charToRegionMap;
	public Vector2 playerStart = new Vector2();
	private List<Vector2> enemyCenters = new ArrayList<Vector2>();
	private AtlasRegion enemyRegion;

	public World(List<AtlasRegion> tileRegions, AtlasRegion coinRegion, AtlasRegion enemyRegion) {
		initCharToRegionMap();

		this.enemyRegion = enemyRegion;
		this.tileRegions = tileRegions;
		this.coinRegion = coinRegion;
		//fillRandom();
		loadFromFile("world1.txt");
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
	
	public void loadFromFile(String fname) {
		coinRects.clear();

		String worldStr = Utils.assetHandle(fname).readString();
		String[] lines = worldStr.split("\n");
		
		gridW = maxLineLength(lines);
		gridH = lines.length;
		
		grid = new char[gridH][gridW];
		fillWhitespace();
		
		int xctr, yctr;
		xctr = yctr = 0;
		for(int j=0; j<lines.length; j++) {
			String line = lines[/*lines.length-j-1*/ j];
			
			for(int i=0; i<line.length(); i++) {
				char c = line.charAt(i);

				int xpos, ypos;
				xpos = xctr * TILE_W;
				ypos = yctr * TILE_H;
				
				if(c == 's') {
					c = ' ';
					playerStart.x = xpos;
					playerStart.y = ypos;
					Utils.debug("Start pos = " + playerStart);
				} else if(c == 'c') {
					c = ' ';
					coinRects.add(new Rectangle(xpos, ypos, coinRegion.getRegionWidth(), coinRegion.getRegionHeight()));
				} else if(c == 'e') {
					c = ' ';
					enemyCenters.add(new Vector2(xpos, ypos));
				}
				
				grid[yctr][xctr] = c;
				xctr++;
			}
			xctr = 0;
			yctr++;
		}
	}
	
	private void fillWhitespace() {
		for(int y = 0; y<gridH; y++)
			for(int x = 0; x<gridW; x++)
				grid[y][x] = ' ';
	}

	private int maxLineLength(String[] lines) {
		int maxlen = 0;
		for(String line : lines) {
			maxlen = line.length() > maxlen ? line.length() : maxlen;
		}
		return maxlen;
	}
	
	public void printGrid() {
		for(int y = 0; y < gridH; y++) {
			for(int x =0; x < gridW; x++) {
				System.out.print(grid[y][x]);
			}
			System.out.println();
		}
	}

	float rotAlpha;
	
	public void render(SpriteBatch sb, float delta) {
		for(int y=0; y<gridH; y++) {
			for(int x=0; x<gridW; x++) {
				char c = grid[y][x];
				if(c != ' ') {
					int regionIndex = charToRegionMap.get(c);
					AtlasRegion region = tileRegions.get(regionIndex);
					sb.draw(region, x * TILE_W - xOffset, y * TILE_H - yOffset);
				}
			}
		}

		for(Rectangle coinRect : coinRects) {
			sb.draw(coinRegion, coinRect.x - xOffset, coinRect.y - yOffset);
		}

		rotAlpha += 0.05f; //delta * 0.01f;

		for(Vector2 enemyCenter : enemyCenters) {
			float xpos = (float) (enemyCenter.x - xOffset + Math.cos(rotAlpha) * ENEMY_RADIUS);
			float ypos = (float) (enemyCenter.y - yOffset + Math.sin(rotAlpha) * ENEMY_RADIUS);
			sb.draw(enemyRegion, xpos, ypos);
		}
	}

	public void scroll(float dx, float dy) {
		xOffset += dx;
		yOffset += dy;
	}
	
	public boolean inTileOfType(Rectangle rect, char type) {
		Vector2[] corners = new Vector2[4];
		corners[0] = new Vector2(rect.x, rect.y+rect.height);
		corners[1] = new Vector2(rect.x+rect.width, rect.y+rect.height);
		corners[2] = new Vector2(rect.x, rect.y);
		corners[3] = new Vector2(rect.x+rect.width, rect.y);
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
		Vector2[] corners = new Vector2[4];
		corners[0] = new Vector2(rect.x, rect.y+rect.height);
		corners[1] = new Vector2(rect.x+rect.width, rect.y+rect.height);
		corners[2] = new Vector2(rect.x, rect.y);
		corners[3] = new Vector2(rect.x+rect.width, rect.y);
		for(Vector2 corner : corners) {
			if(!isEmpty(corner))
				return true;
		}
		return false;
	}
	
	public boolean isEmpty(Vector2 pos) {
		return grid[(int)(pos.y / TILE_H)][(int)(pos.x / TILE_W)] == ' ';
	}

	private List<Rectangle> coinRects = new ArrayList<Rectangle>();

	public int tryCollectCoin(Rectangle playerRect) {
		int collectedAmount = 0;
		List<Rectangle> toDel = new LinkedList<Rectangle>();
		for(Rectangle coinRect : coinRects) {
			if(Intersector.overlapRectangles(coinRect, playerRect)) {
				toDel.add(coinRect);
				collectedAmount += COIN_VALUE;
			}
		}
		coinRects.removeAll(toDel);
		return collectedAmount;
	}

}
