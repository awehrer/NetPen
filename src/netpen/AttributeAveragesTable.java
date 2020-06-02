/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netpen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/**
 *
 * @author awehrer
 */
public class AttributeAveragesTable extends TableView<AttributeAveragesTableRowData>
{
    private NetPenCanvas canvas;
    
    private final SetChangeListener<Vertex> vertexSelectionListener;
    private final ListChangeListener<Vertex> vertexListChangeListener;
    private final MapChangeListener<String, Integer> attributeListChangeListener;

    
    public class BlarghColCb implements Callback<TableColumn<AttributeAveragesTableRowData, String>,TableCell<AttributeAveragesTableRowData, String>>
    {
        @Override
        public TableCell<AttributeAveragesTableRowData, String> call(TableColumn<AttributeAveragesTableRowData, String> param) {
            return new TableCell<AttributeAveragesTableRowData, String>()
            {
                @Override
                public void updateItem(String string, boolean isEmpty)
                {
                    super.updateItem(string, isEmpty);
                    if (!isEmpty)
                    {
                        // http://code.makery.ch/blog/javafx-8-tableview-cell-renderer/
                        // http://stackoverflow.com/questions/12594557/how-to-add-tooltip-for-table-column
                        ObservableList<AttributeAveragesTableRowData> l = getTableView().getItems();
                        if (getTableRow() != null)
                        {
                            int i = getTableRow().getIndex();
                            AttributeAveragesTableRowData data = l.get(i);
                            if (data.getValueRangeSummary() != null && !data.getValueRangeSummary().equals(""))
                            {
                                setText(data.getValueRangeSummary());
                                setTooltip(new Tooltip(data.getValueRangeFull()));
                            }
                            else
                            {
                                setText("");
                                setTooltip(null);
                            }
                        }
                        else
                        {
                            setText("");
                            setTooltip(null);
                        }
                    }
                }
            };
        }
    }

    public class AvgPosColCb implements Callback<TableColumn<AttributeAveragesTableRowData, String>,TableCell<AttributeAveragesTableRowData, String>>
    {
        @Override
        public TableCell<AttributeAveragesTableRowData, String> call(TableColumn<AttributeAveragesTableRowData, String> param) {
            return new TableCell<AttributeAveragesTableRowData, String>()
            {
                @Override
                public void updateItem(String string, boolean isEmpty)
                {
                    super.updateItem(string, isEmpty);
                    if (!isEmpty)
                    {
                        // http://code.makery.ch/blog/javafx-8-tableview-cell-renderer/
                        // http://stackoverflow.com/questions/12594557/how-to-add-tooltip-for-table-column
                        ObservableList<AttributeAveragesTableRowData> l = getTableView().getItems();
                        if (getTableRow() != null)
                        {
                            int i = getTableRow().getIndex();
                            AttributeAveragesTableRowData data = l.get(i);
                            if (data.getAverage() >= -1e-9)
                            {
                                setText(Double.toString(data.getAverage()));
                            }
                            else
                            {
                                setText("");
                            }
                            if (data.getValueRangeSummary() != null && !data.getValueRangeSummary().equals(""))
                            {
                                setTooltip(new Tooltip(data.getValueRangeFull()));
                            }
                            else
                            {
                                setTooltip(null);
                            }
                        }
                        else
                        {
                            setText("");
                            setTooltip(null);
                        }
                    }
                }
            };
        }
    }
    
