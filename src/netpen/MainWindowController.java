/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netpen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;
import netpen.utility.ink.StylusEvent;
import netpen.utility.ink.recognition.InkGesture;
import netpen.utility.ink.recognition.InkGestureListener;

/**
 *
 * @author awehrer, ayee
 */
public class MainWindowController implements Initializable
{
    //public final String defaultFile = "resources/data/sample.gml";
    //public final String defaultFile = "resources/data/monica_small.json";
    //public final String defaultFile = "resources/data/netpen_twitter_nba_v3.json";
    public final String defaultFile = "resources/data/netpen_twitter_jessica.json";
    //public final String defaultFile = "resources/data/netpen_twitter_thatgrapejuice.json";
    
    public final double CellHeight = 50.0;
    
    // listen to attribute changes in the graph, so we can update the color by attrib selector
    public class AttributeListListener implements MapChangeListener<String, Integer>
    {
        @Override
        public void onChanged(Change<? extends String, ? extends Integer> change) {
            if (change.wasAdded())
            {
                choiceColorAttrib.getItems().add(change.getKey());
            }
            
            if (change.wasRemoved())
            {
                choiceColorAttrib.getItems().remove(change.getKey());
            }
        }
    }
    
    private final double widthHeightChangeDelta;
    private NetPenOptions optionsEntry;
    
    public MainWindowController()
    {
        sidebarAtRight = true;
        widthHeightChangeDelta = 50.0;
    }
    
    @FXML
    private void handleExitAction(ActionEvent event)
    {
        System.exit(0);
    }
    
    @FXML
    private void handleOptionsAction(ActionEvent event)
    {
        new NetPenOptionsDialog(optionsEntry).showAndWait();
    }
    
    @FXML
    private void handleOpenAction(ActionEvent event)
    {
        
    }
    
    @FXML private TextField nodeSizeField;
    
    @FXML
    private void handleNodeSizeChange(ObservableValue<? extends Number> value, Number oldValue, Number newValue)
    {
        nodeSizeField.setText(String.valueOf((((int)(newValue.doubleValue() * 10.0)) / 10.0)));
        netPenCanvas.setVertexRadius(newValue.doubleValue());
    }
    
    @FXML private SplitPane canvasSplitPane;
    private boolean sidebarAtRight;
    
    @FXML
    private void handleSidebarSwipe(SwipeEvent event)
    {
        if (sidebarAtRight && event.getEventType().equals(SwipeEvent.SWIPE_LEFT) || (!sidebarAtRight && event.getEventType().equals(SwipeEvent.SWIPE_RIGHT)))
        {
            double[] splitPositions = canvasSplitPane.getDividerPositions();
            splitPositions[0] = 1.0 - splitPositions[0];
            ObservableList<Node> canvasSplitPaneItems = canvasSplitPane.getItems();
            Node temp1 = canvasSplitPaneItems.remove(0);
            Node temp2 = canvasSplitPaneItems.remove(0);
            canvasSplitPaneItems.add(0, temp2);
            canvasSplitPaneItems.add(1, temp1);
            canvasSplitPane.setDividerPosition(0, splitPositions[0]);
            sidebarAtRight = !sidebarAtRight;
            event.consume();
        }
    }
    
    @FXML
    private void handleStylusMoved(StylusEvent event)
    {
        //System.out.println("Stylus MOVED: " + event);
    }
    
    @FXML
    private void handleStylusDown(StylusEvent event)
    {
        statusRect.setFill(Color.WHITE);
    }
    
    @FXML
    private void handleGraphWidthIncreaseAction(ActionEvent event)
    {
        netPenCanvas.changeGraphWidth(widthHeightChangeDelta / netPenCanvas.getModelRenderScale());
    }
    
    @FXML
    private void handleGraphWidthDecreaseAction(ActionEvent event)
    {
        netPenCanvas.changeGraphWidth(-widthHeightChangeDelta / netPenCanvas.getModelRenderScale());
    }
    
    @FXML
    private void handleGraphHeightIncreaseAction(ActionEvent event)
    {
        netPenCanvas.changeGraphHeight(widthHeightChangeDelta / netPenCanvas.getModelRenderScale());
    }
    
