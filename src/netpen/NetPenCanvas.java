/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netpen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import javafx.util.Pair;
import netpen.utility.ink.InkUtility;
import netpen.utility.ink.recognition.InkGesture;
import netpen.utility.ink.recognition.InkGestureListener;
import netpen.utility.ink.recognition.InkGestureRecognizer;
import netpen.utility.ink.recognition.gesture.TapRecognitionProcedure;

/**
 *
 * @author awehrer
 */
public class NetPenCanvas extends GraphInkCanvas
{
    private final double textRowHeight = 30.0;
    private final double branchLabelWidth = 200.0;
    
    private Color defaultEdgeColor;
    private Color defaultVertexFillColor;
    private Color defaultVertexStrokeColor;
    private double vertexRadius;
    private double layoutXScale; // layout scale
    private double layoutYScale; // layout scale
    private double layoutInitX;
    private double layoutInitY;
    private double vertexSpacing;
    
    private Graph lastModelCached;
    private final HashMap<Vertex, VertexMetadata> vertexMetadataCache;
    private final HashMap<String, AttributeMetadata> attrMetadataCache;
    private final LinkedList<EdgeMetadata> edgeMetadataCache;
    //private final TreeSet<Vertex> collapsedCache;
    private ObservableSet<Vertex> selectedVertices;
    private List<Polygon> selectionPolygons;
    
    private ScrollPane viewport;
    
    private StringProperty colorByAttribute;
    
    public NetPenCanvas()
    {
        defaultEdgeColor = Color.GRAY;
        defaultVertexFillColor = Color.WHITESMOKE;
        defaultVertexStrokeColor = Color.TAN;
        
        vertexRadius = 5.0;
        layoutXScale = 1.0;
        layoutYScale = 1.0;
        layoutInitX = 36.0;
        layoutInitY = 36.0;
        vertexSpacing = 10.0;
        
        vertexMetadataCache = new HashMap<>();
        attrMetadataCache = new HashMap<>();
        edgeMetadataCache = new LinkedList<>();
        //collapsedCache = new TreeSet<>();
        selectedVertices = FXCollections.observableSet();
        selectionPolygons = new ArrayList<>();
        
        colorByAttribute = new SimpleStringProperty(this, null);
        
        InkGestureRecognizer recognizer = new InkGestureRecognizer();
        recognizer.addGestureRecognitionProcedure(new TapRecognitionProcedure());
        setProcessInkGestureRecognizer(recognizer);
        setGestureRecognizer(new NetPenInkGestureRecognizer());
        
        selectedVertices.addListener(new SetChangeListener<Vertex>()
        {
            @Override
            public void onChanged(SetChangeListener.Change<? extends Vertex> change)
            {
                if (change.wasAdded())
                {
                    NetPenCanvas.this.vertexMetadataCache.get(change.getElementAdded()).selected = true;
                }
                else // was removed
                {
                    NetPenCanvas.this.vertexMetadataCache.get(change.getElementRemoved()).selected = false;
                }
            }
        });
        
        this.addListener(new InkGestureListener()
        {
            @Override
            public void gestureRecognized(InkGesture inkGesture)
            {
                if (inkGesture != null)
                {
                    String gestureIdentifier = inkGesture.getIdentifier();

                    switch (gestureIdentifier)
                    {
                        case "Rectangle":
                            processRectangleZoomGesture((Rectangle)inkGesture.getIdentifiedShape());
                            break;
                        case "Lasso":
                            processLassoSelectionGesture((Polygon)inkGesture.getIdentifiedShape());
                            break;
                    }
                }
            }
        });
        
        addEventHandler(ScrollEvent.ANY, new EventHandler<ScrollEvent>()
        {
            @Override
            public void handle(ScrollEvent event)
            {
                if (!NetPenCanvas.this.getSelectedVertices().isEmpty())
                    NetPenCanvas.this.clearSelection();
            }
        });
        
        addEventHandler(ZoomEvent.ANY, new EventHandler<ZoomEvent>()
        {
            @Override
            public void handle(ZoomEvent event)
            {
                if (!NetPenCanvas.this.getSelectedVertices().isEmpty())
                    NetPenCanvas.this.clearSelection();
            }
        });
    }
    
