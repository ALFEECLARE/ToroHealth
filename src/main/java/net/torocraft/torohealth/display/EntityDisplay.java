package net.torocraft.torohealth.display;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class EntityDisplay {
	private static final float RENDER_HEIGHT = 30;
	private static final float RENDER_WIDTH = 18;
	private static final float WIDTH = 40;
	private static final float HEIGHT = WIDTH;

	private LivingEntity entity;
	private BlockEntity block;
	private float entityScale = 1;

	private float xOffset;
	private float yOffset;

	public void setEntity(LivingEntity entity) {
		this.entity = entity;
		updateScale();
	}

	public void setBlock(BlockEntity block) {
		this.block = block;
	}

	public void draw(GuiGraphics matrix, float scale) {
		if (entity != null) {
			try {
		        renderEntityInInventory(matrix, (int) xOffset, (int) yOffset, (int)entityScale, -80, -20, entity, scale);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (block != null) {
			if (block instanceof SignBlockEntity sbe) {
				try {
					matrix.renderFakeItem(new ItemStack(sbe.getBlockState().getBlock()), (int)((WIDTH - 16) / 2), (int)((HEIGHT - 16) / 2)); 
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

  private void updateScale() {
    if (entity == null) {
      return;
    }

    float scaleY = RENDER_HEIGHT / Math.max(entity.getBbHeight(),entity.getBbWidth());
    float scaleX = RENDER_WIDTH / Math.max(entity.getBbHeight(),entity.getBbWidth());
    entityScale = Math.min(scaleX, scaleY);

    entityScale *= (float)switch (entity) {
    	case IronGolem ironGolem                          -> 1.2;
    	case Armadillo armadillo                          -> 0.9;
    	case Villager villager when villager.isSleeping() -> ((float)(villager.isBaby() ? 21 : 12)) / entityScale;
    	default                                           -> 1;
    };

    xOffset = (WIDTH) / 2;
    xOffset += switch (entity) {
    	case Villager villager when villager.isSleeping() -> -3;
		default                                            -> 0;
    };

    yOffset = (HEIGHT + RENDER_HEIGHT) / 2;
    yOffset -= switch (entity) {
    	case Ghast ghast                                  -> 10;
    	case Turtle turtle                                -> 3;
    	case Villager villager when villager.isSleeping() -> 15;
    	default                                           -> 0;
    };
  }

  /**
   * copied from InventoryScreen.renderEntityInInventory() to expose the matrixStack
   */
  public static void renderEntityInInventory(GuiGraphics pGuiGraphics, float pX, float pY, int pSize, 
		  float pMouseX, float pMouseY, LivingEntity pEntity , float pScale) {
	float f2 = (float)Math.atan((double)(pMouseX / 40.0F));
	float f3 = (float)Math.atan((double)(pMouseY / 40.0F));
    float f4 = pEntity.yBodyRot;
    float f5 = pEntity.getYRot();
    float f6 = pEntity.getXRot();
    float f7 = pEntity.yHeadRotO;
    float f8 = pEntity.yHeadRot;
	pEntity.yBodyRot = 180.0F + f2 * 20.0F;
	pEntity.setYRot(180.0F + f2 * 40.0F);
	pEntity.setXRot(-f3 * 20.0F);
	pEntity.yHeadRot = pEntity.getYRot();
	pEntity.yHeadRotO = pEntity.getYRot();
	pGuiGraphics.pose().pushPose();
	pGuiGraphics.pose().translate((double)pX * pScale, (double)pY * pScale, (double)1050.0 * pScale);
	pGuiGraphics.pose().scale(1, 1, -1);
	Vector3f pTranslate = new Vector3f(0.0F, 0.0F, 1000.0F);
	pGuiGraphics.pose().translate(pTranslate.x, pTranslate.y, pTranslate.z);
	pGuiGraphics.pose().scale(pSize * 1.25f, pSize * 1.25f, pSize * 1.25f);
	Quaternionf pPose = new Quaternionf().rotateZ(180.0F * (float) (Math.PI / 180.0)); 
   	Quaternionf pCameraOrientation = new Quaternionf().rotateX(f3 * 20.0F * (float) (Math.PI / 180.0));
	pPose.mul(pCameraOrientation);
	pGuiGraphics.pose().mulPose(pPose);
	Lighting.setupForEntityInInventory();
	EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
	if (pCameraOrientation != null) {
		entityrenderdispatcher.overrideCameraOrientation(pCameraOrientation.conjugate(new Quaternionf()).rotateY((float) Math.PI));
	}
	
	entityrenderdispatcher.setRenderShadow(false);
	RenderSystem.runAsFancy(() -> entityrenderdispatcher.render(pEntity, 0.0, 0.0, 0.0, 0.0F, 1.0F, pGuiGraphics.pose(), pGuiGraphics.bufferSource(), 15728880));
	pGuiGraphics.flush();
	entityrenderdispatcher.setRenderShadow(true);
	Lighting.setupFor3DItems();
	pEntity.yBodyRot = f4;
    pEntity.setYRot(f5);
    pEntity.setXRot(f6);
    pEntity.yHeadRotO = f7;
    pEntity.yHeadRot = f8;
	pGuiGraphics.pose().popPose();
  }
}
