package com.gmalvestiti.minecraft.template.platform;

//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
//?} elif neoforge {
/*import net.neoforged.fml.loading.FMLPaths;
*///?}

import java.nio.file.Path;

public final class Platform {

    //? if fabric {
    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static boolean isFabric() {
        return true;
    }

    public static boolean isNeoForge() {
        return false;
    }
    //?} elif neoforge {
    /*public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static boolean isFabric() {
        return true;
    }

    public static boolean isNeoForge() {
        return false;
    }
    *///?}
}
