package com.planetdestroyer;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import java.util.*;

/** One lightweight state machine per activation. No entities are spawned. */
public class CosmicManager {
	static final int FALL = 50, GAP = 25, PORTAL = 60, FINAL_FALL = 120, FINAL_DARK = 40;
	static final List<Seq> ACTIVE = new ArrayList<>();

	static class Seq {
		ServerWorld w; UUID owner; BlockPos target; int tick = 0;
		int travel; int planets; boolean craterStarted, ended;
		Deque<BlockPos> craterQueue = new ArrayDeque<>();
		int portalAt, firstFallAt, finalAt, impactAt;
		Set<Integer> done = new HashSet<>();
	}

	public static void start(ServerWorld w, ServerPlayerEntity p, BlockPos target, Vec3d from) {
		Seq s = new Seq();
		s.w = w; s.owner = p.getUuid(); s.target = target;
		s.travel = Math.max(5, (int) (from.distanceTo(Vec3d.ofCenter(target)) / 4.0));   // ~4 blocks/tick
		s.planets = PDConfig.INSTANCE.planetCount;
		s.portalAt = s.travel;
		s.firstFallAt = s.portalAt + PORTAL;
		s.finalAt = s.firstFallAt + s.planets * (FALL + GAP) + FINAL_DARK;
		s.impactAt = s.finalAt + FINAL_FALL;
		ACTIVE.add(s);
		w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, SoundCategory.PLAYERS, 2f, 0.6f);
		send(s, 0, 0, s.travel);
	}

	static void send(Seq s, int kind, int idx, int dur) {
		CosmicPayload pl = new CosmicPayload(kind, s.target, idx, dur);
		for (ServerPlayerEntity p : PlayerLookup.around(s.w, Vec3d.ofCenter(s.target), 256)) ServerPlayNetworking.send(p, pl);
	}

	public static void tick(ServerWorld w) {
		Iterator<Seq> it = ACTIVE.iterator();
		while (it.hasNext()) {
			Seq s = it.next();
			if (s.w != w) continue;
			step(s);
			if (s.ended) it.remove();
		}
	}

	static void step(Seq s) {
		int t = s.tick++;
		if (t == s.portalAt) {
			send(s, 1, 0, PORTAL);
			s.w.playSound(null, s.target, SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.AMBIENT, 4f, 0.5f);
		}
		// normal planets, strictly one after another
		for (int i = 0; i < s.planets; i++) {
			int start = s.firstFallAt + i * (FALL + GAP);
			if (t == start) send(s, 2, i, FALL);
			if (t == start + FALL) {
				send(s, 3, i, 0);
				impact(s, 24 + 4 * i, PDConfig.INSTANCE.planetDamage, false);
			}
		}
		if (t == s.finalAt) send(s, 4, 0, FINAL_FALL);
		if (t == s.impactAt) {
			send(s, 5, 0, 0);
			impact(s, PDConfig.INSTANCE.maxRadius, PDConfig.INSTANCE.finalDamage, true);
			buildCraterQueue(s);
			s.craterStarted = true;
		}
		if (s.craterStarted) {
			int n = PDConfig.INSTANCE.blocksPerTick;
			while (n-- > 0 && !s.craterQueue.isEmpty()) breakBlock(s, s.craterQueue.poll());
			if (s.craterQueue.isEmpty()) { send(s, 6, 0, 0); s.ended = true; }
		}
	}

	static void impact(Seq s, int radius, float damage, boolean big) {
		ServerWorld w = s.w;
		Vec3d c = Vec3d.ofCenter(s.target);
		double r = Math.min(radius, PDConfig.INSTANCE.maxRadius);
		Box box = new Box(c, c).expand(r);
		ServerPlayerEntity owner = (ServerPlayerEntity) w.getPlayerByUuid(s.owner);
		for (LivingEntity e : w.getEntitiesByClass(LivingEntity.class, box, e -> e.isAlive())) {
			double d = e.getPos().distanceTo(c);
			if (d > r) continue;
			float dmg = (float) (damage * (1.0 - d / r * 0.6));
			e.damage(w, w.getDamageSources().explosion(owner, owner), dmg);
			Vec3d push = e.getPos().subtract(c).normalize().multiply(big ? 2.5 : 1.0).add(0, 0.6, 0);
			e.addVelocity(push); e.velocityModified = true;
		}
		w.playSound(null, s.target, big ? SoundEvents.ENTITY_GENERIC_EXPLODE.value() : SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
				SoundCategory.BLOCKS, big ? 8f : 4f, big ? 0.4f : 0.7f);
		// capped particle bursts (server sends small counts; client adds more locally)
		w.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y + 1, c.z, big ? 3 : 1, 2, 0.5, 2, 0);
		w.spawnParticles(ParticleTypes.REVERSE_PORTAL, c.x, c.y + 1, c.z, 40, 3, 1, 3, 0.3);
	}

	/** Bowl-shaped crater: depth falls off with distance, center deepest. Sorted outer->inner top-down is not needed. */
	static void buildCraterQueue(Seq s) {
		if (!PDConfig.INSTANCE.terrainDestruction) return;
		int R = PDConfig.INSTANCE.maxRadius;
		int maxDepth = Math.max(6, R * 2 / 3);
		int cx = s.target.getX(), cy = s.target.getY(), cz = s.target.getZ();
		List<BlockPos> list = new ArrayList<>();
		for (int dx = -R; dx <= R; dx++) for (int dz = -R; dz <= R; dz++) {
			double d = Math.sqrt(dx * dx + dz * dz);
			if (d > R) continue;
			double f = 1.0 - (d / R);
			int depth = (int) Math.round(maxDepth * f * f) + 1;   // quadratic bowl
			int height = (int) (6 * f) + 2;                       // clear air above too
			for (int dy = height; dy >= -depth; dy--) list.add(new BlockPos(cx + dx, cy + dy, cz + dz));
		}
		list.sort(Comparator.comparingInt(p -> -p.getY()));       // top-down looks natural
		s.craterQueue.addAll(list);
	}

	static void breakBlock(Seq s, BlockPos p) {
		ServerWorld w = s.w;
		if (w.isOutOfHeightLimit(p)) return;
		BlockState st = w.getBlockState(p);
		if (st.isAir() || st.getHardness(w, p) < 0) return;                      // keeps bedrock etc.
		ServerPlayerEntity owner = (ServerPlayerEntity) w.getPlayerByUuid(s.owner);
		if (owner != null && !owner.canModifyAt(w, p)) return;                   // spawn protection / adventure
		// NOTIFY_LISTENERS only: skips neighbour updates + drops to avoid update storms
		w.setBlockState(p, net.minecraft.block.Blocks.AIR.getDefaultState(),
				net.minecraft.block.Block.NOTIFY_LISTENERS | net.minecraft.block.Block.SKIP_DROPS);
	}
}
