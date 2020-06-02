/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netpen;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.transform.Affine;
import netpen.utility.LayeredList;
import netpen.utility.ink.InkCanvas;

/**
 *
 * @author awehrer
 */
public abstract class GraphInkCanvas extends InkCanvas
{
    private ObjectProperty<BoundingBox> defaultGraphBounds;
    private ObjectProperty<BoundingBox> graphBounds;
    
    private final ObjectProperty<Graph> graphModel;
    private final ObservableList<Node> vertexMarkers;
    private final ObservableList<Line> edgeLines;
    private final ObservableList<Node> labels;
    private final Pane modelRenderPane;
    private final LayeredList<Node> layeredModelRenderChildren;
    
    private DoubleProperty modelRenderTranslationX;
    private DoubleProperty modelRenderTranslationY;
    private DoubleProperty modelRenderScale;
    
    private Affine manipulationTransform;
    
    public GraphInkCanvas()
    {
        modelRenderPane = new Pane();
        getChildren().add(modelRenderPane);
        
        vertexMarkers = FXCollections.observableList(new ArrayList<Node>());
        edgeLines = FXCollections.observableList(new ArrayList<Line>());
        labels = FXCollections.observableList(new ArrayList<Node>());
        layeredModelRenderChildren = new LayeredList<>(3);
        
        vertexMarkers.addListener(new VertexMarkerListChangeListener());
        edgeLines.addListener(new EdgeLineListChangeListener());
        labels.addListener(new LabelListChangeListener());
        
        graphModel = new SimpleObjectProperty<>();
        defaultGraphBounds = new SimpleObjectProperty<>();
        graphBounds = new SimpleObjectProperty<>();
        
        modelRenderTranslationX = new SimpleDoubleProperty(0.0);
        modelRenderTranslationY = new SimpleDoubleProperty(0.0);
        modelRenderScale = new SimpleDoubleProperty(1.0);
        
        manipulationTransform = new Affine();
        getModelRenderPane().getTransforms().add(manipulationTransform);
        modelRenderScale.bindBidirectional(manipulationTransform.mxxProperty());
        modelRenderScale.bindBidirectional(manipulationTransform.myyProperty());
        modelRenderTranslationX.bindBidirectional(manipulationTransform.txProperty());
        modelRenderTranslationY.bindBidirectional(manipulationTransform.tyProperty());
        
        addEventHandler(ScrollEvent.ANY, new TranslationHandler());
        addEventHandler(ZoomEvent.ANY, new ZoomHandler());
    }
    
    private class TranslationHandler implements EventHandler<ScrollEvent>
    {
        @Override
        public void handle(ScrollEvent event)
        {
            if (!event.getEventType().equals(ScrollEvent.SCROLL_STARTED))
            {
                translateModelRender(event.getDeltaX() / getModelRenderScale(), event.getDeltaY() / getModelRenderScale());
            }
            
            event.consume();
        }
    }
    
    private class ZoomHandler implements EventHandler<ZoomEvent>
    {
        @Override
        public void handle(ZoomEvent event)
        {
            if (event.getEventType().equals(ZoomEvent.ZOOM))
            {
                Point2D point = getModelRenderPane().parentToLocal(event.getX(), event.getY());
                manipulationTransform.appendScale(event.getZoomFactor(), event.getZoomFactor(), point.getX(), point.getY());
                //System.out.println(point.getX() + ", " + point.getY());
                
            }
            else
            {
                //System.out.println(event.getEventType());
            }
            
            event.consume();
        }
    }
    
    private class VertexMarkerListChangeListener implements ListChangeListener<Node>
    {
        private class ModelRenderVertexMarkerAdditionTask implements Runnable
        {
            private final int startIndex;
            private final List<? extends Node> childrenToAdd;
            
            public ModelRenderVertexMarkerAdditionTask(int startIndex, List<? extends Node> childrenToAdd)
            {
                this.startIndex = startIndex;
                this.childrenToAdd = new ArrayList<>(childrenToAdd);
            }
            
            @Override
            public void run()
            {
                ObservableList<Node> modelRenderPaneChildren = GraphInkCanvas.this.modelRenderPane.getChildren();
                int additionIndex = GraphInkCanvas.this.layeredModelRenderChildren.addAllToLayer(1, startIndex, (List)childrenToAdd);
                modelRenderPaneChildren.addAll(additionIndex, childrenToAdd);
                //System.out.println(modelRenderPaneChildren.toString() + "\n");
            }
        }
        
