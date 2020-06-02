/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netpen.utility;

import java.awt.Point;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import jpen.owner.PenClip;

/**
 * A PenClip object defines the screen area where {@link jpen.PenEvent}s are
 * fired or a drag-out operation can be started. This particular class is geared
 * toward JavaFX Node scene objects.
 * @author Anthony Wehrer
 */
public class NodePenClip implements PenClip
{
    private final NodePenOwner penOwner;
    private Bounds nodeBounds;
    
    /**
     * Creates a NodePenClip instance.
     * @param penOwner The PenOwner object associated with the PenClip object.
     */
    public NodePenClip(NodePenOwner penOwner)
    {
        this.penOwner = penOwner;
        
        nodeBounds = penOwner.getNode().getBoundsInLocal();
        
        penOwner.getNode().boundsInLocalProperty().addListener(new ChangeListener<Bounds>()
        {
            @Override
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue)
            {
                nodeBounds = newValue;
            }
        });
    }
    
    /**
     * Tests whether the given point is within the PenClip area.
     * This method is called while holding the {@link jpen.owner.PenOwner.PenManagerHandle#getPenSchedulerLock()}.
     * @param point The point to test for containment within the PenClip area,
     * given in PenClip coordinates.
     * @return {@code true} if the given point is inside the PenClip area.
     */
    @Override
    public boolean contains(java.awt.geom.Point2D.Float point)
    {
        return point.x >= 0.0f && point.y >= 0.0f && point.x < nodeBounds.getWidth() && point.y < nodeBounds.getHeight();
    }
    
    /**
     * Evaluates the current location of the origin of this PenClip on the screen, using screen coordinates.
     * This method is called while holding the {@link jpen.owner.PenOwner.PenManagerHandle#getPenSchedulerLock()}.
     * @param locationOnScreen a Point object in which to store the determined origin of the PenClip area on the screen.
     */
    @Override
    public void evalLocationOnScreen(Point locationOnScreen)
    {
        Point2D nodeOriginOnScreen = penOwner.getNode().localToScreen(Point2D.ZERO);
        locationOnScreen.setLocation(nodeOriginOnScreen.getX(), nodeOriginOnScreen.getY());
    }
}
