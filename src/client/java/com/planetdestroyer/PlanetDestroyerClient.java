package com.planetdestroyer.client;

import com.planetdestroyer.CosmicPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

public class PlanetDestroyerClient implements ClientModInitializer {
	@Override public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(CosmicPayload.ID, (pl, ctx) -> ctx.client().execute(() -> {
			CosmicState.onEvent(pl);
			var w = MinecraftClient.getInstance().world;
			if (w == null) return;
			Vec3d c = Vec3d.ofCenter(pl.pos());
			if (pl.kind() == 3 || pl.kind() == 5) {
				int n = pl.kind() == 5 ? 80 : 30;               // hard-capped
				for (int i = 0; i < n; i++) {
					double a = i * 6.2831853 / n;
					w.addParticleClient(ParticleTypes.CAMPFIRE_COSY_SMOKE, c.x + Math.cos(a) * 2, c.y + 1, c.z + Math.sin(a) * 2,
							Math.cos(a) * 0.6, 0.15, Math.sin(a) * 0.6);   // expanding shockwave ring
				}
			}
		}));
		// TODO: hook sky/planet rendering to CosmicState (see README).
	}
}
