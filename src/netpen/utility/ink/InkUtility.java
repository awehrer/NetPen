/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netpen.utility.ink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Pair;
import netpen.utility.ink.recognition.AugmentedInkStroke;

/**
 *
 * @author awehrer
 */
public class InkUtility
{
    // http://alienryderflex.com/polygon/
    public static boolean isPointInPolygon(List<Point2D> polygonPoints, Point2D testPoint, Bounds inkBoundingBox)
    {
        if (inkBoundingBox != null)
        {
            if (!inkBoundingBox.contains(testPoint))
                return false;
        }
        
        Point2D [] polygonCorners = polygonPoints.toArray(new Point2D[polygonPoints.size()]);
        double x = testPoint.getX();
        double y = testPoint.getY();
        
        int i, j = polygonCorners.length - 1;
        boolean oddNodes = false;
        
        for (i = 0; i < polygonCorners.length; i++)
        {
            if ((polygonCorners[i].getY() < y && polygonCorners[j].getY() >= y
                    || polygonCorners[j].getY() < y && polygonCorners[i].getY() >= y)
                    && (polygonCorners[i].getX() <= x || polygonCorners[j].getX() <= x))
            {
                if (polygonCorners[i].getX() + (y - polygonCorners[i].getY()) / (polygonCorners[j].getY() - polygonCorners[i].getY()) * (polygonCorners[j].getX() - polygonCorners[i].getX()) < x)
                    oddNodes = !oddNodes;
            }
            
            j = i;
        }
        
        return oddNodes;
    }
    
    public static boolean pointInPolygon(AugmentedInkStroke polyline, Point2D testPoint)
    {
        return isPointInPolygon((List)polyline.getResampledPoints(), testPoint, polyline.getBoundingBox());
    }
    
    // returns: 0 for unknown, 1 for ellipse, 2 for circle
    public static int isRoundedShape(List<StylusPoint> points)
    {
        return isRoundedShape(points, null);
    }
    
