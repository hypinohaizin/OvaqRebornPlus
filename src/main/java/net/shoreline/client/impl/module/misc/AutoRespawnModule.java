package net.shoreline.client.impl.module.misc;

import net.minecraft.client.gui.screen.DeathScreen;
import net.shoreline.client.api.event.EventStage;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.ScreenOpenEvent;
import net.shoreline.client.impl.event.TickEvent;

/**
 * @author linus
 * @since 1.0
 */
public class AutoRespawnModule extends ToggleModule {
    //
    private boolean respawn;

    /**
     *
     */
    public AutoRespawnModule() {
        super("AutoRespawn", "Respawns automatically after a death",
                ModuleCategory.MISC);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE && respawn && mc.player.isDead()) {
            mc.player.requestRespawn();
            respawn = false;
        }
    }

    @EventListener
    public void onScreenOpen(ScreenOpenEvent event) {
        if (event.getScreen() instanceof DeathScreen) {
            respawn = true;
        }
    }
}
