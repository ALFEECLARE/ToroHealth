package net.torocraft.torohealth.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.torocraft.torohealth.ToroHealth;
import net.torocraft.torohealth.config.Config.DataViewType;

public class EntityUtil {

  public enum Relation {
    FRIEND, FOE, UNKNOWN
  }

  public static Relation determineRelation(Entity entity) {
    if (entity instanceof Monster) {
      return Relation.FOE;
    } else if (entity instanceof Slime) {
      return Relation.FOE;
    } else if (entity instanceof Ghast) {
      return Relation.FOE;
    } else if (entity instanceof Animal) {
      return Relation.FRIEND;
    } else if (entity instanceof Squid) {
      return Relation.FRIEND;
    } else if (entity instanceof AmbientCreature) {
      return Relation.FRIEND;
    } else if (entity instanceof AgeableMob) {
      return Relation.FRIEND;
    } else if (entity instanceof AbstractFish) {
      return Relation.FRIEND;
    } else {
      return Relation.UNKNOWN;
    }
  }

  public static boolean showHealthBar(Entity entity, Minecraft client) {
    return entity instanceof LivingEntity && !(entity instanceof ArmorStand)
        && (!entity.isInvisibleTo(client.player) || entity.isCurrentlyGlowing() || entity.isOnFire()
            || entity instanceof Creeper && ((Creeper) entity).isPowered()

            || StreamSupport.stream(entity.getAllSlots().spliterator(), false)
                .anyMatch(is -> !is.isEmpty()))
        && entity != client.player && !entity.isSpectator();
  }


	public static List<String> getEntityExtraDataList(LivingEntity entity, Minecraft mc) {
		List<String> answer = new ArrayList<String>();
		if (entity == null) {
			return answer;
		}
		if (entity instanceof AbstractHorse) {
			AbstractHorse horseEntity = (AbstractHorse)entity;
			answer.addAll(getHorseExtraData(horseEntity));
		}
		if (entity instanceof Villager) {
			answer.add(Component.translatable("net.torocraft.torohealth.villagerBiome").getString() + " : " + Component.translatable("biome.minecraft." + ((Villager)entity).getVillagerData().getType().toString()).getString());
		}
		switch(entity.getClass().getName()) {
			case "":
				break;
			case "net.minecraft.world.entity.animal.goat.Goat":
				Goat goatEntity = (Goat)entity;
				answer.add(Component.translatable("net.torocraft.torohealth.goat." + (goatEntity.isScreamingGoat() ? "scream" : "normal")).getString() + " / " + getGortHornInstrumentName(goatEntity));
				break;
			case "net.minecraft.world.entity.animal.Panda":
				Panda pandaEntity = (Panda)entity;
				answer.add(Component.translatable("net.torocraft.torohealth.panda.maingene").getString() + " : " + Component.translatable("net.torocraft.torohealth.panda.gene." + pandaEntity.getMainGene().name()).getString());
				answer.add(Component.translatable("net.torocraft.torohealth.panda.hiddengene").getString() + " : " + Component.translatable("net.torocraft.torohealth.panda.gene." + pandaEntity.getHiddenGene().name()).getString());
				answer.add(Component.translatable("net.torocraft.torohealth.panda.finallygene").getString() + " : " + Component.translatable("net.torocraft.torohealth.panda.gene." + pandaEntity.getVariant().name()).getString());
				break;
			default:
				break;
		}
		return answer;
	}

	public static List<String> getHorseExtraData(AbstractHorse horseEntity) {
		DataViewType dataViewType = ToroHealth.CONFIG.hud.showDataType;
		List<String> answer = new ArrayList<String>();
		if (horseEntity instanceof Camel) {
			// do nothing.
		} else if (horseEntity instanceof SkeletonHorse) {
			answer.add(Component.translatable("net.torocraft.torohealth.horse.jumppower").getString() + " : " + getValueMinMaxDiscription(getHorseJumpFromInnerValue((float)horseEntity.getAttribute(Attributes.JUMP_STRENGTH).getValue()), getHorseJumpFromInnerValue(0.4f), getHorseJumpFromInnerValue(1f), dataViewType));
		} else if (horseEntity instanceof Llama) {
			Llama llamaEntity = (Llama)horseEntity;
			answer.add(Component.translatable("net.torocraft.torohealth.horse.maxhealth").getString() + " : " + getValueMinMaxDiscription((float)llamaEntity.getMaxHealth(), 15, 30, dataViewType));
			answer.add(Component.translatable("net.torocraft.torohealth.horse.inventory").getString() + " : " + getValueMinMaxDiscription((float)llamaEntity.getStrength() * 3, 3, 15, dataViewType));
		} else {
			answer.add(Component.translatable("net.torocraft.torohealth.horse.speed").getString() + " : " + getValueMinMaxDiscription(getHorseSpeedFromInnerValue((float)horseEntity.getAttribute(Attributes.MOVEMENT_SPEED).getValue()), getHorseSpeedFromInnerValue(0.1125f), getHorseSpeedFromInnerValue(0.3375f), dataViewType));
			answer.add(Component.translatable("net.torocraft.torohealth.horse.jumppower").getString() + " : " + getValueMinMaxDiscription(getHorseJumpFromInnerValue((float)horseEntity.getAttribute(Attributes.JUMP_STRENGTH).getValue()), getHorseJumpFromInnerValue(0.4f), getHorseJumpFromInnerValue(1f), dataViewType));
			answer.add(Component.translatable("net.torocraft.torohealth.horse.maxhealth").getString() + " : " + getValueMinMaxDiscription((float)horseEntity.getAttribute(Attributes.MAX_HEALTH).getValue(), 15, 30, dataViewType));
		}
		return answer;
	}

	
	public static String getValueMinMaxDiscription(float value, float minValue, float maxValue, DataViewType dataViewType) {
		switch (dataViewType) {
			case PERCENTAGE:
				return String.format("%.1f", value) + "(" + String.format("%.1f",(value - minValue) / (maxValue - minValue) * 100) + "%)";
			case MINMAX:
				return String.format("%.1f", minValue) + "/" + String.format("%.1f", value) + "/" + String.format("%.1f", maxValue);
			default:
				return String.format("%.1f", value);
		}
	}
	
	public static float getHorseSpeedFromInnerValue(float innerValue) {
		return innerValue * 42.175f;
	}

	public static float getHorseJumpFromInnerValue(float innerValue) {
		return -0.1817584952f * (float)Math.pow(innerValue,3) + 3.689713992f * (float)Math.pow(innerValue,2) + 2.128599134f * innerValue - 0.343930367f;
	}
	
	public static String getGortHornInstrumentName(Goat goatEntity) {
		ItemStack hornStack = goatEntity.createHorn();
		InstrumentItem hornItem = (InstrumentItem)hornStack.getItem();
		Optional<ResourceKey<Instrument>> optional = hornItem.getInstrument(hornStack).flatMap(Holder::unwrapKey);
		if (optional.isPresent()) {
	         MutableComponent mutablecomponent = Component.translatable(Util.makeDescriptionId("instrument", optional.get().location()));
	         return mutablecomponent.getString();
		} else {
			return "";
		}
	}
}
