/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netpen;

/**
 *
 * @author awehrer
 */
public class Edge
{
    private Vertex source;
    private Vertex target;
    private String label;
    
    public Edge(Vertex source, Vertex target)
    {
        this.source = source;
        this.target = target;
    }
    
    public Vertex getSource()
    {
        return source;
    }
    
    public Vertex getTarget()
    {
        return target;
    }
    
    public Vertex getOtherEndpoint(Vertex vertex)
    {
        if (vertex == getSource())
            return getTarget();
        else if (vertex == getTarget())
            return getSource();
        else
            throw new IllegalArgumentException("The specified endpoint does not belong to this edge.");
    }
    
    public String getLabel()
    {
        return label;
    }
    
    public void setLabel(String label)
    {
        this.label = label;
    }
    
    public boolean isIncidentWith(Vertex vertex)
    {
        return getSource() == vertex || getTarget() == vertex;
    }
    
    public boolean isIndicentFrom(Vertex vertex)
    {
        return getSource() == vertex;
    }
    
    public boolean isIndicentTo(Vertex vertex)
    {
        return getTarget() == vertex;
    }
}
