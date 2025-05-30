package net.shoreline.client.impl.event.render;

import net.shoreline.client.api.event.Cancelable;
import net.shoreline.client.api.event.Event;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class RenderFogEvent extends Event
{
    private final float viewDistance;
    private float start, end;

    public RenderFogEvent(float viewDistance)
    {
        this.viewDistance = viewDistance;
    }

    public float getViewDistance()
    {
        return viewDistance;
    }

    public float getStart()
    {
        return start;
    }

    public void setStart(float start)
    {
        this.start = start;
    }

    public float getEnd()
    {
        return end;
    }

    public void setEnd(float end)
    {
        this.end = end;
    }
}
