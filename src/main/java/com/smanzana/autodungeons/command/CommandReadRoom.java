package com.smanzana.autodungeons.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.autodungeons.event.GetPlayerSelectionEvent;
import com.smanzana.autodungeons.world.blueprints.Blueprint;
import com.smanzana.autodungeons.world.blueprints.Blueprint.LoadContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.Direction;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.command.EnumArgument;

public class CommandReadRoom {
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("readroom")
					.requires(s -> s.hasPermissionLevel(2))
					.then(Commands.argument("name", StringArgumentType.string())
						.then(Commands.argument("direction", EnumArgument.enumArgument(Direction.class))
								.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "name"), ctx.getArgument("direction", Direction.class)))
								)
						.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "name"), Direction.EAST))
						)
				);
	}

	private static final int execute(CommandContext<CommandSource> context, final String name, final Direction facing) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		
		if (!player.isCreative()) {
			context.getSource().sendFeedback(new StringTextComponent("This command must be run as a creative player"), true);
			return 1;
		}
		
		// Try to get a player selection
		GetPlayerSelectionEvent event = new GetPlayerSelectionEvent(player);
		MinecraftForge.EVENT_BUS.post(event);
		
		if (event.getSelection() == null) {
			context.getSource().sendFeedback(new StringTextComponent("No selected position was obtained by any providing mods"), true);
			return 1;
		}
		
		File file = new File("./DungeonData/room_blueprint_captures/" + name + ".dat");
		if (!file.exists()) {
			file = new File("./DungeonData/room_blueprint_captures/" + name + ".gat");
		}
		if (file.exists()) {
			CompoundNBT nbt = null;
			try {
				if (file.getName().endsWith(".gat")) {
					nbt = CompressedStreamTools.readCompressed(new FileInputStream(file));
				} else {
					nbt = CompressedStreamTools.read(file);
				}
				context.getSource().sendFeedback(new StringTextComponent("Room read from " + file.getPath()), true);
			} catch (IOException e) {
				e.printStackTrace();
				
				System.out.println("Failed to read out serialized file " + file.toString());
				context.getSource().sendFeedback(new StringTextComponent("Failed to read room"), true);
			}
			
			if (nbt != null) {
				Blueprint blueprint = Blueprint.FromNBT(new LoadContext(file.getAbsolutePath()), (CompoundNBT) nbt.get("blueprint"));
				if (blueprint != null) {
					blueprint.spawn(player.world, event.getSelection(), facing, null, null);
				} else {
					context.getSource().sendFeedback(new StringTextComponent("Room failed to load"), true);
				}
			}
		} else {
			context.getSource().sendFeedback(new StringTextComponent("Room not found"), true);
		}
		
		return 0;
	}

}
