package com.smanzana.autodungeons.network.message;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.world.dungeon.DungeonRecord;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * Server is providing an update to a player's dungeon status
 * @author Skyler
 *
 */
public class DungeonTrackerUpdateMessage {


	public static void handle(DungeonTrackerUpdateMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			final Player player = AutoDungeons.GetProxy().getPlayer();
			final UUID myID = player.getUUID();
			if (!myID.equals(message.id)) {
				AutoDungeons.LOGGER.error("Received DungeonTrackerUpdateMessage message for a different player: " + message.id);
			} else {
				AutoDungeons.GetDungeonTracker().overrideClientDungeon(player, message.record);
			}
		});
	}
		
	private final @Nonnull UUID id;
	private final @Nullable DungeonRecord record;
	
	public DungeonTrackerUpdateMessage(@Nonnull UUID id, @Nullable DungeonRecord record) {
		this.id = id;
		this.record = record;
	}

	public static DungeonTrackerUpdateMessage decode(FriendlyByteBuf buf) {
		UUID id = buf.readUUID();
		DungeonRecord record = null;
		if (buf.readBoolean()) {
			record = DungeonRecord.FromNBT(buf.readNbt());
		}
		
		return new DungeonTrackerUpdateMessage(id, record);
	}

	public static void encode(DungeonTrackerUpdateMessage msg, FriendlyByteBuf buf) {
		buf.writeUUID(msg.id);
		buf.writeBoolean(msg.record != null);
		if (msg.record != null) {
			buf.writeNbt(msg.record.toNBT());
		}		
	}

}
