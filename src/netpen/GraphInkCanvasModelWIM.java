/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netpen;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * WIM = World In Miniature
 * @author awehrer
 */
public class GraphInkCanvasModelWIM extends Pane
{
    private GraphInkCanvas world;
    private final ChangeListener<Graph> graphModelPropertyChangeListener;
    private final ChangeListener<BoundingBox> graphBoundsPropertyChangeListener;
    private final ChangeListener<Bounds> viewportBoundsPropertyChangeListener;
    private final ChangeListener<Number> modelRenderTranslationXChangeListener;
    private final ChangeListener<Number> modelRenderTranslationYChangeListener;
    private final ChangeListener<Number> modelRenderScaleChangeListener;
    private final ListChangeListener<Line> worldEdgeListChangeListener;
    private final ObservableList<Line> edgeLines;
    private final Group edgeGroup;
    private final Group elementGroup;
    private final Rectangle viewRectangle;
    private double xScale;
    private double yScale;
    private final double defaultEdgeWidth;
    private ScrollPane worldViewport;
    
    public GraphInkCanvasModelWIM()
    {
        defaultEdgeWidth = 0.4;
        
        edgeLines = FXCollections.observableList(new ArrayList<Line>());
        edgeLines.addListener(new EdgeLineListChangeListener());
        elementGroup = new Group();
        edgeGroup = new Group();
        
        viewRectangle = new Rectangle();
        viewRectangle.setStroke(Color.BLACK);
        viewRectangle.setStrokeWidth(2.5);
        viewRectangle.setFill(Color.TRANSPARENT);
        
        elementGroup.getChildren().add(edgeGroup);
        
        getChildren().add(elementGroup);
        elementGroup.relocate(0, 0);
        edgeGroup.relocate(0, 0);
        viewRectangle.relocate(0, 0);
        
        graphModelPropertyChangeListener = new ChangeListener<Graph>()
        {
            @Override
            public void changed(ObservableValue<? extends Graph> observable, Graph oldValue, Graph newValue)
            {
                GraphInkCanvasModelWIM.this.clear();
                GraphInkCanvasModelWIM.this.render();
            }
        };
        
        graphBoundsPropertyChangeListener = new ChangeListener<BoundingBox>()
        {
            @Override
            public void changed(ObservableValue<? extends BoundingBox> observable, BoundingBox oldValue, BoundingBox newValue)
            {
                double oldXScale = getXScale();
                double oldYScale = getYScale();
                updateScale();
                double xScaleChangeFactor = getXScale() / oldXScale;
                double yScaleChangeFactor = getYScale() / oldYScale;
                
                for (Line edgeLine : edgeLines)
                {
                    edgeLine.setStartX(edgeLine.getStartX() * xScaleChangeFactor);
                    edgeLine.setStartY(edgeLine.getStartY() * yScaleChangeFactor);
                    edgeLine.setEndX(edgeLine.getEndX() * xScaleChangeFactor);
                    edgeLine.setEndY(edgeLine.getEndY() * yScaleChangeFactor);
                }
                
                viewRectangle.setWidth(viewRectangle.getWidth() * xScaleChangeFactor);
                viewRectangle.setHeight(viewRectangle.getHeight() * yScaleChangeFactor);
            }
        };
        
        viewportBoundsPropertyChangeListener = new ChangeListener<Bounds>()
        {
            @Override
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue)
            {
                viewRectangle.setWidth(newValue.getWidth() * getXScale() / getWorld().getModelRenderScale());
                viewRectangle.setHeight(newValue.getHeight() * getYScale()  / getWorld().getModelRenderScale());
            }
        };
        
