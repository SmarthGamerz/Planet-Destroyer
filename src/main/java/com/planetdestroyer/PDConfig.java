package com.planetdestroyer;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;

public class PDConfig {
	public int maxRadius = 32;
	public int cooldownSeconds = 60;
	public boolean terrainDestruction = true;
	public float planetDamage = 60f;
	public float finalDamage = 400f;
	public int planetCount = 6;
	public int blocksPerTick = 400;   // crater work is spread over ticks
	public static PDConfig INSTANCE = new PDConfig();

	public static void load() {
		Path p = FabricLoader.getInstance().getConfigDir().resolve("planetdestroyer.json");
		Gson g = new GsonBuilder().setPrettyPrinting().create();
		try {
			if (Files.exists(p)) INSTANCE = g.fromJson(Files.readString(p), PDConfig.class);
			Files.writeString(p, g.toJson(INSTANCE));
		} catch (Exception e) { e.printStackTrace(); }
		INSTANCE.maxRadius = Math.max(4, Math.min(INSTANCE.maxRadius, 96));
	}
}
