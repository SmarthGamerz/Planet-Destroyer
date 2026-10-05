package com.planetdestroyer.client;

import com.planetdestroyer.CosmicPayload;
import net.minecraft.util.math.BlockPos;

/** Client-side visual state. Render code reads this; it is cleared automatically on kind 6 or timeout. */
public class CosmicState {
	public static boolean active; public static BlockPos target;
	public static int phase, planetIndex; public static long phaseStart; public static int duration;
	static long lastEvent;

	public static void onEvent(CosmicPayload p) {
		lastEvent = System.currentTimeMillis();
		if (p.kind() == 6) { active = false; return; }
		active = true; target = p.pos(); phase = p.kind(); planetIndex = p.index(); duration = p.durationTicks();
		phaseStart = lastEvent;
	}
	/** Safety cleanup: if no packet for 30s the effect is dropped. */
	public static boolean isActive() {
		if (active && System.currentTimeMillis() - lastEvent > 30000) active = false;
		return active;
	}
}
