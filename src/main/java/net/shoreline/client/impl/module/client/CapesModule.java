package net.shoreline.client.impl.module.client;

import net.minecraft.util.Identifier;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author h_ypi
 * @since 1.0
 */
public final class CapesModule extends ToggleModule {
    public static final Identifier TEXTURE = new Identifier("ovaqreborn", "capes/cape.png");
    //pastebinからuuidを取得する方式に変更　リモートで変更出来るよう
    Config<Boolean> userConfig = new BooleanConfig("User Cape", "show users", true);
    Config<Boolean> optifineConfig = new BooleanConfig("Optifine", "If to show optifine capes", true);

    public CapesModule() {
        super("Capes", "Shows player capes", ModuleCategory.CLIENT);
        enable();
    }

    public Config<Boolean> getOptifineConfig() {
        return optifineConfig;
    }

    public Config<Boolean> getUserConfig() {
        return userConfig;
    }
}
