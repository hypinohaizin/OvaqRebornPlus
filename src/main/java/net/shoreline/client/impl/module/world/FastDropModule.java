package net.shoreline.client.impl.module.world;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.EventStage;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.init.Managers;

/**
 * @author linus
 * @since 1.0
 */
public class FastDropModule extends ToggleModule {

    Config<Integer> delayConfig = new NumberConfig<>("Delay", "The delay for dropping items", 0, 0, 4);

    private int dropTicks;

    public FastDropModule() {
        super("FastDrop", "Drops items from the hotbar faster", ModuleCategory.WORLD);
    }

    /**
     * @param event
     */
    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        if (mc.options.dropKey.isPressed() && dropTicks > delayConfig.getValue()) {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.DROP_ITEM,
                    BlockPos.ORIGIN, Direction.DOWN));
            dropTicks = 0;
        }
        ++dropTicks;
    }
}
