package com.andreschnabel.deathjam;

public class FloodFill {

	private final int gridW, gridH;
	private final char[][] grid;

	public final static int UNVISITED = 0;
	public final static int INSIDE = 1;
	public final static int OUTSIDE = -1;

	public FloodFill(char[][] grid, int gridW, int gridH) {
		this.grid = grid;
		this.gridW = gridW;
		this.gridH = gridH;
	}

	public int[][] fillFromPos(int startX, int startY) {
		int[][] state = new int[gridH][gridW];
		recursiveFill(startX, startY, state);
		return state;
	}

	private void recursiveFill(int x, int y, int[][] state) {
		state[y][x] = INSIDE;
		checkPos(x-1, y, state);
		checkPos(x+1, y, state);
		checkPos(x, y-1, state);
		checkPos(x, y+1, state);
	}

	private void checkPos(int x, int y, int[][] state) {
		if(x < 0 || y < 0 || x >= gridW || y >= gridH) return;

		if(state[y][x] == UNVISITED) {
			if(grid[y][x] == ' ') recursiveFill(x, y, state);
			else state[y][x] = OUTSIDE;
		}
	}

}
