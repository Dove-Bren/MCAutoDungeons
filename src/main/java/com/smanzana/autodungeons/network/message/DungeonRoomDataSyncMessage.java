package com.smanzana.autodungeons.network.message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.world.dungeon.room.DungeonRoomLoader;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.network.NetworkEvent;

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
		
	private final List<CompoundNBT> roomData;
	
	public DungeonRoomDataSyncMessage(List<CompoundNBT> roomData) {
		this.roomData = roomData;
	}
	
	public DungeonRoomDataSyncMessage(DungeonRoomLoader loader) {
		this(loader.getServerData());
	}

	public static DungeonRoomDataSyncMessage decode(PacketBuffer buf) {
		final int dataCount = buf.readVarInt();
		final List<CompoundNBT> roomData = new ArrayList<>(dataCount);
		
		CompoundNBT bigData = null;
		try {
			bigData = CompressedStreamTools.readCompressed(new ByteBufInputStream(buf));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ListNBT list = bigData.getList("1", NBT.TAG_COMPOUND);
		
		for (int i = 0; i < list.size(); i++) {
			final CompoundNBT tag;
			//tag = buf.readCompoundTag();
			tag = list.getCompound(i);
			
			roomData.add(tag);
		}
		
		return new DungeonRoomDataSyncMessage(roomData);
	}

	public static void encode(DungeonRoomDataSyncMessage msg, PacketBuffer buf) {
		buf.writeVarInt(msg.roomData.size());
		CompoundNBT bigData = new CompoundNBT();
		ListNBT list = new ListNBT();
		for (CompoundNBT data : msg.roomData) {
			// Want to just do this, but this doesn't actually compress the NBT data so it ends up being MASSIVE (24MB)
			//buf.writeCompoundTag(data);
			list.add(data);
		}
		bigData.put("1", list);
		
		try {
			CompressedStreamTools.writeCompressed(bigData, new ByteBufOutputStream(buf));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
