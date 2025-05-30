package net.shoreline.client.impl.event.entity;


import net.shoreline.client.api.event.Cancelable;
import net.shoreline.client.api.event.Event;

/**
 * @author hockeyl8
 * @since 1.0
 */
@Cancelable
public final class SwingSpeedEvent extends Event
{
    int swingSpeed;
    boolean selfOnly;

    public void setSwingSpeed(int swingSpeed)
    {
        this.swingSpeed = swingSpeed;
    }

    public int getSwingSpeed()
    {
        return swingSpeed;
    }

    public void setSelfOnly(boolean selfOnly)
    {
        this.selfOnly = selfOnly;
    }

    public boolean getSelfOnly()
    {
        return selfOnly;
    }
}