package com.smanzana.autodungeons.world.blueprints;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.block.IDirectionalBlock;
import com.smanzana.autodungeons.block.IEntryMarker;
import com.smanzana.autodungeons.block.IExitMarker;
import com.smanzana.autodungeons.block.IHorizontalBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class BlueprintBlock {
		
	private static Map<BlockState, BlueprintBlock> BLUEPRINT_CACHE = new HashMap<>();
	
	private static BlueprintBlock CHECK_BLUEPRINT_CACHE(BlockState state) {
		return BLUEPRINT_CACHE.get(state);
	}
	
	private static void SET_BLUEPRINT_CACHE(BlockState state, BlueprintBlock block) {
		BLUEPRINT_CACHE.put(state, block);
	}
	
	public static BlueprintBlock getBlueprintBlock(BlockState state, CompoundNBT teData) {
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
	private CompoundNBT tileEntityData;
	
	private BlueprintBlock(BlockState state, CompoundNBT teData) {
		this.state = state;
		this.tileEntityData = teData;
		
		// Refuse to store air
		if (state != null && state.getBlock() == Blocks.AIR) {
			state = null;
			tileEntityData = null;
		}
	}
	
	public BlueprintBlock(IWorld world, BlockPos pos) {
		if (world.isEmptyBlock(pos)) {
			; //leave null
		} else {
			this.state = world.getBlockState(pos);
			TileEntity te = world.getBlockEntity(pos);
			if (te != null) {
				this.tileEntityData = new CompoundNBT();
				te.save(this.tileEntityData);
			}
		}
	}
	
//	public static BlueprintBlock MakeFromData(BlockState state, CompoundNBT teData) {
//		return new BlueprintBlock(state, teData);
//	}
	
	public static BlueprintBlock Air = new BlueprintBlock((BlockState) null, null);
	
	private static final String NBT_TILE_ENTITY = "te_data";
	private static final String NBT_BLOCKSTATE_TAG = "blockstate";
	
	public static BlueprintBlock fromNBT(byte version, CompoundNBT nbt) {
		BlockState state = null;
		CompoundNBT teData = null;
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
			state = NBTUtil.readBlockState(nbt.getCompound(NBT_BLOCKSTATE_TAG));
			
			if (state != null && nbt.contains(NBT_TILE_ENTITY)) {
				teData = nbt.getCompound(NBT_TILE_ENTITY);
			}
			break;
		default:
			throw new RuntimeException("Blueprint block doesn't understand version " + version);
		}
		
		return BlueprintBlock.getBlueprintBlock(state, teData);
	}
	
	public CompoundNBT toNBT() {
		CompoundNBT tag = new CompoundNBT();
		
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
			tag.put(NBT_BLOCKSTATE_TAG, NBTUtil.writeBlockState(state));
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
				if (block instanceof HorizontalBlock) {
					Direction cur = placeState.getValue(HorizontalBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.setValue(HorizontalBlock.FACING, cur);
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
				} else if (block instanceof StairsBlock) {
					Direction cur = placeState.getValue(StairsBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.setValue(StairsBlock.FACING, cur);
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
	
	public CompoundNBT getTileEntityData() {
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
		} else if (block instanceof HorizontalBlock) {
			ret = state.getValue(HorizontalBlock.FACING);
		} else if (block instanceof WallTorchBlock) {
			ret = state.getValue(WallTorchBlock.FACING);
		} else if (block instanceof RedstoneWallTorchBlock) {
			ret = state.getValue(RedstoneWallTorchBlock.FACING);
		} else if (block instanceof LadderBlock) {
			ret = state.getValue(LadderBlock.FACING);
		} else if (block instanceof StairsBlock) {
			ret = state.getValue(StairsBlock.FACING);
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
	
	public @Nullable CompoundNBT getRawTileEntityData() {
		return this.tileEntityData;
	}
}