    @FXML
    private void handleGraphHeightDecreaseAction(ActionEvent event)
    {
        netPenCanvas.changeGraphHeight(-widthHeightChangeDelta / netPenCanvas.getModelRenderScale());
    }
    
    @FXML
    private void handleGraphWidthResetAction(ActionEvent event)
    {
        netPenCanvas.resetGraphWidth();
    }
    
    @FXML
    private void handleGraphHeightResetAction(ActionEvent event)
    {
        netPenCanvas.resetGraphHeight();
    }
    
    @FXML
    private void handleGraphDimensionsResetAction(ActionEvent event)
    {
        netPenCanvas.resetGraphDimensions();
    }
    
    private double roundDouble(double value, int numDecimalPlaces)
    {
        double factor = Math.pow(10, numDecimalPlaces);
        return Math.round(value * factor) / factor;
    }
    
    @FXML
    private void handleClearInkAction(ActionEvent event)
    {
        netPenCanvas.getInkStrokes().clear();
    }
    
    @FXML
    private void handleProcessInkAction(ActionEvent event)
    {
        netPenCanvas.processInkForGestureRecognition();
    }
    
    @FXML
    private void handleClearSelectionAction(ActionEvent event)
    {
        netPenCanvas.clearSelection();
    }
    
    private StringProperty colorAttribCurrent = new SimpleStringProperty(this, "colorAttribCurrent", "(none)");
    
    private void handleColorAttrib(String newValue)
    {
        colorAttribCurrent.set(newValue);
        netPenCanvas.recolorVertices();
    }

    private void handleColorAttribLow(Color newValue)
    {
        netPenCanvas.SetColorAttribLow(newValue);
        netPenCanvas.recolorVertices();
    }

    private void handleColorAttribHigh(Color newValue)
    {
        netPenCanvas.SetColorAttribHigh(newValue);
        netPenCanvas.recolorVertices();
    }
    
    private void handleColorAttribNoAttr(Color newValue)
    {
        netPenCanvas.SetColorAttribIfNo(newValue);
        netPenCanvas.recolorVertices();
    }
    
    @FXML private NetPenCanvas netPenCanvas;
    @FXML private GraphInkCanvasModelWIM minigraph;
    @FXML private ScrollPane canvasViewport;
    @FXML private Label translationLabel;
    @FXML private Label zoomLabel;
    @FXML private GraphResizeInkCanvas graphResizeCanvas;
    @FXML private Rectangle statusRect;
    @FXML private Label fileNameLabel;
    @FXML private GraphAttributeTable attribTable;
    @FXML private AttributeAveragesTable attributeAveragesTable;
    @FXML private ListView choiceColorAttrib;
    @FXML private SortByList sortByList;
    
    @FXML private ColorPicker choiceColorAttribLow;
    @FXML private ColorPicker choiceColorAttribHigh;
    @FXML private ColorPicker choiceColorAttribNoAttr;
    
    //@FXML private TextField textColorAttribLowValue;
    //@FXML private TextField textColorAttribHighValue;

    //@FXML private CheckBox checkColorAttribLowAuto;
    //@FXML private CheckBox checkColorAttribHighAuto;
    
    private AttributeListListener attrListener = new AttributeListListener();
    
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        optionsEntry = new NetPenOptions("");
        
        String defaultFontFamily = Font.getDefault().getFamily();
        double defaultFontSize = Font.getDefault().getSize();
        fileNameLabel.setFont(Font.font(defaultFontFamily, FontWeight.NORMAL, defaultFontSize + 2.0));
        
