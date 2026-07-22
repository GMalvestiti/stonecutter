package com.gmalvestiti.minecraft.template;

import java.nio.file.Path;

public sealed interface Platform {

    Platform INSTANCE =
        /*? if fabric{*/new FabricPlatform();
        /*?} elif neoforge *///new NeoForgePlatform();

    Path getConfigDir();

    //? if fabric {
    final class FabricPlatform implements Platform {

        private final net.fabricmc.loader.api.FabricLoader loader = net.fabricmc.loader.api.FabricLoader.getInstance();

        @Override
        public Path getConfigDir() {
            return loader.getConfigDir();
        }
    }
    //?} elif neoforge {
    /*final class NeoForgePlatform implements Platform {

        @Override
        public Path getConfigDir() {
            return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
        }
    }
    *///?}
}
