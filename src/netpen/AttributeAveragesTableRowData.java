/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netpen;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author awehrer
 */
public class AttributeAveragesTableRowData
{
    private final StringProperty attributeName;
    private final DoubleProperty average;
    private final StringProperty averageAsStr;
    private final DoubleProperty standardDeviation;
    private final StringProperty standardDeviationAsStr;
    private final StringProperty valueRangeSummary;
    private final StringProperty valueRangeFull;
    
    public AttributeAveragesTableRowData()
    {
        attributeName = new SimpleStringProperty(this, "attributeName", "");
        average = new SimpleDoubleProperty(this, "average", 0.0);
        averageAsStr = new SimpleStringProperty(this, "averageAsStr", Double.toString(average.get()));
        standardDeviation = new SimpleDoubleProperty(this, "standardDeviation", 0.0);
        standardDeviationAsStr = new SimpleStringProperty(this, "standardDeviationAsStr", Double.toString(standardDeviation.get()));
        valueRangeSummary = new SimpleStringProperty(this, "valueRangeSummary", "");
        valueRangeFull = new SimpleStringProperty(this, "valueRangeFull", "");
    }

    public AttributeAveragesTableRowData(String attributeName, double average, double standardDeviation)
    {
        this.attributeName = new SimpleStringProperty(this, "attributeName", attributeName);
        this.average = new SimpleDoubleProperty(this, "average", average);
        this.averageAsStr = new SimpleStringProperty(this, "averageAsStr", (average < 0.0) ? "" : Double.toString(average));
        this.standardDeviation = new SimpleDoubleProperty(this, "standardDeviation", standardDeviation);
        standardDeviationAsStr = new SimpleStringProperty(this, "standardDeviationAsStr", (standardDeviation < 0.0) ? "" : Double.toString(standardDeviation));
        valueRangeSummary = new SimpleStringProperty(this, "valueRangeSummary", "");
        valueRangeFull = new SimpleStringProperty(this, "valueRangeFull", "");
    }

    public AttributeAveragesTableRowData(String attributeName, double average, double standardDeviation, String valueRangeSummary, String valueRangeFull)
    {
        this.attributeName = new SimpleStringProperty(this, "attributeName", attributeName);
        this.average = new SimpleDoubleProperty(this, "average", average);
        this.averageAsStr = new SimpleStringProperty(this, "averageAsStr", (average < 0.0) ? "" : Double.toString(average));
        this.standardDeviation = new SimpleDoubleProperty(this, "standardDeviation", standardDeviation);
        this.standardDeviationAsStr = new SimpleStringProperty(this, "standardDeviationAsStr", (standardDeviation < 0.0) ? "" : Double.toString(standardDeviation));
        this.valueRangeSummary = new SimpleStringProperty(this, "valueRangeSummary", valueRangeSummary);
        this.valueRangeFull = new SimpleStringProperty(this, "valueRangeFull", valueRangeFull);
    }
    
    public StringProperty attributeNameProperty()
    {
        return attributeName;
    }

    public void setAttributeName(String attributeName)
    {
        attributeNameProperty().set(attributeName);
    }

    public String getAttributeName()
    {
        return attributeNameProperty().get();
    }

    public DoubleProperty averageProperty()
    {
        return average;
    }

    public void setAverage(double average)
    {
        setAverageAsStr((average < 0.0) ? "" : Double.toString(average));
        averageProperty().set(average);
    }

    public double getAverage()
    {
        return averageProperty().get();
    }

    public StringProperty averageAsStrProperty()
    {
        return averageAsStr;
    }

    public void setAverageAsStr(String average)
    {
        averageAsStrProperty().set(average);
    }

    public String getAverageAsStr()
    {
        return averageAsStrProperty().get();
    }
    
    public DoubleProperty standardDeviationProperty()
    {
        return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation)
    {
        setStandardDeviationAsStr((standardDeviation < 0.0) ? "" : Double.toString(standardDeviation));
        standardDeviationProperty().set(standardDeviation);
    }

    public double getStandardDeviation()
    {
        return standardDeviationProperty().get();
    }

    public StringProperty standardDeviationAsStrProperty()
    {
        return standardDeviationAsStr;
    }

    public void setStandardDeviationAsStr(String standardDeviation)
    {
        standardDeviationAsStrProperty().set(standardDeviation);
    }

    public String getStandardDeviationAsStr()
    {
        return standardDeviationAsStrProperty().get();
    }

    public StringProperty valueRangeSummaryProperty()
    {
        return valueRangeSummary;
    }

    public void setValueRangeSummary(String valueRangeSummary)
    {
        valueRangeSummaryProperty().set(valueRangeSummary);
    }

    public String getValueRangeSummary()
    {
        return valueRangeSummaryProperty().get();
    }
    
    public StringProperty valueRangeFullProperty()
    {
        return valueRangeFull;
    }

    public void setValueRangeFull(String valueRangeFull)
    {
        valueRangeFullProperty().set(valueRangeFull);
    }

    public String getValueRangeFull()
    {
        return valueRangeFullProperty().get();
    }
}
