package com.andreschnabel.deathjam;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2;

import java.io.File;

public class Launcher {
	public static void main(String[] args) {
		updateAtlas();
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.fullscreen = false;
		config.useGL20 = false;
		config.width = Globals.PSCR_W;
		config.height = Globals.PSCR_H;
		config.title = "DEATH";
		config.resizable = false;
		new LwjglApplication(new DeathGame(), config);
		//new LwjglApplication(new DeathGame(), "DEATH", Globals.PSCR_W, Globals.PSCR_H, false);
	}

	private static void updateAtlas() {
		final boolean FORCE_REPACK = false;
		if(FORCE_REPACK || new File("src/data/atlas.pack").lastModified() < new File("src/data/texsrc").lastModified())
			TexturePacker2.process("src/data/texsrc", "src/data", "atlas.pack");
	}

}
