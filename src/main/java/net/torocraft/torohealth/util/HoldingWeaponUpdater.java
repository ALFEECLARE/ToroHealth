package net.torocraft.torohealth.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.torocraft.torohealth.ToroHealth;
import net.torocraft.torohealth.config.Config.Mode;

public class HoldingWeaponUpdater {
  private static Class<?>[] weaponTypeArray = {SwordItem.class,AxeItem.class,TridentItem.class,MaceItem.class,ShieldItem.class,BowItem.class,CrossbowItem.class,ArrowItem.class,PotionItem.class};
	
  public static void update() {
    if (Mode.NONE.equals(ToroHealth.CONFIG.inWorld.mode))
      return;
    Minecraft minecraft = Minecraft.getInstance();
    Player player = minecraft.player;
    if (player == null) {
      ToroHealth.IS_HOLDING_WEAPON = false;
      return;
    }
    ToroHealth.IS_HOLDING_WEAPON =
        isWeapon(player.getMainHandItem()) || isWeapon(player.getOffhandItem());
  }

  private static boolean isWeapon(ItemStack itemStack) {
	Item item = itemStack.getItem();
	//効果付きの矢、光の矢は矢のサブクラスなので考慮不要
	for (Class<?> cls : weaponTypeArray) {
		if (cls.isInstance(item))
			return true;
	}
    return false;
  }
}
