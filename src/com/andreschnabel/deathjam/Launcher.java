package com.andreschnabel.deathjam;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2;

import java.io.File;

public class Launcher {
	public static void main(String[] args) {
		updateAtlas();
		new LwjglApplication(new DeathGame(), "DEATH", Globals.PSCR_W, Globals.PSCR_H, false);
	}

	private static void updateAtlas() {
		if(new File("data/atlas.pack").lastModified() < new File("data/texsrc").lastModified())
			TexturePacker2.process("data/texsrc", "data", "atlas.pack");
	}

}
