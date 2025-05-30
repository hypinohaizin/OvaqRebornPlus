package net.shoreline.client.impl.module.misc;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.shoreline.client.OvaqPlus;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.event.EventStage;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.network.PacketEvent;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class PacketLoggerModule extends ToggleModule
{

    Config<Boolean> logConfig = (new BooleanConfig("Log", "Adds packets to the logs", false));
    Config<Boolean> chatConfig = (new BooleanConfig("LogChat", "Logs packets in the chats", false));
    Config<Boolean> disconnectConfig = (new BooleanConfig("LogDisconnect", "Logs packets on client disconnect", true));
    Config<Boolean> moveFullConfig = (new BooleanConfig("PlayerMoveFull", "Logs PlayerMoveC2SPacket", false));
    Config<Boolean> moveLookConfig = (new BooleanConfig("PlayerMoveLook", "Logs PlayerMoveC2SPacket", false));
    Config<Boolean> movePosConfig = (new BooleanConfig("PlayerMovePosition", "Logs PlayerMoveC2SPacket", false));
    Config<Boolean> moveGroundConfig = (new BooleanConfig("PlayerMoveGround", "Logs PlayerMoveC2SPacket", false));
    Config<Boolean> vehicleMoveConfig = (new BooleanConfig("VehicleMove", "Logs VehicleMoveC2SPacket", false));
    Config<Boolean> playerActionConfig = (new BooleanConfig("PlayerAction", "Logs PlayerActionC2SPacket", false));
    Config<Boolean> updateSlotConfig = (new BooleanConfig("UpdateSelectedSlot", "Logs UpdateSelectedSlotC2SPacket", false));
    Config<Boolean> clickSlotConfig = (new BooleanConfig("ClickSlot", "Logs ClickSlotC2SPacket", false));
    Config<Boolean> pickInventoryConfig = (new BooleanConfig("PickInventory", "Logs PickFromInventoryC2SPacket", false));
    Config<Boolean> handSwingConfig = (new BooleanConfig("HandSwing", "Logs HandSwingC2SPacket", false));
    Config<Boolean> interactEntityConfig = (new BooleanConfig("InteractEntity", "Logs PlayerInteractEntityC2SPacket", false));
    Config<Boolean> interactBlockConfig = (new BooleanConfig("InteractBlock", "Logs PlayerInteractBlockC2SPacket", false));
    Config<Boolean> interactItemConfig = (new BooleanConfig("InteractItem", "Logs PlayerInteractItemC2SPacket", false));
    Config<Boolean> commandConfig = (new BooleanConfig("ClientCommand", "Logs ClientCommandC2SPacket", false));
    Config<Boolean> statusConfig = (new BooleanConfig("ClientStatus", "Logs ClientStatusC2SPacket", false));
    Config<Boolean> closeScreenConfig = (new BooleanConfig("CloseScreen", "Logs CloseHandledScreenC2SPacket", false));
    Config<Boolean> teleportConfirmConfig = (new BooleanConfig("TeleportConfirm", "Logs TeleportConfirmC2SPacket", false));
    Config<Boolean> pongConfig = (new BooleanConfig("Pong", "Logs CommonPongC2SPacket", false));

    private final List<PacketLog> packetLogs = new CopyOnWriteArrayList<>();

    public PacketLoggerModule()
    {
        super("PacketLogger", "Logs client packets", ModuleCategory.MISC);
    }

    @Override
    public void onEnable()
    {
        OvaqPlus.info("PacketLogger enabled ...");
    }

    @Override
    public void onDisable()
    {
        OvaqPlus.info("PacketLogger disabled ...");
    }

    private void logPacket(Packet<?> packet, String msg, Object... args)
    {
        String s = String.format(msg, args);
        if (logConfig.getValue())
        {
            OvaqPlus.info(s);
        }
        if (chatConfig.getValue())
        {
            sendModuleMessage(s);
        }

        packetLogs.add(new PacketLog(packet, System.currentTimeMillis()));
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() == EventStage.PRE)
        {
            packetLogs.removeIf(l -> System.currentTimeMillis() - l.time() > 3000);
        }
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event)
    {
        if (!disconnectConfig.getValue())
        {
            return;
        }

        Map<Identifier, Integer> packetCountMap = new HashMap<>();
        for (PacketLog packetLog : packetLogs)
        {
            Packet<?> packet = packetLog.packet();
            Identifier identifier = Identifier.tryParse(packet.getNewNetworkState().getId());
            if (packetCountMap.containsKey(identifier))
            {
                packetCountMap.replace(identifier, packetCountMap.get(identifier) + 1);
            }
            else
            {
                packetCountMap.put(identifier, 1);
            }
        }

        List<String> strings = new ArrayList<>();
        for (Map.Entry<Identifier, Integer> entry : packetCountMap.entrySet())
        {
            Identifier packet = entry.getKey();
            strings.add(packet.toShortTranslationKey() + ": " + entry.getValue());
        }

        OvaqPlus.info(String.join(",", strings));
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof PlayerMoveC2SPacket.Full packet && moveFullConfig.getValue())
        {
            StringBuilder builder = new StringBuilder();
            builder.append("PlayerMove Full - ");
            if (packet.changesPosition())
            {
                builder.append("x: ").append(packet.getX(0.0)).append(", y: ").append(packet.getY(0.0)).append(", z: ").append(packet.getZ(0.0)).append(" ");
            }
            if (packet.changesLook())
            {
                builder.append("yaw: ").append(packet.getYaw(0.0f)).append(", pitch: ").append(packet.getPitch(0.0f)).append(" ");
            }
            builder.append(" onground: ").append(packet.isOnGround());
            logPacket(packet, builder.toString());
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround packet && movePosConfig.getValue())
        {
            StringBuilder builder = new StringBuilder();
            builder.append("PlayerMove PosGround - ");
            if (packet.changesPosition())
            {
                builder.append("x: ").append(packet.getX(0.0)).append(", y: ").append(packet.getY(0.0)).append(", z: ").append(packet.getZ(0.0)).append(" ");
            }
            builder.append(" onground: ").append(packet.isOnGround());
            logPacket(packet, builder.toString());
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround packet && moveLookConfig.getValue())
        {
            StringBuilder builder = new StringBuilder();
            builder.append("PlayerMove LookGround - ");
            if (packet.changesLook())
            {
                builder.append("yaw: ").append(packet.getYaw(0.0f)).append(", pitch: ").append(packet.getPitch(0.0f)).append(" ");
            }
            builder.append(" onground: ").append(packet.isOnGround());
            logPacket(packet, builder.toString());
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket.OnGroundOnly packet && moveGroundConfig.getValue())
        {
            String s = "PlayerMove Ground - onground: " + packet.isOnGround();
            logPacket(packet, s);
        }
        if (event.getPacket() instanceof VehicleMoveC2SPacket packet && vehicleMoveConfig.getValue())
        {
            logPacket(packet, "VehicleMove - x: %s, y: %s, z: %s, yaw: %s, pitch: %s", packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
        }
        if (event.getPacket() instanceof PlayerActionC2SPacket packet && playerActionConfig.getValue())
        {
            logPacket(packet, "PlayerAction - action: %s, direction: %s, pos: %s", packet.getAction().name(), packet.getDirection().name(), packet.getPos().toShortString());
        }
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket packet && updateSlotConfig.getValue())
        {
            logPacket(packet, "UpdateSlot - slot: %d", packet.getSelectedSlot());
        }
        if (event.getPacket() instanceof HandSwingC2SPacket packet && handSwingConfig.getValue())
        {
            logPacket(packet, "HandSwing - hand: %s", packet.getHand().name());
        }
        if (event.getPacket() instanceof CommonPongC2SPacket packet && pongConfig.getValue())
        {
            logPacket(packet, "Pong - %d", packet.getParameter());
        }
        if (event.getPacket() instanceof PlayerInteractEntityC2SPacket packet && mc.world != null && interactEntityConfig.getValue())
        {
            logPacket(packet, "InteractEntity");
        }
        if (event.getPacket() instanceof PlayerInteractBlockC2SPacket packet && interactBlockConfig.getValue())
        {
            BlockHitResult blockHitResult = packet.getBlockHitResult();
            logPacket(packet, "InteractBlock - pos: %s, dir: %s, hand: %s", blockHitResult.getBlockPos().toShortString(), blockHitResult.getSide().name(), packet.getHand().name());
        }
        if (event.getPacket() instanceof PlayerInteractItemC2SPacket packet && interactItemConfig.getValue())
        {
            logPacket(packet, "InteractItem - hand: %s", packet.getHand().name());
        }
        if (event.getPacket() instanceof CloseHandledScreenC2SPacket packet && closeScreenConfig.getValue())
        {
            logPacket(packet, "CloseScreen - id: %s", packet.getSyncId());
        }
        if (event.getPacket() instanceof ClientCommandC2SPacket packet && commandConfig.getValue())
        {
            logPacket(packet, "ClientCommand - mode: %s", packet.getMode().name());
        }
        if (event.getPacket() instanceof ClientStatusC2SPacket packet && statusConfig.getValue())
        {
            logPacket(packet, "ClientStatus - mode: %s", packet.getMode().name());
        }
        if (event.getPacket() instanceof ClickSlotC2SPacket packet && clickSlotConfig.getValue())
        {
            logPacket(packet, "ClickSlot - type: %s, slot: %s, button: %s, id: %s", packet.getActionType().name(), packet.getSlot(), packet.getButton(), packet.getSyncId());
        }
        if (event.getPacket() instanceof PickFromInventoryC2SPacket packet && pickInventoryConfig.getValue())
        {
            logPacket(packet, "PickInventory - slot: %s", packet.getSlot());
        }
        if (event.getPacket() instanceof TeleportConfirmC2SPacket packet && teleportConfirmConfig.getValue())
        {
            logPacket(packet, "TeleportConfirm - id: %s", packet.getTeleportId());
        }
    }

    public record PacketLog(Packet<?> packet, long time) {}
}
