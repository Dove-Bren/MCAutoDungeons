package com.smanzana.autodungeons.world.gen;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.util.DimensionUtils;
import com.smanzana.autodungeons.util.WorldUtil;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.dungeon.DungeonRecord;
import com.smanzana.autodungeons.world.dungeon.DungeonRoomInstance;
import com.smanzana.autodungeons.world.dungeon.Dungeon;
import com.smanzana.autodungeons.world.dungeon.DungeonInstance;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class DungeonStructure extends Structure<NoFeatureConfig> {
	
//	public static final class NostrumDungeonConfig implements IFeatureConfig {
//		
////		public static final Codec<EmptyChunkGen> CODEC = RecordCodecBuilder.create(instance -> instance.group( 
////				RegistryLookupCodec.getLookUpCodec(Registry.BIOME_KEY).forGetter(EmptyChunkGen::getBiomeRegistry),
////				ResourceLocation.CODEC.xmap(s -> RegistryKey.getOrCreateKey(Registry.BIOME_KEY, s), k -> k.getLocation()).fieldOf("biome").forGetter(EmptyChunkGen::getBiome)
////			).apply(instance, EmptyChunkGen::new));
//		
//		public static Codec<NostrumDungeonConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
//				Codec.INT.optionalFieldOf("dummy", 1).forGetter(NostrumDungeonConfig::getDummy)
//			).apply(instance, NostrumDungeonConfig::new));
//
//		public NostrumDungeonConfig(int dummy) {
//			;
//		}
//		
//		public int getDummy() {
//			return 1;
//		}
//
//		public boolean allowedInDimension(RegistryKey<World> dimension) {
//			return DimensionUtils.IsOverworld(dimension);
//		}
//		
//		public boolean allowedAtPos(IWorld world, BlockPos pos) {
//			return true;
//		}
//		
//	}
	
	protected final Dungeon dungeon;
	
	public DungeonStructure(Dungeon dungeon) {
		super(NoFeatureConfig.field_236558_a_);
		this.dungeon = dungeon;
	}
	
	@Override
	protected boolean /*hasStartAt*/ func_230363_a_(ChunkGenerator generator, BiomeProvider biomeProvider, long seed, SharedSeedRandom rand, int x, int z, Biome biome, ChunkPos pos, NoFeatureConfig config) {
		return super.func_230363_a_(generator, biomeProvider, seed, rand, x, z, biome, pos, config);
	}

	@Override
	public IStartFactory<NoFeatureConfig> getStartFactory() {
		return (Structure<NoFeatureConfig> parent, int i1, int i2, MutableBoundingBox bounds, int i3, long l1)
				-> {
					return new Start(this.getDungeon(), parent, i1, i2, bounds, i3, l1);
				};
	}
	
	public Dungeon getDungeon() {
		return this.dungeon;
	}
	
//	@Override
//	public int getSize() {
//		return 8;
//	}

	/**
	 * Generation stage for when to generate the structure. there are 10 stages you can pick from!
	 * This surface structure stage places the structure before plants and ores are generated.
	 */
	@Override
	public GenerationStage.Decoration getDecorationStage() {
		return GenerationStage.Decoration.SURFACE_STRUCTURES;
	}
	
	protected static final SharedSeedRandom MakeRandom(int x, int z, long seed) {
		// Copied from vanilla structure starts which all do this to make their random
		SharedSeedRandom random = new SharedSeedRandom();
		random.setLargeFeatureSeed(seed, x, z);
		return random;
	}
	
	public static final @Nullable DungeonRecord GetDungeonAt(ServerWorld world, BlockPos at, DungeonStructure structure) {
		// Would like to consider using GetContainingStructure() and use the start, but the start can't carry instance info through a write/read.
		StructurePiece piece = WorldUtil.GetContainingStructurePiece(world, at, structure, true);
		if (piece != null && piece instanceof DungeonPiece) {
			DungeonPiece dungeonPiece = ((DungeonPiece) piece);
			return new DungeonRecord(structure, dungeonPiece.instance.getDungeonInstance(), dungeonPiece.instance);
		}
		return null;
	}
	
	// structures aren't set up by the first time this is set up.
	//private static final NostrumDungeonStructure[] TYPES = {NostrumStructures.DUNGEON_PORTAL, NostrumStructures.DUNGEON_DRAGON, NostrumStructures.DUNGEON_PLANTBOSS};
	
	public static final @Nullable DungeonRecord GetDungeonAt(ServerWorld world, BlockPos at) {
		List<DungeonStructure> dungeonStructures = ForgeRegistries.STRUCTURE_FEATURES.getValues().stream().filter(s -> s instanceof DungeonStructure).map(s -> (DungeonStructure) s).collect(Collectors.toList());
		for (DungeonStructure structure : dungeonStructures) {
			@Nullable DungeonRecord record = GetDungeonAt(world, at, structure);
			if (record != null) {
				return record;
			}
		}
		return null;
	}
	
	// What is basically an 'instance' of the struct in MC gen. Doesn't have to do much besides generate logical dungeon and populate children list.
	public static class Start extends StructureStart<NoFeatureConfig> {
		
		private final Dungeon dungeon;
		private final DungeonInstance instance;
		
		//private static final String NBT_INSTNACE = "dungeonInstance";
		
		public Start(Dungeon dungeon, Structure<NoFeatureConfig> parent, int i1, int i2, MutableBoundingBox bounds, int i3, long l1) {
			super(parent, i1, i2, bounds, i3, l1);
			this.dungeon = dungeon;
			this.instance = DungeonInstance.Random(dungeon, MakeRandom(i1, i2, l1));
		}

		@Override
		public void /*init*/ func_230364_a_(DynamicRegistries registries, ChunkGenerator generator, TemplateManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn, NoFeatureConfig config) {
			// Pick random Y between 30 and 60 to start
			final int y = this.rand.nextInt(30) + 30;
			// Center in chunk to try and avoid some 'CanSpawnHere' chunk spillage
			final int x = (chunkX * 16) + 8;
			final int z = (chunkZ * 16) + 8;
			
			final BlueprintLocation start = new BlueprintLocation(new BlockPos(x, y, z), Direction.Plane.HORIZONTAL.random(this.rand));
			List<DungeonRoomInstance> instances = this.dungeon.generate((type, cx, cz) -> generator.getHeight(cx, cz, type), start, this.instance);
			
			for (DungeonRoomInstance instance : instances) {
				components.add(new DungeonPiece(instance));
			}
			
			this.recalculateStructureSize();
		}
		
		// In 1.16, I can override this but can't override a READ anywhere that is effective.
		// So there's no way for a deserialized start to know any extra info.
//		@Override
//		public CompoundNBT write(int chunkX, int chunkZ) {
//			CompoundNBT base = super.write(chunkX, chunkZ);
//			base.put(NBT_INSTNACE, this.instance.toNBT());
//			// Don't need to write actual dungeon reference
//			return base;
//		}
	}
	
	public static class DungeonPiece extends StructurePiece {
		
		protected final DungeonRoomInstance instance;
		
		public DungeonPiece(DungeonRoomInstance instance) {
			super(DungeonPieceSerializer.instance, 0);
			this.instance = instance;
			
			this.boundingBox = instance.getBounds();
		}
		
		@Override
		protected void readAdditional(CompoundNBT tagCompound) { // Note: Actually "WRITE" !!!
			DungeonPieceSerializer.write(this, tagCompound);
		}

		@Override
		public boolean /*addComponentParts*/ func_230383_a_(ISeedReader worldIn, StructureManager manager, ChunkGenerator chunkGen,
				Random randomIn, MutableBoundingBox structureBoundingBoxIn,
				ChunkPos chunkPosIn, BlockPos something) {
			
			// Stop gap: is this the overworld?
			if (!DimensionUtils.IsOverworld(worldIn.getWorld())) {
				return false;
			}
			
			instance.spawn(worldIn, structureBoundingBoxIn);
			return true;
		}
		
	}
	
	public static class DungeonPieceSerializer implements IStructurePieceType {
		
		public static final String PIECE_ID = "autodungeon:dungeonpiecedynamic";
		public static final DungeonPieceSerializer instance = new DungeonPieceSerializer();
		
		private static final String NBT_DATA = "autodungeondata";

		@Override
		public DungeonPiece load(TemplateManager templateManager, CompoundNBT tag) {
			final CompoundNBT subTag = tag.getCompound(NBT_DATA);
			DungeonRoomInstance instance = DungeonRoomInstance.fromNBT(subTag);
			return new DungeonPiece(instance);
		}
		
		public static void write(DungeonPiece piece, CompoundNBT tagCompound) {
			tagCompound.put(NBT_DATA, piece.instance.toNBT(null));
		}
		
	}
}
