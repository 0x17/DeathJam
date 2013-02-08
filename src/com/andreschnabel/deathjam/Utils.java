package com.andreschnabel.deathjam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.Random;

public class Utils {
	
	private static Random rand = new Random();
	
	public static FileHandle assetHandle(String assetName) {
		return Gdx.files.internal("data/" + assetName);
	}

	public static int randInt(int max) {
		return rand.nextInt(max);
	}

	public static void debug(String msg) {
		System.out.println(msg);
		System.out.flush();
	}
}
