package net.shoreline.client.impl.event.render.item;

import net.shoreline.client.api.event.Cancelable;
import net.shoreline.client.api.event.Event;

@Cancelable
public class EatTransformationEvent extends Event
{
    private float factor;

    public void setFactor(float factor)
    {
        this.factor = factor;
    }

    public float getFactor()
    {
        return factor;
    }
}
