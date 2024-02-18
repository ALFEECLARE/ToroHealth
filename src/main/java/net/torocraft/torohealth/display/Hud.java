package net.torocraft.torohealth.display;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.torocraft.torohealth.ToroHealth;
import net.torocraft.torohealth.config.Config;
import net.torocraft.torohealth.config.Config.AnchorPoint;
import net.torocraft.torohealth.util.EntityUtil;

public class Hud implements IGuiOverlay {
  private static final ResourceLocation BACKGROUND_TEXTURE =
      new ResourceLocation(ToroHealth.MODID + ":textures/gui/default_skin_basic.png");
  private EntityDisplay entityDisplay = new EntityDisplay();
  private int BASE_X_OFFSET = -10;
  private int BASE_Y_OFFSET = -10;
  private int DRAW_AREA_WIDTH = BarDisplay.BAR_OFFSET_X + BarDisplay.BAR_WIDTH + BASE_X_OFFSET;
  private int DRAW_AREA_HEIGHT = 60 + BASE_Y_OFFSET;
  private Minecraft minecraft;
  private LivingEntity entity;
  private BarDisplay barDisplay;
  private Config config = new Config();
  private int age;

  public Hud() {
    this.minecraft = Minecraft.getInstance();
    barDisplay = new BarDisplay(Minecraft.getInstance());
  }

  public void render(ForgeGui gui,GuiGraphics guigraphics, float partialTick, int width, int height) {
	gui.setTitle(Component.literal("ToroHealth HUD"));
    if (this.minecraft.options.renderDebug) {
      return;
    }
    this.config = ToroHealth.CONFIG;
    if (this.config == null) {
      this.config = new Config();
    }
    float scale = config.hud.scale;
    List<String> extraDataList = EntityUtil.getEntityExtraDataList(entity, minecraft);
    float x = determineX(scale);
    float y = determineY(scale, extraDataList);
    draw(guigraphics, x, y, scale, extraDataList);
  }

  private float determineX(float scale) {
    float x = config.hud.x;
    AnchorPoint anchor = config.hud.anchorPoint;
    float wScreen = minecraft.getWindow().getGuiScaledWidth();

    switch (anchor) {
      case BOTTOM_CENTER:
      case TOP_CENTER:
        return (wScreen / scale - DRAW_AREA_WIDTH) / 2 - x;
      case BOTTOM_RIGHT:
      case TOP_RIGHT:
        return (wScreen) / scale - DRAW_AREA_WIDTH  - x;
      default:
        return BASE_X_OFFSET + x;
    }
  }

  private float determineY(float scale, List<String> extraDataList) {
    float y = config.hud.y;
    AnchorPoint anchor = config.hud.anchorPoint;
    float hScreen = minecraft.getWindow().getGuiScaledHeight();
    int actualDrawAreaHeight = Math.max(DRAW_AREA_HEIGHT, minecraft.font.lineHeight * extraDataList.size() + BarDisplay.EXTRADATA_Y_BASE_OFFSET - BASE_Y_OFFSET);
    
    switch (anchor) {
      case BOTTOM_CENTER:
      case BOTTOM_LEFT:
      case BOTTOM_RIGHT:
          return (hScreen) / scale - actualDrawAreaHeight - y;
      default:
        return BASE_Y_OFFSET + y;
    }
  }

  public void tick() {
    age++;
  }

  public void setEntity(LivingEntity entity) {
    if (entity != null) {
      age = 0;
    }

    if (entity == null && age > config.hud.hideDelay) {
      setEntityWork(null);
    }

    if (entity != null && entity != this.entity) {
      setEntityWork(entity);
    }
  }

  private void setEntityWork(LivingEntity entity) {
    this.entity = entity;
    entityDisplay.setEntity(entity);
  }

  public LivingEntity getEntity() {
    return entity;
  }

  private void draw(GuiGraphics gui,float x, float y, float scale, List<String> extraDataList) {
    if (entity == null) {
      return;
    }
    
    if (config.hud.onlyWhenHurt && entity.getHealth() >= entity.getMaxHealth()) {
      return;
    }

    gui.pose().pushPose();
    gui.pose().scale(scale, scale, scale);
    gui.pose().translate(x, y, 0);
    if (config.hud.showSkin) {
      this.drawSkin(gui);
    }
    gui.pose().translate(10, 10, 0);
    if (config.hud.showEntity) {
      entityDisplay.draw(gui.pose(), scale);
    }
    gui.pose().translate(44, 0, 0);
    if (config.hud.showBar) {
      barDisplay.draw(gui, entity);
    }
    gui.pose().popPose();
    gui.flush();
  }

  private void drawSkin(GuiGraphics gui) {
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    int w = 160, h = 60;
    gui.blit(BACKGROUND_TEXTURE, 0, 0, 0.0f, 0.0f, w, h, w, h);
  }
}

