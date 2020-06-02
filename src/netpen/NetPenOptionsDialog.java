/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netpen;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

/**
 *
 * @author User
 */
public class NetPenOptionsDialog extends Dialog<Void>
{
    private final TextField girderBaseURLField;
    private final NetPenOptions optionsEntry;
    
    public NetPenOptionsDialog(NetPenOptions optionsEntry)
    {
        this.setTitle("Options");
        
        this.optionsEntry = optionsEntry;
        
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        
        Label girderBaseURLFieldLabel = new Label("Girder base URL:");
        girderBaseURLField = new TextField(optionsEntry.getGirderBaseURL());
        girderBaseURLField.setPrefColumnCount(30);
        
        HBox mainContentPane = new HBox(4);
        mainContentPane.getChildren().addAll(girderBaseURLFieldLabel, girderBaseURLField);
        
        getDialogPane().setContent(mainContentPane);
        
        this.setResultConverter(new Callback<ButtonType, Void>()
        {
            @Override
            public Void call(ButtonType param)
            {
                if (param.equals(ButtonType.OK))
                {
                    optionsEntry.setGirderBaseURL(girderBaseURLField.getText());
                }
                
                return null;
            }
        });
    }
}
