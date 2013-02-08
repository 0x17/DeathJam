package com.andreschnabel.deathjam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
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

	public static void playSound(Sound snd) {
		if(!Globals.NO_SOUND) snd.play();
	}

	public static void playSong(Music mus) {
		if(!Globals.NO_SOUND) mus.play();
	}

	public static long getTicks() {
		return System.currentTimeMillis();
	}
}
