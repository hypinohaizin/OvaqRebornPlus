package net.shoreline.client.impl.event.entity;

import net.shoreline.client.api.event.Cancelable;
import net.shoreline.client.api.event.Event;

/**
 * @author hockeyl8
 * @since 1.0
 */
@Cancelable
public final class LimbAnimationEvent extends Event
{
    float speed;

    public void setSpeed(float speed)
    {
        this.speed = speed;
    }

    public float getSpeed()
    {
        return speed;
    }
}