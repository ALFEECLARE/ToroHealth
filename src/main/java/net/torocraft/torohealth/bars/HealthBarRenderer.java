package net.torocraft.torohealth.bars;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.torocraft.torohealth.ToroHealth;
import net.torocraft.torohealth.ToroHealthClient;
import net.torocraft.torohealth.config.Config;
import net.torocraft.torohealth.config.Config.InWorld;
import net.torocraft.torohealth.config.Config.Mode;
import net.torocraft.torohealth.display.Hud;
import net.torocraft.torohealth.util.EntityUtil;
import net.torocraft.torohealth.util.EntityUtil.Relation;
import net.torocraft.torohealth.util.RenderUtils;

public class HealthBarRenderer {

  private static final ResourceLocation GUI_ENTITY_BAR_BACKGROUND_TEXTURE = ResourceLocation.parse(ToroHealth.MODID + ":textures/gui/bar_back_entity.png");
  private static final ResourceLocation GUI_ENTITY_BAR_FRONT_TEXTURE = ResourceLocation.parse(ToroHealth.MODID + ":textures/gui/bar_front_entity.png");
  private static final ResourceLocation GUI_WORLD_BAR_BACKGROUND_TEXTURE = ResourceLocation.parse(ToroHealth.MODID + ":textures/gui/bar_back_world.png");
  private static final ResourceLocation GUI_WORLD_BAR_FRONT_TEXTURE = ResourceLocation.parse(ToroHealth.MODID + ":textures/gui/bar_front_world.png");
  private static final ResourceLocation GUI_BARS_TEXTURES = ResourceLocation.parse(ToroHealth.MODID + ":textures/gui/bars.png");
  private static final int DARK_GRAY = 0xff808080;
  private static final int FULL_SIZE = 40;
  private static final int BAR_HEIGHT_IN_WORLD = 4;
  private static final int SOURCE_TEXTURE_WHOLE_WIDTH = 256;
  private static final int SOURCE_TEXTURE_WHOLE_HEIGHT = 256;
  private static final int SOURCE_TEXTURE_WIDTH = 92;
  private static final int SOURCE_TEXTURE_HEIGHT = 5;

  private static InWorld getConfig() {
    return ToroHealth.CONFIG.inWorld;
  }

  private static final List<LivingEntity> renderedEntities = new ArrayList<>();

  public static void prepareRenderInWorld(LivingEntity entity) {
    Minecraft client = Minecraft.getInstance();

    if (!EntityUtil.showHealthBar(entity, client)) {
      return;
    }

    if (ToroHealth.HUD.isIgnoreEntity(entity, Hud.IgnoreCheckTarget.WORLD)) {
    	return;
    };
    
    if (entity.distanceTo(client.getCameraEntity()) > ToroHealth.CONFIG.inWorld.distance) {
      return;
    }

    BarStates.getState(entity);

    if (Mode.WHEN_HOLDING_WEAPON.equals(getConfig().mode) && !ToroHealth.IS_HOLDING_WEAPON) {
      return;
    }

    if (Mode.NONE.equals(getConfig().mode)) {
      return;
    }

    if (ToroHealth.CONFIG.inWorld.onlyWhenLookingAt && ToroHealthClient.RAYTRACE.getEntityInCrosshair(0, ToroHealth.CONFIG.hud.distance) != entity) {
      return;
    }

    if (ToroHealth.CONFIG.inWorld.onlyWhenHurt && entity.getHealth() >= entity.getMaxHealth()) {
      return;
    }

    renderedEntities.add(entity);

  }

  public static void renderInWorld(float partialTick, GuiGraphics gui, Camera camera) {

    Minecraft client = Minecraft.getInstance();

    if (camera == null) {
      camera = client.getEntityRenderDispatcher().camera;
    }

    if (camera == null) {
      renderedEntities.clear();
      return;
    }

    if (renderedEntities.isEmpty()) {
      return;
    }

	RenderPipeline usingPipeLine = RenderUtils.buildEntityPipeline("world_health_bar_render");
    RenderSystem.AutoStorageIndexBuffer asBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);

