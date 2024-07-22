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
import com.smanzana.autodungeons.world.dungeon.NostrumDungeon;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;

public class CommandSpawnDungeon {

	private static Random rand = null;
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("spawndungeon")
					.requires(s -> s.hasPermissionLevel(2))
					.then(Commands.argument("type", StringArgumentType.string())
							.suggests(CommandSpawnDungeon::GetSuggestions)
							.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "type")))
							)
				);
	}
	
	private static final int execute(CommandContext<CommandSource> context, final String typeName) throws CommandSyntaxException {
		
		NostrumDungeon dungeon = null;
		for (NostrumDungeon candidate : GetDungeons()) {
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

	private static final int execute(CommandContext<CommandSource> context, final NostrumDungeon dungeon) throws CommandSyntaxException {
		if (CommandSpawnDungeon.rand == null) {
			CommandSpawnDungeon.rand = new Random();
		}
		
		ServerPlayerEntity player = context.getSource().asPlayer();
		dungeon.spawn(player.world, new BlueprintLocation(player.getPosition(), Direction.fromAngle(player.rotationYaw)));
		
		return 0;
	}
	
	private static final Collection<NostrumDungeon> GetDungeons() {
		return DungeonRegistry.GetForgeRegistry().getValues();
	}
	
	private static final <S> CompletableFuture<Suggestions> GetSuggestions(CommandContext<S> ctx, SuggestionsBuilder sb) {
		return ISuggestionProvider.suggest(GetDungeons().stream().map(d -> d.getRegistryName().toString()), sb);
	}
}
