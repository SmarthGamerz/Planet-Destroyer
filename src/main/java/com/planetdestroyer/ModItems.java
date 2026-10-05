package com.planetdestroyer;

import net.minecraft.item.Item;
import net.minecraft.registry.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {
	public static Item COSMIC_CORE, COSMIC_ARROW, PLANET_DESTROYER;

	static Item.Settings s(String n) {
		return new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(PlanetDestroyerMod.ID, n)));
	}
	static Item reg(String n, Item i) {
		return Registry.register(Registries.ITEM, Identifier.of(PlanetDestroyerMod.ID, n), i);
	}
	public static void init() {
		COSMIC_CORE = reg("cosmic_core", new Item(s("cosmic_core").rarity(Rarity.EPIC).maxCount(16)));
		COSMIC_ARROW = reg("cosmic_destruction_arrow", new Item(s("cosmic_destruction_arrow").rarity(Rarity.EPIC).maxCount(16)));
		PLANET_DESTROYER = reg("planet_destroyer", new PlanetDestroyerItem(s("planet_destroyer").rarity(Rarity.EPIC).maxCount(1).fireproof()));
	}
}
