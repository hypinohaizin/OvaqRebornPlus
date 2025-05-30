package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;

/**
 * @author h_ypi
 * @since 1.0
 */
public class ShaderModule extends ToggleModule {

    public ShaderModule() {
        super("Shaders", "Clean debug screen", ModuleCategory.RENDER);
    }
}
