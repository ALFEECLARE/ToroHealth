package net.torocraft.torohealth.config.loader;

import java.io.File;

import net.neoforged.fml.loading.FMLPaths;

public class ConfigFolder {

  public static File get() {
    return FMLPaths.CONFIGDIR.get().toFile();
  }

}
