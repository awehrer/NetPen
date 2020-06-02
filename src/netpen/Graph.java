/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netpen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 *
 * @author awehrer, ayee
 */
public class Graph
{
    private ObservableList<Vertex> vertices;
    private List<Edge> edges;
    private ObservableMap<String, Integer> attributeList;
    
    public Graph()
    {
        // using an observable list allows passing the vertices list to the
        // tableview for auto-updating based on the properties of items in the
        // list; use of the observableArrayList(...) function versus a standard
        // new allows the observable list to be backed by a linkedlist
        vertices = FXCollections.observableList(new LinkedList<Vertex>());
        edges = new LinkedList<>();
        attributeList = FXCollections.observableHashMap();
    }
    
    public Graph(File file) throws FileNotFoundException, IOException, NumberFormatException
    {
        this();
        loadFromGML(file);
    }

    static public Graph fromJSON(File file) throws FileNotFoundException, IOException, NumberFormatException
    {
        Graph g = new Graph();
        g.loadFromJSON(file);
        return g;
    }

    static public Graph fromJSON(String fn) throws FileNotFoundException, IOException, NumberFormatException
    {
        Graph g = new Graph();
        g.loadFromJSON(new File(fn));
        return g;
    }
    
    private void loadFromJSON(File file) throws FileNotFoundException, IOException, NumberFormatException
    {
        Map<Integer,Vertex> vertexMap = new HashMap<Integer,Vertex>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String s;
        JsonParser parser = new JsonParser();
        while((s=reader.readLine()) != null)
        {
            JsonObject o = (JsonObject)parser.parse(s).getAsJsonObject();
            if (o == null) continue;
            String type = (String)o.get("type").getAsString();
            
            // node type: get name and id
            if (type.equals("node"))
            {
                JsonObject data = (JsonObject)o.getAsJsonObject("data");
                int id = data.get("id").getAsInt();
                String name = data.get("name").getAsString();
                Vertex v = new Vertex(id);
                v.setLabel(name);
                for(Map.Entry<String,JsonElement> entry : data.entrySet())
                {
                    if (!entry.getKey().equals("id") && !entry.getKey().equals("name"))
                    {
                        try
                        {
                            String value = entry.getValue().getAsString();
                            
                            boolean numeric = true, floating = true;
                            for(char c : value.toCharArray())
                            {
                                if (c == '.') numeric = false;
                                else if (!Character.isDigit(c)) { numeric = false; floating = false; }
                            }
                            
                            boolean done = false;
                            if (numeric)
                            {
                                try { v.setAttribute(entry.getKey(), Integer.parseInt(value)); done = true; }
                                catch(Exception e2) {}
                            }
                            else if (floating)
                            {
                                try { v.setAttribute(entry.getKey(), Float.parseFloat(value)); done = true; }
                                catch(Exception e2) {}
                            }
                            
                            if (!done)
                                v.setAttribute(entry.getKey(), value);
                            
                            this.attributeList.put(entry.getKey(), this.attributeList.getOrDefault(entry.getKey(), 0) + 1);
                        }
                        catch (Exception e) { }
                    }
                }
                vertexMap.put(id, v);
                addVertex(v);
            }
            
            else if (type.equals("link"))
            {
                JsonObject data = (JsonObject)o.getAsJsonObject("data");
                int src = data.get("source").getAsInt();
                int tgt = data.get("target").getAsInt();
                String lbl = data.has("name") ? data.get("name").getAsString(): null;
                Edge edge = new Edge(vertexMap.get(src), vertexMap.get(tgt));
                if (lbl != null) edge.setLabel(lbl);
                addEdge(edge);
            }
        }

        int a = 42;
    }
    private void loadFromGML(File file) throws FileNotFoundException, IOException, NumberFormatException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        Map<Integer,Vertex> vertexMap = new HashMap<>();
        
        String line, attributeValue;
        Vertex currentVertex = null;
        Edge currentEdge = null;
        boolean inVertexDeclaration = false, inEdgeDeclaration = false; 
        int id;
        int source = -1, target = -1;
        StringBuilder strBuilder;
        
