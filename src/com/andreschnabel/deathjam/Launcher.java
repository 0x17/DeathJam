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
		final boolean FORCE_REPACK = true;
		if(FORCE_REPACK || new File("src/data/atlas.pack").lastModified() < new File("src/data/texsrc").lastModified())
			TexturePacker2.process("src/data/texsrc", "src/data", "atlas.pack");
	}

}
