/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netpen;

import java.util.HashMap;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author awehrer, ayee
 */
public class GraphAttributeTable extends TableView
{
    public final double cellHeight = 70.0;
    
    public class AttributeListListener implements MapChangeListener<String, Integer>
    {
        @Override
        public void onChanged(Change<? extends String, ? extends Integer> change) {
            if (hasItems)
            {
                if (change.wasAdded() && !colList.containsKey(change.getKey()))
                {
                    TableColumn<Vertex,String> c = new TableColumn<Vertex,String>(change.getKey());
                    c.setCellValueFactory(new PropertyValueFactory(change.getKey()));
                    getColumns().add(change.getKey());
                    colList.put(change.getKey(), c);
                }
                if (change.wasRemoved() && colList.containsKey(change.getKey()))
                {
                    getColumns().remove(change.getKey());
                    colList.remove(change.getKey());
                }
            }
        }        
    }
    
    public class SelectedVerticesListener implements SetChangeListener<Vertex>
    {
        @Override
        public void onChanged(Change<? extends Vertex> change) {
            
        }
    }
    
    private boolean hasItems = false;
    private HashMap<String,TableColumn<Vertex,String>> colList = new HashMap<String,TableColumn<Vertex,String>>();
    private AttributeListListener attrListener = new AttributeListListener();
    private SelectedVerticesListener selListener = new SelectedVerticesListener();
    
    // https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TableView.html
    // http://stackoverflow.com/questions/25204068/how-do-i-point-a-propertyvaluefactory-to-a-value-of-a-map
    public GraphAttributeTable()
    {   
        this.setFixedCellSize(cellHeight);
        
        // why number not integer? see http://stackoverflow.com/questions/24889638/javafx-properties-in-tableview
        TableColumn<Vertex,Number> idCol = new TableColumn<Vertex,Number>("ID");
        TableColumn<Vertex,String> labelCol = new TableColumn<Vertex,String>("Label");
        
        idCol.setCellValueFactory(item -> item.getValue().idProperty());
        labelCol.setCellValueFactory(item -> item.getValue().labelProperty());
        
        getColumns().setAll(idCol,labelCol);
    }
    
    public void AttachVertices(ObservableList<Vertex> vertices)
    {
        this.setItems(vertices);
        hasItems = true;
    }
    
    public void AttachAttributeList(ObservableMap<String, Integer> attributeList)
    {
        for(String s : attributeList.keySet())
        {
            if (!colList.containsKey(s))
            {
                TableColumn<Vertex,String> c = new TableColumn<>(s);
                c.setCellValueFactory(data -> data.getValue().getAttributeProperty(s));
                getColumns().add(c);
                colList.put(s, c);
            }
        }
        attributeList.addListener(attrListener);
    }
    
    public void AttachSelectedVertices(ObservableSet<Vertex> selectedVertices)
    {
        selectedVertices.addListener(selListener);
    }
}
