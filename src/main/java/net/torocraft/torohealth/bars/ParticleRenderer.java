package net.torocraft.torohealth.bars;

import java.util.OptionalDouble;
import java.util.OptionalInt;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.torocraft.torohealth.ToroHealth;
import net.torocraft.torohealth.util.RenderUtils;

public class ParticleRenderer {

  public static void renderParticles(GuiGraphics gui, Camera camera) {
    for (BarParticle p : BarStates.PARTICLES) {
      renderParticle(gui, p, camera);
    }
  }

  private static void renderParticle(GuiGraphics gui, BarParticle particle, Camera camera) {
    double distanceSquared = camera.getPosition().distanceToSqr(particle.x, particle.y, particle.z);
    if (distanceSquared > ToroHealth.CONFIG.particle.distanceSquared) {
      return;
    }

    float scaleToGui = 0.025f;

    Minecraft client = Minecraft.getInstance();
    float tickDelta = client.getDeltaTracker().getGameTimeDeltaTicks();

    double x = Mth.lerp((double) tickDelta, particle.xPrev, particle.x);
    double y = Mth.lerp((double) tickDelta, particle.yPrev, particle.y);
    double z = Mth.lerp((double) tickDelta, particle.zPrev, particle.z);

    Vec3 camPos = camera.getPosition();
    double camX = camPos.x;
    double camY = camPos.y;
    double camZ = camPos.z;

    gui.pose().pushPose();
    gui.pose().translate(x - camX, y - camY, z - camZ);
    gui.pose().mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
    gui.pose().mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
    gui.pose().scale(-scaleToGui, -scaleToGui, scaleToGui);

	RenderPipeline usingPipeLine = RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
			.withLocation(ToroHealth.MODID + "/particle_render")
			.withFragmentShader(RenderUtils.SHADER_POSITION_COLOR)
			.withVertexShader(RenderUtils.SHADER_POSITION_COLOR)
			.withDepthTestFunction(DepthTestFunction.EQUAL_DEPTH_TEST)
			.withBlend(BlendFunction.PANORAMA)
			.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
			.build();
    RenderSystem.AutoStorageIndexBuffer asBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
	RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
    GpuBuffer gpuBuffer = RenderSystem.getDevice()
            .createBuffer(() -> "particle buffer", BufferType.VERTICES, BufferUsage.DYNAMIC_WRITE, 24 * DefaultVertexFormat.POSITION_TEX.getVertexSize());

	/*
	RenderSystem.setShader(CoreShaders.POSITION_COLOR);
    RenderSystem.enableDepthTest();
    RenderSystem.enableBlend();
    RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE,
        GL11.GL_ZERO);
    */
    HealthBarRenderer.drawDamageNumber(gui, particle.damage, 0, 0, 10);

    //RenderSystem.disableBlend();

    try (RenderPass renderpass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(renderTarget.getColorTexture(), OptionalInt.empty(), renderTarget.getDepthTexture(), OptionalDouble.empty())) {
        renderpass.setPipeline(usingPipeLine);
        renderpass.setVertexBuffer(0, gpuBuffer);
        renderpass.setIndexBuffer(asBuffer.getBuffer(36), asBuffer.type());

    }

    gui.pose().popPose();
  }
}
