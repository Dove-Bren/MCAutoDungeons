package com.smanzana.autodungeons.client.render;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.smanzana.autodungeons.item.IBlueprintHolder;
import com.smanzana.autodungeons.world.blueprints.BlueprintBlock;
import com.smanzana.autodungeons.world.blueprints.IBlueprint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BlueprintRenderer {
	
	private IBlueprint cachedBlueprint = null;
	private VertexBuffer cachedRenderList = new VertexBuffer();
	private boolean cachedRenderDirty = true;
	
	private @Nullable IBlueprint forcedBlueprint = null;
	
	public BlueprintRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void forceBlueprintPreview(@Nullable IBlueprint blueprint) {
		this.forcedBlueprint = blueprint;
	}
	
	@SubscribeEvent
	public void onHighlight(DrawSelectionEvent.HighlightBlock event) {
		if (event.getTarget().getType() == HitResult.Type.BLOCK) {
			Minecraft mc = Minecraft.getInstance();
			LocalPlayer player = mc.player;
			final BlockHitResult blockResult = (BlockHitResult) event.getTarget();
			final BlockPos blockPos = blockResult.getBlockPos().relative(blockResult.getDirection());
			IBlueprint blueprint = this.forcedBlueprint;
			if (blueprint == null) {
				final ItemStack blueprintStack = this.findStackToRender(player, blockPos);
				if (!blueprintStack.isEmpty()) {
					blueprint = ((IBlueprintHolder) blueprintStack.getItem()).getBlueprint(player, blueprintStack, blockPos);
					
					
				}
			}
			
			if (blueprint != null) {
				if (cachedBlueprint != blueprint) {
					cachedBlueprint = blueprint;
					cachedRenderDirty = true;
				}
				
				if (cachedBlueprint != null) {
					final PoseStack matrixStackIn = event.getMatrix();
					Matrix4f proj = RenderSystem.getProjectionMatrix();
					final MultiBufferSource.BufferSource bufferIn = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
					Vec3 center = event.getTarget().getLocation();
					Direction face = Direction.getNearest((float) (center.x - player.getX()), 0f, (float) (center.z - player.getZ()));
					
					// Template is saved with some starting rotation. We want to render it such that the entry rotation is {face}.
					// The preview is with no rotation AKA if it was spawned with the same rotation is was catured with.
					// To render WITH the desired entry rotation, we have to figure out the diff and render with that.
					face = IBlueprint.GetModDir(cachedBlueprint.getEntry().getFacing(), face);
					
					renderBlueprintPreview(matrixStackIn, proj, bufferIn, blockPos, cachedBlueprint.getPreview(), face, event.getPartialTicks());
					bufferIn.endBatch();
				}
			}
		}
	}
	
	protected ItemStack findStackToRender(Player player, BlockPos pos) {
		ItemStack ret = ItemStack.EMPTY;
		for (ItemStack held : player.getHandSlots()) {
			if (!held.isEmpty() && held.getItem() instanceof IBlueprintHolder) {
				IBlueprintHolder holder = (IBlueprintHolder) held.getItem();
				if (holder.hasBlueprint(player, held) && holder.shouldDisplayBlueprint(player, held, pos)) {
					ret = held;
					break;
				}
			}
		}
		return ret;
	}
	
	private void renderBlueprintPreview(PoseStack matrixStackIn, Matrix4f projection, MultiBufferSource bufferIn, BlockPos center, BlueprintBlock[][][] preview, Direction rotation, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		Vec3 playerPos = mc.gameRenderer.getMainCamera().getPosition();//player.getEyePosition(partialTicks).subtract(0, player.getEyeHeight(), 0);
		Vec3 offset = new Vec3(center.getX() - playerPos.x,
				center.getY() - playerPos.y,
				center.getZ() - playerPos.z);
		
		// Compile drawlist if not present
		if (cachedRenderDirty) {
			cachedRenderDirty = false;
			//cachedRenderList.reset(); Reset by doing new upload
			
			final int width = preview.length;
			final int height = preview[0].length;
			final int depth = preview[0][0].length;
			BufferBuilder buffer = new BufferBuilder(4096);
			
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
			
			for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
			for (int z = 0; z < depth; z++) {
				final BlueprintBlock block = preview[x][y][z];
				if (block == null) {
					continue;
				}
				
				final int xOff = x - (width/2);
				final int yOff = y - 1;
				final int zOff = z - (depth/2);
				
				BlockState state = block.getSpawnState(Direction.NORTH);
				
				if (state == null || state.getBlock() == Blocks.AIR) {
					continue;
				}
				
				BakedModel model = null;
				if (state != null) {
					model = mc.getBlockRenderer().getBlockModel(state);
				}
				
				if (model == null || model == mc.getBlockRenderer().getBlockModelShaper().getModelManager().getMissingModel()) {
					model = mc.getBlockRenderer().getBlockModel(Blocks.STONE.defaultBlockState());
				}
				
				final int fakeLight = 15728880;
				PoseStack renderStack = new PoseStack();
				renderStack.pushPose();
				renderStack.translate(xOff, yOff, zOff);
				
				RenderModel(renderStack, buffer, model, fakeLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, .6f);
				
				renderStack.popPose();
			}
			buffer.end();
			cachedRenderList.upload(buffer); // Capture what we rendered
		}
		
		final float angle;
		final int rotX;
		final int rotZ;
		switch (rotation) {
		case NORTH:
		case UP:
		case DOWN:
		default:
			angle = 0;
			rotX = 0;
			rotZ = 0;
			break;
		case EAST:
			angle = 270;
			rotX = 1;
			rotZ = 0;
			break;
		case SOUTH:
			angle = 180;
			rotX = 1;
			rotZ = 1;
			break;
		case WEST:
			angle = 90;
			rotX = 0;
			rotZ = 1;
			break;
		}
		
		matrixStackIn.pushPose();
		
		matrixStackIn.translate(offset.x + rotX, offset.y + .001, offset.z + rotZ);
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(angle));
		
//		if (false) {
//			RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
//			
//			//cachedRenderList.bind();
//			//DefaultVertexFormat.BLOCK.setupBufferState();
//			RenderSystem.enableBlend();
//			RenderSystem.defaultBlendFunc();
//			cachedRenderList.drawWithShader(matrixStackIn.last().pose(), projection, RenderSystem.getShader());
//			//RenderSystem.setProjectionMatrix(matrixStackIn.last().pose());
//			//cachedRenderList.drawChunkLayer();
//			RenderSystem.disableBlend();
//			//VertexBuffer.unbind();
//	        //DefaultVertexFormat.BLOCK.clearBufferState();
//		}
//		else {
			final int width = preview.length;
			final int height = preview[0].length;
			final int depth = preview[0][0].length;
			
			for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
			for (int z = 0; z < depth; z++) {
				final BlueprintBlock block = preview[x][y][z];
				if (block == null) {
					continue;
				}
				
				final int xOff = x - (width/2);
				final int yOff = y - 1;
				final int zOff = z - (depth/2);
				
				BlockState state = block.getSpawnState(Direction.NORTH);
				
				if (state == null || state.getBlock() == Blocks.AIR) {
					continue;
				}
				
				BakedModel model = null;
				if (state != null) {
					model = mc.getBlockRenderer().getBlockModel(state);
				}
				
				if (model == null || model == mc.getBlockRenderer().getBlockModelShaper().getModelManager().getMissingModel()) {
					model = mc.getBlockRenderer().getBlockModel(Blocks.STONE.defaultBlockState());
				}
				
				final int fakeLight = 15728880;
				matrixStackIn.pushPose();
				matrixStackIn.translate(xOff, yOff, zOff);
				
				final var buffer = bufferIn.getBuffer(RenderType.translucentNoCrumbling());
				
				//mc.getBlockRenderer().getModelRenderer().renderModel(matrixStackIn.last(), buffer, state, model, 1f, 1f, 1f, 1, 1, EmptyModelData.INSTANCE);
				RenderModel(matrixStackIn, buffer, model, fakeLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, .6f);
				
				matrixStackIn.popPose();
			}
//		}
		
		
		matrixStackIn.popPose();
	}
	
	private static final Random RenderRandom(Random existing) {
		existing.setSeed(42); // Copied from Vanilla
		return existing;
	}
	private static final Random RenderModelRandom = new Random();
	
	private static void RenderModel(PoseStack stack, VertexConsumer buffer, BakedModel model, int combinedLight, int combinedOverlay, float red, float green, float blue, float alpha) {
		RenderModel(stack.last(), buffer, model, combinedLight, combinedOverlay, red, green, blue, alpha);
	}
	
	private static void RenderModel(PoseStack.Pose stackLast, VertexConsumer buffer, BakedModel model, int combinedLight, int combinedOverlay, float red, float green, float blue, float alpha) {
		
		for(Direction side : Direction.values()) {
			List<BakedQuad> quads = model.getQuads(null, side, RenderRandom(RenderModelRandom), EmptyModelData.INSTANCE);
			if(!quads.isEmpty()) 
				for(BakedQuad quad : quads) {
					buffer.putBulkData(stackLast, quad, red, green, blue, alpha, combinedLight, combinedOverlay, true);
//					LightUtil.renderQuadColor(buffer, quad, color);
				}
		}
		List<BakedQuad> quads = model.getQuads(null, null, RenderRandom(RenderModelRandom), EmptyModelData.INSTANCE);
		if(!quads.isEmpty()) {
			for(BakedQuad quad : quads) 
				buffer.putBulkData(stackLast, quad, red, green, blue, alpha, combinedLight, combinedOverlay, true);
				//LightUtil.renderQuadColor(buffer, quad, color);
		}

	}
	
}
