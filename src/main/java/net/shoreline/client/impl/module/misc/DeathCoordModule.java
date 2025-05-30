package net.shoreline.client.impl.module.misc;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.util.chat.ChatUtil;
import net.shoreline.client.api.event.EventDispatcher;
import net.shoreline.client.api.event.listener.EventListener;

/**
 * @author h_ypi
 * @since 1.0
 */

public class DeathCoordModule extends ToggleModule {

    private boolean DCS = false;

    public DeathCoordModule() {
        super("DeathCoord", "Displays your death coordinates in chat", ModuleCategory.MISC);
        EventDispatcher.register(EntityDeathEvent.class, this::onPlayerDeath);
        EventDispatcher.register(TickEvent.class, this::onTick);
    }

    @EventListener
    public void onPlayerDeath(EntityDeathEvent event) {
        if (!DCS && event.getEntity() instanceof PlayerEntity player && player == mc.player) {
            BlockPos deathPos = player.getBlockPos();
            String deathCoordinates = String.format("You died at X: %d, Y: %d, Z: %d", deathPos.getX(), deathPos.getY(), deathPos.getZ());
            ChatUtil.clientSendMessage(deathCoordinates);
            DCS = true;
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (DCS && mc.player != null && mc.player.isAlive()) {
            DCS = false;
        }
    }
}