        try
        {            
            File graphFile = new File(defaultFile);
            if (defaultFile.endsWith(".json"))
                netPenCanvas.setGraphModel(Graph.fromJSON(graphFile));
            else
                netPenCanvas.setGraphModel(new Graph(graphFile));
            
            netPenCanvas.colorByAttributeProperty().bindBidirectional(colorAttribCurrent, new StringConverter<String>()
            {

                @Override
                public String toString(String object)
                {
                    if (object.equals("(none)"))
                        return null;
                    else
                        return object;
                }

                @Override
                public String fromString(String string)
                {
                    if (string == null)
                        return "(none)";
                    else
                        return string;
                }
                
            });
            
            // attach observable items/sets/lists to the attribute table, so it can be notified of changes
            attribTable.AttachVertices(netPenCanvas.getGraphModel().getVertices());
            attribTable.AttachAttributeList(netPenCanvas.getGraphModel().getAttributeList());
            attribTable.AttachSelectedVertices(netPenCanvas.getSelectedVertices());

            choiceColorAttribLow.setValue(Color.BLACK);
            choiceColorAttribHigh.setValue(Color.WHITE);
            choiceColorAttribNoAttr.setValue(Color.BLACK);
            
            // https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ListView.html#fixedCellSizeProperty
            choiceColorAttrib.setFixedCellSize(CellHeight);
            
            choiceColorAttrib.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            choiceColorAttrib.setItems(FXCollections.observableArrayList());
            choiceColorAttrib.getItems().add("(none)");
            choiceColorAttrib.getSelectionModel().select(0);
            netPenCanvas.getGraphModel().getAttributeList().addListener(attrListener);
            for(String s : netPenCanvas.getGraphModel().getAttributeList().keySet()) {
                if (!choiceColorAttrib.getItems().contains(s)) choiceColorAttrib.getItems().add(s);
            }
            choiceColorAttrib.getSelectionModel().selectedItemProperty().addListener((observable, oldvalue, newvalue)->handleColorAttrib((String)newvalue));
            choiceColorAttribLow.valueProperty().addListener((observable, oldvalue, newvalue)->handleColorAttribLow((Color)newvalue));
            choiceColorAttribHigh.valueProperty().addListener((observable, oldvalue, newvalue)->handleColorAttribHigh((Color)newvalue));
            choiceColorAttribNoAttr.valueProperty().addListener((observable, oldvalue, newvalue)->handleColorAttribNoAttr((Color)newvalue));
     
            fileNameLabel.setText("File: " + graphFile.getAbsolutePath());
            fileNameLabel.setTooltip(new Tooltip("File: " + graphFile.getAbsolutePath()));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("ERROR: File not found.");
        }
        catch (IOException e)
        {
            System.out.println("ERROR: An I/O exception occurred.");
        }
        
        netPenCanvas.setScratchOutErasingEnabled(true);
        netPenCanvas.setViewport(canvasViewport);
        minigraph.setWorld(netPenCanvas);
        minigraph.setWorldViewport(canvasViewport);
        attributeAveragesTable.setCanvas(netPenCanvas);
        sortByList.setCanvas(netPenCanvas);
        
        canvasViewport.addEventHandler(ScrollEvent.ANY, new EventHandler<ScrollEvent>()
        {
            @Override
            public void handle(ScrollEvent event)
            {
                System.out.println("Warning: ScrollPane Moved.");
                event.consume();
            }
        });
        
        netPenCanvas.modelRenderTanslationXProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                translationLabel.setText("Translation: (" + roundDouble(newValue.doubleValue(), 1) + ", " + roundDouble(netPenCanvas.getModelRenderTranslationY(), 1) + ")");
            }
        });
        
        netPenCanvas.modelRenderTanslationYProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                translationLabel.setText("Translation: (" + roundDouble(netPenCanvas.getModelRenderTranslationX(), 1) + ", " + roundDouble(newValue.doubleValue(), 1) + ")");
            }
        });
        
        netPenCanvas.modelRenderScaleProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                zoomLabel.setText("Zoom: " + roundDouble(newValue.doubleValue() * 100, 1) + "%");
            }
        });
        
        graphResizeCanvas.setGraphCanvas(netPenCanvas);
        
        netPenCanvas.addListener(new InkGestureListener()
        {
            public void gestureRecognized(InkGesture gesture)
            {
                if (gesture == null)
                    statusRect.setFill(Color.RED);
                else
                    statusRect.setFill(Color.LIME);
            }
        });
    }
    
}
