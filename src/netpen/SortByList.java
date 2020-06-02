/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netpen;

import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

/**
 *
 * @author awehrer
 */
public class SortByList extends AttributeAveragesTable
{
    public SortByList()
    {
        // add a filter to the row mouse events so that a row is deselected when clicked a second time.
        setRowFactory(new Callback<TableView<AttributeAveragesTableRowData>, TableRow<AttributeAveragesTableRowData>>()
        {
            @Override
            public TableRow<AttributeAveragesTableRowData> call(TableView<AttributeAveragesTableRowData> tableView)
            {
                final TableRow<AttributeAveragesTableRowData> row = new TableRow<>();
                
                row.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>()
                {
                    @Override
                    public void handle(MouseEvent event)
                    {
                        final int index = row.getIndex();
                        
                        if (index >= 0 && index < tableView.getItems().size() && tableView.getSelectionModel().isSelected(index))
                        {
                            tableView.getSelectionModel().clearSelection();
                            event.consume();
                        }
                    }
                });
                
                return row;
            }
        });
        
        getSelectionModel().getSelectedCells().addListener(new ListChangeListener<TablePosition>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends TablePosition> c)
            {
                if (SortByList.this.getSelectionModel().isEmpty())
                    SortByList.this.getCanvas().sortVerticesByAttribute(null);
                else
                    SortByList.this.getCanvas().sortVerticesByAttribute(SortByList.this.getSelectionModel().getSelectedItem().getAttributeName());
            }
        });
    }
}
