package net.torocraft.torohealth.display;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.torocraft.torohealth.bars.HealthBarRenderer;

public class BarDisplay {

  private static final ResourceLocation ICON_TEXTURES =
      new ResourceLocation("textures/gui/icons.png");
  private final Minecraft mc;

  public BarDisplay(Minecraft mc) {
    this.mc = mc;
  }

  private String getEntityName(LivingEntity entity) {
    return entity.getDisplayName().getString();
  }

  public void draw(GuiGraphics guigraphic, LivingEntity entity) {
    int xOffset = 0;

    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderTexture(0, ICON_TEXTURES);
    RenderSystem.enableBlend();

    HealthBarRenderer.render(guigraphic, entity, 63, 14, 130, false);
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
  }

  private void renderArmorIcon(GuiGraphics gui, int x, int y) {
    gui.blit(ICON_TEXTURES, x, y, 34, 9, 9, 9);
  }

  private void renderHeartIcon(GuiGraphics gui, int x, int y) {
    gui.blit(ICON_TEXTURES, x, y, 16 + 36, 0, 9, 9);
  }
}