    private void processLassoSelectionGesture(Polygon selectionArea)
    {
        List<Point2D> selectionAreaPoints = new ArrayList<>(selectionArea.getPoints().size() / 2);
        
        // figure out which vertices have been selected
        Iterator<Double> pointIterator = selectionArea.getPoints().iterator();
        
        while (pointIterator.hasNext())
            selectionAreaPoints.add(new Point2D(pointIterator.next(), pointIterator.next()));
        
        Circle vertexMarker;
        Point2D testPoint;
        
        for (Vertex vertex : getGraphModel().getVertices())
        {
            vertexMarker = (Circle) vertexMetadataCache.get(vertex).marker;
            testPoint = new Point2D(innerToOuterX(vertexMarker.getCenterX()), innerToOuterY(vertexMarker.getCenterY()));
            
            if (InkUtility.isPointInPolygon(selectionAreaPoints, testPoint, selectionArea.getBoundsInLocal()))
                getSelectedVertices().add(vertex);
        }
        
        selectionArea.setFill(Color.LIGHTGRAY);
        selectionArea.setOpacity(0.5);
        selectionArea.setStroke(getDrawingAttributes().getColor());
        selectionArea.setStrokeWidth(getDrawingAttributes().getStrokeWidth());
        
        this.getChildren().add(selectionArea);
        selectionPolygons.add(selectionArea);
        
        updateLayout();
        getInkStrokes().clear();
    }
    
    private void processRectangleZoomGesture(Rectangle zoomRectangle)
    {
        final Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(25.0), new RectangleZoomHandler(zoomRectangle, timeline)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        getInkStrokes().clear();
    }
    
    private class RectangleZoomHandler implements EventHandler<ActionEvent>
    {
        private final Timeline timeline;
        private final Rectangle zoomRectangle2;
        private long lastUpdateTime;
        private final double zoomScaleTarget;
        private final double zoomTargetX;
        private final double zoomTargetY;
        
        public RectangleZoomHandler(Rectangle zoomRectangle, Timeline timeline)
        {
            // convert corners to inner canvas extents
            double upperLeftX = outerToInnerX(zoomRectangle.getX()), upperLeftY = outerToInnerY(zoomRectangle.getY());
            double lowerRightX = outerToInnerX(zoomRectangle.getX() + zoomRectangle.getWidth()), lowerRightY = outerToInnerY(zoomRectangle.getY() + zoomRectangle.getHeight());
            Rectangle zoomRectangle2 = new Rectangle(upperLeftX, upperLeftY, lowerRightX - upperLeftX, lowerRightY - upperLeftY);
            zoomRectangle2.setFill(zoomRectangle.getFill());
            zoomRectangle2.setStroke(zoomRectangle.getStroke());
            zoomRectangle2.setStrokeWidth(zoomRectangle.getStrokeWidth());
            getModelRenderPane().getChildren().add(zoomRectangle2);
            double zoomFactor = Math.min(getViewport().getViewportBounds().getWidth() / zoomRectangle.getWidth(), getViewport().getViewportBounds().getHeight() / zoomRectangle.getHeight());
            zoomScaleTarget = getModelRenderScale() * zoomFactor;
            zoomTargetX = zoomRectangle2.getX() + (zoomRectangle2.getWidth() / 2.0);
            zoomTargetY = zoomRectangle2.getY() + (zoomRectangle2.getHeight() / 2.0);
            
            this.zoomRectangle2 = zoomRectangle2;
            this.timeline = timeline;
            this.lastUpdateTime = System.currentTimeMillis();
        }
        
        @Override
        public void handle(ActionEvent event)
        {
            double zoomRate = 2.5; // per second
            long currentTime = System.currentTimeMillis();

            double newScale = Math.min(getModelRenderScale() + zoomRate * ((currentTime - lastUpdateTime) / 1000.0f), zoomScaleTarget);
            setModelRenderScale(newScale);
            translateModelRenderTo(-(zoomTargetX * newScale - getViewport().getViewportBounds().getWidth() * 0.5), -(zoomTargetY * newScale - getViewport().getViewportBounds().getHeight() * 0.5));

            lastUpdateTime = currentTime;
            
            if (newScale == zoomScaleTarget)
            {
                getModelRenderPane().getChildren().remove(zoomRectangle2);
                timeline.stop();
            }
        }
    }
    
    public ScrollPane getViewport()
    {
        return viewport;
    }
    
    public void setViewport(ScrollPane viewport)
    {
        this.viewport = viewport;
    }
    
    private double outerToInnerX(double x) { return (x - getModelRenderTranslationX()) / getModelRenderScale(); }
    private double outerToInnerY(double y) { return (y - getModelRenderTranslationY()) / getModelRenderScale(); }

