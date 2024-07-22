package com.smanzana.autodungeons.client.overlay;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.util.ColorUtil;
import com.smanzana.autodungeons.world.dungeon.DungeonRecord;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OverlayRenderer extends AbstractGui {
	
	public static final ResourceLocation ICON_COPPER_KEY = new ResourceLocation(AutoDungeons.MODID, "textures/models/copper_key.png");
	public static final ResourceLocation ICON_SILVER_KEY = new ResourceLocation(AutoDungeons.MODID, "textures/models/silver_key.png");
	
	private int keyIndex; // Controls dungoen key overlay fade in and out
	private @Nullable DungeonRecord lastDungeon;
	private static final int keyFadeDur = 60;

	public OverlayRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
		
		//healthbarOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.EXPERIENCE_BAR_ELEMENT, "PetCommand::healthbarOverlay", this::renderHealthbarOverlay);
		//modeIconOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.CROSSHAIR_ELEMENT, "PetCommand::modeOverlay", this::renderModeOverlay);
	}
	
	@SubscribeEvent
	public void onRender(RenderGameOverlayEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		MainWindow window = event.getWindow();
		MatrixStack matrixStackIn = event.getMatrixStack();
		
		if (event.getType() == ElementType.EXPERIENCE) {

			renderDungeonKeys(matrixStackIn, player, window, AutoDungeons.GetDungeonTracker().getDungeon(player), mc.fontRenderer);
		}
	}
	
	private void renderDungeonKeys(MatrixStack matrixStackIn, ClientPlayerEntity player, MainWindow window, @Nullable DungeonRecord dungeon, FontRenderer fonter) {
		
		if (dungeon != null) {
			lastDungeon = dungeon;
		}
		
		if (lastDungeon != null) {
			final int smallCount = AutoDungeons.GetWorldKeys().getKeyCount(lastDungeon.instance.getSmallKey());
			final int largeCount = AutoDungeons.GetWorldKeys().getKeyCount(lastDungeon.instance.getLargeKey());
			
			if (dungeon != null && (smallCount > 0 || largeCount > 0)) {
				this.keyIndex = Math.min(keyFadeDur, keyIndex + 1);
			} else {
				this.keyIndex = Math.max(0, keyIndex - 1);
			}
			
			if (keyIndex > 0) {
				final float slideProg = (float) keyIndex / (float) keyFadeDur;
				if (smallCount > 0 || largeCount > 0) {
					final int width = 45;
					final int xOffset = window.getScaledWidth() - width;
					final int height = 14;
					final int yOffset = window.getScaledHeight() - height;
					final int colorTop = 0x20000000;
					final int colorBottom = 0xFF000000;
					final int iconWidth = 12;
					final int iconHeight = 12;
					final int textYOffset = (iconHeight - fonter.FONT_HEIGHT) / 2;
					Minecraft mc = Minecraft.getInstance();
					
					matrixStackIn.push();
					
					// Fade in/out
					matrixStackIn.translate(0, (1f-slideProg) * height, 0);
					
					matrixStackIn.translate(xOffset, yOffset, 0);
					drawGradientRect(matrixStackIn, 0, 0, width, height,
							colorTop, colorTop,
							colorBottom, colorBottom);
					
					matrixStackIn.translate(width - 2, 2, 0);
					matrixStackIn.scale(.8f, .8f, 1f);
					
					mc.getTextureManager().bindTexture(ICON_COPPER_KEY);
					blit(matrixStackIn, -iconWidth, 0, 0, 0, iconWidth, iconWidth, iconWidth, iconWidth);
					matrixStackIn.translate(-(iconWidth + 6), 0, 0);
					
					fonter.drawString(matrixStackIn, "" + smallCount, 0, textYOffset + 1, 0xFFFFFFFF);
					matrixStackIn.translate(-(5 + 2), 0, 0);
					
					if (largeCount > 0) {
						mc.getTextureManager().bindTexture(ICON_SILVER_KEY);
						blit(matrixStackIn, -iconWidth, 0, 0, 0, iconWidth, iconWidth, iconWidth, iconWidth);
						matrixStackIn.translate(-(iconWidth + 6), 0, 0);
						
						fonter.drawString(matrixStackIn, "" + largeCount, 0, textYOffset + 1, 0xFFFFFFFF);
						matrixStackIn.translate(-(5 + 2), 0, 0);
					}
					
					matrixStackIn.pop();
				}
			}
		}
	}
	
	private static final void drawGradientRect(MatrixStack stack, int minX, int minY, int maxX, int maxY, int colorTopLeft, int colorTopRight, int colorBottomLeft, int colorBottomRight) {
		final Matrix4f transform = stack.getLast().getMatrix();
		final float[] colorTR = ColorUtil.ARGBToColor(colorTopRight);
		final float[] colorTL = ColorUtil.ARGBToColor(colorTopLeft);
		final float[] colorBL = ColorUtil.ARGBToColor(colorBottomLeft);
		final float[] colorBR = ColorUtil.ARGBToColor(colorBottomRight);
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableAlphaTest();
		RenderSystem.shadeModel(GL11.GL_SMOOTH);
		{
			bufferbuilder.pos(transform, minX, minY, 0).color(colorTL[0], colorTL[1], colorTL[2], colorTL[3]).endVertex();
			bufferbuilder.pos(transform, minX, maxY, 0).color(colorBL[0], colorBL[1], colorBL[2], colorBL[3]).endVertex();
			bufferbuilder.pos(transform, maxX, maxY, 0).color(colorBR[0], colorBR[1], colorBR[2], colorBR[3]).endVertex();
			bufferbuilder.pos(transform, maxX, minY, 0).color(colorTR[0], colorTR[1], colorTR[2], colorTR[3]).endVertex();
		}

		bufferbuilder.finishDrawing();
		WorldVertexBufferUploader.draw(bufferbuilder);
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
		RenderSystem.enableTexture();
		RenderSystem.shadeModel(GL11.GL_FLAT);
	}
}
