/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netpen;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author awehrer, ayee
 */
public class Vertex implements Comparable<Vertex>
{
    private final IntegerProperty id;
    private final StringProperty label;
    private List<String> attributes;
    private List<Object> attributeValues;
    private List<StringProperty> attributeProps;
    
    public Vertex(int id)
    {
        this.id = new SimpleIntegerProperty(this, "id", id);
        this.label = new SimpleStringProperty(this, "label");
        this.attributes = new LinkedList<>();
        this.attributeValues = new LinkedList<>();
        this.attributeProps = new LinkedList<>();
        
    }
    
    public StringProperty labelProperty()
    {
        return label;
    }
    
    public void setLabel(String label)
    {
        labelProperty().set(label);
    }
    
    public String getLabel()
    {
        return labelProperty().get();
    }
    
    public Object getAttributeValue(String attributeName)
    {
        int index = attributes.indexOf(attributeName);
        
        if (index > 0)
            return attributeValues.get(index);
        else
            return null;
    }
    
    public void setAttribute(String attributeName, Object attributeValue)
    {
        int index = attributes.indexOf(attributeName);
        
        if (index < 0)
        {
            index = attributes.size();
            attributes.add(attributeName);
            attributeValues.add(attributeValue);
            attributeProps.add(new SimpleStringProperty(this, attributeName, attributeValue.toString()));
        }
        else
            attributeValues.set(index, attributeValue);
        
        if (attributeValue == null)
        {
            attributes.remove(index);
            attributeValues.remove(index);
            // \TODO: make sure this doesn't leak memory or something
            // http://stackoverflow.com/questions/14558266/clean-javafx-property-listeners-and-bindings-memory-leaks
            attributeProps.remove(index);
        }
    }
    
    public IntegerProperty idProperty()
    {
        return id;
    }
    
    public int getId()
    {
        return idProperty().get();
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Vertex)
            return getId() == ((Vertex)obj).getId();
        
        return false;
    }

    @Override
    public int hashCode()
    {
        return getId();
    }

    @Override
    public int compareTo(Vertex other)
    {
        if (other == null) return 1;
        return new Integer(getId()).compareTo(other.getId());
    }
    
    public final List<String> getAttributes()
    {
        return attributes;
    }
    
    public StringProperty getAttributeProperty(String s)
    {
        int index = attributes.indexOf(s);
        
        if (index >= 0)
        {
            return attributeProps.get(index);
        }
        else
        {
            return null;
        }
    }
    
    public Double getNumericalAttribute(String s)
    {
        int i = attributes.indexOf(s);
        if (i < 0) return -Double.MAX_VALUE;
        Object o = attributeValues.get(i);
        if (!(o instanceof Number)) return -Double.MAX_VALUE;
        return ((Number)o).doubleValue();
    }
    
    public Map<String, Double> getNumericalAttributeMap()
    {
        return getNumericalAttributeMap(null);
    }
    
    public Map<String, Double> getNumericalAttributeMap(Map<String, String> stringMap)
    {
        Map<String, Double> map = new TreeMap<>();
        
        Iterator<String> attributeIterator = attributes.iterator();
        Iterator<Object> attributeValueIterator = attributeValues.iterator();
        String attribute;
        Object attributeValue;
        
        while (attributeIterator.hasNext())
        {
            attribute = attributeIterator.next();
            attributeValue = attributeValueIterator.next();
            
            if (attributeValue instanceof Number)
            {
                map.put(attribute, ((Number)attributeValue).doubleValue());
            }
            else if (stringMap != null && attributeValue instanceof String)
            {
                stringMap.put(attribute, (String)attributeValue);
            }
        }
        
        return map;
    }
    
    public boolean hasAttribute(String s)
    {
        return (attributes.indexOf(s) >= 0);
    }
}
