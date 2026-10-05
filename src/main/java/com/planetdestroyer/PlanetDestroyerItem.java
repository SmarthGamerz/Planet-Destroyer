package com.planetdestroyer;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.consume.UseAction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.*;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class PlanetDestroyerItem extends Item {
	static final int FULL_CHARGE = 30; // ticks

	public PlanetDestroyerItem(Settings s) { super(s); }

	@Override public UseAction getUseAction(ItemStack st) { return UseAction.BOW; }
	@Override public int getMaxUseTime(ItemStack st, LivingEntity u) { return 72000; }

	@Override
	public ActionResult use(World w, PlayerEntity p, Hand h) {
		ItemStack st = p.getStackInHand(h);
		if (p.getItemCooldownManager().isCoolingDown(st)) return ActionResult.FAIL;
		if (!p.isCreative() && findArrow(p) < 0) return ActionResult.FAIL;   // arrow required
		if (p.isCreative() && findArrow(p) < 0) return ActionResult.FAIL;    // creative also needs one
		p.setCurrentHand(h);
		return ActionResult.CONSUME;
	}

	static int findArrow(PlayerEntity p) {
		for (int i = 0; i < p.getInventory().size(); i++)
			if (p.getInventory().getStack(i).isOf(ModItems.COSMIC_ARROW)) return i;
		return -1;
	}

	@Override
	public boolean onStoppedUsing(ItemStack st, World w, LivingEntity user, int remaining) {
		if (!(user instanceof ServerPlayerEntity p) || !(w instanceof ServerWorld sw)) return false;
		int used = getMaxUseTime(st, user) - remaining;
		if (used < FULL_CHARGE) return false;                 // must be fully charged
		if (p.getItemCooldownManager().isCoolingDown(st)) return false;
		int slot = findArrow(p);
		if (slot < 0) return false;

		// Server decides the target
		Vec3d eye = p.getEyePos(), end = eye.add(p.getRotationVec(1f).multiply(160));
		BlockHitResult hit = sw.raycast(new RaycastContext(eye, end,
				RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, p));
		if (hit.getType() != HitResult.Type.BLOCK) return false;   // arrow must "land"

		p.getInventory().getStack(slot).decrement(1);              // arrow consumed
		p.getItemCooldownManager().set(st, PDConfig.INSTANCE.cooldownSeconds * 20);
		CosmicManager.start(sw, p, hit.getBlockPos(), eye);
		return true;
	}
}
