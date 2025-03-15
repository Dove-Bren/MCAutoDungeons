package com.smanzana.autodungeons.command;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.dungeon.DungeonRegistry;
import com.smanzana.autodungeons.world.dungeon.Dungeon;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Direction;

public class CommandSpawnDungeon {

	private static Random rand = null;
	
	public static final void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("spawndungeon")
					.requires(s -> s.hasPermission(2))
					.then(Commands.argument("type", StringArgumentType.string())
							.suggests(CommandSpawnDungeon::GetSuggestions)
							.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "type")))
							)
				);
	}
	
	private static final int execute(CommandContext<CommandSourceStack> context, final String typeName) throws CommandSyntaxException {
		
		Dungeon dungeon = null;
		for (Dungeon candidate : GetDungeons()) {
			final String name = candidate.getRegistryName().toString();
			if (name.equalsIgnoreCase(typeName)) {
				dungeon = candidate;
				break;
			}
		}
		
		if (dungeon == null) {
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create(); 
		} else {
			return execute(context, dungeon);
		}
	}

	private static final int execute(CommandContext<CommandSourceStack> context, final Dungeon dungeon) throws CommandSyntaxException {
		if (CommandSpawnDungeon.rand == null) {
			CommandSpawnDungeon.rand = new Random();
		}
		
		ServerPlayer player = context.getSource().getPlayerOrException();
		dungeon.spawn(player.level, new BlueprintLocation(player.blockPosition(), Direction.fromYRot(player.getYRot())));
		
		return 0;
	}
	
	private static final Collection<Dungeon> GetDungeons() {
		return DungeonRegistry.GetForgeRegistry().getValues();
	}
	
	private static final <S> CompletableFuture<Suggestions> GetSuggestions(CommandContext<S> ctx, SuggestionsBuilder sb) {
		return SharedSuggestionProvider.suggest(GetDungeons().stream().map(d -> d.getRegistryName().toString()), sb);
	}
}
