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
import javafx.scene.paint.Color;
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
public class SingleStrokeTriangleRecognitionProcedure implements InkGestureRecognizer.InkGestureRecognitionProcedure
{
    @Override
    public InkGestureRecognizer.InkGestureRecognitionProcedureResult recognize(Collection<AugmentedInkStroke> inkStrokes, Map<String, Object> contextData)
    {
        if (inkStrokes.size() == 1)
        {
            AugmentedInkStroke stroke = inkStrokes.iterator().next();
            List<StylusPoint> p = stroke.getResampledPoints();
            List<Integer> cornerIndices = new ArrayList<>(stroke.getCornerIndices());

            // if 5 corners and the first and last corners are close together, merge them
            if (cornerIndices.size() == 4)
            {
                double distanceBetweenEndpoints = InkUtility.distance(p.get(cornerIndices.get(0)), p.get(cornerIndices.get(3)));
                
                if (distanceBetweenEndpoints <= 25.0)
                {
                    cornerIndices.remove(cornerIndices.get(cornerIndices.size() - 1));
                }
                else if (stroke.getNumSelfIntersections() > 0)
                {
                    // if we have self-intersection data, see if the lines of the first and last corners intersect and
                    // overshoot each other by some distance; if less, remove the ends, and merge at the
                    // intersection point

                    for (Pair<Integer, Integer> intersectionIndexPair : stroke.getSelfIntersectionIndexPairs())
                    {
                        if (intersectionIndexPair.getKey() >= 0 && intersectionIndexPair.getKey() <= cornerIndices.get(1) && intersectionIndexPair.getValue() >= cornerIndices.get(2)
                                && InkUtility.distance(p.get(cornerIndices.get(0)), p.get(intersectionIndexPair.getKey())) / InkUtility.distance(p.get(cornerIndices.get(0)), p.get(cornerIndices.get(1))) < 0.2
                                && InkUtility.distance(p.get(intersectionIndexPair.getValue()), p.get(cornerIndices.get(3))) / InkUtility.distance(p.get(cornerIndices.get(2)), p.get(cornerIndices.get(3))) < 0.2)
                        {
                            cornerIndices.set(0, intersectionIndexPair.getKey());
                            cornerIndices.remove(3);
                            break;
                        }
                    }

                    if (cornerIndices.size() != 3)
                    {
                        return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
                    }
                }
                else
                {
                    return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
                }
            }
            else
            {
                return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
            }
            
            // interior angles of triangle add up to 180 degrees, or PI radians
            // side z, y, x
            /*double sideAB, sideBC, sideCA, sideAB2, sideBC2, sideCA2;
            double angleA, angleB, angleC;
            
            sideAB = Math.abs(p.get(cornerIndices.get(0)).distance(p.get(cornerIndices.get(1))));
            sideBC = Math.abs(p.get(cornerIndices.get(1)).distance(p.get(cornerIndices.get(2))));
            sideCA = Math.abs(p.get(cornerIndices.get(2)).distance(p.get(cornerIndices.get(0))));
            
            sideAB2 = sideAB * sideAB;
            sideBC2 = sideBC * sideBC;
            sideCA2 = sideCA * sideCA;
            
            angleA = Math.acos((sideCA2 + sideAB2 - sideBC2) / (2 * sideCA * sideAB));
            angleB = Math.acos((sideBC2 + sideAB2 - sideCA2) / (2 * sideBC * sideAB));
            angleC = Math.acos((sideCA2 + sideBC2 - sideAB2) / (2 * sideCA * sideBC));
            
            double interiorAngleSum = angleA + angleB + angleC;
            System.out.println("Interior angle sum difference: " + Math.abs(Math.PI - interiorAngleSum));
            
            if (Double.isNaN(angleA) || Double.isNaN(angleB) || Double.isNaN(angleC) || Math.abs(Math.PI - interiorAngleSum) > interiorAngleDifferenceThreshold)
            {
                return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
            }*/

            Polygon identifiedShape = new Polygon();
            identifiedShape.getPoints().addAll(new Double[]
            {
                p.get(cornerIndices.get(0)).getX(), p.get(cornerIndices.get(0)).getY(),
                p.get(cornerIndices.get(1)).getX(), p.get(cornerIndices.get(1)).getY(),
                p.get(cornerIndices.get(2)).getX(), p.get(cornerIndices.get(2)).getY(),
            });
            
            identifiedShape.setFill(Color.TRANSPARENT);
            identifiedShape.setStroke(stroke.getDrawingAttributes().getColor());
            identifiedShape.setStrokeWidth(stroke.getDrawingAttributes().getStrokeWidth());
            return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(true, identifiedShape);
        }

        return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
    }

    @Override
    public String getGestureIdentifier()
    {
        return "Triangle";
    }
}