    // Detect a circle or ellipse; expects resampled points, a la ShortStraw / IStraw, but not corners (obviously :-)
    public static int isRoundedShape(List<StylusPoint> points, Polygon roundedShape)
    {
        int N = (int)Math.min(14,(int)Math.floor(points.size() / 2.0));

        // get average of points; if circle, should average to center point
        double centerSumX = 0.0, centerSumY = 0.0;
        
        for (StylusPoint point : points)
        {
            centerSumX += point.getX();
            centerSumY += point.getY();
        }
        
        Point2D center = new Point2D(centerSumX / points.size(), centerSumY / points.size());

        // create angular bins
        ArrayList<Integer> binCounts = new ArrayList<>(N);
        ArrayList<Double> binDists = new ArrayList<>(N);
        ArrayList<Double> binXs = new ArrayList<>(N);
        ArrayList<Double> binYs = new ArrayList<>(N);
        
        for (int i = 0; i < N; i++)
        {
            binCounts.add(0);
            binDists.add(0.0);
            binXs.add(0.0);
            binYs.add(0.0);
        }

        // bin the points
         for (StylusPoint point : points)
        {
            double angle = Math.atan2(point.getY() - center.getY(), point.getX() - center.getX());
            int bin = (int)Math.floor((angle + Math.PI) / (Math.PI * 2.0 / N));
            if (bin < 0) bin = 0;
            else if (bin >= N) bin = N - 1;
            binCounts.set(bin, binCounts.get(bin) + 1);
            binDists.set(bin, binDists.get(bin) + InkUtility.distance(center, point));
            binXs.set(bin, binXs.get(bin) + point.getX());
            binYs.set(bin, binYs.get(bin) + point.getY());
        }

        // fail if a spoke doesn't get points
        for (int i = 0; i < N; i++)
        {
            if (binCounts.get(i) == 0)
                return 0; // unknown
            binDists.set(i, binDists.get(i) / binCounts.get(i));
            binXs.set(i, binXs.get(i) / binCounts.get(i));
            binYs.set(i, binYs.get(i) / binCounts.get(i));
        }

        // compute the angles between each spoke average point
        double lastAngle = 0.0;
        int fail = 0;
        List<Double> angleDiffs = new ArrayList<>(N);
        double avgAngleDiff = 0.0;

        for (int i = 1; i < N; i++)
        {
            double angle = Math.atan2(binYs.get(i) - binYs.get(i - 1), binXs.get(i) - binXs.get(i - 1));
            if (angle <= -Math.PI) angle += Math.PI * 2.0;
            else if (angle > Math.PI) angle -= Math.PI * 2.0;
            double angleDiff = angle - lastAngle;
            if (angleDiff <= -Math.PI) angleDiff += Math.PI * 2.0;
            else if (angleDiff > Math.PI) angleDiff -= Math.PI * 2.0;
            if (i != 1 && Math.abs(angleDiff) > 50.0 * Math.PI / 180.0) fail++;
            lastAngle = angle;
            angleDiffs.add(angleDiff);
            avgAngleDiff += angleDiff;
        }
        
        avgAngleDiff /= N;

        int fail2 = 0;

        for (int i = 0, nOver2 = (int)(N / 2.0); i < nOver2; i++)
        {
            double angle0 = Math.atan2(binYs.get(i) - center.getY(), binXs.get(i) - center.getX());
            double angle1 = Math.atan2(binYs.get(nOver2 + i) - center.getY(), binXs.get(nOver2 + i) - center.getX());
            if (angle0 < 0.0) angle0 += Math.PI * 2.0;
            if (angle0 >= Math.PI) angle0 -= Math.PI;
            if (angle1 < 0.0) angle1 += Math.PI * 2.0;
            if (angle1 >= Math.PI) angle1 -= Math.PI;
            double angleMix = Math.abs(angle0 - angle1);
            if (Math.abs(binDists.get(i) - binDists.get(nOver2 + i)) / Math.max(binDists.get(i), binDists.get(nOver2 + i)) > 0.2)
                fail2++;
        }

        // check if enough bins succeeded in the tests to recognize the shape
        if (fail < 3 && fail2 < 3)
        {
            if (roundedShape != null)
            {
                roundedShape.getPoints().clear();
                roundedShape.setStroke(Color.BLACK);
                roundedShape.setStrokeWidth(3.0);
                roundedShape.setFill(Color.TRANSPARENT);
            }

            /*double sdAngleDiff = 0.0;
            
            for (Double angleDiff : angleDiffs)
                sdAngleDiff += Math.pow(angleDiff - avgAngleDiff, 2.0);
            
            sdAngleDiff = Math.sqrt(sdAngleDiff / (angleDiffs.size() - 1));*/

            // hypersample the bins
            N = (int)Math.max(42,(int)Math.floor(points.size() / 50.0));

            binCounts = new ArrayList<>(N);
            binXs = new ArrayList<>(N);
            binYs = new ArrayList<>(N);
            
            for (int i = 0; i < N; i++)
            {
                binCounts.add(0);
                binXs.add(0.0);
                binYs.add(0.0);
            }

            // bin the points
            for (StylusPoint point : points)
            {
                double angle = Math.atan2(point.getY() - center.getY(), point.getX() - center.getX());
                int bin = (int)Math.floor((angle + Math.PI) / (Math.PI * 2.0 / N));
                if (bin < 0) bin = 0;
                else if (bin >= N) bin = N - 1;
                binCounts.set(bin, binCounts.get(bin) + 1);
                binXs.set(bin, binXs.get(bin) + point.getX());
                binYs.set(bin, binYs.get(bin) + point.getY());
            }

            double newMinX = Double.MAX_VALUE, newMaxX = Double.MIN_VALUE, newMinY = Double.MAX_VALUE, newMaxY = Double.MIN_VALUE;
            
            for (int i = 0; i < N; i++)
            {
                if (binCounts.get(i) > 0)
                {
                    double x = binXs.get(i) / binCounts.get(i), y = binYs.get(i) / binCounts.get(i);
                    if (x < newMinX) newMinX = x;
                    if (x > newMaxX) newMaxX = x;
                    if (y < newMinY) newMinY = y;
                    if (y > newMaxY) newMaxY = y;
                    
                    if (roundedShape != null)
                    {
                        roundedShape.getPoints().add(x);
                        roundedShape.getPoints().add(y);
                    }
                }
            }
            
            double widthHeight = (newMaxX - newMinX) / (newMaxY - newMinY);
            
            if (widthHeight > 1.00)
                widthHeight = 1.0 / widthHeight;
            
            return (widthHeight < 0.75 ? 1 : 2); // ellipse or circle
        }
        
        return 0; // unknown
    }
    
