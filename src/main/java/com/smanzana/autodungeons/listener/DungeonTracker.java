package com.smanzana.autodungeons.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.network.NetworkHandler;
import com.smanzana.autodungeons.network.message.DungeonTrackerUpdateMessage;
import com.smanzana.autodungeons.world.dungeon.DungeonRecord;
import com.smanzana.autodungeons.world.gen.DungeonStructure;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.LogicalSidedProvider;

/**
 * Track which dungeon a player is in. Synchronized between server and client.
 * @author Skyler
 *
 */
public class DungeonTracker {
	
	private final Map<Player, DungeonRecord> dungeonMap;
	
	public DungeonTracker() {
		this.dungeonMap = new HashMap<>();
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public DungeonRecord getDungeon(Player player) {
		return dungeonMap.get(player);
	}
	
	protected void setDungeon(Player player, @Nullable DungeonRecord record) {
		DungeonRecord prev = this.dungeonMap.put(player, record);
		if ((prev == null || !Objects.equals(prev, record)) && !player.getCommandSenderWorld().isClientSide()) {
			notifyPlayer((ServerPlayer) player);
		}
	}
	
	protected void notifyPlayer(ServerPlayer player) {
		@Nullable DungeonRecord record = this.getDungeon(player);
		NetworkHandler.sendTo(new DungeonTrackerUpdateMessage(player.getUUID(), record), player);
	}
	
	public void overrideClientDungeon(Player player, @Nullable DungeonRecord record) {
		if (!AutoDungeons.GetProxy().hasIntegratedServer()) {
			this.setDungeon(player, record);
		}
	}
	
	protected void updatePlayer(ServerPlayer player) {
		final @Nullable DungeonRecord current;
		if (!player.isAlive()) {
			current = null;
		} else {
			current = DungeonStructure.GetDungeonAt(player.getLevel(), player.blockPosition());
		}
		setDungeon(player, current);
	}
	
	@SubscribeEvent
	public void serverTick(ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			for (ServerLevel world : LogicalSidedProvider.INSTANCE.<MinecraftServer>get(LogicalSide.SERVER).getAllLevels()) {
				if (world.players().isEmpty()) {
					continue;
				}
				
				for (ServerPlayer player : world.players()) {
					updatePlayer(player);
				}
			}
		}
	}
	
	public static class Client extends DungeonTracker {
	
		public Client() {
			super();
		}
		
		@SubscribeEvent
		public void clientTick(ClientTickEvent event) {
			if (event.phase == TickEvent.Phase.END) {
				Player player = AutoDungeons.GetProxy().getPlayer();
				if (player != null) {
					DungeonRecord record = getDungeon(player);
					if (record != null) {
						record.structure.getDungeon().clientTick(player.getCommandSenderWorld(), player);
					}
				}
			}
		}
		
		@SubscribeEvent
		public void onFogDensityCheck(EntityViewRenderEvent.FogDensity event) {
			Player player = AutoDungeons.GetProxy().getPlayer();
			if (player != null) {
				DungeonRecord record = getDungeon(player);
				if (record != null) {
					record.structure.getDungeon().setClientFogDensity(player.level, player, event);
				}
			}
		}
		
		@SubscribeEvent
		public void onFogColorCheck(EntityViewRenderEvent.FogColors event) {
			Player player = AutoDungeons.GetProxy().getPlayer();
			if (player != null) {
				DungeonRecord record = getDungeon(player);
				if (record != null) {
					record.structure.getDungeon().setClientFogColor(player.level, player, event);
				}
			}
		}
	}
}