        while ((line = reader.readLine()) != null)
        {
            // split line into id tokens
            String [] items = line.trim().split("\\s+");
            
            if (items.length > 1)
            {
                if (items[0].equalsIgnoreCase("node"))
                {
                    inVertexDeclaration = true;
                    inEdgeDeclaration = false;
                    currentVertex = null;
                }
                else if (items[0].equalsIgnoreCase("edge"))
                {
                    inVertexDeclaration = false;
                    inEdgeDeclaration = true;
                    source = -1;
                    target = -1;
                }
                else if (inVertexDeclaration)
                {
                    if (items[0].equalsIgnoreCase("id") && currentVertex == null)
                    {
                        id = Integer.parseInt(items[1]);
                        currentVertex = new Vertex(id);
                        vertexMap.put(id, currentVertex);
                        addVertex(currentVertex);
                    }
                    else if (items[0].equalsIgnoreCase("label"))
                    {
                        if (items.length > 2)
                        {
                            strBuilder = new StringBuilder();
                            strBuilder.append(items[1]);

                            for (int i = 2; i < items.length; i++)
                            {
                                strBuilder.append(" ");
                                strBuilder.append(items[i]);
                            }
                            
                            attributeValue = strBuilder.toString();
                        }
                        else
                            attributeValue = items[1];
                        
                        currentVertex.setLabel(attributeValue.substring(1, attributeValue.length() - 1));
                    }
                    else if (!items[0].equalsIgnoreCase("comment")) // set attribute
                    {
                        if (items.length > 2)
                        {
                            strBuilder = new StringBuilder();
                            strBuilder.append(items[1]);

                            for (int i = 2; i < items.length; i++)
                            {
                                strBuilder.append(" ");
                                strBuilder.append(items[i]);
                            }
                            
                            attributeValue = strBuilder.toString();
                        }
                        else
                            attributeValue = items[1];
                        
                        if (attributeValue.startsWith("\""))
                            currentVertex.setAttribute(items[0], attributeValue.substring(1, attributeValue.length() - 1));
                        else if (attributeValue.contains("."))
                            currentVertex.setAttribute(items[0], Float.parseFloat(attributeValue));
                        else
                            currentVertex.setAttribute(items[0], Integer.parseInt(attributeValue));
                        
                        this.attributeList.put(items[0], this.attributeList.getOrDefault(items[0], 0) + 1);
                    }
                }
                else if (inEdgeDeclaration)
                {
                    if (items[0].equalsIgnoreCase("source"))
                    {
                        source = Integer.parseInt(items[1]);
                    }
                    else if (items[0].equalsIgnoreCase("target"))
                    {
                        target = Integer.parseInt(items[1]);
                        currentEdge = new Edge(vertexMap.get(source), vertexMap.get(target));
                        addEdge(currentEdge);
                    }
                    else if (items[0].equalsIgnoreCase("label"))
                    {
                        if (items.length > 2)
                        {
                            strBuilder = new StringBuilder();
                            strBuilder.append(items[1]);

                            for (int i = 2; i < items.length; i++)
                            {
                                strBuilder.append(" ");
                                strBuilder.append(items[i]);
                            }
                            
                            attributeValue = strBuilder.toString();
                        }
                        else
                            attributeValue = items[1];
                        
                        currentEdge.setLabel(attributeValue.substring(1, attributeValue.length() - 1));
                    }
                }
            } // end if
        } // end while
        
    } // end loadFromGML
    
    public void addVertex(Vertex vertex)
    {
        vertices.add(vertex);
    }
    
    public void addEdge(Edge edge)
    {
        edges.add(edge);
    }
    
    public void addEdge(Vertex source, Vertex target)
    {
        edges.add(new Edge(source, target));
    }
    
    public void removeVertex(Vertex vertex)
    {
        removeVertex(vertex, false);
    }
    
    public void removeVertex(Vertex vertex, boolean removeIncidentEdges)
    {
        for(String attr : vertex.getAttributes())
        {
            int cnt = this.attributeList.getOrDefault(attr, 0);
            if (cnt <= 1)
            {
                this.attributeList.remove(attr);
            }
        }

        vertices.remove(vertex);
        
        if (removeIncidentEdges)
        {
            Iterator<Edge> edgeIterator = createEdgeIterator();
            Edge edge;
            
            while (edgeIterator.hasNext())
            {
                edge = edgeIterator.next();
                
                if (edge.isIncidentWith(vertex))
                    edgeIterator.remove();
            }
        }
    }
    
    public void removeEdge(Edge edge)
    {
        edges.remove(edge);
    }
    
    public Iterator<Vertex> createVertexIterator()
    {
        return vertices.iterator();
    }
    
    public Iterator<Edge> createEdgeIterator()
    {
        return edges.iterator();
    }
    
    public int getNumVertices()
    {
        return vertices.size();
    }
    
    public int getNumEdges()
    {
        return edges.size();
    }
    
    public ObservableList<Vertex> getVertices()
    {
        return vertices;
    }
    
    public ObservableMap<String, Integer> getAttributeList()
    {
        return attributeList;
    }
} // end Graph class