    public class SdevPosColCb implements Callback<TableColumn<AttributeAveragesTableRowData, String>,TableCell<AttributeAveragesTableRowData, String>>
    {
        @Override
        public TableCell<AttributeAveragesTableRowData, String> call(TableColumn<AttributeAveragesTableRowData, String> param) {
            return new TableCell<AttributeAveragesTableRowData, String>()
            {
                @Override
                public void updateItem(String string, boolean isEmpty)
                {
                    super.updateItem(string, isEmpty);
                    if (!isEmpty)
                    {
                        // http://code.makery.ch/blog/javafx-8-tableview-cell-renderer/
                        // http://stackoverflow.com/questions/12594557/how-to-add-tooltip-for-table-column
                        ObservableList<AttributeAveragesTableRowData> l = getTableView().getItems();
                        if (getTableRow() != null)
                        {
                            int i = getTableRow().getIndex();
                            AttributeAveragesTableRowData data = l.get(i);
                            if (data.getStandardDeviation() >= -1e-9)
                            {
                                setText(Double.toString(data.getStandardDeviation()));
                            }
                            else
                            {
                                setText("");
                            }
                            if (data.getValueRangeSummary() != null && !data.getValueRangeSummary().equals(""))
                            {
                                setTooltip(new Tooltip(data.getValueRangeFull()));
                            }
                            else
                            {
                                setTooltip(null);
                            }
                        }
                        else
                        {
                            setText("");
                            setTooltip(null);
                        }
                    }
                }
            };
        }
    }
    
