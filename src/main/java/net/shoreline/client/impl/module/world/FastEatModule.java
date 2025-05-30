package net.shoreline.client.impl.module.world;

import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.network.SetCurrentHandEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.player.MovementUtil;
import net.shoreline.client.util.string.EnumFormatter;

/**
 * @author xgraza/linus
 * @since 1.0
 */
public final class FastEatModule extends ToggleModule {
    private final Config<Mode> modeConfig = new EnumConfig<>("Mode", "The bypass mode", Mode.VANILLA, Mode.values());
    private final Config<Integer> ticksConfig = new NumberConfig<>("Ticks", "The amount of ticks to have 'consumed' an item before fast eating", 0, 10, 30);
    private int packets;

    public FastEatModule() {
        super("FastEat", "Allows you to consume items faster", ModuleCategory.WORLD);
    }

    @Override
    public String getModuleData() {
        return EnumFormatter.formatEnum(modeConfig.getValue());
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        if (MovementUtil.isMoving() || !mc.player.isOnGround()) {
            packets = Math.max(packets - 1, 0);
        } else {
            packets = Math.min(packets + 1, 100);
        }

        if (!mc.player.isUsingItem()) {
            return;
        }

        ItemStack stack = mc.player.getStackInHand(mc.player.getActiveHand());
        if (stack.isEmpty() || (!stack.isFood() && !(stack.getItem() instanceof PotionItem))) {
            return;
        }

        int timeUsed = mc.player.getItemUseTime();
        if (timeUsed >= ticksConfig.getValue()) {
            int usePackets = 32 - timeUsed;
            for (int i = 0; i < usePackets; i++) {
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
            }
        }
    }

    @EventListener
    public void onSetCurrentHand(SetCurrentHandEvent event) {
        if (modeConfig.getValue() != Mode.SHIFT) {
            return;
        }

        ItemStack stack = event.getStackInHand();
        if (!stack.isFood() && !(stack.getItem() instanceof PotionItem)) {
            return;
        }

        int maxUseTime = stack.getMaxUseTime();
        if (packets < maxUseTime) {
            return;
        }

        for (int i = 0; i < maxUseTime; i++) {
            Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
        }
        packets -= maxUseTime;
        event.cancel();
        stack.getItem().finishUsing(stack, mc.world, mc.player);
    }

    private enum Mode {
        VANILLA,
        SHIFT,
        GRIM
    }
}
