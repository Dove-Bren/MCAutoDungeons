package com.smanzana.autodungeons.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Hacky event I'm making to EXPORT the concept of player-designated location.
 */
public class GetPlayerSelectionEvent extends PlayerEvent {

	protected BlockPos selection;
	
	public GetPlayerSelectionEvent(Player player) {
		super(player);
	}

	public BlockPos getSelection() {
		return selection;
	}

	public void setSelection(BlockPos selection) {
		this.selection = selection;
	}
}
