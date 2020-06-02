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
import javafx.scene.shape.Polyline;
import netpen.utility.ink.InkUtility;
import netpen.utility.ink.StylusPoint;
import netpen.utility.ink.recognition.AugmentedInkStroke;
import netpen.utility.ink.recognition.InkGestureRecognizer;

/**
 *
 * @author Arbor
 */
public class LeftSquareBracketRecognitionProcedure implements InkGestureRecognizer.InkGestureRecognitionProcedure
{
    @Override
    public InkGestureRecognizer.InkGestureRecognitionProcedureResult recognize(Collection<AugmentedInkStroke> inkStrokes, Map<String, Object> contextData)
    {
        if (inkStrokes.size() == 1)
        {
            AugmentedInkStroke stroke = inkStrokes.iterator().next();
            List<StylusPoint> p = stroke.getResampledPoints();

            double angleRightThreshold = 15.0;
            double angleParallelThreshold = 15.0;
            double lengthRatioThreshold = 0.7;
            double lengthThreshold = 16.0;
            double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY, minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

            List<Integer> cornerIndices = new ArrayList<>(stroke.getCornerIndices());

            if (cornerIndices.size() != 4 || stroke.getNumSelfIntersections() > 0)
                return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);

            // get angle of line between first and second corner and angle of line between fourth and third corner
            double angleA0 = Math.atan2(p.get(cornerIndices.get(1)).getY() - p.get(cornerIndices.get(0)).getY(), p.get(cornerIndices.get(1)).getX() - p.get(cornerIndices.get(0)).getX());
            double angleA1 = Math.atan2(p.get(cornerIndices.get(3)).getY() - p.get(cornerIndices.get(2)).getY(), p.get(cornerIndices.get(3)).getX() - p.get(cornerIndices.get(2)).getX());

            // get length of line between first and second corner and length of line between fourth and third corner
            double lengthA0 = InkUtility.distance(p.get(cornerIndices.get(1)), p.get(cornerIndices.get(0)));
            double lengthA1 = InkUtility.distance(p.get(cornerIndices.get(3)), p.get(cornerIndices.get(2)));

            // get angle of line between second and third corner and angle of line between first and fourth corner
            double angleB0 = Math.atan2(p.get(cornerIndices.get(1)).getY() - p.get(cornerIndices.get(2)).getY(), p.get(cornerIndices.get(1)).getX() - p.get(cornerIndices.get(2)).getX());
            double angleB1 = Math.atan2(p.get(cornerIndices.get(0)).getY() - p.get(cornerIndices.get(3)).getY(), p.get(cornerIndices.get(0)).getX() - p.get(cornerIndices.get(3)).getX());

            // get length of line between second and third corner and length of line between first and fourth corner
            double lengthB0 = InkUtility.distance(p.get(cornerIndices.get(1)), p.get(cornerIndices.get(2)));
            double lengthB1 = InkUtility.distance(p.get(cornerIndices.get(0)), p.get(cornerIndices.get(3)));

            // check if either set of opposing lines are equal length
            boolean linesAEqualLength = ((((lengthA0 <= lengthA1) ? (lengthA0 / lengthA1) : (lengthA1 / lengthA0)) >= lengthRatioThreshold)
                || Math.abs(lengthA0 - lengthA1) <= lengthThreshold);
            boolean linesBEqualLength = ((((lengthB0 <= lengthB1) ? (lengthB0 / lengthB1) : (lengthB1 / lengthB0)) >= lengthRatioThreshold)
                || Math.abs(lengthB0 - lengthB1) <= lengthThreshold);

            // check if either set of opposing lines are parallel
            boolean linesAParallel = (Math.abs(angleA0 - angleA1) <= angleParallelThreshold);
            boolean linesBParallel = (Math.abs(angleB0 - angleB1) <= angleParallelThreshold);

            double halfwayAngleA = (angleA0 + angleA1) * 0.5;
            double halfwayAngleB = (angleB0 + angleB1) * 0.5;
            boolean halfwayAngleRight = (Math.abs(halfwayAngleA - halfwayAngleB) <= angleRightThreshold);
            
            // if at least one set of opposing sides are not parallel, or at least one set of 
            // opposing sides do not have equal length, or angles are not approx 90 degrees,
            // it's neither a square or a rectangle
            if (!linesAParallel || !linesBParallel || !linesAEqualLength || !linesBEqualLength || !halfwayAngleRight)
            {
                return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
            }

            /*minX = Double.POSITIVE_INFINITY;
            maxX = Double.NEGATIVE_INFINITY;
            minY = Double.POSITIVE_INFINITY;
            maxY = Double.NEGATIVE_INFINITY;*/

            for (int i = 0; i < 4; i++)
            {
                if (p.get(cornerIndices.get(i)).getX() < minX) minX = p.get(cornerIndices.get(i)).getX();
                if (p.get(cornerIndices.get(i)).getX() > maxX) maxX = p.get(cornerIndices.get(i)).getX();
                if (p.get(cornerIndices.get(i)).getY() < minY) minY = p.get(cornerIndices.get(i)).getY();
                if (p.get(cornerIndices.get(i)).getY() > maxY) maxY = p.get(cornerIndices.get(i)).getY();
            }

            Polyline identifiedShape = new Polyline(maxX, minY, minX, minY, minX, minY, maxX, maxY);
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
        return "Square bracket";
    }
}