    private double innerToOuterX(double x) { return x * getModelRenderScale() + getModelRenderTranslationX(); }
    private double innerToOuterY(double y) { return y * getModelRenderScale() + getModelRenderTranslationY(); }
    
    private class VertexMetadata
    {
        public Node marker;
        public Label label;
        public Paint fill;
        public Paint stroke;
	public int index;
        public boolean selected;
        public boolean selectedBeforeHover;
        public LinkedList<Pair<Line, Edge>> lineEdgePairs;
        
        public VertexMetadata()
        {
            lineEdgePairs = new LinkedList<>();
            selected = false;
            selectedBeforeHover = false;
        }
        
        public void addLineEdgePair(Line edgeLine, Edge edge)
        {
            lineEdgePairs.add(new Pair<>(edgeLine, edge));
        }
        
        public boolean removeLineEdgePair(Line edgeLine, Edge edge)
        {
            return lineEdgePairs.remove(new Pair<>(edgeLine, edge));
        }
    }
    
    private class AttributeMetadata
    {
        double minVal = Double.MAX_VALUE;
        double maxVal = Double.MIN_VALUE;
        double range = Double.MIN_VALUE;
    }
    
    private class EdgeMetadata
    {
        public Edge edge;
        public Line edgeLine;
    }
    
    protected void clearCache()
    {
        lastModelCached = null;
        vertexMetadataCache.clear();
    }
    
    protected Vertex getVertex(Node vertexMarker)
    {
        for (Map.Entry<Vertex, VertexMetadata> entry : vertexMetadataCache.entrySet())
        {
            if (entry.getValue().marker == vertexMarker)
            {
                return entry.getKey();
            }
        }
        
        return null;
    }
    
    private static double roundDouble(double value, int numDecimalPlaces)
    {
        double factor = Math.pow(10, numDecimalPlaces);
        return Math.round(value * factor) / factor;
    }
    
    public StringProperty colorByAttributeProperty()
    {
        return colorByAttribute;
    }
    
    public void setColorByAttribute(String attributeName)
    {
        colorByAttributeProperty().set(attributeName);
    }
    
    public String getColorByAttribute()
    {
        return colorByAttributeProperty().get();
    }
    
    private Color colorAttribLow = Color.BLACK;
    private Color colorAttribHigh = Color.WHITE;
    private Color colorAttribIfNo = Color.BLACK;
    
    public void SetColorAttribLow(Color c) { colorAttribLow = c; }
    public void SetColorAttribHigh(Color c) { colorAttribHigh = c; }
    public void SetColorAttribIfNo(Color c) { colorAttribIfNo = c; }
    
    // used when changing colorbyattribute, either intentionally or when an attribute disappears from all nodes
    public void recolorVertices()
    {
        AttributeMetadata attrMetadata = (getColorByAttribute() != null) ? attrMetadataCache.getOrDefault(getColorByAttribute(), null) : null;
        
        if (attrMetadata != null && attrMetadata.range < 1e-11)
            attrMetadata.range = attrMetadata.maxVal - attrMetadata.minVal;

        for (Map.Entry<Vertex,VertexMetadata> kv : vertexMetadataCache.entrySet())
        {
            VertexMetadata vertexMetadata = kv.getValue();
            Vertex vertex = kv.getKey();
            
            if (getColorByAttribute() != null)
            {
                if (attrMetadata != null)
                {
                    Double d = vertex.getNumericalAttribute(getColorByAttribute());
                    // alternate approach: http://stackoverflow.com/questions/4414673/android-color-between-two-colors-based-on-percentage
                    if (d > -Double.MAX_VALUE + 1)
                    {
                        vertexMetadata.fill = colorAttribLow.interpolate(colorAttribHigh, (d - attrMetadata.minVal) / attrMetadata.range);
                    }
                    else
                    {
                        vertexMetadata.fill = colorAttribIfNo;
                    }
                }
                else
                {
                    vertexMetadata.fill = colorAttribIfNo;
                }
            }
            else
            {
                vertexMetadata.fill = defaultVertexFillColor;
            }
        }
        
        updateLayout();
    }
    
