package com.planetdestroyer;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/** kind: 0=arrow flight, 1=portal open, 2=planet fall start, 3=impact, 4=final fall start, 5=final impact, 6=end */
public record CosmicPayload(int kind, BlockPos pos, int index, int durationTicks) implements CustomPayload {
	public static final Id<CosmicPayload> ID = new Id<>(Identifier.of(PlanetDestroyerMod.ID, "cosmic"));
	public static final PacketCodec<RegistryByteBuf, CosmicPayload> CODEC = PacketCodec.tuple(
			PacketCodec.of((v, b) -> b.writeVarInt(v), b -> b.readVarInt()), CosmicPayload::kind,
			BlockPos.PACKET_CODEC, CosmicPayload::pos,
			PacketCodec.of((v, b) -> b.writeVarInt(v), b -> b.readVarInt()), CosmicPayload::index,
			PacketCodec.of((v, b) -> b.writeVarInt(v), b -> b.readVarInt()), CosmicPayload::durationTicks,
			CosmicPayload::new);
	@Override public Id<? extends CustomPayload> getId() { return ID; }
}