    public AttributeAveragesTable()
    {
        this.setFixedCellSize(70.0);
        this.setItems(FXCollections.observableList(new LinkedList<>()));
        
        // create columns
        // why Number not Double? see http://linux2biz.net/215599/add-progressbar-in-treetableview-returning-double-value-to-observable
        TableColumn<AttributeAveragesTableRowData, String> attributeColumn = new TableColumn<>("Attribute");
        attributeColumn.setCellValueFactory(new PropertyValueFactory("attributeName"));
        attributeColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        
        TableColumn<AttributeAveragesTableRowData, Number> averageColumn = new TableColumn<>("Population Avg");
        averageColumn.setCellValueFactory(new PropertyValueFactory("average"));
        //TableColumn<AttributeAveragesTableRowData, String> averageColumn = new TableColumn<>("Population Avg");
        //averageColumn.setCellFactory(new AvgPosColCb());
        averageColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        
        TableColumn<AttributeAveragesTableRowData, Number> standardDeviationColumn = new TableColumn<>("Standard Dev.");
        standardDeviationColumn.setCellValueFactory(new PropertyValueFactory("standardDeviation"));
        //TableColumn<AttributeAveragesTableRowData, String> standardDeviationColumn = new TableColumn<>("Standard Dev.");
        //standardDeviationColumn.setCellFactory(new SdevPosColCb());
        standardDeviationColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        
        TableColumn<AttributeAveragesTableRowData, String> blarghColumn = new TableColumn<>("Value Set");
        blarghColumn.setCellFactory(new BlarghColCb());
        blarghColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        
        this.getColumns().setAll(attributeColumn, averageColumn, standardDeviationColumn, blarghColumn);
        
        
        vertexSelectionListener = new SetChangeListener<Vertex>()
        {
            @Override
            public void onChanged(SetChangeListener.Change<? extends Vertex> change)
            {
                update();
            }
        };
        
        vertexListChangeListener = new ListChangeListener<Vertex>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Vertex> change)
            {
                update();
            }
        };
        
        attributeListChangeListener = new MapChangeListener<String, Integer>()
        {
            @Override
            public void onChanged(MapChangeListener.Change<? extends String, ? extends Integer> change)
            {
                update();
            }
        };
    }
    
    public void setCanvas(NetPenCanvas canvas)
    {
        if (this.canvas != canvas)
        {
            if (this.canvas != null)
            {
                this.canvas.getSelectedVertices().removeListener(vertexSelectionListener);
                this.canvas.getGraphModel().getVertices().removeListener(vertexListChangeListener);
                this.canvas.getGraphModel().getAttributeList().removeListener(attributeListChangeListener);
            }
            
            this.canvas = canvas;
            
            if (this.canvas != null)
            {
                this.canvas.getSelectedVertices().addListener(vertexSelectionListener);
                this.canvas.getGraphModel().getVertices().addListener(vertexListChangeListener);
                this.canvas.getGraphModel().getAttributeList().addListener(attributeListChangeListener);
            }
            
            update();
        }
    }
    
    public NetPenCanvas getCanvas()
    {
        return canvas;
    }
    
    private double roundDouble(double value, int numDecimalPlaces)
    {
        double factor = Math.pow(10, numDecimalPlaces);
        return Math.round(value * factor) / factor;
    }
    
    protected void update()
    {
        Collection<Vertex> vertices = (getCanvas().getSelectedVertices().size() > 0)
                ? getCanvas().getSelectedVertices() : getCanvas().getGraphModel().getVertices();
        
        //System.out.println(this.getClass().getName() + ": " + vertices.size());
        //(new Throwable()).printStackTrace();
        
        // first pass: get all the attribute values and average for each attribute discovered in selected vertices set
        Map<String, ArrayList<Double>> items = new TreeMap<>();
        Map<String, ArrayList<String>> itemsStr = new TreeMap<>();
        
        for (Vertex vertex : vertices)
        {
            Map<String,String> strs = new TreeMap<>();
            Map<String,Double> nums = vertex.getNumericalAttributeMap(strs);
            
            for (Map.Entry<String,Double> kv : nums.entrySet())
            {
                ArrayList<Double> item = items.getOrDefault(kv.getKey(), null);
                
                if (item == null)
                {
                    item = new ArrayList<>();
                    items.put(kv.getKey(), item);
                }
                
                item.add(kv.getValue());
            }
            
            for (Map.Entry<String,String> kv : strs.entrySet())
            {
                ArrayList<String> item = itemsStr.getOrDefault(kv.getKey(), null);
                
                if (item == null)
                {
                    item = new ArrayList<>();
                    itemsStr.put(kv.getKey(), item);
                }
                
                item.add(kv.getValue());
            }
        }

        // second pass: get the average and stdev for each attribute and update list
        AttributeAveragesTableRowData data;
        ListIterator<AttributeAveragesTableRowData> tableRowDataIterator = getItems().listIterator();

        for (Map.Entry<String, ArrayList<Double>> kv : items.entrySet())
        {
            double avg = 0.0;
            
            for (Double d : kv.getValue())
                avg += (d / kv.getValue().size());
            
            avg = roundDouble(avg, 6);

            double stdev = 0.0;
            
            for (Double d : kv.getValue())
                stdev += Math.pow(d - avg, 2);
            
            stdev = roundDouble((kv.getValue().size() > 1 ? Math.sqrt(stdev / (kv.getValue().size() - 1)) : 0.0), 6);
            
            if (tableRowDataIterator.hasNext())
            {
                data = tableRowDataIterator.next();
                data.setAttributeName(kv.getKey());
                data.setAverage(avg);
                //System.out.println(avg);
                data.setStandardDeviation(stdev);
                data.setValueRangeSummary("");
                data.setValueRangeFull("");
            }
            else
            {
                tableRowDataIterator.add(new AttributeAveragesTableRowData(kv.getKey(), avg, stdev, "", ""));
            }
        }
        
        for (Map.Entry<String, ArrayList<String>> kv : itemsStr.entrySet())
        {
            StringBuilder sb = new StringBuilder(), sb2 = new StringBuilder();
            sb.append("(");
            int i = 0;
            for(String ss : kv.getValue())
            {
                sb.append("\"").append(ss).append("\"");
                if (i == 0) sb2.append("(\"").append(ss).append("\"");
                if (++i != kv.getValue().size())
                    sb.append(", ");
                else
                {
                    if (kv.getValue().size() > 2)
                        sb2.append(", ..., \"").append(ss).append("\"");
                    else
                        sb2.append(", \"").append(ss).append("\"");
                }
            }
            sb.append(")");
            sb2.append(")");
            
            if (tableRowDataIterator.hasNext())
            {
                data = tableRowDataIterator.next();
                data.setAttributeName(kv.getKey());
                data.setAverage(-1.0);
                data.setStandardDeviation(-1.0);
                data.setValueRangeFull(sb.toString());
                data.setValueRangeSummary(sb2.toString());
            }
            else
            {
                tableRowDataIterator.add(new AttributeAveragesTableRowData(kv.getKey(), -1.0, -1.0, sb2.toString(), sb.toString()));
            }
        }
        
        //int selectedIndex = getSelectionModel().getSelectedIndex();
        
        //if (selectedIndex > 0 && selectedIndex < getItems().size())
        //    getSelectionModel().select(selectedIndex);
    }
}