    // passing null resets the sort to the vertices' natural order.
    public void sortVerticesByAttribute(final String attributeName)
    {
        if (attributeName != null)
        {
            List<Vertex> sortedList = new ArrayList<>(getGraphModel().getVertices());
            Collections.sort(sortedList, new Comparator<Vertex>()
            {
                @Override
                public int compare(Vertex o1, Vertex o2)
                {
                    Object attributeValue1 = o1.getAttributeValue(attributeName);
                    Object attributeValue2 = o2.getAttributeValue(attributeName);

                    if (attributeValue1 != null && attributeValue2 != null)
                    {
                        if (attributeValue1 instanceof Integer)
                            return ((Integer)attributeValue1).compareTo((Integer)attributeValue2);
                        else if (attributeValue1 instanceof Double)
                            return ((Double)attributeValue1).compareTo((Double)attributeValue2);
                        else if (attributeValue1 instanceof Float)
                            return ((Float)attributeValue1).compareTo((Float)attributeValue2);
                        else if (attributeValue1 instanceof String)
                            return ((String)attributeValue1).compareTo((String)attributeValue2);
                        else
                            return 0;
                    }
                    else if (attributeValue1 != null && attributeValue2 == null)
                    {
                        return 1;
                    }
                    else if (attributeValue1 == null && attributeValue2 != null)
                    {
                        return -1;
                    }
                    else
                    {
                        return 0;
                    }
                }
            });

            int index = 0;

            for (Vertex vertex : sortedList)
            {
                vertexMetadataCache.get(vertex).index = index;
                index++;
            }
        }
        else
        {
            int index = 0;
            
            for (Vertex vertex : getGraphModel().getVertices())
            {
                vertexMetadataCache.get(vertex).index = index;
                index++;
            }
        }
        
        updateLayout();
    }
    
