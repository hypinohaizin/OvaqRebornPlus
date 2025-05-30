package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.OvaqPlus;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.config.setting.StringConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * h_ypi
 * @since 1.0
 */
public class FollowModule extends ToggleModule {

    Config<String> TargetConfig = new StringConfig("Target", "dev", "");
    Config<FollowMode> modeConfig = new EnumConfig<>("Mode", "Mode to follow the target", FollowMode.MOTION, FollowMode.values());
    Config<Float> speedConfig = new NumberConfig<>("Speed", "Speed of following", 1.0f, 0.1f, 10.0f);

    public FollowModule() {
        super("Follow", "Automatically follow a target player.", ModuleCategory.MOVEMENT);
    }

    private PlayerEntity getTarget() {
        String name = TargetConfig.getValue().trim();
        if (!name.isEmpty()) {
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (p.getName().getString().equalsIgnoreCase(name)) {
                    return p;
                }
            }

            OvaqPlus.error("Player" + name + "not found");
            return null;
        }

        List<PlayerEntity> players = mc.world.getPlayers().stream()
                .filter(p -> !p.equals(mc.player))
                .collect(Collectors.toList());
        return players.isEmpty() ? null : players.get(0);
    }

    @EventListener
    public void onTick(TickEvent event) {
        PlayerEntity target = getTarget();
        if (target == null) return;

        switch (modeConfig.getValue()) {
            case MOTION:
                followUsingMotion(target);
                break;
            case TELEPORT:
                mc.player.teleport(target.getX(), target.getY(), target.getZ());
                break;
            case SET_POSITION:
                mc.player.setPos(target.getX(), target.getY(), target.getZ());
                break;
        }
    }

    private void followUsingMotion(PlayerEntity target) {
        Vec3d cur = mc.player.getPos();
        Vec3d tgt = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d off = tgt.subtract(cur);
        double dist = off.length();

        if (dist < 0.1) {
            mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
            return;
        }

        double spd = Math.min(speedConfig.getValue(), dist);
        Vec3d vel = off.normalize().multiply(spd);
        mc.player.setVelocity(vel.x, vel.y, vel.z);
    }

    private enum FollowMode {
        MOTION,
        TELEPORT,
        SET_POSITION
    }
}
