package com.smanzana.autodungeons.world.blueprints;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.block.IDirectionalBlock;
import com.smanzana.autodungeons.block.IEntryMarker;
import com.smanzana.autodungeons.block.IExitMarker;
import com.smanzana.autodungeons.block.IHorizontalBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public class BlueprintBlock {
		
	private static Map<BlockState, BlueprintBlock> BLUEPRINT_CACHE = new HashMap<>();
	
	private static BlueprintBlock CHECK_BLUEPRINT_CACHE(BlockState state) {
		return BLUEPRINT_CACHE.get(state);
	}
	
	private static void SET_BLUEPRINT_CACHE(BlockState state, BlueprintBlock block) {
		BLUEPRINT_CACHE.put(state, block);
	}
	
	public static BlueprintBlock getBlueprintBlock(BlockState state, CompoundTag teData) {
		BlueprintBlock block = null;
		if (teData == null) {
			block = CHECK_BLUEPRINT_CACHE(state);
		}
		
		if (block == null) {
			if (state == null || state.getBlock() == Blocks.AIR) {
				block = Air;
			} else {
				block = new BlueprintBlock(state, teData);
			}
			if (teData == null) {
				SET_BLUEPRINT_CACHE(state, block);
			}
		}
		
		return block;
	}
	
	private BlockState state;
	private CompoundTag tileEntityData;
	
	private BlueprintBlock(BlockState state, CompoundTag teData) {
		this.state = state;
		this.tileEntityData = teData;
		
		// Refuse to store air
		if (state != null && state.getBlock() == Blocks.AIR) {
			state = null;
			tileEntityData = null;
		}
	}
	
	public BlueprintBlock(LevelAccessor world, BlockPos pos) {
		if (world.isEmptyBlock(pos)) {
			; //leave null
		} else {
			this.state = world.getBlockState(pos);
			BlockEntity te = world.getBlockEntity(pos);
			if (te != null) {
				this.tileEntityData = te.saveWithId();
			}
		}
	}
	
//	public static BlueprintBlock MakeFromData(BlockState state, CompoundNBT teData) {
//		return new BlueprintBlock(state, teData);
//	}
	
	public static BlueprintBlock Air = new BlueprintBlock((BlockState) null, null);
	
	private static final String NBT_TILE_ENTITY = "te_data";
	private static final String NBT_BLOCKSTATE_TAG = "blockstate";
	
	public static BlueprintBlock fromNBT(byte version, CompoundTag nbt) {
		BlockState state = null;
		CompoundTag teData = null;
		switch (version) {
		case 0:
//				state = Block.getStateById(nbt.getInt(NBT_BLOCK));
//				
//				// Block.getStateById defaults to air. Remove it!
//				if (state != null && state.getBlock() == Blocks.AIR) {
//					state = null;
//				}
//				
//				if (state != null && nbt.contains(NBT_TILE_ENTITY)) {
//					teData = nbt.getCompound(NBT_TILE_ENTITY);
//				}
//				break;
			// was int id for blockstate; deprecated (and not minecraft save portable)
			throw new RuntimeException("Blueprint block doesn't understand version " + version);
		case 1:
			// I don't remember what this version was
			throw new RuntimeException("Blueprint block doesn't understand version " + version);
		case 2:
			// Was block name + (int) meta saving.
			// No more meta, so this doesn't work anymore
			throw new RuntimeException("Blueprint block doesn't understand version " + version);
		case 3:
			state = NbtUtils.readBlockState(nbt.getCompound(NBT_BLOCKSTATE_TAG));
			
			if (state != null && nbt.contains(NBT_TILE_ENTITY)) {
				teData = nbt.getCompound(NBT_TILE_ENTITY);
			}
			break;
		default:
			throw new RuntimeException("Blueprint block doesn't understand version " + version);
		}
		
		return BlueprintBlock.getBlueprintBlock(state, teData);
	}
	
	public CompoundTag toNBT() {
		CompoundTag tag = new CompoundTag();
		
		// Version 0
//			if (state != null) {
//				tag.putInt(NBT_BLOCK, Block.getStateId(state));
//				if (tileEntityData != null) {
//					tag.put(NBT_TILE_ENTITY, tileEntityData);
//				}
//			}
		
//			// Version 2
//			if (state != null) {
//				tag.putString(NBT_BLOCK_TYPE, state.getBlock().getRegistryName().toString());
//				tag.putInt(NBT_BLOCK_STATE, state.getBlock().getMetaFromState(state));
//				if (tileEntityData != null) {
//					tag.put(NBT_TILE_ENTITY, tileEntityData);
//				}
//			}
		
		// Version 3
		if (state != null) {
			tag.put(NBT_BLOCKSTATE_TAG, NbtUtils.writeBlockState(state));
			if (tileEntityData != null) {
				tag.put(NBT_TILE_ENTITY, tileEntityData);
			}
		}
		
		
		return tag;
	}
	
	private static Direction rotate(Direction in, Direction mod) {
		if (in != Direction.UP && in != Direction.DOWN) {
			int count = mod.getOpposite().get2DDataValue();
			while (count > 0) {
				count--;
				in = in.getClockWise();
			}
		}
		
		return in;
	}
	
	public BlockState getSpawnState(Direction facing) {
		if (state != null) {
			BlockState placeState = state;
			
			if (facing != null && facing.getOpposite().get2DDataValue() != 0) {
				
				Block block = placeState.getBlock();
				if (block instanceof HorizontalDirectionalBlock) {
					Direction cur = placeState.getValue(HorizontalDirectionalBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.setValue(HorizontalDirectionalBlock.FACING, cur);
				} else if (block instanceof WallTorchBlock) {
					Direction cur = placeState.getValue(WallTorchBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.setValue(WallTorchBlock.FACING, cur);
				} else if (block instanceof RedstoneWallTorchBlock) {
					Direction cur = placeState.getValue(RedstoneWallTorchBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.setValue(RedstoneWallTorchBlock.FACING, cur);
				} else if (block instanceof LadderBlock) {
					Direction cur = placeState.getValue(LadderBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.setValue(LadderBlock.FACING, cur);
				} else if (block instanceof StairBlock) {
					Direction cur = placeState.getValue(StairBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.setValue(StairBlock.FACING, cur);
				} else if (block instanceof DirectionalBlock) {
					// Only want to rotate horizontally
					Direction cur = placeState.getValue(DirectionalBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.setValue(DirectionalBlock.FACING, cur);
				} else if (block instanceof IHorizontalBlock) {
					// Only want to rotate horizontally
					Direction cur = placeState.getValue(IHorizontalBlock.HORIZONTAL_FACING);
					cur = rotate(cur, facing);
					placeState = placeState.setValue(IHorizontalBlock.HORIZONTAL_FACING, cur);
				} else if (block instanceof IDirectionalBlock) {
					// Only want to rotate horizontally
					Direction cur = placeState.getValue(IDirectionalBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.setValue(IDirectionalBlock.FACING, cur);
				} else if (block instanceof ChestBlock) {
					// Doesn't implement directional interfaces
					// Only want to rotate horizontally
					Direction cur = placeState.getValue(ChestBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.setValue(ChestBlock.FACING, cur);
				}
			}
			
			return placeState;
		} else {
			return null;
		}
	}
	
	public CompoundTag getTileEntityData() {
		return tileEntityData;
	}
	
	public Direction getFacing() {
		Direction ret = null;
		Block block = state.getBlock();
		if (block instanceof IEntryMarker) {
			ret = ((IEntryMarker) block).getFacing(state);
			// HACK: Reverse if special enterance block cause they're backwards LOL
			ret = ret.getOpposite();
		} else if (block instanceof IExitMarker) {
			ret = ((IExitMarker) block).getFacing(state);
			// HACK: Reverse if special exit block cause they're backwards LOL
			ret = ret.getOpposite();
		} else if (block instanceof HorizontalDirectionalBlock) {
			ret = state.getValue(HorizontalDirectionalBlock.FACING);
		} else if (block instanceof WallTorchBlock) {
			ret = state.getValue(WallTorchBlock.FACING);
		} else if (block instanceof RedstoneWallTorchBlock) {
			ret = state.getValue(RedstoneWallTorchBlock.FACING);
		} else if (block instanceof LadderBlock) {
			ret = state.getValue(LadderBlock.FACING);
		} else if (block instanceof StairBlock) {
			ret = state.getValue(StairBlock.FACING);
		} else if (block instanceof DirectionalBlock) {
			ret = state.getValue(DirectionalBlock.FACING);
		} else if (block instanceof IDirectionalBlock) {
			ret = state.getValue(IDirectionalBlock.FACING);
		} else if (block instanceof ChestBlock) {
			ret = state.getValue(ChestBlock.FACING);
		}
		return ret;
	}
	
	public BlockState getState() {
		return this.state;
	}
	
	public @Nullable CompoundTag getRawTileEntityData() {
		return this.tileEntityData;
	}
}
