/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netpen.utility;

/**
 *
 * @author Arbor
 */
public class RemScaler
{
    static
    {
        rem = javafx.scene.text.Font.getDefault().getSize();
    }
    
    private static double rem;
    private double expectedFontSize;
    
    public RemScaler()
    {
        this.expectedFontSize = 12.0;
    }
    
    public RemScaler(double expectedFontSize)
    {
        this.expectedFontSize = expectedFontSize;
    }
    
    public double getRem()
    {
        return rem;
    }
    
    public double getExpectedFontSize()
    {
        return expectedFontSize;
    }
    
    public void setExpectedFontSize(double fontSize)
    {
        this.expectedFontSize = fontSize;
    }
    
    public double scale(double sizeAtExpectedFontSize)
    {
        return (sizeAtExpectedFontSize / expectedFontSize) * rem;
    }
}
