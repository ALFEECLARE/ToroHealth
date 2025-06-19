package net.torocraft.torohealth.display;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.torocraft.torohealth.ToroHealth;
import net.torocraft.torohealth.bars.HealthBarRenderer;
import net.torocraft.torohealth.config.Config;
import net.torocraft.torohealth.util.EntityUtil;

public class BarDisplay {

	private final Minecraft mc;
	private static final ResourceLocation HEART_TEXTURES = Gui.HeartType.NORMAL.getSprite(false, false, false);
	private static final ResourceLocation ARMOR_TEXTURES = ResourceLocation.withDefaultNamespace("hud/armor_full");
	private static final ResourceLocation ACTUAL_ARMOR_TEXTURES = ResourceLocation.withDefaultNamespace("textures/gui/sprites/hud/armor_full.png");
	public static final int BAR_OFFSET_X = 63;
	public static final int BAR_OFFSET_Y = 12;
	public static final int BAR_WIDTH = 130;
	public static final int BAR_HEIGHT = 6; //when inWorld is false.
	public static final int EXTRADATA_Y_BASE_OFFSET = 20; //when inWorld is false.
	public Config config = null;

	public BarDisplay(Minecraft mc) {
		this.mc = mc;
	    this.config = ToroHealth.CONFIG;
	    if (this.config == null) {
	      this.config = new Config();
	    }
	}

	private String getEntityName(LivingEntity entity) {
		return entity.getDisplayName().getString();
	}

	public void draw(GuiGraphics guigraphic, LivingEntity entity, BlockEntity block) {
		int xOffset = 0;
		
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShader(CoreShaders.POSITION_TEX);
		RenderSystem.setShaderTexture(0, ACTUAL_ARMOR_TEXTURES);
		RenderSystem.enableBlend();

		if (entity != null) { 
			HealthBarRenderer.render(guigraphic, entity, BAR_OFFSET_X, BAR_OFFSET_Y, BAR_WIDTH, false);
	
		  	String name = getEntityName(entity);
			int healthMax = Mth.ceil(entity.getMaxHealth());
			int healthCur = Math.min(Mth.ceil(entity.getHealth()), healthMax);
			String healthText = healthCur + "/" + healthMax;
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	
			guigraphic.drawString(mc.font, name, xOffset, (int) 2, 16777215, true);
			xOffset += mc.font.width(name) + 5;
	
			renderHeartIcon(guigraphic, xOffset, (int) 1);
			xOffset += 10;
	
			guigraphic.drawString(mc.font, healthText, xOffset, 2, 0xe0e0e0, true);
			xOffset += mc.font.width(healthText) + 5;
	
			int armor = entity.getArmorValue();// getArmor();
	
			if (armor > 0) {
				renderArmorIcon(guigraphic, xOffset, (int) 1);
				xOffset += 10;
				guigraphic.drawString(mc.font, entity.getArmorValue() + "", xOffset, 2, 0xe0e0e0, true);
			}
	
			if (config.hud.showExtraData) {
				xOffset = 0;
				int yOffset = 0;
				for (String extraDataString : EntityUtil.getEntityExtraDataList(entity, mc)) {
					guigraphic.drawString(mc.font, extraDataString, 0, EXTRADATA_Y_BASE_OFFSET + yOffset, 0xe0e0e0, true);
					yOffset += mc.font.lineHeight;
				}
			}
			guigraphic.flush();
		} else if (block != null) {
			if (block instanceof SignBlockEntity sbe) {
				//guigraphic.drawString(mc.font, "看板", 0, (int) 2, 16777215, true);
				int yOffset = 0;
				SignText sgnText = (sbe.isFacingFrontText(mc.player) ? sbe.getFrontText() : sbe.getBackText());

				for (Component comp : sgnText.getMessages(true)) {
					//看板はバー表示をしないので頭から表示
					guigraphic.drawString(mc.font, comp.getString(), 0, yOffset, 0xe0e0e0, true);
					yOffset += mc.font.lineHeight;
				}
			}
		}
	}

	private void renderArmorIcon(GuiGraphics gui, int x, int y) {
		gui.blitSprite(RenderType.GUI_TEXTURED, ARMOR_TEXTURES, x, y, 9, 9);
	}

	private void renderHeartIcon(GuiGraphics gui, int x, int y) {
		gui.blitSprite(RenderType.GUI_TEXTURED, HEART_TEXTURES, x, y, 9, 9);
	}
}
