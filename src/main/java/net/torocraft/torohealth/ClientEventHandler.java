
package net.torocraft.torohealth;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.EntityModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.torocraft.torohealth.bars.BarStates;
import net.torocraft.torohealth.bars.HealthBarRenderer;
import net.torocraft.torohealth.bars.ParticleRenderer;
import net.torocraft.torohealth.util.HoldingWeaponUpdater;

public class ClientEventHandler {

  public static void init(IEventBus modEventBus, ModContainer modContainer) {
    NeoForge.EVENT_BUS.addListener(ClientEventHandler::playerTick);
    NeoForge.EVENT_BUS.addListener(ClientEventHandler::entityRender);
    NeoForge.EVENT_BUS.addListener(ClientEventHandler::renderParticles);
    modEventBus.addListener(ClientEventHandler::registerOverlays);
  }

  private static void registerOverlays(final RegisterGuiLayersEvent event) {
    event.registerAbove(VanillaGuiLayers.EFFECTS, ResourceLocation.parse("torohealth_hud"), ToroHealthClient.HUD::render);
  }

  @SubscribeEvent
  private static void entityRender(
          RenderLivingEvent.Post<? extends LivingEntity, ? extends EntityModel<?>> event) {
    HealthBarRenderer.prepareRenderInWorld(event.getEntity());
  }

  @SubscribeEvent
    private static void renderParticles(RenderLevelStageEvent event) {
      if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        GuiGraphics gui = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
        gui.pose().mulPose(event.getPoseStack().last().pose());
        ParticleRenderer.renderParticles(gui, camera);
        HealthBarRenderer.renderInWorld(event.getPartialTick().getGameTimeDeltaPartialTick(false), gui, camera);
      }
    }

  @SubscribeEvent
  private static void playerTick(PlayerTickEvent.Post event) {
    if (!event.getEntity().isLocalPlayer()) {
      return;
    }
    ToroHealthClient.HUD.setEntity(
        ToroHealthClient.RAYTRACE.getEntityInCrosshair(0, ToroHealth.CONFIG.hud.distance));
    BarStates.tick();
    HoldingWeaponUpdater.update();
    ToroHealthClient.HUD.tick();
  }
}
