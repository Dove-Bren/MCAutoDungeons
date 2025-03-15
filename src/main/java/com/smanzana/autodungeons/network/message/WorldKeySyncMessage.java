package com.smanzana.autodungeons.network.message;

import java.util.function.Supplier;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.world.WorldKeyRegistry;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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
			AutoDungeons.GetWorldKeys().deserializeNBT(message.keyData);
		});
	}
		
	private final CompoundNBT keyData;
	
	public WorldKeySyncMessage(CompoundNBT keyData) {
		this.keyData = keyData;
	}
	
	public WorldKeySyncMessage(WorldKeyRegistry registry) {
		this(registry.serializeNBT());
	}

	public static WorldKeySyncMessage decode(PacketBuffer buf) {
		return new WorldKeySyncMessage(buf.readNbt());
	}

	public static void encode(WorldKeySyncMessage msg, PacketBuffer buf) {
		buf.writeNbt(msg.keyData);
	}

}
