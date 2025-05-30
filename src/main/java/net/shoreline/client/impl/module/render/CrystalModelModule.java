package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.render.entity.RenderCrystalEvent;

public class CrystalModelModule extends ToggleModule
{
    private static CrystalModelModule INSTANCE;

    Config<Float> spinConfig = (new NumberConfig<>("Spin", "The spin speed of crystals", 0.0f, 1.0f, 10.0f));
    Config<Boolean> bounceCrystalConfig = (new BooleanConfig("Bounce", "The crystal bounce", true));
    Config<Float> scaleConfig = (new NumberConfig<>("Scale", "The scale of crystals", 0.10f, 1.00f, 1.50f));

    public CrystalModelModule()
    {
        super("CrystalModel", "Renders the crystal model", ModuleCategory.RENDER);
        INSTANCE = this;
    }

    public static CrystalModelModule getInstance()
    {
        return INSTANCE;
    }

    @EventListener
    public void onRender(RenderCrystalEvent event)
    {
        event.setSpin(spinConfig.getValue());
        event.setBounce(bounceCrystalConfig.getValue());
        event.setScale(scaleConfig.getValue());
    }

    public boolean getBounce()
    {
        return bounceCrystalConfig.getValue();
    }

    public float getSpin()
    {
        return spinConfig.getValue();
    }

    public float getScale()
    {
        return scaleConfig.getValue();
    }
}
