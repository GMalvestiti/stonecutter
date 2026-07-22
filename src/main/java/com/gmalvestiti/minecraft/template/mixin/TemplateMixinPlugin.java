package com.gmalvestiti.minecraft.template.mixin;

import com.gmalvestiti.minecraft.template.TemplateCommon;
import com.gmalvestiti.minecraft.template.config.ConfigManager;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.gmalvestiti.minecraft.template.config.ConfigManager.CONFIG;

public class TemplateMixinPlugin implements IMixinConfigPlugin {

    private String mixinPackage;

    @Override
    public void onLoad(String mixinPackage) {
        try {
            this.mixinPackage = mixinPackage;

            ConfigManager.loadConfig();
            ConfigManager.validateConfig();
            TemplateCommon.info("Configuration loaded.");
        } catch (Exception e) {
            TemplateCommon.error("Failed to load configuration. Check your template.json config file.", e);
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (Objects.nonNull(this.mixinPackage) && mixinClassName.startsWith(this.mixinPackage)) {
            if (Objects.isNull(CONFIG)) {
                TemplateCommon.info("Configuration is null. Defaulting mixin application to TRUE.");
                return true;
            }
            return CONFIG.ENABLED;
        }
        return true;
    }

    @Override
    public String getRefMapperConfig() {return null;}
    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override
    public List<String> getMixins() {return null;}
    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
