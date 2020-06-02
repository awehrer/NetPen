/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netpen.utility.ink;

import javafx.geometry.Point2D;

/**
 *
 * @author awehrer
 */
public class StylusPoint extends Point2D
{
    /**
     * The stylus pressure ranges from 0 to 1.
     **/
    private float pressure;
    /**
     * The angle between the z-axis (points up) and the projection of the
     * stylus against the x-z plane, ranging from -pi/2 to pi/2 radians.
     */
    private float tiltX;
    /**
     * The angle between the z-axis (points up) and the projection of the
     * stylus against the y-z plane, ranging from -pi/2 to pi/2 radians.
     */
    private float tiltY;
    /**
     * Barrel button or wheel, ranging from 0 to 1 towards the stylus tip.
     */
    private float sidePressure;
    /**
     * Stylus rotation from 0 to 2*pi clockwise.
     **/
    private float rotation;
    
    private long timestamp;
    
    public StylusPoint(double x, double y)
    {
        this(x, y, 0.5f);
    }
    
    public StylusPoint(double x, double y, float pressure)
    {
        this(x, y, pressure, 0.0f, 0.0f, 0.0f, 0.0f, 0);
    }
    
    public StylusPoint(double x, double y, float pressure, float tiltX, float tiltY, float sidePressure, float rotation, long timestamp)
    {
        super(x, y);
        this.pressure = pressure;
        this.tiltX = tiltX;
        this.tiltY = tiltY;
        this.sidePressure = sidePressure;
        this.rotation = rotation;
        this.timestamp = timestamp;
    }
    
    public StylusPoint(double x, double y, StylusPoint oldPoint)
    {
        super(x, y);
        this.pressure = oldPoint.pressure;
        this.tiltX = oldPoint.tiltX;
        this.tiltY = oldPoint.tiltY;
        this.sidePressure = oldPoint.sidePressure;
        this.rotation = oldPoint.rotation;
        this.timestamp = oldPoint.timestamp;
    }
    
    public float getPressure()
    {
        return pressure;
    }
    
    public float getTiltX()
    {
        return tiltX;
    }
    
    public float getTiltY()
    {
        return tiltY;
    }
    
    public float getSidePressure()
    {
        return sidePressure;
    }
    
    public float getRotation()
    {
        return rotation;
    }
    
    public long getTimestamp()
    {
        return timestamp;
    }
    
    /**
    * Returns a string representation of this {@code StylusPoint} object.
    * @return a string representation of this {@code StylusPoint} object.
    */
    @Override
    public String toString()
    {
       final StringBuilder builder = new StringBuilder("StylusPoint [");
       
       builder.append("x = ").append(getX());
       builder.append(", y = ").append(getY());
       builder.append(", pressure = ").append(getPressure());
       builder.append(", tiltX = ").append(getTiltX());
       builder.append(", tiltY = ").append(getTiltY());
       builder.append(", sidePressure = ").append(getSidePressure());
       builder.append(", rotation = ").append(getRotation());
       builder.append(", timestamp = ").append(getTimestamp());
       builder.append("]");

       return builder.toString();
    }
}