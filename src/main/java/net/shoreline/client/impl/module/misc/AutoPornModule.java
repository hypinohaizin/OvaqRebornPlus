package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.util.chat.ChatUtil;

import java.awt.*;
import java.net.URI;

public class AutoPornModule extends ToggleModule {
    public AutoPornModule() {
        super("AutoPorn", "", ModuleCategory.MISC);
    }

    @Override
    public void onEnable() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(new URI("https://jp.pornhub.com/"));
                ChatUtil.clientSendMessage("PornHubを開きました");
                disable();
            }
        } catch (Exception e) {
        }
    }
}
