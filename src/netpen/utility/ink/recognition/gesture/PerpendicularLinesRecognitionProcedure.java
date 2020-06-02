/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netpen.utility.ink.recognition.gesture;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import netpen.utility.ink.InkUtility;
import netpen.utility.ink.StylusPoint;
import netpen.utility.ink.recognition.AugmentedInkStroke;
import netpen.utility.ink.recognition.InkGestureRecognizer;

/**
 *
 * @author awehrer
 */
public class PerpendicularLinesRecognitionProcedure implements InkGestureRecognizer.InkGestureRecognitionProcedure
{
    @Override
    public InkGestureRecognizer.InkGestureRecognitionProcedureResult recognize(Collection<AugmentedInkStroke> inkStrokes, Map<String, Object> contextData)
    {
        if (inkStrokes.size() == 2)
        {
            Iterator<AugmentedInkStroke> inkStrokesIterator = inkStrokes.iterator();
            AugmentedInkStroke stroke1 = inkStrokesIterator.next();
            AugmentedInkStroke stroke2 = inkStrokesIterator.next();
            List<StylusPoint> points1 = stroke1.getResampledPoints();
            List<StylusPoint> points2 = stroke2.getResampledPoints();
            List<Integer> cornerIndices1 = stroke1.getCornerIndices();
            List<Integer> cornerIndices2 = stroke2.getCornerIndices();
            
            if (cornerIndices1.size() == 2 && cornerIndices2.size() == 2
                    && InkUtility.isLine(points1, 0, points1.size() - 1)
                    && InkUtility.isLine(points2, 0, points2.size() - 1)
                    && InkUtility.getNumIntersections(stroke1, stroke2) == 0)
            {
                double horizontalLineAngleDiffThreshold = 80.0;
                double verticalLineAngleDiffThreshold = 30.0;
                double horizontalAngle1, horizontalAngle2;
                horizontalAngle1 = Math.toDegrees(InkUtility.computeAngleOfLineWithXAxis(points1.get(0), points1.get(points1.size() - 1)));
                horizontalAngle2 = Math.toDegrees(InkUtility.computeAngleOfLineWithXAxis(points2.get(0), points2.get(points2.size() - 1)));
                //System.out.println("horizontalAngle1 = " + horizontalAngle1);
                //System.out.println("horizontalAngle2 = " + horizontalAngle2);
                
                horizontalAngle1 = Math.abs(horizontalAngle1);
                horizontalAngle2 = Math.abs(horizontalAngle2);
                
                if ((Math.abs(horizontalAngle1 - 90.0) < verticalLineAngleDiffThreshold && horizontalAngle2 < horizontalLineAngleDiffThreshold)
                        || (Math.abs(horizontalAngle2 - 90.0) < verticalLineAngleDiffThreshold && horizontalAngle1 < horizontalLineAngleDiffThreshold))
                {
                    return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(true);
                }
            }
        }

        return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
    }

    @Override
    public String getGestureIdentifier()
    {
        return "Perpendicular lines";
    }
}
