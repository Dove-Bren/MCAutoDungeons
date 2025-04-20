package com.smanzana.autodungeons.network.message;

import java.util.function.Supplier;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.world.WorldKeyRegistry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server is sending updated world key information to a client
 * @author Skyler
 *
 */
public class WorldKeySyncMessage {

	public static void handle(WorldKeySyncMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			AutoDungeons.LOGGER.debug("Recieved world key update message from server");
			AutoDungeons.GetWorldKeys().loadFromNBT(message.keyData);
		});
	}
		
	private final CompoundTag keyData;
	
	public WorldKeySyncMessage(CompoundTag keyData) {
		this.keyData = keyData;
	}
	
	public WorldKeySyncMessage(WorldKeyRegistry registry) {
		this(registry.save(new CompoundTag()));
	}

	public static WorldKeySyncMessage decode(FriendlyByteBuf buf) {
		return new WorldKeySyncMessage(buf.readNbt());
	}

	public static void encode(WorldKeySyncMessage msg, FriendlyByteBuf buf) {
		buf.writeNbt(msg.keyData);
	}

}