    // returns radians
    // remember that the y-axis is inverted
    public static double computeAngleOfLineWithXAxis(Point2D lineEndpoint1, Point2D lineEndpoint2)
    {
        double xDiff = lineEndpoint2.getX() - lineEndpoint1.getX();
        double yDiff = lineEndpoint2.getY() - lineEndpoint1.getY();
        
        return Math.atan2(yDiff, xDiff);
    }
    
    // rotate point path by w radians
    public static List<StylusPoint> rotateBy(Collection<StylusPoint> points, double radians)
    {
        Point2D centroid = InkUtility.computeCentroid((List)points);
        List<StylusPoint> rotatedPoints = new ArrayList<>();
        double cosRadians = Math.cos(radians);
        double sinRadians = Math.sin(radians);

        for (StylusPoint point : points)
        {
            double x = (point.getX() - centroid.getX()) * cosRadians - (point.getY() - centroid.getY()) * sinRadians + centroid.getX();
            double y = (point.getX() - centroid.getX()) * sinRadians + (point.getY() - centroid.getY()) * cosRadians + centroid.getY();
            rotatedPoints.add(new StylusPoint(x, y, point));
        }

        return rotatedPoints;
    }
    
    // computes and returns the centroid of the specified points
    public static Point2D computeCentroid(List<Point2D> points)
    {
        double sumX = 0.0;
        double sumY = 0.0;

        for (Point2D p : points)
        {
            sumX = sumX + p.getX();
            sumY = sumY + p.getY();
        }

        return new Point2D(sumX / points.size(), sumY / points.size());
    }
    
    public static boolean isClockwiseOrder(List<StylusPoint> points)
    {
        return isClockwiseOrder((List)points, true);
    }
    
    // http://stackoverflow.com/questions/1165647/how-to-determine-if-a-list-of-polygon-points-are-in-clockwise-order
    public static boolean isClockwiseOrder(List<Point2D> points, boolean isInvertedYAxis)
    {
        double twoTimesSignedArea = 0.0;
        Point2D previousPoint, currentPoint;
        Iterator<Point2D> pointIterator = points.iterator();
        
        if (pointIterator.hasNext())
        {
            previousPoint = pointIterator.next();
            
            while (pointIterator.hasNext())
            {
                currentPoint = pointIterator.next();
                twoTimesSignedArea += (currentPoint.getX() - previousPoint.getX()) * (currentPoint.getY() + previousPoint.getY());
                previousPoint = currentPoint;
            }
        }
        
        // Inverted y-axis: if area is negative, the point order is clockwise (otherwise, counterclockwise)
        // Normal Cartesian: if area is positive, the point order is clockwise
        return (isInvertedYAxis ? twoTimesSignedArea > 0.0 : twoTimesSignedArea < 0.0);
    }
    
    public static BoundingBox getBoundingBox(Collection<InkStroke> inkStrokes)
    {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        
        for (InkStroke inkStroke : inkStrokes)
        {
            for (StylusPoint p : (inkStroke instanceof AugmentedInkStroke ? ((AugmentedInkStroke)inkStroke).getResampledPoints() : inkStroke.getStylusPoints()))
            {
                if (minX > p.getX())
                    minX = p.getX();
                if (minY > p.getY())
                    minY = p.getY();
                if (maxX < p.getX())
                    maxX = p.getX();
                if (maxY < p.getY())
                    maxY = p.getY();
            }
        }

        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }
    
    public static BoundingBox getBoundingBox(InkStroke stroke)
    {
        return getBoundingBox(stroke.getStylusPoints());
    }
    
    // returns bounding box around specified points
    public static BoundingBox getBoundingBox(List<StylusPoint> points)
    {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (StylusPoint p : points)
        {
            if (minX > p.getX())
                minX = p.getX();
            if (minY > p.getY())
                minY = p.getY();
            if (maxX < p.getX())
                maxX = p.getX();
            if (maxY < p.getY())
                maxY = p.getY();
        }

        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }
    
