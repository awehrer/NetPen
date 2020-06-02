/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netpen;

/**
 *
 * @author User
 */
public class NetPenOptions
{
    private String girderBaseURL;
    
    public NetPenOptions(String girderBaseURL)
    {
        setGirderBaseURL(girderBaseURL);
    }
    
    public void setGirderBaseURL(String girderBaseURL)
    {
        this.girderBaseURL = girderBaseURL;
    }
    
    public String getGirderBaseURL()
    {
        return girderBaseURL;
    }
}
