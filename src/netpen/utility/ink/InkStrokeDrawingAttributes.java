/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netpen.utility.ink;

import javafx.scene.paint.Color;

/**
 *
 * @author awehrer
 */
public class InkStrokeDrawingAttributes
{
    private Color color;
    private double strokeWidth;
    private StylusTip stylusTip;
    private boolean highlighter;
    
    public InkStrokeDrawingAttributes()
    {
        this(Color.BLACK, 3.0, StylusTip.ROUND, false);
    }
    
    public InkStrokeDrawingAttributes(Color color, double strokeWidth, StylusTip stylusTip, boolean highlighter)
    {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.stylusTip = stylusTip;
        this.highlighter = highlighter;
    }
    
    public Color getColor()
    {
        return color;
    }
    
    public double getStrokeWidth()
    {
        return strokeWidth;
    }
    
    public StylusTip getStylusTip()
    {
        return stylusTip;
    }
    
    public boolean isHighlighter()
    {
        return highlighter;
    }
}
