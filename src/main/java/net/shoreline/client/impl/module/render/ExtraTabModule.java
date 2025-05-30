package net.shoreline.client.impl.module.render;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.gui.hud.PlayerListEvent;
import net.shoreline.client.impl.event.gui.hud.PlayerListNameEvent;
import net.shoreline.client.init.Managers;

/**
 * @author h_ypi
 * @since 1.0
 */
public class ExtraTabModule extends ToggleModule {

    Config<Integer> sizeConfig = new NumberConfig<>("Size", "The number of players to show", 80, 200, 1000);
    Config<Boolean> friendsConfig = new BooleanConfig("Friends", "Highlights friends in the tab list", true);

    public ExtraTabModule() {
        super("ExtraTab", "Expands the tab list size to allow for more players",
                ModuleCategory.RENDER);
    }

    @EventListener
    public void onPlayerListName(PlayerListNameEvent event) {
        if (friendsConfig.getValue() && Managers.SOCIAL.isFriend(event.getPlayerName())) {
            event.cancel();
            event.setPlayerName(Text.of(Formatting.AQUA + event.getPlayerName().getString()));
        }
    }

    @EventListener
    public void onPlayerList(PlayerListEvent event) {
        event.cancel();
        event.setSize(sizeConfig.getValue());
    }
}
