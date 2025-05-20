package net.torocraft.torohealth.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BlockUtil {
	public static BlockPos getLookingBlockPos(Minecraft mc) {
		HitResult hitResult = mc.hitResult;
		if (hitResult instanceof BlockHitResult bir) {
			return bir.getBlockPos();
		} else if (hitResult != null) {
			Vec3 pos = hitResult.getLocation();
			return new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
		} else {
			return new BlockPos(0, 0, 0);
		}
	}
}
