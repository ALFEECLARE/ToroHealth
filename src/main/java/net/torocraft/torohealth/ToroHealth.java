package net.torocraft.torohealth;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.torocraft.torohealth.config.Config;
import net.torocraft.torohealth.config.loader.ConfigLoader;
import net.torocraft.torohealth.display.Hud;
import net.torocraft.torohealth.util.RayTrace;

@Mod(ToroHealth.MODID)
public class ToroHealth {

  public static final String MODID = "torohealth";

  public static Config CONFIG = new Config();
  public static Hud HUD = new Hud();
  public static RayTrace RAYTRACE = new RayTrace();
  public static boolean IS_HOLDING_WEAPON = false;
  public static Random RAND = new Random();
  private static final Logger log = LogManager.getLogger(MODID);

  private static ConfigLoader<Config> CONFIG_LOADER = new ConfigLoader<>(new Config(),
      ToroHealth.MODID + ".json", config -> ToroHealth.CONFIG = config);

  public ToroHealth(IEventBus modEventBus, ModContainer modContainer) {
    modEventBus.addListener(this::setup);
    ToroHealthClient.init(modEventBus, modContainer);
  }

  private void setup(final FMLCommonSetupEvent event) {
    CONFIG_LOADER.load();
  }

  public static void log(String message) {
	if (log == null)
		return;
	log.info("[{}] {}", log.getName(), message);
  }
}
