package net.shoreline.client.impl.event.gui;

import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.client.api.event.Event;

public class RenderScreenEvent extends Event {
    public final MatrixStack matrixStack;

    public RenderScreenEvent(MatrixStack matrixStack) {
        this.matrixStack = matrixStack;
    }
}
