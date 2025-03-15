package com.smanzana.autodungeons.world.gen;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.util.DimensionUtils;
import com.smanzana.autodungeons.util.WorldUtil;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.dungeon.Dungeon;
import com.smanzana.autodungeons.world.dungeon.DungeonInstance;
import com.smanzana.autodungeons.world.dungeon.DungeonRecord;
import com.smanzana.autodungeons.world.dungeon.DungeonRoomInstance;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class DungeonStructure extends StructureFeature<NoneFeatureConfiguration> {
	
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
		super(NoneFeatureConfiguration.CODEC);
		this.dungeon = dungeon;
	}
	
	@Override
	protected boolean /*hasStartAt*/ isFeatureChunk(ChunkGenerator generator, BiomeSource biomeProvider, long seed, WorldgenRandom rand, ChunkPos pos, Biome biome, ChunkPos posCopy, NoneFeatureConfiguration config, LevelHeightAccessor height) {
		return super.isFeatureChunk(generator, biomeProvider, seed, rand, pos, biome, pos, config, height);
	}

	@Override
	public StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return (StructureFeature<NoneFeatureConfiguration> parent, ChunkPos pos, int i3, long l1)
				-> {
					return new Start(this.getDungeon(), parent, pos, i3, l1);
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
	public GenerationStep.Decoration step() {
		return GenerationStep.Decoration.SURFACE_STRUCTURES;
	}
	
	protected static final WorldgenRandom MakeRandom(ChunkPos pos, long seed) {
		// Copied from vanilla structure starts which all do this to make their random
		WorldgenRandom random = new WorldgenRandom();
		random.setLargeFeatureSeed(seed, pos.x, pos.z);
		return random;
	}
	
	public static final @Nullable DungeonRecord GetDungeonAt(ServerLevel world, BlockPos at, DungeonStructure structure) {
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
	
	public static final @Nullable DungeonRecord GetDungeonAt(ServerLevel world, BlockPos at) {
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
	public static class Start extends StructureStart<NoneFeatureConfiguration> {
		
		private final Dungeon dungeon;
		private final DungeonInstance instance;
		
		//private static final String NBT_INSTNACE = "dungeonInstance";
		
		public Start(Dungeon dungeon, StructureFeature<NoneFeatureConfiguration> parent, ChunkPos pos, int i3, long l1) {
			super(parent, pos, i3, l1);
			this.dungeon = dungeon;
			this.instance = DungeonInstance.Random(dungeon, MakeRandom(pos, l1));
		}

		@Override
		public void /*init*/ generatePieces(RegistryAccess registries, ChunkGenerator generator, StructureManager templateManagerIn, ChunkPos pos, Biome biomeIn, NoneFeatureConfiguration config, LevelHeightAccessor height) {
			// Pick random Y between 30 and 60 to start
			final int y = this.random.nextInt(30) + 30;
			// Center in chunk to try and avoid some 'CanSpawnHere' chunk spillage
			final int x = (pos.x * 16) + 8;
			final int z = (pos.z * 16) + 8;
			
			final BlueprintLocation start = new BlueprintLocation(new BlockPos(x, y, z), Direction.Plane.HORIZONTAL.getRandomDirection(this.random));
			List<DungeonRoomInstance> instances = this.dungeon.generate((type, cx, cz) -> generator.getBaseHeight(cx, cz, type, height), start, this.instance);
			
			for (DungeonRoomInstance instance : instances) {
				//pieces.add(new DungeonPiece(instance));
				this.addPiece(new DungeonPiece(instance));
			}
			
			//this.calculateBoundingBox();
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
			super(DungeonPieceSerializer.instance, 0, instance.getBounds());
			this.instance = instance;
		}
		
		@Override
		protected void addAdditionalSaveData(ServerLevel world, CompoundTag tagCompound) { // Note: Actually "WRITE" !!!
			DungeonPieceSerializer.write(this, tagCompound);
		}

		@Override
		public boolean /*addComponentParts*/ postProcess(WorldGenLevel worldIn, StructureFeatureManager manager, ChunkGenerator chunkGen,
				Random randomIn, BoundingBox structureBoundingBoxIn,
				ChunkPos chunkPosIn, BlockPos something) {
			
			// Stop gap: is this the overworld?
			if (!DimensionUtils.IsOverworld(worldIn.getLevel())) {
				return false;
			}
			
			instance.spawn(worldIn, structureBoundingBoxIn);
			return true;
		}
		
	}
	
	public static class DungeonPieceSerializer implements StructurePieceType {
		
		public static final String PIECE_ID = "autodungeon:dungeonpiecedynamic";
		public static final DungeonPieceSerializer instance = new DungeonPieceSerializer();
		
		private static final String NBT_DATA = "autodungeondata";

		@Override
		public DungeonPiece load(ServerLevel level, CompoundTag tag) {
			final CompoundTag subTag = tag.getCompound(NBT_DATA);
			DungeonRoomInstance instance = DungeonRoomInstance.fromNBT(subTag);
			return new DungeonPiece(instance);
		}
		
		public static void write(DungeonPiece piece, CompoundTag tagCompound) {
			tagCompound.put(NBT_DATA, piece.instance.toNBT(null));
		}
		
	}
}
