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
import netpen.utility.ink.InkUtility;
import netpen.utility.ink.StylusPoint;
import netpen.utility.ink.recognition.AugmentedInkStroke;
import netpen.utility.ink.recognition.InkGestureRecognizer;

/**
 *
 * @author awehrer
 */
public class CLikeCurveRecognitionProcedure implements InkGestureRecognizer.InkGestureRecognitionProcedure
{
    @Override
    public InkGestureRecognizer.InkGestureRecognitionProcedureResult recognize(Collection<AugmentedInkStroke> inkStrokes, Map<String, Object> contextData)
    {
        if (inkStrokes.size() == 1)
        {
            AugmentedInkStroke stroke = inkStrokes.iterator().next();
            List<StylusPoint> points = stroke.getResampledPoints();
            List<Integer> cornerIndices = stroke.getCornerIndices();
            
            // get gesture bound
            BoundingBox strokeBoundingBox = stroke.getBoundingBox();
            Point2D strokeBoundingBoxCenter = new Point2D((strokeBoundingBox.getMinX() + strokeBoundingBox.getMaxX()) * 0.5, (strokeBoundingBox.getMinY() + strokeBoundingBox.getMaxY()) * 0.5);
            
            // center points, and rotate so first stroke is along an axis (specifically, the X-axis)
            List<StylusPoint> rotatedPoints = InkUtility.rotateBy(points, -Math.atan2(points.get(0).getY() - strokeBoundingBoxCenter.getY(), points.get(0).getX() - strokeBoundingBoxCenter.getX()));
            BoundingBox rotatedStrokeBoundingBox = InkUtility.getBoundingBox(rotatedPoints);
            Point2D rotatedStrokeBoundingBoxCenter = new Point2D((rotatedStrokeBoundingBox.getMinX() + rotatedStrokeBoundingBox.getMaxX()) * 0.5, (rotatedStrokeBoundingBox.getMinY() + rotatedStrokeBoundingBox.getMaxY()) * 0.5);
            
            // assign points into four quadrants (0 = UL, 1 = LL, 2 = UR, 3 = LR)
            List<StylusPoint> upperLeftQuad = new ArrayList<>();
            List<StylusPoint> lowerLeftQuad = new ArrayList<>();
            List<StylusPoint> upperRightQuad = new ArrayList<>();
            List<StylusPoint> lowerRightQuad = new ArrayList<>();
            
            for (StylusPoint rotatedPoint : rotatedPoints)
            {
                if (rotatedPoint.getX() <= rotatedStrokeBoundingBoxCenter.getX())
                {
                    if (rotatedPoint.getY() <= rotatedStrokeBoundingBoxCenter.getY())
                        upperLeftQuad.add(rotatedPoint);
                    else
                        lowerLeftQuad.add(rotatedPoint);
                }
                else
                {
                    if (rotatedPoint.getY() <= rotatedStrokeBoundingBoxCenter.getY())
                        upperRightQuad.add(rotatedPoint);
                    else
                        lowerRightQuad.add(rotatedPoint);
                }
            }
            
            // mirror quad across from one with least points into the one with the least points
            int leastSide = 0, leastCount = upperLeftQuad.size();
            
            if (lowerLeftQuad.size() < leastCount)
            {
                leastSide = 1;
                leastCount = lowerLeftQuad.size();
            }
            
            if (upperRightQuad.size() < leastCount)
            {
                leastSide = 2;
                leastCount = upperRightQuad.size();
            }
            
            if (lowerRightQuad.size() < leastCount)
            {
                leastSide = 3;
                leastCount = lowerRightQuad.size();
            }
            
            List<StylusPoint> srcFlip = null, dstFlip = null;
            
            switch (leastSide)
            {
                case 0:
                    dstFlip = upperLeftQuad;
                    srcFlip = lowerRightQuad;
                    break;
                case 1:
                    dstFlip = lowerLeftQuad;
                    srcFlip = upperRightQuad;
                    break;
                case 2:
                    dstFlip = upperRightQuad;
                    srcFlip = lowerLeftQuad;
                    break;
                case 3:
                    dstFlip = lowerRightQuad;
                    srcFlip = upperLeftQuad;
                    break;
            }
            
            dstFlip.clear();
            
            for (StylusPoint point : srcFlip)
                dstFlip.add(new StylusPoint(2.0 * rotatedStrokeBoundingBoxCenter.getX() - point.getX(), 2.0 * rotatedStrokeBoundingBoxCenter.getY() - point.getY(), point));
            
            // recombine the points, pass to IsRoundedShape to check if circle
            List<StylusPoint> recombinedPoints = new ArrayList<>(upperLeftQuad.size() + lowerLeftQuad.size() + upperRightQuad.size() + lowerRightQuad.size());
            recombinedPoints.addAll(upperLeftQuad);
            recombinedPoints.addAll(lowerLeftQuad);
            recombinedPoints.addAll(upperRightQuad);
            recombinedPoints.addAll(lowerRightQuad);
            
            return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(InkUtility.isRoundedShape(recombinedPoints) > 0);
        }
        
        return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
    }
    
    @Override
    public String getGestureIdentifier()
    {
        return "C-like curve";
    }
}
