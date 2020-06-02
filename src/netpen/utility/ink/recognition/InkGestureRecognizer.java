/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netpen.utility.ink.recognition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javafx.scene.shape.Shape;

/**
 *
 * @author awehrer
 */
public class InkGestureRecognizer
{
    public interface InkGestureRecognitionProcedure
    {
        public InkGestureRecognitionProcedureResult recognize(Collection<AugmentedInkStroke> inkStrokes, Map<String, Object> contextData);
        public String getGestureIdentifier();
    }
    
    public static class InkGestureRecognitionProcedureResult
    {
        private final boolean match;
        private final Shape identifiedShape;
        
        public InkGestureRecognitionProcedureResult(boolean match)
        {
            this(match, null);
        }
        
        public InkGestureRecognitionProcedureResult(boolean match, Shape identifiedShape)
        {
            this.match = match;
            this.identifiedShape = identifiedShape;
        }
        
        public boolean isMatch()
        {
            return match;
        }
        
        public Shape getIdentifiedShape()
        {
            return identifiedShape;
        }
    }
    
    private final ArrayList<InkGestureRecognitionProcedure> gestureRecognitionProcedures;
    
    public InkGestureRecognizer()
    {
        this.gestureRecognitionProcedures = new ArrayList<>();
    }
    
    public InkGestureRecognizer(Collection<InkGestureRecognitionProcedure> gestureRecognitionProcedures)
    {
        this.gestureRecognitionProcedures = new ArrayList<>(gestureRecognitionProcedures);
    }
    
    public InkGesture analyzeForGesture(Collection<AugmentedInkStroke> inkStrokes, Map<String, Object> contextData)
    {
        InkGestureRecognitionProcedureResult result;
        
        for (InkGestureRecognitionProcedure gestureRecognitionProcedure : gestureRecognitionProcedures)
        {
            result = gestureRecognitionProcedure.recognize(inkStrokes, contextData);
            
            if (result.isMatch())
            {
                System.out.println(gestureRecognitionProcedure.getGestureIdentifier());
                return new InkGesture(gestureRecognitionProcedure.getGestureIdentifier(), inkStrokes, result.getIdentifiedShape());
            }
        }
        
        return null;
    }
    
    public void addGestureRecognitionProcedure(InkGestureRecognitionProcedure gestureRecognitionProcedure)
    {
        gestureRecognitionProcedures.add(gestureRecognitionProcedure);
    }
    
    public void removeGesturesRecognitionProcedure(InkGestureRecognitionProcedure gestureRecognitionProcedure)
    {
        gestureRecognitionProcedures.remove(gestureRecognitionProcedure);
    }
}