    protected Node createVertexMarker(double centerX, double centerY, double radius, final Vertex vertex)
    {
        Circle vertexCircle = new Circle(centerX, centerY, radius);
        vertexCircle.setStrokeWidth(2.0);
        
        vertexCircle.setFill(defaultVertexFillColor);
        vertexCircle.setStroke(defaultVertexStrokeColor);
        
        vertexCircle.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                if (!vertexMetadataCache.get(vertex).selected)
                {
                    NetPenCanvas.this.getSelectedVertices().add(vertex);
                    NetPenCanvas.this.updateLayout();
                    vertexMetadataCache.get(vertex).selectedBeforeHover = false;
                }
                else
                {
                    vertexMetadataCache.get(vertex).selectedBeforeHover = true;
                }

                for (VertexMetadata vertexMetadata : vertexMetadataCache.values())
                    vertexMetadata.marker.setOpacity(0.5);
                
                Circle vertexMarker;
                
                for (Pair<Line, Edge> lineEdgePair : NetPenCanvas.this.vertexMetadataCache.get(vertex).lineEdgePairs)
                {
                    lineEdgePair.getKey().setVisible(true);
                    vertexMarker = (Circle) vertexMetadataCache.get(lineEdgePair.getValue().getOtherEndpoint(vertex)).marker;
                    vertexMarker.setOpacity(1.0);
                    vertexMarker.setFill(Color.LIME);
                    vertexMarker.setStroke(Color.LIMEGREEN);
                }
                
                for (Vertex selectedVertex : NetPenCanvas.this.getSelectedVertices())
                    ((Circle)vertexMetadataCache.get(selectedVertex).marker).setOpacity(1.0);
            }
        });
        
        vertexCircle.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                for (VertexMetadata vertexMetadata : vertexMetadataCache.values())
                    vertexMetadata.marker.setOpacity(1.0);
                
                for (Pair<Line, Edge> lineEdgePair : NetPenCanvas.this.vertexMetadataCache.get(vertex).lineEdgePairs)
                    lineEdgePair.getKey().setVisible(false);
                
                if (!vertexMetadataCache.get(vertex).selectedBeforeHover)
                {
                    NetPenCanvas.this.getSelectedVertices().remove(vertex);
                    vertexMetadataCache.get(vertex).selectedBeforeHover = false;
                }
                
                NetPenCanvas.this.updateLayout();
            }
        });
        
        return vertexCircle;
    }
    
    protected void updateVertexMarker(Node vertexMarker, double centerX, double centerY, double radius, Vertex vertex, VertexMetadata metadata)
    {
        Circle vertexCircle = (Circle) vertexMarker;
        vertexCircle.setCenterX(centerX);
        vertexCircle.setCenterY(centerY);
        vertexCircle.setRadius(radius);
        
        if (metadata.selected)
        {
            vertexCircle.setFill(Color.YELLOW);
            vertexCircle.setStroke(Color.GOLDENROD);
        }
        else
        {
            vertexCircle.setFill(metadata.fill);
            vertexCircle.setStroke(metadata.stroke);
        }
    }
    
    private Node getUpdatedVertexMarker(double centerX, double centerY, Vertex vertex, VertexMetadata metadata)
    {
        Node vertexMarker;
        
        if (metadata.marker == null)
        {
            vertexMarker = createVertexMarker(centerX, centerY, vertexRadius, vertex);
            metadata.marker = vertexMarker;
            getVertexMarkers().add(vertexMarker);
            
            if (vertexMarker instanceof Shape)
            {
                metadata.fill = ((Shape)vertexMarker).getFill();
                metadata.stroke = ((Shape)vertexMarker).getStroke();
                
                if (metadata.selected)
                {
                    ((Shape)vertexMarker).setFill(Color.YELLOW);
                    ((Shape)vertexMarker).setStroke(Color.GOLDENROD);
                }
            }
        }
        else
        {
            vertexMarker = metadata.marker;
            updateVertexMarker(vertexMarker, centerX, centerY, vertexRadius, vertex, metadata);
        }
        
        return vertexMarker;
    }
    
    private Line createEdgeLine(double x0, double y0, double x1, double y1)
    {
        Line edgeLine = new Line(x0, y0, x1, y1);
        edgeLine.setStroke(defaultEdgeColor);
        edgeLine.setVisible(false);
        return edgeLine;
    }
    
    @Override
    protected void layoutGraphModel()
    {
        if (getGraphModel() != null)
        {
            int maxNumVerticesPerRow = (int) Math.ceil(Math.sqrt(getGraphModel().getNumVertices()));
            
            Iterator<Vertex> vertexIterator = getGraphModel().createVertexIterator();
            VertexMetadata vertexMetadata;
            Vertex vertex;
            int vertexNum = 0;
            int col, row;
            
            while (vertexIterator.hasNext())
            {
                vertex = vertexIterator.next();
                vertexMetadata = vertexMetadataCache.get(vertex);

                if (vertexMetadata == null)
                {
                    vertexMetadata = new VertexMetadata();
                    vertexMetadata.index = vertexNum;
                    vertexMetadataCache.put(vertex, vertexMetadata);
                }
                
                col = vertexMetadata.index % maxNumVerticesPerRow;
                row = vertexMetadata.index / maxNumVerticesPerRow;

                getUpdatedVertexMarker(layoutInitX + layoutXScale * (col * (vertexSpacing + vertexRadius * 2.0)) + vertexRadius,
                        layoutInitY + layoutYScale * (row * (vertexSpacing + vertexRadius * 2.0)) + vertexRadius,
                        vertex, vertexMetadata);

                Map<String, Double> nattrs = vertex.getNumericalAttributeMap();
                
                for (Map.Entry<String, Double> kv : nattrs.entrySet())
                {
                    AttributeMetadata attrMetadata = attrMetadataCache.get(kv.getKey());
					
                    if (attrMetadata == null)
                    {
                        attrMetadata = new AttributeMetadata();
                        attrMetadataCache.put(kv.getKey(), attrMetadata);
                    }
					
                    if (kv.getValue() < attrMetadata.minVal) attrMetadata.minVal = kv.getValue();
                    if (kv.getValue() > attrMetadata.maxVal) attrMetadata.maxVal = kv.getValue();
                }
                
                vertexNum++;
            }
            
            int maxRow = getGraphModel().getNumVertices() / maxNumVerticesPerRow;
            int maxCol;
            
            if (maxRow == 0)
                maxCol = getGraphModel().getNumVertices() - 1;
            else
                maxCol = maxNumVerticesPerRow;
            
            setDefaultGraphBounds(new BoundingBox(layoutInitX, layoutInitY,
                    maxCol * vertexSpacing + (maxCol + 1) * vertexRadius * 2.0,
                    maxRow * vertexSpacing + (maxRow + 1) * vertexRadius * 2.0));
            setGraphBounds(new BoundingBox(layoutInitX, layoutInitY,
                    layoutXScale * (maxCol * vertexSpacing + (maxCol + 1) * vertexRadius * 2.0),
                    layoutYScale * (maxRow * vertexSpacing + (maxRow + 1) * vertexRadius * 2.0)));
            
            Edge edge;
            EdgeMetadata edgeMetadata;
            VertexMetadata sourceMetadata, targetMetadata;
            Node sourceMarker, targetMarker;
            Iterator<Edge> edgeIterator = getGraphModel().createEdgeIterator();
            ListIterator<EdgeMetadata> edgeMetadataIterator = edgeMetadataCache.listIterator();
            
            while (edgeIterator.hasNext())
            {
                edge = edgeIterator.next();
                edgeMetadata = null;
                
                while (edgeMetadataIterator.hasNext())
                {
                    edgeMetadata = edgeMetadataIterator.next();
                    
                    if (edgeMetadata.edge == edge)
                        break;
                    else
                    {
                        edgeMetadataIterator.remove();
                        getEdgeLines().remove(edgeMetadata.edgeLine);
                        vertexMetadataCache.get(edgeMetadata.edge.getSource()).removeLineEdgePair(edgeMetadata.edgeLine, edgeMetadata.edge);
                        vertexMetadataCache.get(edgeMetadata.edge.getTarget()).removeLineEdgePair(edgeMetadata.edgeLine, edgeMetadata.edge);
                    }
                }
                
                sourceMetadata = vertexMetadataCache.get(edge.getSource());
                sourceMarker = sourceMetadata.marker;
                targetMetadata = vertexMetadataCache.get(edge.getTarget());
                targetMarker = targetMetadata.marker;
                
                if (edgeMetadata == null)
                {
                    edgeMetadata = new EdgeMetadata();
                    edgeMetadata.edge = edge;
                    edgeMetadata.edgeLine = createEdgeLine(
                            ((Circle)sourceMarker).getCenterX(),
                            ((Circle)sourceMarker).getCenterY(),
                            ((Circle)targetMarker).getCenterX(),
                            ((Circle)targetMarker).getCenterY());
                    
                    getEdgeLines().add(edgeMetadata.edgeLine);
                    sourceMetadata.addLineEdgePair(edgeMetadata.edgeLine, edgeMetadata.edge);
                    targetMetadata.addLineEdgePair(edgeMetadata.edgeLine, edgeMetadata.edge);
                    edgeMetadataIterator.add(edgeMetadata);
                }
                else // update existing line
                {
                    edgeMetadata.edgeLine.setStartX(((Circle)sourceMarker).getCenterX());
                    edgeMetadata.edgeLine.setStartY(((Circle)sourceMarker).getCenterY());
                    edgeMetadata.edgeLine.setEndX(((Circle)targetMarker).getCenterX());
                    edgeMetadata.edgeLine.setEndY(((Circle)targetMarker).getCenterY());
                }
            }
        }
    }
    
    @Override
    public void updateLayout()
    {
        layoutGraphModel();
    }
    
    @Override
    public boolean setGraphDimensions(double width, double height)
    {
        if (super.setGraphDimensions(width, height) && getDefaultGraphWidth() > 0
                && getDefaultGraphHeight() > 0)
        {
            layoutXScale = getGraphWidth() / getDefaultGraphWidth();
            layoutYScale = getGraphHeight() / getDefaultGraphHeight();
            updateLayout();
            return true;
        }
        
        return false;
    }
    
    public void setVertexRadius(double radius)
    {
        this.vertexRadius = radius;
        updateLayout();
    }
    
    public double getVertexRadius()
    {
        return vertexRadius;
    }
    
    @Override
    public void clearModelRender()
    {
        super.clearModelRender();
        clearCache();
    }
    
    public ObservableSet<Vertex> getSelectedVertices()
    {
        return selectedVertices;
    }
    
    @Override
    protected Map<String, Object> collectContextDataForInkRecognition()
    {
        Map<String, Object> contextData = new TreeMap<>();
        List<Point2D> selectablePoints = new ArrayList<>();
        Circle vertexCircle;
        
        for (Node vertexMarker : this.getVertexMarkers())
        {
            if (vertexMarker instanceof Circle)
            {
                vertexCircle = (Circle) vertexMarker;
                selectablePoints.add(new Point2D(innerToOuterX(vertexCircle.getCenterX()), innerToOuterY(vertexCircle.getCenterY())));
            }
        }
        
        contextData.put("selectablePoints", selectablePoints);
        
        return contextData;
    }
    
    public void clearSelection()
    {
        getSelectedVertices().clear();
        getChildren().removeAll(selectionPolygons);
        selectionPolygons.clear();
        updateLayout();
    }
}
