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

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.command.EnumArgument;

public class CommandReadRoom {
	
	public static final void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("readroom")
					.requires(s -> s.hasPermission(2))
					.then(Commands.argument("name", StringArgumentType.string())
						.then(Commands.argument("direction", EnumArgument.enumArgument(Direction.class))
								.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "name"), ctx.getArgument("direction", Direction.class)))
								)
						.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "name"), Direction.EAST))
						)
				);
	}

	private static final int execute(CommandContext<CommandSourceStack> context, final String name, final Direction facing) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		
		if (!player.isCreative()) {
			context.getSource().sendSuccess(new TextComponent("This command must be run as a creative player"), true);
			return 1;
		}
		
		// Try to get a player selection
		GetPlayerSelectionEvent event = new GetPlayerSelectionEvent(player);
		MinecraftForge.EVENT_BUS.post(event);
		
		if (event.getSelection() == null) {
			context.getSource().sendSuccess(new TextComponent("No selected position was obtained by any providing mods"), true);
			return 1;
		}
		
		File file = new File("./DungeonData/room_blueprint_captures/" + name + ".dat");
		if (!file.exists()) {
			file = new File("./DungeonData/room_blueprint_captures/" + name + ".gat");
		}
		if (file.exists()) {
			CompoundTag nbt = null;
			try {
				if (file.getName().endsWith(".gat")) {
					nbt = NbtIo.readCompressed(new FileInputStream(file));
				} else {
					nbt = NbtIo.read(file);
				}
				context.getSource().sendSuccess(new TextComponent("Room read from " + file.getPath()), true);
			} catch (IOException e) {
				e.printStackTrace();
				
				System.out.println("Failed to read out serialized file " + file.toString());
				context.getSource().sendSuccess(new TextComponent("Failed to read room"), true);
			}
			
			if (nbt != null) {
				Blueprint blueprint = Blueprint.FromNBT(new LoadContext(file.getAbsolutePath()), (CompoundTag) nbt.get("blueprint"));
				if (blueprint != null) {
					blueprint.spawn(player.level, event.getSelection(), facing, null, null);
					
					//TODO test code
//					if (AutoDungeons.GetProxy() instanceof ClientProxy) {
//						((ClientProxy) AutoDungeons.GetProxy()).getBlueprintRenderer().forceBlueprintPreview(blueprint);
//					}
				} else {
					context.getSource().sendSuccess(new TextComponent("Room failed to load"), true);
				}
			}
		} else {
			context.getSource().sendSuccess(new TextComponent("Room not found"), true);
		}
		
		return 0;
	}

}
