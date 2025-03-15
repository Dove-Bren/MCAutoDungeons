package com.smanzana.autodungeons.client.overlay;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.util.ColorUtil;
import com.smanzana.autodungeons.world.dungeon.DungeonRecord;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;

public class OverlayRenderer implements IIngameOverlay {
	
	public static final ResourceLocation ICON_COPPER_KEY = new ResourceLocation(AutoDungeons.MODID, "textures/models/copper_key.png");
	public static final ResourceLocation ICON_SILVER_KEY = new ResourceLocation(AutoDungeons.MODID, "textures/models/silver_key.png");
	
	private int keyIndex; // Controls dungoen key overlay fade in and out
	private @Nullable DungeonRecord lastDungeon;
	private static final int keyFadeDur = 60;
	
	//protected final IIngameOverlay dungeonKeyOverlay;

	public OverlayRenderer() {
		
	}
	
	public void render(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int windowWidth, int windowHeight) {
		//PoseStack matrixStackIn, LocalPlayer player, Window window, @Nullable DungeonRecord dungeon, Font fonter
		
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		@Nullable DungeonRecord dungeon = AutoDungeons.GetDungeonTracker().getDungeon(player);
		final Font fonter = mc.font;
		
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
					final int xOffset = windowWidth - width;
					final int height = 14;
					final int yOffset = windowHeight - height;
					final int colorTop = 0x20000000;
					final int colorBottom = 0xFF000000;
					final int iconWidth = 12;
					final int iconHeight = 12;
					final int textYOffset = (iconHeight - fonter.lineHeight) / 2;
					
					matrixStackIn.pushPose();
					
					// Fade in/out
					matrixStackIn.translate(0, (1f-slideProg) * height, 0);
					
					matrixStackIn.translate(xOffset, yOffset, 0);
					drawGradientRect(matrixStackIn, 0, 0, width, height,
							colorTop, colorTop,
							colorBottom, colorBottom);
					
					matrixStackIn.translate(width - 2, 2, 0);
					matrixStackIn.scale(.8f, .8f, 1f);
					
					RenderSystem.setShaderTexture(0, ICON_COPPER_KEY);
					GuiComponent.blit(matrixStackIn, -iconWidth, 0, 0, 0, iconWidth, iconWidth, iconWidth, iconWidth);
					matrixStackIn.translate(-(iconWidth + 6), 0, 0);
					
					fonter.draw(matrixStackIn, "" + smallCount, 0, textYOffset + 1, 0xFFFFFFFF);
					matrixStackIn.translate(-(5 + 2), 0, 0);
					
					if (largeCount > 0) {
						RenderSystem.setShaderTexture(0, ICON_SILVER_KEY);
						GuiComponent.blit(matrixStackIn, -iconWidth, 0, 0, 0, iconWidth, iconWidth, iconWidth, iconWidth);
						matrixStackIn.translate(-(iconWidth + 6), 0, 0);
						
						fonter.draw(matrixStackIn, "" + largeCount, 0, textYOffset + 1, 0xFFFFFFFF);
						matrixStackIn.translate(-(5 + 2), 0, 0);
					}
					
					matrixStackIn.popPose();
				}
			}
		}
	}
	
	private static final void drawGradientRect(PoseStack stack, int minX, int minY, int maxX, int maxY, int colorTopLeft, int colorTopRight, int colorBottomLeft, int colorBottomRight) {
		final Matrix4f transform = stack.last().pose();
		final float[] colorTR = ColorUtil.ARGBToColor(colorTopRight);
		final float[] colorTL = ColorUtil.ARGBToColor(colorTopLeft);
		final float[] colorBL = ColorUtil.ARGBToColor(colorBottomLeft);
		final float[] colorBR = ColorUtil.ARGBToColor(colorBottomRight);
		
		Tesselator tessellator = Tesselator.getInstance();
		
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		//RenderSystem.disableAlphaTest();
		//RenderSystem.shadeModel(GL11.GL_SMOOTH);
		{
			bufferbuilder.vertex(transform, minX, minY, 0).color(colorTL[0], colorTL[1], colorTL[2], colorTL[3]).endVertex();
			bufferbuilder.vertex(transform, minX, maxY, 0).color(colorBL[0], colorBL[1], colorBL[2], colorBL[3]).endVertex();
			bufferbuilder.vertex(transform, maxX, maxY, 0).color(colorBR[0], colorBR[1], colorBR[2], colorBR[3]).endVertex();
			bufferbuilder.vertex(transform, maxX, minY, 0).color(colorTR[0], colorTR[1], colorTR[2], colorTR[3]).endVertex();
		}

		bufferbuilder.end();
		BufferUploader.end(bufferbuilder);
		RenderSystem.disableBlend();
		//RenderSystem.enableAlphaTest();
		RenderSystem.enableTexture();
		//RenderSystem.shadeModel(GL11.GL_FLAT);
	}
}
