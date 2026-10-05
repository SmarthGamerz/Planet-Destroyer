package com.planetdestroyer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.item.ItemGroups;

public class PlanetDestroyerMod implements ModInitializer {
	public static final String ID = "planetdestroyer";

	@Override public void onInitialize() {
		PDConfig.load();
		ModItems.init();
		PayloadTypeRegistry.playS2C().register(CosmicPayload.ID, CosmicPayload.CODEC);
		ServerTickEvents.END_WORLD_TICK.register(CosmicManager::tick);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(e -> {
			e.add(ModItems.PLANET_DESTROYER); e.add(ModItems.COSMIC_ARROW);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(e -> e.add(ModItems.COSMIC_CORE));
		// Intentionally NO commands registered.
	}
}