        modelRenderTranslationXChangeListener = new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                viewRectangle.setX(-newValue.doubleValue() * getXScale() / getWorld().getModelRenderScale());
            }
        };
        
        modelRenderTranslationYChangeListener = new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                viewRectangle.setY(-newValue.doubleValue() * getYScale() / getWorld().getModelRenderScale());
            }
        };
        
        modelRenderScaleChangeListener = new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                double scaleChangeFactor = oldValue.doubleValue() / newValue.doubleValue();
                double newWidth = viewRectangle.getWidth() * scaleChangeFactor;
                double newHeight = viewRectangle.getHeight() * scaleChangeFactor;
                viewRectangle.setWidth(newWidth);
                viewRectangle.setHeight(newHeight);
            }
        };
        
        worldEdgeListChangeListener = new ListChangeListener<Line>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Line> change)
            {
                while (change.next())
                {
                    if (change.wasAdded() && !change.wasRemoved())
                    {
                        for (Line newEdgeLine : new ArrayList<>(change.getAddedSubList()))
                            edgeLines.add(createMimickingLineClone(newEdgeLine));
                    }
                }
            }
        };
        
        EventHandler<MouseEvent> mouseEventHandler = new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                moveViewTo(event.getX() - (viewRectangle.getWidth() / 2.0), event.getY() - (viewRectangle.getHeight() / 2.0));
            }
        };
        
        addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEventHandler);
    }
    
    private class EdgeLineListChangeListener implements ListChangeListener<Line>
    {
        private class ModelRenderEdgeLineAdditionTask implements Runnable
        {
            private final int startIndex;
            private final List<? extends Line> childrenToAdd;
            
            public ModelRenderEdgeLineAdditionTask(int startIndex, List<? extends Line> childrenToAdd)
            {
                this.startIndex = startIndex;
                this.childrenToAdd = new ArrayList<>(childrenToAdd);
            }
            
            @Override
            public void run()
            {
                ObservableList<Node> modelRenderPaneChildren = GraphInkCanvasModelWIM.this.edgeGroup.getChildren();
                modelRenderPaneChildren.addAll(startIndex, childrenToAdd);
            }
        }
        
        private class ModelRenderEdgeLineRemovalTask implements Runnable
        {
            private final List<? extends Line> childrenToRemove;
            
            public ModelRenderEdgeLineRemovalTask(List<? extends Line> childrenToRemove)
            {
                this.childrenToRemove = new ArrayList<>(childrenToRemove);
            }
            
            @Override
            public void run()
            {
                GraphInkCanvasModelWIM.this.edgeGroup.getChildren().removeAll(childrenToRemove);
            }
        }
        
        @Override
        public void onChanged(ListChangeListener.Change<? extends Line> change)
        {
            while (change.next())
            {
                if (change.wasAdded() && !change.wasRemoved())
                    Platform.runLater(new ModelRenderEdgeLineAdditionTask(change.getFrom(), change.getAddedSubList()));
                else if (!change.wasAdded() && change.wasRemoved())
                    Platform.runLater(new ModelRenderEdgeLineRemovalTask(change.getRemoved()));
                else
                    System.out.println("Change not handled.");
            }
        }
    }
    
    public void moveViewTo(double wimX, double wimY)
    {
        double worldX = -(wimX / getXScale()) * getWorld().getModelRenderScale();
        double worldY = -(wimY / getYScale()) * getWorld().getModelRenderScale();
        
        getWorld().translateModelRenderTo(worldX, worldY);
    }
    
    public void setWorld(GraphInkCanvas world)
    {
        if (this.world != world)
        {
            if (this.world != null)
            {
                this.world.graphModelProperty().removeListener(graphModelPropertyChangeListener);
                this.world.getEdgeLines().removeListener(worldEdgeListChangeListener);
                clear();
                this.world.graphBoundsProperty().removeListener(graphBoundsPropertyChangeListener);
                this.world.modelRenderScaleProperty().removeListener(modelRenderScaleChangeListener);
                this.world.modelRenderTanslationXProperty().removeListener(modelRenderTranslationXChangeListener);
                this.world.modelRenderTanslationYProperty().removeListener(modelRenderTranslationYChangeListener);
            }
            
            this.world = world;
            
            if (this.world != null)
            {
                render();
                this.world.getEdgeLines().addListener(worldEdgeListChangeListener);
                this.world.graphModelProperty().addListener(graphModelPropertyChangeListener);
                this.world.graphBoundsProperty().addListener(graphBoundsPropertyChangeListener);
                this.world.modelRenderScaleProperty().addListener(modelRenderScaleChangeListener);
                this.world.modelRenderTanslationXProperty().addListener(modelRenderTranslationXChangeListener);
                this.world.modelRenderTanslationYProperty().addListener(modelRenderTranslationYChangeListener);
            }
        }
    }
    
    public void setWorldViewport(ScrollPane viewport)
    {
        if (getWorldViewport() == null && viewport != null)
            elementGroup.getChildren().add(viewRectangle);
        else if (getWorldViewport() != null && viewport == null)
            elementGroup.getChildren().remove(viewRectangle);
        
        if (getWorldViewport() != null)
            getWorldViewport().viewportBoundsProperty().removeListener(viewportBoundsPropertyChangeListener);
        
        if (viewport != null)
            viewport.viewportBoundsProperty().addListener(viewportBoundsPropertyChangeListener);
        
        worldViewport = viewport;
    }
    
    public ScrollPane getWorldViewport()
    {
        return worldViewport;
    }
    
    protected void clear()
    {
        edgeLines.clear();
    }
    
    protected void render()
    {
        updateScale();
        
        for (Line edgeLine : getWorld().getEdgeLines())
            edgeLines.add(createMimickingLineClone(edgeLine));
    }
    
    private Line createMimickingLineClone(Line edgeLine)
    {
        final Line edgeClone = new Line(edgeLine.getStartX() * getXScale(), edgeLine.getStartY() * getYScale(), edgeLine.getEndX() * getXScale(), edgeLine.getEndY() * getYScale());
        edgeClone.setStroke(Color.BLACK);
        edgeClone.setStrokeWidth(defaultEdgeWidth);
        
        edgeLine.startXProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                edgeClone.setStartX(newValue.doubleValue() * getXScale());
            }
        });
        
        edgeLine.startYProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                edgeClone.setStartY(newValue.doubleValue() * getYScale());
            }
        });
        
        edgeLine.endXProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                edgeClone.setEndX(newValue.doubleValue() * getXScale());
            }
        });
        
        edgeLine.endYProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                edgeClone.setEndY(newValue.doubleValue() * getYScale());
            }
        });
        
        edgeClone.visibleProperty().bind(edgeLine.visibleProperty());
        
        edgeLine.parentProperty().addListener(new ChangeListener<Parent>()
        {
            @Override
            public void changed(ObservableValue<? extends Parent> observable, Parent oldValue, Parent newValue)
            {
                if (newValue == null)
                {
                    edgeLines.remove(edgeClone);
                    //System.out.println("Edge clone removed");
                }
            }
        });
        
        return edgeClone;
    }
    
    public double getXScale()
    {
        return xScale;
    }
    
    public double getYScale()
    {
        return yScale;
    }
    
    private void updateScale()
    {
        BoundingBox graphBounds = getWorld().getGraphBounds();
        
        if (graphBounds == null)
        {
            xScale = 1.0;
            yScale = 1.0;
        }
        else
        {
            xScale = getPrefWidth() / graphBounds.getMaxX();
            yScale = getPrefHeight() / graphBounds.getMaxY();
        }
    }
    
    public GraphInkCanvas getWorld()
    {
        return world;
    }
}
