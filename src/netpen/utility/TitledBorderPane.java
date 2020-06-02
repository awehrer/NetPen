/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netpen.utility;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

/**
 *
 * @author awehrer
 */
public class TitledBorderPane extends StackPane 
{
    //@FXML private StackPane innerPane;
    @FXML private Label titleLabel;
    @FXML private StackPane contentPane;
    private Node content;

    public TitledBorderPane() 
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TitledBorderPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try
        {
            fxmlLoader.load();
        } catch(IOException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    public void setContent(Node content)
    {
        content.getStyleClass().add("bordered-titled-content");
        contentPane.getChildren().add(content);
    }

    public Node getContent()
    {
        return content;
    }

    public void setTitle(String title)
    {
        titleLabel.setText(" " + title + " ");
    }
    
    public String getTitle()
    {
        return titleLabel.getText();
    }
}
