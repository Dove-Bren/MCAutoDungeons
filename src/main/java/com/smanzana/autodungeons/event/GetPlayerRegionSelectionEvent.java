package com.smanzana.autodungeons.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Hacky event I'm making to EXPORT the concept of player region selection to other mods (NostrumMagica) so that I don't have to
 * add some one-off selection tool and rendering to this mod.
 * This mod means AutoDungeons wants a world selection from a player.
 */
public class GetPlayerRegionSelectionEvent extends PlayerEvent {

	protected BlockPos pos1;
	protected BlockPos pos2;
	
	public GetPlayerRegionSelectionEvent(Player player) {
		super(player);
	}

	public BlockPos getPos1() {
		return pos1;
	}

	public void setPos1(BlockPos pos1) {
		this.pos1 = pos1;
	}

	public BlockPos getPos2() {
		return pos2;
	}

	public void setPos2(BlockPos pos2) {
		this.pos2 = pos2;
	}
	
	public boolean hasRegion() {
		return pos1 != null && pos2 != null;
	}
	
}