        private class ModelRenderVertexMarkerRemovalTask implements Runnable
        {
            private final List<? extends Node> childrenToRemove;
            
            public ModelRenderVertexMarkerRemovalTask(List<? extends Node> childrenToRemove)
            {
                this.childrenToRemove = new ArrayList<>(childrenToRemove);
            }
            
            @Override
            public void run()
            {
                GraphInkCanvas.this.modelRenderPane.getChildren().removeAll(childrenToRemove);
                GraphInkCanvas.this.layeredModelRenderChildren.removeAllFromLayer(1, (List)childrenToRemove);
            }
        }
        
        @Override
        public void onChanged(ListChangeListener.Change<? extends Node> change)
        {
            while (change.next())
            {
                if (change.wasAdded() && !change.wasRemoved())
                    Platform.runLater(new ModelRenderVertexMarkerAdditionTask(change.getFrom(), change.getAddedSubList()));
                else if (!change.wasAdded() && change.wasRemoved())
                    Platform.runLater(new ModelRenderVertexMarkerRemovalTask(change.getRemoved()));
                else
                    System.out.println("Change not handled.");
            }
        }
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
                ObservableList<Node> modelRenderPaneChildren = GraphInkCanvas.this.modelRenderPane.getChildren();
                int additionIndex = GraphInkCanvas.this.layeredModelRenderChildren.addAllToLayer(0, startIndex, (List)childrenToAdd);
                modelRenderPaneChildren.addAll(additionIndex, childrenToAdd);
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
                GraphInkCanvas.this.modelRenderPane.getChildren().removeAll(childrenToRemove);
                GraphInkCanvas.this.layeredModelRenderChildren.removeAllFromLayer(0, (List)childrenToRemove);
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
    
    private class LabelListChangeListener implements ListChangeListener<Node>
    {
        private class ModelRenderLabelAdditionTask implements Runnable
        {
            private final int startIndex;
            private final List<? extends Node> childrenToAdd;
            
            public ModelRenderLabelAdditionTask(int startIndex, List<? extends Node> childrenToAdd)
            {
                this.startIndex = startIndex;
                this.childrenToAdd = new ArrayList<>(childrenToAdd);
            }
            
            @Override
            public void run()
            {
                ObservableList<Node> modelRenderPaneChildren = GraphInkCanvas.this.modelRenderPane.getChildren();
                int additionIndex = GraphInkCanvas.this.layeredModelRenderChildren.addAllToLayer(2, startIndex, (List)childrenToAdd);
                modelRenderPaneChildren.addAll(additionIndex, childrenToAdd);
            }
        }
        
        private class ModelRenderLabelRemovalTask implements Runnable
        {
            private final List<? extends Node> childrenToRemove;
            
            public ModelRenderLabelRemovalTask(List<? extends Node> childrenToRemove)
            {
                this.childrenToRemove = new ArrayList<>(childrenToRemove);
            }
            
            @Override
            public void run()
            {
                GraphInkCanvas.this.modelRenderPane.getChildren().removeAll(childrenToRemove);
                GraphInkCanvas.this.layeredModelRenderChildren.removeAllFromLayer(2, (List)childrenToRemove);
            }
        }
        
        @Override
        public void onChanged(ListChangeListener.Change<? extends Node> change)
        {
            while (change.next())
            {
                if (change.wasAdded() && !change.wasRemoved())
                    Platform.runLater(new ModelRenderLabelAdditionTask(change.getFrom(), change.getAddedSubList()));
                else if (!change.wasAdded() && change.wasRemoved())
                    Platform.runLater(new ModelRenderLabelRemovalTask(change.getRemoved()));
                else
                    System.out.println("Change not handled.");
            }
        }
    }
    
    protected Affine getManipulationTransform()
    {
        return manipulationTransform;
    }
    
    public DoubleProperty modelRenderTanslationXProperty()
    {
        return modelRenderTranslationX;
    }
    
    public DoubleProperty modelRenderTanslationYProperty()
    {
        return modelRenderTranslationY;
    }
    
    public void setModelRenderTranslationX(double x)
    {
        modelRenderTranslationX.set(x);
    }
    
    public void setModelRenderTranslationY(double y)
    {
        modelRenderTranslationY.set(y);
    }
    
    public double getModelRenderTranslationX()
    {
        return modelRenderTranslationX.get();
    }
    
    public double getModelRenderTranslationY()
    {
        return modelRenderTranslationY.get();
    }
    
