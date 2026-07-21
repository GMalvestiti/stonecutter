package com.gmalvestiti.template;

//? if fabric {
import net.fabricmc.api.ModInitializer;

public class TemplateFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        TemplateCommon.init();
    }
}
//?}
