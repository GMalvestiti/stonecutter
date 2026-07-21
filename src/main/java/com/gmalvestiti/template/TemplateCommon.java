package com.gmalvestiti.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static com.gmalvestiti.template.config.ConfigManager.CONFIG;

public class TemplateCommon {

    public static final String MOD_ID = /*$ mod_id*/ "template";
    public static final Logger LOGGER = LoggerFactory.getLogger( TemplateCommon.MOD_ID);

    public static void init() {
        if (Objects.nonNull(CONFIG) && CONFIG.ENABLED) {
            TemplateCommon.info("Mod loaded.");
        } else {
            TemplateCommon.info("Mod disabled.");
        }
    }

    public static void info(String message) {
        TemplateCommon.LOGGER.info("[{}] {}", TemplateCommon.MOD_ID, message);
    }

    public static void error(String message, Exception e) {
        TemplateCommon.LOGGER.error("[{}] {}", TemplateCommon.MOD_ID, message, e);
    }
}
