package com.smanzana.autodungeons.network;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.network.message.DungeonRoomDataSyncMessage;
import com.smanzana.autodungeons.network.message.DungeonTrackerUpdateMessage;
import com.smanzana.autodungeons.network.message.WorldKeySyncMessage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkHandler {

	private static SimpleChannel syncChannel;
	
	private static int discriminator = 10;
	
	private static final String CHANNEL_SYNC_NAME = "autodung_channel";
	private static final String PROTOCOL = "1";
	
	public static SimpleChannel getSyncChannel() {
		getInstance();
		return syncChannel;
	}
	
	private static NetworkHandler instance;
	
	public static NetworkHandler getInstance() {
		if (instance == null)
			instance = new NetworkHandler();
		
		return instance;
	}
	
	public NetworkHandler() {
		
		syncChannel = NetworkRegistry.newSimpleChannel(new ResourceLocation(AutoDungeons.MODID, CHANNEL_SYNC_NAME),
				() -> PROTOCOL,
				PROTOCOL::equals,
				PROTOCOL::equals
				);
		
		syncChannel.registerMessage(discriminator++, DungeonTrackerUpdateMessage.class, DungeonTrackerUpdateMessage::encode, DungeonTrackerUpdateMessage::decode, DungeonTrackerUpdateMessage::handle);
		syncChannel.registerMessage(discriminator++, DungeonRoomDataSyncMessage.class, DungeonRoomDataSyncMessage::encode, DungeonRoomDataSyncMessage::decode, DungeonRoomDataSyncMessage::handle);
		syncChannel.registerMessage(discriminator++, WorldKeySyncMessage.class, WorldKeySyncMessage::encode, WorldKeySyncMessage::decode, WorldKeySyncMessage::handle);
	}
	
	public static <T> void sendTo(T msg, ServerPlayerEntity player) {
		NetworkHandler.syncChannel.sendTo(msg, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
	}
	
	public static <T> void sendToServer(T msg) {
		NetworkHandler.syncChannel.sendToServer(msg);
	}

	public static <T> void sendToAll(T msg) {
		NetworkHandler.syncChannel.send(PacketDistributor.ALL.noArg(), msg);
	}

	public static <T> void sendToDimension(T msg, RegistryKey<World> dimension) {
		NetworkHandler.syncChannel.send(PacketDistributor.DIMENSION.with(() -> dimension), msg);
	}
	
	public static <T> void sendToAllAround(T msg, TargetPoint point) {
		NetworkHandler.syncChannel.send(PacketDistributor.NEAR.with(() -> point), msg);
	}

	public static <T> void sendToAllTracking(T msg, Entity ent) {
		NetworkHandler.syncChannel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> ent), msg);
	}

}
