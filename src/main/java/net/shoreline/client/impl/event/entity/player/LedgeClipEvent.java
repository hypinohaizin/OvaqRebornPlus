package net.shoreline.client.impl.event.entity.player;

import net.shoreline.client.api.event.Cancelable;
import net.shoreline.client.api.event.Event;

@Cancelable
public class LedgeClipEvent extends Event
{
    private boolean clip;

    public void setClipped(boolean clip)
    {
        this.clip = clip;
    }

    public boolean isClipped()
    {
        return clip;
    }
}
