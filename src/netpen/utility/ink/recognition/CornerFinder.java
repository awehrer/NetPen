/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netpen.utility.ink.recognition;

import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;
import netpen.utility.ink.InkStroke;
import netpen.utility.ink.InkUtility;
import netpen.utility.ink.StylusPoint;

/**
 * Class whose instances identify cusps in InkStroke objects. This is based on the ShortStraw algorithm.
 * 
 * @author Anthony Wehrer
 */
public class CornerFinder
{
   public CornerFinder()
   {

   }

    // returns corner indices corresponding to the resampled points, a list of which is included as the second value in the pair.
    public Pair<List<Integer>, List<StylusPoint>> getCorners(InkStroke stroke)
    {
        if (stroke.getStylusPoints().size() > 0)
        {
            double resampleSpacing = getResampleSpacing(stroke);
            List<StylusPoint> resampled = resamplePoints(stroke, resampleSpacing);
            List<Integer> corners = getCorners(resampled);

            return new Pair<>(corners, resampled);
        }
        
        return new Pair<>(new ArrayList<Integer>(), new ArrayList<StylusPoint>());
    }

   // returns list of cusp indices in resampled list.
    private List<Integer> getCorners(List<StylusPoint> resampled)
    {
        // indices of corners in the resampled set
        List<Integer> corners = new ArrayList<>();

        // first endpoint is a corner
        corners.add(0);

        int window = 3;
        if (resampled.size() <= 2 * window)
        {
            if (resampled.size() > 1)
                corners.add(resampled.size() - 1);

            return corners;
        }
        double [] straws = new double[(resampled.size() - window) - window];
        StylusPoint point1;
        StylusPoint point2;

        // calculate straw distances for points
        for (int i = window; i < resampled.size() - window; i++)
        {
            point1 = resampled.get(i - window);
            point2 = resampled.get(i + window);
            straws[i - window] = InkUtility.distance(point1.getX(), point1.getY(), point2.getX(), point2.getY());
        }

        double cornerThreshold = InkUtility.median(straws) * 0.95;
        double localMin;
        int localMinIndex;

        // identify potential corners using computed threshold
        for (int i = window; i < resampled.size() - window; i++)
        {
            if (straws[i - window] < cornerThreshold)
            {
                localMin = Double.POSITIVE_INFINITY;
                localMinIndex = i;
                while (i < straws.length && straws[i - window] < cornerThreshold)
                {
                    if (straws[i - window] < localMin)
                    {
                        localMin = straws[i - window];
                        localMinIndex = i;
                    }

                    i++;
                }

                corners.add(localMinIndex);
            }
        }

        // last point is a corner
        corners.add(resampled.size() - 1);

        // Next step in processing corners
        corners = postProcessCorners(resampled, corners, straws);
        return corners;
   }

   // checks the corner candidates to see if any corners can be removed or added based on higher-level polyline rules.
   private List<Integer> postProcessCorners(List<StylusPoint> resampled, List<Integer> corners, double [] straws)
   {
       boolean continueLoop;
       int c1, c2;
       int newCorner;

       do
       {
           continueLoop = true;

           for (int i = 1; i < corners.size(); i++)
           {
               c1 = corners.get(i - 1);
               c2 = corners.get(i);

               if (!InkUtility.isLine(resampled, c1, c2))
               {
                   newCorner = halfwayCorner(straws, c1, c2);

                   if (newCorner > c1 && newCorner < c2)
                   {
                       corners.add(i, newCorner);
                       continueLoop = false;
                   }
               }
           }
       } while (!continueLoop);

       for (int i = 1; i < corners.size() - 1; i++)
       {
           c1 = corners.get(i - 1);
           c2 = corners.get(i + 1);

           if (InkUtility.isLine(resampled, c1, c2))
           {
               corners.remove(corners.get(i));
               i--;
           }
       }

       return corners;
   }

   // finds a corner roughly halfway between the point indices, corner1 and corner2.
   private int halfwayCorner(double[] straws, int corner1, int corner2)
   {
       int window = 3;
       int quarter = (corner2 - corner1) / 4;
       double minValue = Double.POSITIVE_INFINITY;
       int minIndex = -1;

       try
       {
           for (int i = corner1 + quarter; i < corner2 - quarter; i++)
           {
               if (straws[i - window] < minValue)
               {
                   minValue = straws[i - window];
                   minIndex = i;
               }
           }
       }
       catch (IndexOutOfBoundsException e)
       {
           return -1;
       }

       return minIndex;
   }

   // determines the interspacing pixel distance between resampled points
   private double getResampleSpacing(InkStroke stroke)
   {
       // determine diagonal endpoints of bounding box around stroke
       double topLeftX = Double.POSITIVE_INFINITY;
       double topLeftY = Double.POSITIVE_INFINITY;
       double bottomRightX = Double.NEGATIVE_INFINITY;
       double bottomRightY = Double.NEGATIVE_INFINITY;

       for (StylusPoint p : stroke.getStylusPoints())
       {
           if (topLeftX > p.getX())
               topLeftX = p.getX();
           if (topLeftY > p.getY())
               topLeftY = p.getY();
           if (bottomRightX < p.getX())
               bottomRightX = p.getX();
           if (bottomRightY < p.getY())
               bottomRightY = p.getY();
       }

       // calculate diagonal length of bounding box around stroke
       double diagonalLength = InkUtility.distance(bottomRightX, bottomRightY, topLeftX, topLeftY);

       return diagonalLength / 40.0;
   }

   // resamples the points in a stroke to be interspaced [resampleSpacing] pixel distance away
   // from each other.
   private List<StylusPoint> resamplePoints(InkStroke stroke, double resampleSpacing)
   {
       double D = 0.0, d;
       List<StylusPoint> resampled = new ArrayList<>();
       StylusPoint prevPoint;
       StylusPoint currentPoint = stroke.getStylusPoints().get(0);
       StylusPoint newPoint;

       // add first point of stroke to resampled set
       resampled.add(currentPoint);

       for (int i = 1; i < stroke.getStylusPoints().size(); i++)
       {
           prevPoint = stroke.getStylusPoints().get(i - 1);
           currentPoint = stroke.getStylusPoints().get(i);
           d = InkUtility.distance(prevPoint.getX(), prevPoint.getY(), currentPoint.getX(), currentPoint.getY());

           if (D + d >= resampleSpacing)
           {
               newPoint = new StylusPoint(prevPoint.getX() + ((resampleSpacing - D) / d) * (currentPoint.getX() - prevPoint.getX()),
                                          prevPoint.getY() + ((resampleSpacing - D) / d) * (currentPoint.getY() - prevPoint.getY()));
               resampled.add(newPoint);
               //stroke.getStylusPoints().add(i, newPoint);
               D = 0.0;
           }
           else
           {
               D = d + D;
           }
       }

       return resampled;
   }
}
