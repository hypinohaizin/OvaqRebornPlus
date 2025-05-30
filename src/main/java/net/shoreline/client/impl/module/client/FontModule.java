package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;

/**
 * @author h_ypi
 * @since 1.0
 */
public class FontModule extends ToggleModule {

    public FontModule() {
        super("Font", "Changes the client text to custom font rendering", ModuleCategory.CLIENT);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {

    }
}
