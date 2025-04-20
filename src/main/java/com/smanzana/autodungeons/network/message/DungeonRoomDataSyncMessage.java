package com.smanzana.autodungeons.network.message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.world.dungeon.room.DungeonRoomLoader;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server is synchronizing data pack info (dungeon rooms) with client
 * @author Skyler
 *
 */
public class DungeonRoomDataSyncMessage {

	public static void handle(DungeonRoomDataSyncMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			AutoDungeons.LOGGER.info("Recieved {} dungeon rooms from the server", message.roomData.size());
			DungeonRoomLoader.instance().loadServerData(message.roomData);
		});
	}
		
	private final List<CompoundTag> roomData;
	
	public DungeonRoomDataSyncMessage(List<CompoundTag> roomData) {
		this.roomData = roomData;
	}
	
	public DungeonRoomDataSyncMessage(DungeonRoomLoader loader) {
		this(loader.getServerData());
	}

	public static DungeonRoomDataSyncMessage decode(FriendlyByteBuf buf) {
		final int dataCount = buf.readVarInt();
		final List<CompoundTag> roomData = new ArrayList<>(dataCount);
		
		CompoundTag bigData = null;
		try {
			bigData = NbtIo.readCompressed(new ByteBufInputStream(buf));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ListTag list = bigData.getList("1", Tag.TAG_COMPOUND);
		
		for (int i = 0; i < list.size(); i++) {
			final CompoundTag tag;
			//tag = buf.readCompoundTag();
			tag = list.getCompound(i);
			
			roomData.add(tag);
		}
		
		return new DungeonRoomDataSyncMessage(roomData);
	}

	public static void encode(DungeonRoomDataSyncMessage msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.roomData.size());
		CompoundTag bigData = new CompoundTag();
		ListTag list = new ListTag();
		for (CompoundTag data : msg.roomData) {
			// Want to just do this, but this doesn't actually compress the NBT data so it ends up being MASSIVE (24MB)
			//buf.writeCompoundTag(data);
			list.add(data);
		}
		bigData.put("1", list);
		
		try {
			NbtIo.writeCompressed(bigData, new ByteBufOutputStream(buf));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
