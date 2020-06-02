/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netpen.utility.ink;

import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.input.InputEvent;

/**
 *
 * @author awehrer
 */
public class StylusEvent extends InputEvent
{
    private final StylusPoint stylusPoint;
    private boolean inAir;
    private final long timestamp;
    
    /**
     * Common super-type for all stylus event types.
     */
    public static final EventType<StylusEvent> ANY = new EventType<StylusEvent>(InputEvent.ANY, "STYLUS");
    
    /**
     * This event occurs when the stylus is placed down on the digitizer.
     */
    public static final EventType<StylusEvent> STYLUS_DOWN = new EventType<StylusEvent>(ANY, "STYLUS_DOWN");
    
    /**
     * This event occurs when the stylus is moved.
     */
    public static final EventType<StylusEvent> STYLUS_MOVED = new EventType<StylusEvent>(ANY, "STYLUS_MOVED");
    
    /**
     * This event occurs when the stylus is removed from the digitizer.
     */
    public static final EventType<StylusEvent> STYLUS_UP = new EventType<StylusEvent>(ANY, "STYLUS_UP");

    /**
     * This event occurs when the stylus is detected near the digitizer.
     */
    public static final EventType<StylusEvent> STYLUS_IN_RANGE = new EventType<StylusEvent>(ANY, "STYLUS_IN_RANGE");

    /**
     * This event occurs when the stylus is no longer detected near the digitizer.
     */
    public static final EventType<StylusEvent> STYLUS_OUT_OF_RANGE = new EventType<StylusEvent>(ANY, "STYLUS_OUT_OF_RANGE");

    public StylusEvent(Object source, EventTarget target, EventType<StylusEvent> eventType, StylusPoint stylusPoint, boolean inAir, long timestamp)
    {
        super(source, target, eventType);
        this.stylusPoint = stylusPoint;
        this.inAir = inAir;
        this.timestamp = timestamp;
    }
    
    public StylusEvent(EventType<StylusEvent> eventType, StylusPoint stylusPoint, boolean inAir, long timestamp)
    {
        this(null, null, eventType, stylusPoint, inAir, timestamp);
    }

    public final double getX()
    {
        return (stylusPoint != null ? stylusPoint.getX() : Double.NaN);
    }

    public final double getY()
    {
        return (stylusPoint != null ? stylusPoint.getY() : Double.NaN);
    }

    public final float getPressure()
    {
        return (stylusPoint != null ? stylusPoint.getPressure() : Float.NaN);
    }

    public final StylusPoint getStylusPoint()
    {
        return stylusPoint;
    }

    public final long getTimestamp()
    {
        return timestamp;
    }

    public final boolean isInAir()
    {
        return inAir;
    }

    /**
    * Returns a string representation of this {@code StylusEvent} object.
    * @return a string representation of this {@code StylusEvent} object.
    */
    @Override
    public String toString()
    {
       final StringBuilder builder = new StringBuilder("StylusEvent [");
       
       builder.append("source = ").append(getSource());
       builder.append(", target = ").append(getTarget());
       builder.append(", eventType = ").append(getEventType());
       builder.append(", consumed = ").append(isConsumed());
       builder.append(", inAir = ").append(isInAir());
       builder.append(", stylusPoint = ").append(getStylusPoint());
       builder.append("]");

       return builder.toString();
    }
}
