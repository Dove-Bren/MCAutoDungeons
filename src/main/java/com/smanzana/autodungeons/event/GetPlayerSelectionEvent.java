package com.smanzana.autodungeons.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Hacky event I'm making to EXPORT the concept of player-designated location.
 */
public class GetPlayerSelectionEvent extends PlayerEvent {

	protected BlockPos selection;
	
	public GetPlayerSelectionEvent(PlayerEntity player) {
		super(player);
	}

	public BlockPos getSelection() {
		return selection;
	}

	public void setSelection(BlockPos selection) {
		this.selection = selection;
	}
}