    for (LivingEntity entity : renderedEntities) {
        float scaleToGui = 0.025f;
        boolean sneaking = entity.isCrouching();
        float height = entity.getBbHeight() + 0.6F - (sneaking ? 0.25F : 0.0F);

        double x = Mth.lerp((double) partialTick, entity.xo, entity.getX());
        double y = Mth.lerp((double) partialTick, entity.yo, entity.getY());
        double z = Mth.lerp((double) partialTick, entity.zo, entity.getZ());

        Vec3 camPos = camera.getPosition();
        double camX = camPos.x();
        double camY = camPos.y();
        double camZ = camPos.z();

        gui.pose().pushPose();
        gui.pose().translate(x - camX, (y + height) - camY, z - camZ);
        gui.pose().mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
        gui.pose().mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        gui.pose().scale(-scaleToGui, -scaleToGui, scaleToGui);

        render(gui, entity, -10, 0, FULL_SIZE, BAR_HEIGHT_IN_WORLD, true);

        gui.pose().popPose();
        gui.flush();
    }
    //RenderSystem.disableBlend();
  	RenderUtils.renderIfExists(usingPipeLine,asBuffer,24 * DefaultVertexFormat.POSITION_TEX.getVertexSize());

    renderedEntities.clear();
  }

  public static void render(GuiGraphics gui, LivingEntity entity, double x, double y,
      int width,int height , boolean inWorld) {

    Relation relation = EntityUtil.determineRelation(entity);

    int color = relation.equals(Relation.FRIEND) ? ToroHealth.CONFIG.bar.friendColor
        : ToroHealth.CONFIG.bar.foeColor;
    int color2 = relation.equals(Relation.FRIEND) ? ToroHealth.CONFIG.bar.friendColorSecondary
        : ToroHealth.CONFIG.bar.foeColorSecondary;

    BarState state = BarStates.getState(entity);

    float percent = Math.min(1, Math.min(state.health, entity.getMaxHealth()) / entity.getMaxHealth());
    float percent2 = Math.min(state.previousHealthDisplay, entity.getMaxHealth()) / entity.getMaxHealth();
    int zOffset = 0;

    //Matrix4f m4f = gui.pose().last().pose();

    gui.pose().pushPose();
    gui.pose().scale(RenderUtils.getScaleValue(SOURCE_TEXTURE_WIDTH,width), RenderUtils.getScaleValue(SOURCE_TEXTURE_HEIGHT, height), 0f);
    drawBar(gui, (int)x, (int)y, 1       , DARK_GRAY, width, height, zOffset++, inWorld);
    drawBar(gui, (int)x, (int)y, percent2, color2   , width, height, zOffset++, inWorld);
    drawBar(gui, (int)x, (int)y, percent , color    , width, height, zOffset  , inWorld);
    gui.pose().popPose();
    
    gui.flush();

    if (inWorld) {
      if (ToroHealth.CONFIG.bar.damageNumberType.equals(Config.NumberType.CUMULATIVE)) {
        drawDamageNumber(gui, state.lastDmgCumulative, x, y, width);
      } else if (ToroHealth.CONFIG.bar.damageNumberType.equals(Config.NumberType.LAST)) {
        drawDamageNumber(gui, state.lastDmg, x, y, width);
      }
    }


}

  public static void drawDamageNumber(GuiGraphics gui, int dmg, double x, double y,
      float width) {
    int i = Math.abs(Math.round(dmg));
    if (i == 0) {
      return;
    }
    String s = Integer.toString(i);
    Minecraft minecraft = Minecraft.getInstance();
    int sw = minecraft.font.width(s);
    int color = dmg < 0 ? ToroHealth.CONFIG.particle.healColor : ToroHealth.CONFIG.particle.damageColor;
    gui.drawString(minecraft.font, s, (int) (x + (width / 2) - sw), (int) y + 5, color);
    gui.flush();
  }

  private static void drawBar(GuiGraphics gui, int x, int y, float percent, int color, int drawWidth, int drawHeight,int zOffset, boolean isHorizontalCentered) {
	int barIndex = 13;

	int drawX = x - (drawWidth) / 2;
	int drawY = y;
	int sourceWidth = (int)(SOURCE_TEXTURE_WIDTH * percent);

    gui.blit(RenderType.GUI_TEXTURED, GUI_BARS_TEXTURES, drawX, drawY, 0f, SOURCE_TEXTURE_HEIGHT * barIndex, sourceWidth, SOURCE_TEXTURE_HEIGHT, SOURCE_TEXTURE_WHOLE_WIDTH, SOURCE_TEXTURE_WHOLE_HEIGHT, color);
     RenderSystem.setShaderColor(1, 1, 1, 1);
  }
}
