package com.smanzana.autodungeons.command;

import java.util.LinkedList;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.autodungeons.event.GetPlayerRegionSelectionEvent;
import com.smanzana.autodungeons.util.WorldUtil;
import com.smanzana.autodungeons.world.blueprints.Blueprint;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.dungeon.room.BlueprintDungeonRoom;
import com.smanzana.autodungeons.world.dungeon.room.DungeonRoomLoader;

import net.minecraft.block.BlockState;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;

public class CommandWriteRoom {
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("writeroom")
					.requires(s -> s.hasPermission(2))
					.then(Commands.argument("name", StringArgumentType.greedyString())
						.then(Commands.argument("weight", IntegerArgumentType.integer(1))
							.then(Commands.argument("cost", IntegerArgumentType.integer(1))
								.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "name"), IntegerArgumentType.getInteger(ctx, "weight"), IntegerArgumentType.getInteger(ctx, "cost")))
								)
							.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "name"), IntegerArgumentType.getInteger(ctx, "weight")))
							)
						.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "name")))
						)
				);
	}
	
	private static final int execute(CommandContext<CommandSource> context, final String name) throws CommandSyntaxException {
		return execute(context, name, 1);
	}
	
	private static final int execute(CommandContext<CommandSource> context, final String name, final int weight) throws CommandSyntaxException {
		return execute(context, name, weight, 1);
	}
	
	private static final int execute(CommandContext<CommandSource> context, final String name, final int weight, final int cost) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrException();
		
		if (!player.isCreative()) {
			context.getSource().sendSuccess(new StringTextComponent("This command must be run as a creative player"), true);
			return 1;
		}
		
		// Try to get a player selection
		GetPlayerRegionSelectionEvent event = new GetPlayerRegionSelectionEvent(player);
		MinecraftForge.EVENT_BUS.post(event);
		
		if (!event.hasRegion()) {
			context.getSource().sendSuccess(new StringTextComponent("No selected region was obtained by any providing mods"), true);
			return 1;
		}
		
		final BlockPos pos1 = event.getPos1();
		final BlockPos pos2 = event.getPos2();
		final BlockPos minPos = new BlockPos(Math.min(pos1.getX(), pos2.getX()),
				Math.min(pos1.getY(), pos2.getY()),
				Math.min(pos1.getZ(), pos2.getZ()));
		final BlockPos maxPos = new BlockPos(Math.max(pos1.getX(), pos2.getX()),
				Math.max(pos1.getY(), pos2.getY()),
				Math.max(pos1.getZ(), pos2.getZ()));
		BlueprintLocation[] foundEntry = {null};
		
		// Look for entry marker
		WorldUtil.ScanBlocks(player.level, minPos, maxPos, (world, pos) -> {
			BlockState state = world.getBlockState(pos);
			if (BlueprintDungeonRoom.IsEntry(state)) {
				foundEntry[0] = new BlueprintLocation(pos.immutable().subtract(minPos), state.getValue(ComparatorBlock.FACING).getOpposite());
				return false;
			}
			
			return true;
		});
		
		Blueprint blueprint = Blueprint.Capture(player.level,
				minPos, maxPos,
				foundEntry[0]);
		
		if (DungeonRoomLoader.instance().writeRoomAsFile(blueprint, name, weight, cost, new LinkedList<>())) {
			context.getSource().sendSuccess(new StringTextComponent("Room written!"), true);
		} else {
			context.getSource().sendSuccess(new StringTextComponent("An error was encountered while writing the room"), true);
		}
		
		return 0;
	}
}