    public void translateModelRender(double deltaX, double deltaY)
    {
        manipulationTransform.appendTranslation(deltaX, deltaY);
    }
    
    public void translateModelRenderTo(double x, double y)
    {
        manipulationTransform.setTx(x);
        manipulationTransform.setTy(y);
    }
    
    public DoubleProperty modelRenderScaleProperty()
    {
        return modelRenderScale;
    }
    
    public void setModelRenderScale(double scale)
    {
        modelRenderScale.set(scale);
    }
    
    public double getModelRenderScale()
    {
        return modelRenderScale.get();
    }
    
    public void clearModelRender()
    {
        vertexMarkers.clear();
        edgeLines.clear();
        labels.clear();
        defaultGraphBounds.set(null);
        graphBounds.set(null);
    }
    
    public void clearGraphModel()
    {
        if (graphModel.get() != null)
        {
            clearModelRender();
            graphModel.set(null);
        }
    }
    
    protected ObservableList<Node> getVertexMarkers()
    {
        return vertexMarkers;
    }
    
    protected ObservableList<Line> getEdgeLines()
    {
        return edgeLines;
    }
    
    protected ObservableList<Node> getLabels()
    {
        return labels;
    }
    
    protected Pane getModelRenderPane()
    {
        return modelRenderPane;
    }
    
    public double getGraphWidth()
    {
        return (graphBounds == null ? 0.0 : graphBounds.get().getWidth());
    }
    
    public double getGraphHeight()
    {
        return (graphBounds == null ? 0.0 : graphBounds.get().getHeight());
    }
    
    protected void setGraphBounds(BoundingBox graphBounds)
    {
        this.graphBounds.set(graphBounds);
    }
    
    public BoundingBox getGraphBounds()
    {
        return graphBounds.get();
    }
    
    public final ObjectProperty<BoundingBox> graphBoundsProperty()
    {
        return graphBounds;
    }
    
    public double getDefaultGraphWidth()
    {
        return (defaultGraphBounds == null ? 0.0 : defaultGraphBounds.get().getWidth());
    }
    
    public double getDefaultGraphHeight()
    {
        return (defaultGraphBounds == null ? 0.0 : defaultGraphBounds.get().getHeight());
    }
    
    protected void setDefaultGraphBounds(BoundingBox defaultGraphBounds)
    {
        this.defaultGraphBounds.set(defaultGraphBounds);
    }
    
    public BoundingBox getDefaultGraphBounds()
    {
        return defaultGraphBounds.get();
    }
    
    public final ObjectProperty<BoundingBox> defaultGraphBoundsProperty()
    {
        return defaultGraphBounds;
    }
    
    public void changeGraphWidthAndHeight(double widthDelta, double heightDelta)
    {
        setGraphDimensions(getGraphWidth() + widthDelta, getGraphHeight() + heightDelta);
    }
    
    public void changeGraphWidth(double delta)
    {
        changeGraphWidthAndHeight(delta, 0.0);
    }
    
    public void changeGraphHeight(double delta)
    {
        changeGraphWidthAndHeight(0.0, delta);
    }
    
    public void resetGraphWidth()
    {
        setGraphDimensions(getDefaultGraphWidth(), getGraphHeight());
    }

    public void resetGraphHeight()
    {
        setGraphDimensions(getGraphWidth(), getDefaultGraphHeight());
    }

    public void resetGraphDimensions()
    {
        setGraphDimensions(getDefaultGraphWidth(), getDefaultGraphHeight());
    }
    
    public boolean setGraphDimensions(double width, double height)
    {
        if (width <= 0)
            width = 1;
        
        if (height <= 0)
            height = 1;
        
        if (width != getGraphWidth() || height != getGraphHeight())
        {
            BoundingBox oldBounds = getGraphBounds();
            setGraphBounds(new BoundingBox(oldBounds.getMinX(), oldBounds.getMinY(), width, height));
            return true;
        }
        
        return false;
    }
    
    protected abstract void layoutGraphModel();
    
    protected abstract void updateLayout();
    
    public void setGraphModel(Graph graph)
    {
        if (graphModel.get() != graph)
        {
            clearGraphModel();
            graphModel.set(graph);
            layoutGraphModel();
        }
    }
    
    public final Graph getGraphModel()
    {
        return graphModel.get();
    }
    
    public final ObjectProperty<Graph> graphModelProperty()
    {
        return graphModel;
    }
}
