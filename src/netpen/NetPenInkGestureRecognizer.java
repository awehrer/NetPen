/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netpen;

import netpen.utility.ink.recognition.InkGestureRecognizer;
import netpen.utility.ink.recognition.gesture.LassoRecognitionProcedure;
import netpen.utility.ink.recognition.gesture.SingleStrokeRectangleRecognitionProcedure;

/**
 *
 * @author User
 */
public class NetPenInkGestureRecognizer extends InkGestureRecognizer
{
    public NetPenInkGestureRecognizer()
    {
        addGestureRecognitionProcedure(new SingleStrokeRectangleRecognitionProcedure());
        addGestureRecognitionProcedure(new LassoRecognitionProcedure());
    }
}
