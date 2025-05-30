package net.shoreline.client.impl.module.render;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.Interpolation;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.EntityOutlineEvent;
import net.shoreline.client.impl.event.entity.decoration.TeamColorEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.init.Modules;
import net.shoreline.client.util.render.ColorUtil;
import net.shoreline.client.util.world.EntityUtil;

import java.awt.*;

/**
 * @author linus
 * @since 1.0
 */
public class ESPModule extends ToggleModule
{
    private static ESPModule INSTANCE;
    //
    Config<Float> rangeConfig = (new NumberConfig<>("Range", "The ESP render range", 10.0f, 50.0f, 200.0f));
    Config<ESPMode> modeConfig = (new EnumConfig<>("Mode", "ESP rendering mode", ESPMode.BOX, ESPMode.values()));
    Config<Boolean> fillConfig = (new BooleanConfig("Fill", "Fills the box render", false, () -> modeConfig.getValue() == ESPMode.BOX));
    Config<Float> widthConfig = (new NumberConfig<>("Width", "ESP rendering line width", 0.1f, 2.0f, 5.0f, () -> modeConfig.getValue() == ESPMode.BOX));
    Config<Boolean> playersConfig = (new BooleanConfig("Players", "Render players through walls", true));
    Config<Boolean> selfConfig = (new BooleanConfig("Self", "Render self through walls", true));
    Config<Color> playersColorConfig = (new ColorConfig("PlayersColor", "The render color for players", new Color(200, 60, 60), false, () -> playersConfig.getValue() || selfConfig.getValue()));
    Config<Boolean> monstersConfig = (new BooleanConfig("Monsters", "Render monsters through walls", true));
    Config<Color> monstersColorConfig = (new ColorConfig("MonstersColor", "The render color for monsters", new Color(200, 60, 60), false, () -> monstersConfig.getValue()));
    Config<Boolean> animalsConfig = (new BooleanConfig("Animals", "Render animals through walls", true));
    Config<Color> animalsColorConfig = (new ColorConfig("AnimalsColor", "The render color for animals", new Color(0, 200, 0), false, () -> animalsConfig.getValue()));
    Config<Boolean> vehiclesConfig = (new BooleanConfig("Vehicles", "Render vehicles through walls", false));
    Config<Color> vehiclesColorConfig = (new ColorConfig("VehiclesColor", "The render color for vehicles", new Color(200, 100, 0), false, () -> vehiclesConfig.getValue()));
    Config<Boolean> itemsConfig = (new BooleanConfig("Items", "Render dropped items through walls", false));
    Config<Color> itemsColorConfig = (new ColorConfig("ItemsColor", "The render color for items", new Color(200, 100, 0), false, () -> itemsConfig.getValue()));
    Config<Boolean> crystalsConfig = (new BooleanConfig("EndCrystals", "Render end crystals through walls", false));
    Config<Color> crystalsColorConfig = (new ColorConfig("EndCrystalsColor", "The render color for end crystals", new Color(200, 100, 200), false, () -> crystalsConfig.getValue()));

    public ESPModule()
    {
        super("ESP", "See entities and objects through walls", ModuleCategory.RENDER);
        INSTANCE = this;
    }

    public static ESPModule getInstance()
    {
        return INSTANCE;
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (modeConfig.getValue() != ESPMode.BOX)
        {
            return;
        }
        RenderBuffers.preRender();
        for (Entity entity : mc.world.getEntities())
        {
            if (entity == mc.player)
            {
                continue;
            }
            if (checkESP(entity))
            {
                Color espColor = getESPColor(entity);
                Box box = Interpolation.getInterpolatedEntityBox(entity);
                if (fillConfig.getValue())
                {
                    RenderManager.renderBox(event.getMatrices(), box,
                            ColorUtil.withAlpha(espColor.getRGB(), 60));
                }
                RenderManager.renderBoundingBox(event.getMatrices(), box,
                        widthConfig.getValue(), ColorUtil.withAlpha(espColor.getRGB(), 144));
            }
        }
        RenderBuffers.postRender();
    }

    @EventListener
    public void onEntityOutline(EntityOutlineEvent event)
    {
        if (mc.player != null && modeConfig.getValue() == ESPMode.GLOW && checkESP(event.getEntity()))
        {
            if (mc.player.squaredDistanceTo(event.getEntity()) > ((NumberConfig) rangeConfig).getValueSq())
            {
                return;
            }
            event.cancel();
        }
    }

    @EventListener
    public void onTeamColor(TeamColorEvent event)
    {
        if (mc.player != null && modeConfig.getValue() == ESPMode.GLOW && checkESP(event.getEntity()))
        {
            if (mc.player.squaredDistanceTo(event.getEntity()) > ((NumberConfig) rangeConfig).getValueSq())
            {
                return;
            }
            event.cancel();
            event.setColor(getESPColor(event.getEntity()).getRGB());
        }
    }

    public Color getESPColor(Entity entity)
    {
        if (entity instanceof PlayerEntity player)
        {
            Managers.SOCIAL.isFriend(player.getName());
            return playersColorConfig.getValue();
        }
        if (EntityUtil.isMonster(entity))
        {
            return monstersColorConfig.getValue();
        }
        if (EntityUtil.isNeutral(entity) || EntityUtil.isPassive(entity))
        {
            return animalsColorConfig.getValue();
        }
        if (EntityUtil.isVehicle(entity))
        {
            return vehiclesColorConfig.getValue();
        }
        if (entity instanceof EndCrystalEntity)
        {
            return crystalsColorConfig.getValue();
        }
        if (entity instanceof ItemEntity)
        {
            return itemsColorConfig.getValue();
        }
        return null;
    }

    public boolean checkESP(Entity entity)
    {
        if (entity instanceof PlayerEntity && playersConfig.getValue())
        {
            return selfConfig.getValue() || entity != mc.player;
        }
        return EntityUtil.isMonster(entity) && monstersConfig.getValue()
                || (EntityUtil.isNeutral(entity)
                || EntityUtil.isPassive(entity)) && animalsConfig.getValue()
                || EntityUtil.isVehicle(entity) && vehiclesConfig.getValue()
                || entity instanceof EndCrystalEntity && crystalsConfig.getValue()
                || entity instanceof ItemEntity && itemsConfig.getValue();
    }

    public enum ESPMode
    {
        BOX,
        GLOW
    }
}
