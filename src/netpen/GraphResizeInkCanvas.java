/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netpen;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import netpen.utility.ink.InkCanvas;
import netpen.utility.ink.InkStroke;
import netpen.utility.ink.InkStrokeDrawingAttributes;

/**
 *
 * @author User
 */
public class GraphResizeInkCanvas extends InkCanvas
{
    private GraphInkCanvas graphCanvas;
    
    public GraphResizeInkCanvas()
    {
        getInkStrokes().addListener(new ListChangeListener<InkStroke>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends InkStroke> change)
            {
                while (change.next())
                {
                    if (change.wasAdded() && !change.wasRemoved())
                    {
                        for (InkStroke stroke : change.getAddedSubList())
                        {
                            if (stroke.getStylusPoints().size() > 2)
                            {
                                stroke.getStylusPoints().remove(1, stroke.getStylusPoints().size() - 1);
                            }
                            
                            if (getGraphCanvas() != null && getGraphCanvas().getGraphModel() != null && stroke.getStylusPoints().size() == 2)
                            {
                                final double widthChange = (stroke.getStylusPoints().get(1).getX() - stroke.getStylusPoints().get(0).getX());
                                final double heightChange = (stroke.getStylusPoints().get(1).getY() - stroke.getStylusPoints().get(0).getY());
                                getGraphCanvas().changeGraphWidthAndHeight(widthChange / getGraphCanvas().getModelRenderScale(), heightChange / getGraphCanvas().getModelRenderScale());
                            }
                            
                            final Timeline timeline = new Timeline();
                            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100.0), new InkStrokeGhostingHandler(stroke, timeline)));
                            timeline.setCycleCount(Timeline.INDEFINITE);
                            timeline.play();
                        }
                    }
                }
            }
        });
    }
    
    private class InkStrokeGhostingHandler implements EventHandler<ActionEvent>
    {
        private final Timeline timeline;
        private final InkStroke stroke;
        private long lastUpdateTime;
        
        public InkStrokeGhostingHandler(InkStroke stroke, Timeline timeline)
        {
            this.stroke = stroke;
            this.timeline = timeline;
            this.lastUpdateTime = System.currentTimeMillis();
        }
        
        @Override
        public void handle(ActionEvent event)
        {
            InkStrokeDrawingAttributes drawingAttributes = stroke.getDrawingAttributes();
            Color oldColor = drawingAttributes.getColor();
            double alpha = oldColor.getOpacity();
            double rate = 0.8; // per second
            long currentMillisFromEpoch = System.currentTimeMillis();
            double newAlpha = Math.max(alpha - (rate * (currentMillisFromEpoch - lastUpdateTime) / 1000.0), 0.0);
            stroke.setDrawingAttributes(new InkStrokeDrawingAttributes(new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), newAlpha), drawingAttributes.getStrokeWidth(), drawingAttributes.getStylusTip(), drawingAttributes.isHighlighter()));
            
            lastUpdateTime = currentMillisFromEpoch;
            
            if (newAlpha == 0.0)
            {
                GraphResizeInkCanvas.this.getInkStrokes().remove(stroke);
                timeline.stop();
            }
        }
    }
    
    public GraphInkCanvas getGraphCanvas()
    {
        return graphCanvas;
    }
    
    public void setGraphCanvas(GraphInkCanvas graphCanvas)
    {
        this.graphCanvas = graphCanvas;
    }
}
