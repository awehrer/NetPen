/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netpen.utility.ink.recognition.gesture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.util.Pair;
import netpen.utility.ink.InkUtility;
import netpen.utility.ink.StylusPoint;
import netpen.utility.ink.recognition.AugmentedInkStroke;
import netpen.utility.ink.recognition.InkGestureRecognizer;

/**
 *
 * @author awehrer
 */
public class LassoRecognitionProcedure implements InkGestureRecognizer.InkGestureRecognitionProcedure
{
    @Override
    public InkGestureRecognizer.InkGestureRecognitionProcedureResult recognize(Collection<AugmentedInkStroke> inkStrokes, Map<String, Object> contextData)
    {
        if (inkStrokes.size() == 1)
        {
            AugmentedInkStroke stroke = inkStrokes.iterator().next();
            BoundingBox bounds = stroke.getBoundingBox();
            List<StylusPoint> points = stroke.getResampledPoints();
            
            if (stroke.getNumSelfIntersections() > 1)
                return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
            
            if (stroke.getNumSelfIntersections() == 1)
            {
                Pair<Integer, Integer> indexPair = stroke.getSelfIntersectionIndexPairs().get(0);
                System.out.println(indexPair);
                points = new ArrayList<>(stroke.getResampledPoints().subList(indexPair.getKey(), indexPair.getValue() + 1));
                bounds = InkUtility.getBoundingBox(points);
            }
            else if (InkUtility.distance(points.get(0), points.get(points.size() - 1)) > 100.0) // no self-intersections; endpoints should be close
            {
                return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
            }
            
            // find one point that is within this polygon
            Point2D testPoint;
            
            for (Object data : (Collection)contextData.get("selectablePoints"))
            {
                if (data instanceof Point2D)
                {
                    testPoint = (Point2D) data;
                    
                    if (InkUtility.isPointInPolygon((List)points, testPoint, bounds))
                    {
                        Polygon shape = new Polygon();
                        
                        for (StylusPoint point : points)
                        {
                            shape.getPoints().add(point.getX());
                            shape.getPoints().add(point.getY());
                        }
                        
                        return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(true, shape);
                    }
                }
            }
        }
        
        return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
    }

    @Override
    public String getGestureIdentifier()
    {
        return "Lasso";
    }
}
