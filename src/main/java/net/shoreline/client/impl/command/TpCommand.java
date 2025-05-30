package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.chat.ChatUtil;


import java.util.ArrayList;
import java.util.List;

public class TpCommand extends Command {

    public TpCommand() {
        super("Tp", "Teleports the player to the specified coordinates or another player", literal("tp"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .then(argument("target", StringArgumentType.string())
                        .executes(c -> {
                            String targetName = StringArgumentType.getString(c, "target");

                            // Get the target player
                            PlayerEntity targetPlayer = mc.world.getPlayers().stream()
                                    .filter(player -> player.getName().getString().equalsIgnoreCase(targetName))
                                    .findFirst()
                                    .orElse(null);

                            if (targetPlayer != null) {
                                teleportToPlayer(targetPlayer);
                                return 1;
                            } else {
                                ChatUtil.error("No players found with that name!");
                                return 1;
                            }
                        }))
                .then(argument("x", DoubleArgumentType.doubleArg())
                        .then(argument("y", DoubleArgumentType.doubleArg())
                                .then(argument("z", DoubleArgumentType.doubleArg())
                                        .executes(c -> {
                                            double x = DoubleArgumentType.getDouble(c, "x");
                                            double y = DoubleArgumentType.getDouble(c, "y");
                                            double z = DoubleArgumentType.getDouble(c, "z");

                                            if (mc.player.isFallFlying() || mc.player.isSleeping() || mc.player.isSpectator()) {
                                                ChatUtil.error("You cannot teleport while flying, sleeping, or spectating!");
                                                return 1;
                                            }

                                            if (y < 0 || y > 256) {
                                                ChatUtil.error("Y coordinate must be between 0 and 256.");
                                                return 1;
                                            }
                                            Managers.POSITION.setPosition(x, y, z);
                                            ChatUtil.clientSendMessage("Teleported to §s" + x + "§f, §s" + y + "§f, §s" + z + "§f.");
                                            return 1;
                                        })
                                )
                        )
                )
                .executes(c -> {
                    ChatUtil.error("Must provide a target player or coordinates!");
                    return 1;
                });
    }

    private void teleportToPlayer(PlayerEntity targetPlayer) {

        new Thread(() -> {
            mc.player.setPosition(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
        }).start();

        ChatUtil.clientSendMessage("Successfully teleported to §a" + targetPlayer.getName().getString());
    }
}