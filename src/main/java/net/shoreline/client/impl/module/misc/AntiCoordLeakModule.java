package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ConcurrentModule;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.command.BaritoneCommand;
import net.shoreline.client.impl.event.gui.chat.ChatMessageEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.chat.ChatUtil;

/**
 * @author h_ypi
 * @since 1.0
 */
public class AntiCoordLeakModule extends ConcurrentModule {

    public AntiCoordLeakModule() {
        super("AntiCoordLeak", "For Tikuwa", ModuleCategory.MISC);
    }

    @EventListener
    public void onChatMessage(ChatMessageEvent.Client event) {
        String message = event.getMessage();

        if (tikuwamoment(message)) {
            event.cancel();
            ChatUtil.clientSendMessage("はいお前座標貼ろうとしたね。戦犯");
        }
    }

    private boolean tikuwamoment(String message) {
        String commandPrefix = Managers.COMMAND.getPrefix();
        if (message.contains("/") || message.contains(commandPrefix) || message.contains("#")) {
            return false;
        }
        return message.matches(".*(?:-?\\d{1,6}\\s*[,\\s]\\s*){2}-?\\d{1,6}.*");
    }
}