    // returns the distance between the two points.
    public static double distance(double x1, double y1, double x2, double y2)
    {
        double deltaX = x2 - x1;
        double deltaY = y2 - y1;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
    
    // returns the distance between the two points.
    public static double distance(Point2D point1, Point2D point2)
    {
        return distance(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }
    
    // returns path distance around points between the indices a and b.
    public static double pathDistance(List<StylusPoint> points, int a, int b)
    {
        double pathDistance = 0;

        for (int i = a; i <= b - 1; i++)
            pathDistance = pathDistance + distance(points.get(i), points.get(i + 1));

        return pathDistance;
    }
    
    public static double pathDistance(List<StylusPoint> points)
    {
        return pathDistance(points, 0, points.size() - 1);
    }
    
    // computes and returns the median of the array of data
    public static double median(double [] array)
    {
        if (array.length == 1)
            return array[0];
        
        // created a sorted copy of the array
        double [] arrayCopy = Arrays.copyOf(array, array.length);
        Arrays.sort(arrayCopy);

        // now, determine median

        // if odd size, take middle element
        if (arrayCopy.length % 2 == 0)
        {
            return arrayCopy[(arrayCopy.length - 1) / 2];
        }
        else // else even size, so take averge of middle elements
        {
            int leftSideOfMiddle = (int)Math.ceil((arrayCopy.length - 1) / 2.0);
            int rightSideOfMiddle = leftSideOfMiddle + 1;
            return ((arrayCopy[leftSideOfMiddle] + arrayCopy[rightSideOfMiddle]) / 2.0);
        }
    }
    
    // endpoint1 and endpoint2 are indices into the points collection.
    // returns whether the points between the specified endpoints represent a line
    public static boolean isLine(List<StylusPoint> points, int endpoint1, int endpoint2)
    {
        double lineThreshold = 0.95;
        double distance = distance(points.get(endpoint1), points.get(endpoint2));
        double pathDistance = pathDistance(points, endpoint1, endpoint2);

        return (distance / pathDistance > lineThreshold);
    }
    
    // returns the number of identified lines in the point collection.
    public static int getNumLines(List<StylusPoint> points, List<Integer> corners)
    {
        int lineCount = 0;

        for (int i = 0; i < corners.size() - 1; i++)
        {
            if (isLine(points, corners.get(i), corners.get(i + 1)))
                lineCount++;
        }

        return lineCount;
    }
    
    // Resample a points path into n evenly spaced points
    // Here, n is a constant: N = 96
    public static List<StylusPoint> resample(List<StylusPoint> points, int n)
    {
        double intervalLength = pathDistance(points, 0, points.size() - 1) / (n - 1);
        double totalDistance = 0.0, distance;
        List<StylusPoint> resampledPoints = new ArrayList<>();
        resampledPoints.add(points.get(0));
        StylusPoint newPoint;

        for (int i = 1; i < points.size(); i++)
        {
            distance = distance(points.get(i - 1), points.get(i));

            if (totalDistance + distance >= intervalLength)
            {
                double x = points.get(i - 1).getX() + ((intervalLength - totalDistance) / distance) * (points.get(i).getX() - points.get(i - 1).getX());
                double y = points.get(i - 1).getY() + ((intervalLength - totalDistance) / distance) * (points.get(i).getY() - points.get(i - 1).getY());
                newPoint = new StylusPoint(x, y);
                resampledPoints.add(newPoint);

                points.add(i, newPoint);
                totalDistance = 0.0;
            }
            else
            {
                totalDistance = totalDistance + distance;
            }
        }

        if (resampledPoints.size() == n - 1)
            resampledPoints.add(new StylusPoint(points.get(points.size() - 1).getX(), points.get(points.size() - 1).getY()));

        return resampledPoints;
    }
    
    // Robust stroke intersection adapted from pseudocode of by Dr. LaViola
   /* public boolean isIntersection(InkStroke primaryStroke, Collection<InkStroke> otherStrokes)
    {
        double penInkWidth = primaryStroke.getDrawingAttributes().getStrokeWidth();
        double penInkHalfWidth = penInkWidth / 2.0;
        
        StylusPoint point = primaryStroke.getStylusPoints().get(0);
        Circle primaryEndpointCircle1 = new Circle(point.getX(), point.getY(), penInkHalfWidth);
        point = primaryStroke.getStylusPoints().get(primaryStroke.getStylusPoints().size() - 1);
        Circle primaryEndpointCircle2 = new Circle(point.getX(), point.getY(), penInkHalfWidth);
        
        return false;
    }
    
    private List<Point2D> toPointList(List<StylusPoint> stylusPoints)
    {
        List<Point2D> points = new ArrayList<>(stylusPoints.size());
        
        for (StylusPoint stylusPoint: stylusPoints)
            points.add((Point2D)stylusPoint);
        
        return points;
    }
    
    private List<StylusPoint> computeStrokeEdges(InkStroke stroke)
    {
        Point2D v1, v2, currentPoint, nextPoint;
        
        List<Point2D> points = toPointList(stroke.getStylusPoints());
        double penInkWidth = stroke.getDrawingAttributes().getStrokeWidth();
        int n = stroke.getStylusPoints().size();
        
        if (n < 3)
            return new ArrayList<>();
        
        for (int index = 0; index <= n - 1; index++)
        {
            nextPoint = points.get(index + 1);
            currentPoint = points.get(index);
            v1 = new Point2D(nextPoint.getY() - currentPoint.getY(), -(nextPoint.getX() - currentPoint.getX()));
            v2 = new Point2D(-(nextPoint.getY() - currentPoint.getY()), nextPoint.getX() - currentPoint.getX());
            
        }
    }*/
    
    // what the heck is this? A determinant? (not cross product)
    private static double pointCross(Point2D p, Point2D q)
    {
        return p.getX() * q.getY() - p.getY() * q.getX();
    }
    
    // see if line segments p to p + r and q to q + s intersect
    public static double getLineSegmentIntersection(Point2D p, Point2D r, Point2D q, Point2D s)
    {
        double crossRS = pointCross(r, s);
        Point2D minusQP = q.subtract(p);

        // parallel lines: either collinear or don't intersect
        if (Math.abs(crossRS) < 1e-7)
        {/*
            if (Math.Abs(PointCross(minusQP, r)) < 1e-6)
                return 0.5;
            else*/
            return Double.MAX_VALUE;
        }

        double t = pointCross(minusQP, s) / crossRS;
        if (t < 0.0 || t > 1.0)
        {
            return Double.MAX_VALUE;
        }

        t = pointCross(minusQP, r) / crossRS;
        
        return (t >= 0.0 && t <= 1.0) ? t : Double.MAX_VALUE;
    }
    
    // returns list of intersecting indices (with respect toe the stylus point collection)
    public static List<Pair<Integer, Integer>> getSelfIntersections(InkStroke stroke)
    {
        return getSelfIntersections(stroke, null);
    }
    
    // returns list of intersecting indices (with respect to the stylus point collection)
    public static List<Pair<Integer, Integer>> getSelfIntersections(InkStroke stroke, List<Point2D> intersectionPointValues)
    {
        // http://en.wikipedia.org/wiki/Line-line_intersection
        // http://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
        
        List<StylusPoint> ps;
        
        if (stroke instanceof AugmentedInkStroke)
            ps = ((AugmentedInkStroke)stroke).getResampledPoints();
        else
            ps = stroke.getStylusPoints();
        
        if (ps.size() > 0)
        {
            // for each point, store line S long centered at current point, orthogonal to line from current to next point
            double ndeltaX, ndeltaY, ndeltaMag;
            double inkWidth = stroke.getDrawingAttributes().getStrokeWidth();
            List<Point2D> ptP = new ArrayList<>(ps.size() - 1), ptP2 = new ArrayList<>(ps.size() - 1), ptN = new ArrayList<>(ps.size() - 1);
            List<Point2D> ptN2 = new ArrayList<>(ps.size() - 1);


            for (int i = 0; i < ps.size() - 1; i++)
            {
                ndeltaX = ps.get(i + 1).getX() - ps.get(i).getX();
                ndeltaY = ps.get(i + 1).getY() - ps.get(i).getY();
                ndeltaMag = Math.sqrt(ndeltaX * ndeltaX + ndeltaY * ndeltaY);
                ptN.add(new Point2D(-ndeltaY / ndeltaMag * inkWidth, ndeltaX / ndeltaMag * inkWidth));
                ptN2.add(new Point2D(ndeltaX, ndeltaY));
                ptP.add(new Point2D(ps.get(i).getX() - 0.5 * ptN.get(ptN.size() - 1).getX(), ps.get(i).getY() - 0.5 * ptN.get(ptN.size() - 1).getY()));
            }

            Map<Integer, Integer> set = new TreeMap<>(), set2 = new TreeMap<>();

            for (int i = 0; i < ps.size() - 1; i++)
            {
                if (set.containsKey(i - 2) || set.containsKey(i - 1)) continue;
                for (int j = i + 1; j < ps.size() - 1; j++)
                {
                    if (!set2.containsKey(j - 2) && !set2.containsKey(j - 1) && !set2.containsKey(j))
                    {
                        double t;

                        if (j > i + 7)
                        {
                            // see if the stroke edges intersect
                            t = getLineSegmentIntersection(ps.get(i), ptN2.get(i), ps.get(j), ptN2.get(j));
                            if (t != Double.MAX_VALUE)
                            {
                                set.put(i, j);
                                set2.put(j, i);
                                if (intersectionPointValues != null)
                                    intersectionPointValues.add(new Point2D(ps.get(i).getX() + t * ptN2.get(i).getX(), ps.get(i).getY() + t * ptN2.get(i).getY()));
                                break;
                            }

                            // see if the stroke actually crosses itself
                            t = getLineSegmentIntersection(ptP.get(i), ptN.get(i), ptP.get(j), ptN.get(j));
                            if (t != Double.MAX_VALUE)
                            {
                                set.put(i, j);
                                set2.put(j, i);
                                if (intersectionPointValues != null)
                                    intersectionPointValues.add(new Point2D(ptP.get(i).getX() + t * ptN.get(i).getX(), ptP.get(i).getY() + t * ptN.get(i).getY()));
                                break;
                            }
                        }
                    }
                }
            }

            List<Pair<Integer, Integer>> intersectionIndexPairs = new LinkedList<>();

            for (Map.Entry<Integer, Integer> pair : set.entrySet())
                intersectionIndexPairs.add(new Pair<>(pair.getKey(), pair.getValue()));

            return intersectionIndexPairs;
        }
        
        return new LinkedList<>();
    }
    
    // get number of two stroke intersections
    public static int getNumIntersections(InkStroke stroke1, InkStroke stroke2)
    {
        // http://en.wikipedia.org/wiki/Line-line_intersection
        // http://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
        
        List<StylusPoint> ps1 = stroke1.getStylusPoints();
        List<StylusPoint> ps2 = stroke2.getStylusPoints();
        double inkWidth1 = stroke1.getDrawingAttributes().getStrokeWidth();
        double inkWidth2 = stroke2.getDrawingAttributes().getStrokeWidth();
        
        // for each point, store line S long centered at current point, orthogonal to line from current to next point
        double ndeltaX, ndeltaY;
        List<Point2D> ptP = new ArrayList<>(ps1.size() - 1), ptP2 = new ArrayList<>(ps1.size() - 1), ptN = new ArrayList<>(ps1.size() - 1);
        List<Point2D> ptN2 = new ArrayList<>(ps1.size() - 1);

        for (int i = 0; i < ps1.size() - 1; i++)
        {
            ndeltaX = ps1.get(i + 1).getX() - ps1.get(i).getX(); // x difference from point to point
            ndeltaY = ps1.get(i + 1).getY() - ps1.get(i).getY(); // y difference from point to point
            double ndeltaMag = Math.sqrt(ndeltaX * ndeltaX + ndeltaY * ndeltaY); // vector magnitude
            ptN.add(new Point2D(-ndeltaY / ndeltaMag * inkWidth1, ndeltaX / ndeltaMag * inkWidth1));
            ptN2.add(new Point2D(ndeltaX, ndeltaY));
            //double distA = distance(ps1.get(i).getX(), ps1.get(i).getY(), ps1.get(i).getX() - ptN.get(ptN.size() - 1).getX(), ps1.get(i).getY() - ptN.get(ptN.size() - 1).getY());
            //double distB = distance(ps1.get(i).getX(), ps1.get(i).getY(), ps1.get(i).getX() + ptN.get(ptN.size() - 1).getX(), ps1.get(i).getY() + ptN.get(ptN.size() - 1).getY());
            ptP.add(new Point2D(ps1.get(i).getX() - 0.5 * ptN.get(ptN.size() - 1).getX(), ps1.get(i).getY() - 0.5 * ptN.get(ptN.size() - 1).getY()));
        }

        List<Point2D> XptP = new ArrayList<>(ps2.size() - 1), XptP2 = new ArrayList<>(ps2.size() - 1), XptN = new ArrayList<>(ps2.size() - 1);
        List<Point2D> XptN2 = new ArrayList<>(ps2.size() - 1);

        for (int i = 0; i < ps2.size() - 1; i++)
        {
            ndeltaX = ps2.get(i + 1).getX() - ps2.get(i).getX();
            ndeltaY = ps2.get(i + 1).getY() - ps2.get(i).getY();
            double ndeltaMag = Math.sqrt(ndeltaX * ndeltaX + ndeltaY * ndeltaY);
            XptN.add(new Point2D(-ndeltaY / ndeltaMag * inkWidth2, ndeltaX / ndeltaMag * inkWidth2));
            XptN2.add(new Point2D(ndeltaX, ndeltaY));
            //double distA = distance(ps2.get(i).getX(), ps2.get(i).getY(), ps2.get(i).getX() - XptN.get(XptN.size() - 1).getX(), ps2.get(i).getY() - XptN.get(XptN.size() - 1).getY());
            //double distB = distance(ps2.get(i).getX(), ps2.get(i).getY(), ps2.get(i).getX() + XptN.get(XptN.size() - 1).getX(), ps2.get(i).getY() + XptN.get(XptN.size() - 1).getY());
            XptP.add(new Point2D(ps2.get(i).getX() - 0.5 * XptN.get(XptN.size() - 1).getX(), ps2.get(i).getY() - 0.5 * XptN.get(XptN.size() - 1).getY()));
        }

        Map<Integer, Integer> set = new TreeMap<>(), set2 = new TreeMap<>();

        for (int i = 0; i < ps1.size() - 1; i++)
        {
            if (set.containsKey(i - 2) || set.containsKey(i - 1)) continue;
            for (int j = 0; j < ps2.size() - 1; j++)
            {
                if (!set2.containsKey(j - 2) && !set2.containsKey(j - 1) && !set2.containsKey(j))
                {
                    double t;

                    t = getLineSegmentIntersection(ps1.get(i), ptN2.get(i), ps2.get(j), XptN2.get(j));
                    
                    if (t != Double.MAX_VALUE)
                    {
                        set.put(i, j);
                        set2.put(j, i);
                        break;
                    }

                    t = getLineSegmentIntersection(ptP.get(i), ptN.get(i), XptP.get(j), XptN.get(j));
                    
                    if (t != Double.MAX_VALUE)
                    {
                        set.put(i, j);
                        set2.put(j, i);
                        break;
                    }
                }
            }
        }

        return set.size();
    }
    
    public static boolean isIntersection(InkStroke stroke1, InkStroke stroke2)
    {
        return isIntersection(stroke1, stroke2.getStylusPoints(), stroke2.getDrawingAttributes().getStrokeWidth());
    }
    
    public static boolean isIntersection(InkStroke stroke, List<StylusPoint> path, double pathInkWidth)
    {
        // http://en.wikipedia.org/wiki/Line-line_intersection
        // http://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
        
        List<StylusPoint> ps1 = stroke.getStylusPoints();
        List<StylusPoint> ps2 = path;
        double inkWidth1 = stroke.getDrawingAttributes().getStrokeWidth();
        double inkWidth2 = pathInkWidth;
        
        // for each point, store line S long centered at current point, orthogonal to line from current to next point
        double ndeltaX, ndeltaY;
        List<Point2D> ptP = new ArrayList<>(ps1.size() - 1), ptP2 = new ArrayList<>(ps1.size() - 1), ptN = new ArrayList<>(ps1.size() - 1);
        List<Point2D> ptN2 = new ArrayList<>(ps1.size() - 1);

        for (int i = 0; i < ps1.size() - 1; i++)
        {
            ndeltaX = ps1.get(i + 1).getX() - ps1.get(i).getX(); // x difference from point to point
            ndeltaY = ps1.get(i + 1).getY() - ps1.get(i).getY(); // y difference from point to point
            double ndeltaMag = Math.sqrt(ndeltaX * ndeltaX + ndeltaY * ndeltaY); // vector magnitude
            ptN.add(new Point2D(-ndeltaY / ndeltaMag * inkWidth1, ndeltaX / ndeltaMag * inkWidth1));
            ptN2.add(new Point2D(ndeltaX, ndeltaY));
            //double distA = distance(ps1.get(i).getX(), ps1.get(i).getY(), ps1.get(i).getX() - ptN.get(ptN.size() - 1).getX(), ps1.get(i).getY() - ptN.get(ptN.size() - 1).getY());
            //double distB = distance(ps1.get(i).getX(), ps1.get(i).getY(), ps1.get(i).getX() + ptN.get(ptN.size() - 1).getX(), ps1.get(i).getY() + ptN.get(ptN.size() - 1).getY());
            ptP.add(new Point2D(ps1.get(i).getX() - 0.5 * ptN.get(ptN.size() - 1).getX(), ps1.get(i).getY() - 0.5 * ptN.get(ptN.size() - 1).getY()));
        }

        List<Point2D> XptP = new ArrayList<>(ps2.size() - 1), XptP2 = new ArrayList<>(ps2.size() - 1), XptN = new ArrayList<>(ps2.size() - 1);
        List<Point2D> XptN2 = new ArrayList<>(ps2.size() - 1);

        for (int i = 0; i < ps2.size() - 1; i++)
        {
            ndeltaX = ps2.get(i + 1).getX() - ps2.get(i).getX();
            ndeltaY = ps2.get(i + 1).getY() - ps2.get(i).getY();
            double ndeltaMag = Math.sqrt(ndeltaX * ndeltaX + ndeltaY * ndeltaY);
            XptN.add(new Point2D(-ndeltaY / ndeltaMag * inkWidth2, ndeltaX / ndeltaMag * inkWidth2));
            XptN2.add(new Point2D(ndeltaX, ndeltaY));
            //double distA = distance(ps2.get(i).getX(), ps2.get(i).getY(), ps2.get(i).getX() - XptN.get(XptN.size() - 1).getX(), ps2.get(i).getY() - XptN.get(XptN.size() - 1).getY());
            //double distB = distance(ps2.get(i).getX(), ps2.get(i).getY(), ps2.get(i).getX() + XptN.get(XptN.size() - 1).getX(), ps2.get(i).getY() + XptN.get(XptN.size() - 1).getY());
            XptP.add(new Point2D(ps2.get(i).getX() - 0.5 * XptN.get(XptN.size() - 1).getX(), ps2.get(i).getY() - 0.5 * XptN.get(XptN.size() - 1).getY()));
        }

        //Map<Integer, Integer> set = new TreeMap<>(), set2 = new TreeMap<>();
        double t;

        for (int i = 0; i < ps1.size() - 1; i++)
        {
            //if (set.containsKey(i - 2) || set.containsKey(i - 1)) continue;
            for (int j = 0; j < ps2.size() - 1; j++)
            {
                t = getLineSegmentIntersection(ps1.get(i), ptN2.get(i), ps2.get(j), XptN2.get(j));

                if (t != Double.MAX_VALUE)
                {
                    return true;
                }

                t = getLineSegmentIntersection(ptP.get(i), ptN.get(i), XptP.get(j), XptN.get(j));

                if (t != Double.MAX_VALUE)
                {
                    return true;
                }
            }
        }

        return false;
    }
    
    // returns subcollection of stylus points between the specified indices.
    public static List<StylusPoint> toSubcollection(List<StylusPoint> stylusPoints, int startIndex, int endIndex)
    {
        List<StylusPoint> subcollection = new ArrayList<>();

        for (int i = startIndex; i <= endIndex; i++)
            subcollection.add(stylusPoints.get(i));

        return subcollection;
    }
    
    // returns true if n lines that make up the specified points collection intersect with the specified stroke
    public static boolean isNLineIntersected(InkStroke strokeToCheck, List<StylusPoint> points, double inkWidthOfPoints, int n, List<Integer> corners)
    {
        int numLineIntersects = 0;
        int endpoint1; // index of endpoint in points
        int endpoint2; // index of endpoint in points

        for (int i = 0; i < corners.size() - 1; i++)
        {
            endpoint1 = corners.get(i);
            endpoint2 = corners.get(i + 1);

            if (isLine(points, endpoint1, endpoint2))
            {
                List<StylusPoint> path = toSubcollection(points, endpoint1, endpoint2);

                if (isIntersection(strokeToCheck, path, inkWidthOfPoints))
                {
                    numLineIntersects++;
                }
            }
        }

        return numLineIntersects >= n;
    }
    
    // filters the stroke by removing any duplicate points
    public static void removeDuplicatePoints(List<StylusPoint> points)
    {
        StylusPoint currentPoint;
        StylusPoint nextPoint;
        int i = 0;

        while (i < points.size() - 1)
        {
            currentPoint = points.get(i);
            nextPoint = points.get(i + 1);

            // if current point same as next point, remove next point
            if (currentPoint.getX() == nextPoint.getX() && currentPoint.getY() == nextPoint.getY())
                points.remove(i + 1);
            else
                i++; // otherwise, move to the next point
        }
    }
}
