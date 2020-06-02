/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netpen.utility.ink;

import java.util.ArrayList;
import java.util.Collection;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 *
 * @author awehrer
 */
public class InkStroke extends Polyline
{
    private ObservableList<StylusPoint> stylusPoints;
    private InkStrokeDrawingAttributes drawingAttributes;
    private int duration;
    
    public InkStroke(InkStrokeDrawingAttributes attributes)
    {
        this(null, attributes, 0);
    }
    
    public InkStroke(Collection<StylusPoint> points)
    {
        this(points, new InkStrokeDrawingAttributes(), 0);
    }
    
    public InkStroke(Collection<StylusPoint> points, InkStrokeDrawingAttributes attributes)
    {
        this(points, attributes, 0);
    }
    
    public InkStroke(Collection<StylusPoint> points, InkStrokeDrawingAttributes attributes, int duration)
    {
        if (points == null)
            stylusPoints = FXCollections.observableList(new ArrayList<StylusPoint>());
        else
            stylusPoints = FXCollections.observableList(new ArrayList<StylusPoint>(points));
        
        setDrawingAttributes(attributes);
        
        for (StylusPoint point : stylusPoints)
        {
            getPoints().add(point.getX());
            getPoints().add(point.getY());
        }
        
        this.duration = duration;
        
        stylusPoints.addListener(new ListChangeListener<StylusPoint>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends StylusPoint> change)
            {
                while (change.next())
                {
                    if (change.wasAdded() && !change.wasRemoved())
                    {
                        int index = change.getFrom();
                        for (StylusPoint addedStylusPoint : change.getAddedSubList())
                        {
                            InkStroke.this.getPoints().add(2 * index, addedStylusPoint.getX());
                            InkStroke.this.getPoints().add(2 * index + 1, addedStylusPoint.getY());
                            index++;
                        }
                    }
                    else if (!change.wasAdded() && change.wasRemoved())
                    {
                        InkStroke.this.getPoints().remove(2 * change.getFrom(), 2 * change.getRemovedSize());
                    }
                    else
                    {
                        System.out.println("Change not handled.");
                    }
                }
            }
        });
    }
    
    public ObservableList<StylusPoint> getStylusPoints()
    {
        return stylusPoints;
    }
    
    public int getDuration()
    {
        return duration;
    }
    
    public InkStrokeDrawingAttributes getDrawingAttributes()
    {
        return drawingAttributes;
    }
    
    public void setDrawingAttributes(InkStrokeDrawingAttributes drawingAttributes)
    {
        if (drawingAttributes == null)
            drawingAttributes = new InkStrokeDrawingAttributes();
        
        this.drawingAttributes = drawingAttributes;
        
        setStroke(drawingAttributes.isHighlighter()? drawingAttributes.getColor().deriveColor(0.0, 1.0, 1.0, 0.7) : drawingAttributes.getColor());
        setStrokeWidth(drawingAttributes.getStrokeWidth());
        
        switch (drawingAttributes.getStylusTip())
        {
            case ROUND:
                setStrokeLineCap(StrokeLineCap.ROUND);
                setStrokeLineJoin(StrokeLineJoin.ROUND);
            case SQUARE:
                setStrokeLineCap(StrokeLineCap.SQUARE);
                setStrokeLineJoin(StrokeLineJoin.MITER);
        }
    }
}
