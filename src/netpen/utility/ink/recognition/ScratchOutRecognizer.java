/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netpen.utility.ink.recognition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.util.Pair;
import netpen.utility.ink.InkStroke;
import netpen.utility.ink.InkUtility;
import netpen.utility.ink.StylusPoint;

/**
 * A class whose instances recognize the scratch-out gesture from ink strokes.
 * 
 * @author Anthony Wehrer, Andrew Yee
 */
public class ScratchOutRecognizer
{
    public ScratchOutRecognizer()
    {

    }
    
    public boolean performIfScratchOut(InkStroke stroke, Collection<InkStroke> canvasInkStrokeList)
    {
        if (stroke instanceof AugmentedInkStroke)
            return performIfScratchOut((AugmentedInkStroke) stroke, canvasInkStrokeList);
        else
            return performIfScratchOut(new AugmentedInkStroke(stroke), canvasInkStrokeList);
    }

    // If it is the scratch-out gesture, strokes will be removed from allStrokes, including the classified stroke.
    private boolean performIfScratchOut(AugmentedInkStroke stroke, Collection<InkStroke> canvasInkStrokeList)
    {
        Collection<InkStroke> allOtherStrokes = new ArrayList<>(canvasInkStrokeList.size() - 1);
        
        for (InkStroke s : canvasInkStrokeList)
        {
            if (s != stroke)
                allOtherStrokes.add(s);
        }
        
        List<InkStroke> nLineIntersectedStrokes = getScratchedOutStrokes(stroke, allOtherStrokes);

        if (nLineIntersectedStrokes != null)
        {
            for (InkStroke s : nLineIntersectedStrokes)
            {
                //System.out.println(s);
                canvasInkStrokeList.remove(s);
            }

            canvasInkStrokeList.remove(stroke);

            return true;
        }

        return false;
    }
    
    public boolean isScratchOut(InkStroke stroke, Collection<InkStroke> allOtherStrokes)
    {
        if (stroke instanceof AugmentedInkStroke)
            return isScratchOut((AugmentedInkStroke) stroke, allOtherStrokes);
        else
            return isScratchOut(new AugmentedInkStroke(stroke), allOtherStrokes);
    }

    private boolean isScratchOut(AugmentedInkStroke stroke, Collection<InkStroke> allOtherStrokes)
    {
        return getScratchedOutStrokes(stroke, allOtherStrokes) != null;
    }
    
    public List<InkStroke> getScratchedOutStrokes(InkStroke stroke, Collection<InkStroke> allOtherStrokes)
    {
        if (stroke instanceof AugmentedInkStroke)
            return getScratchedOutStrokes((AugmentedInkStroke) stroke, allOtherStrokes);
        else
            return getScratchedOutStrokes(new AugmentedInkStroke(stroke), allOtherStrokes);
    }

    private List<InkStroke> getScratchedOutStrokes(AugmentedInkStroke stroke, Collection<InkStroke> allOtherStrokes)
    {
        List<Integer> corners;
        List<StylusPoint> resampledPoints;

        // Classification done here

        CornerFinder cornerFinder = new CornerFinder();
        Pair<List<Integer>, List<StylusPoint>> results = cornerFinder.getCorners(stroke);
        corners = results.getKey();
        resampledPoints = results.getValue();

        if (resampledPoints.size() > 3)
        {
            // find strokes that this stroke n-line intersects (has n lines that intersect the other stroke)
            int n = 4;
            List<InkStroke> nLineIntersectedStrokes = new ArrayList<>();

            for (InkStroke s : allOtherStrokes)
            {
                if (s != stroke)
                {
                    if (InkUtility.isNLineIntersected(s, resampledPoints, stroke.getDrawingAttributes().getStrokeWidth(), n, corners))
                        nLineIntersectedStrokes.add(s);
                }
            }

            // REMEMBER: THE ENDPOINTS OF THE STROKE WILL ALWAYS BE CORNERS/CUSPS
            int numLines = InkUtility.getNumLines(resampledPoints, corners);

            if (corners.size() >= 5 && numLines >= 4 && nLineIntersectedStrokes.size() > 0)
                return nLineIntersectedStrokes;
        }

        return null;
    }
}
