/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netpen.utility;

import javafx.geometry.Point2D;
import javafx.scene.shape.Line;

/**
 *
 * @author Arbor
 */
public class IntersectionUtility
{
    private static final double EPSILON = 1e-10;
    
    // 2d cross product
    private static double crossProduct2D(Point2D p1, Point2D p2)
    {
        return p1.getX() * p2.getY() - p1.getY() * p2.getX();
    }
    
    // Determines if the lines AB and CD intersect.
    public static boolean isLineSegmentLineSegmentIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
    {
        // http://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
        // the 2-dimensional specialization of the 3D line intersection algorithm from the article "Intersection of two lines in three-space" by Ronald Goldman, published in Graphics Gems, page 304.
        // http://www.codeproject.com/Tips/862988/Find-the-Intersection-Point-of-Two-Line-Segments
        
        /*
        
        Suppose the two line segments run from point p to p + r and from q to q + s. Then any point on
        the first line is representable as p + t * r (for a scalar parameter t) and any point on the
        second line as q + u * s (for a scalar parameter u).
        
        The two line segments intersect if we can find t and u such that:
        p + t * r = q + u * s

        */
        
        Point2D p = new Point2D(x1, y1);
        Point2D q = new Point2D(x3, y3);
        Point2D r = new Point2D(x2 - x1, y2 - y1);
        Point2D s = new Point2D(x4 - x3, y4 - y3);
        double rxs = crossProduct2D(r, s); // 2d cross product is scalar
        Point2D qmp = q.subtract(p);
        Point2D pmq = p.subtract(q);
        double qmpxr = crossProduct2D(qmp, r);

        // If r x s = 0 and (q - p) x r = 0, then the two lines are collinear.
        if (Math.abs(rxs) < EPSILON && Math.abs(qmpxr) < EPSILON)
        {
            // 1. If either  0 <= (q - p) * r <= r * r or 0 <= (p - q) * s <= * s
            // then the two lines are overlapping,
            if ((0 <= qmp.dotProduct(r) && qmp.dotProduct(r) <= r.dotProduct(r)) || (0 <= pmq.dotProduct(s) && pmq.dotProduct(s) <= s.dotProduct(s)))
                return true;

         // 2. If neither 0 <= (q - p) * r = r * r nor 0 <= (p - q) * s <= s * s
         // then the two lines are collinear but disjoint.
         // No need to implement this expression, as it follows from the expression above.
         return false;
        }

        // 3. If r x s = 0 and (q - p) x r != 0, then the two lines are parallel and non-intersecting.
        if (Math.abs(rxs) < EPSILON && !(Math.abs(qmpxr) < EPSILON))
         return false;
        
        double rxsReciprocal = 1.0 / rxs;
        
        // t = (q - p) x s / (r x s)
        double t = crossProduct2D(qmp, s) * rxsReciprocal;

        // u = (q - p) x r / (r x s)

        double u = qmpxr * rxsReciprocal;

        // 4. If r x s != 0 and 0 <= t <= 1 and 0 <= u <= 1
        // the two line segments meet at the point p + t r = q + u s.
        if (!(Math.abs(rxs) < EPSILON) && (0.0 <= t && t <= 1.0) && (0.0 <= u && u <= 1.0))
        {
         // We can calculate the intersection point using either t or u.
         //intersection = p + t*r;

         // An intersection was found.
         return true;
        }

        // 5. Otherwise, the two line segments are not parallel but do not intersect.
        return false;
    }
    
    // doesn't handle vertical lines
    public static boolean isLineSegmentLineSegmentIntersectionApproximately(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
    {
        /*
        DERIVATION:
        http://stackoverflow.com/questions/3838329/how-can-i-check-if-two-segments-intersect
        
        A line segment is defined by the equation A * x + b = y where x is in the interval I.
        
        Suppose we have two line segments L1 and L2, where L1 is joined by (x1, y1) and (x2, y2)
        and L2 is joined by (x3, y3) and (x4, y4). L1 can alternately be defined as
        A1 * x + b1 = y where x is in I1 = [min(x1, x2), max(x1, x2)] and L2 can be defined as
        A2 * x + b2 = y where x is in I2 = [min(x3, x4), max(x3, x4)]. For there to be intersection
        between the two line segments, the intersection of I1 and I2 must be nonempty,
        which becomes our first condition to check.
        
        If max(x1, x2) < min(x3, x4), then the intersection of I1 and I2 is empty and we are done.
        Otherwise, the test continues.
        
        We can then calculate the slopes:
        
        A1 = (y1 - y2) / (x1 - x2)
        A2 = (y3 - y4) / (x3 - x4)
        
        If the slopes are equal (A1 == A2), the lines are parallel and must never intersect.
        Otherwise, the test continues.
        
        We can now solve for b1 and b2 since A1 and A2 are known:
        
        b1 = y1 - A1 * x1
        b2 = y3 - A2 * x3
        
        Suppose there exists a point (xa, ya) such that L1 and L2 intersect.
        Therefore, ya = A1 * xa + b1 and ya = A2 * xa + b2.
        
        A1 * xa + b1 = A2 * xa + b2
        A1 * xa - A2 * x1 = b2 - b1
        xa * (A1 - A2) = b2 - b1
        xa = (b2 - b1) / (A1 - A2)
        
        If xa is not in the intersection of I1 and I2, we have a contradiction,
        and thus, L1 and L2 intersect at no point.
        
        If xa is in the intersection of I1 and I2, then there is a point of intersection.
        */
        
        if (Math.max(x1, x2) < Math.min(x3, x4))
            return false; // non-overlapping x-intervals
        
        
        // handle vertical lines with approximations of slope
        
        double x1MinusX2, x3MinusX4;
        
        if (x1 == x2)
            x1MinusX2 = 1e-10;
        else
            x1MinusX2 = x1 - x2;
        
        if (x3 == x4)
            x3MinusX4 = 1e-10;
        else
            x3MinusX4 = x3 - x4;
        
        double A1 = (y1 - y2) / x1MinusX2;
        double A2 = (y3 - y4) / x3MinusX4;
        
        if (A1 == A2)
            return false; // parallel lines
        
        double b1 = y1 - A1 * x1;
        double b2 = y3 - A2 * x3;
        double xa = (b2 - b1) / (A1 - A2);
        
        if (xa < Math.max(Math.min(x1, x2), Math.min(x3, x4)) || xa > Math.min(Math.max(x1, x2), Math.max(x3, x4)))
          return false; // intersection is out of bound
        else
          return true;
    }
    
    /**
     * Checks for intersection between a line segment (finite) defined by two
     * endpoints and a circle defined by a radius and a center.
     * @param x1 the x-component of the first endpoint of the line
     * @param y1 the y-component of the first endpoint of the line
     * @param x2 the x-component of the second endpoint of the line
     * @param y2 the y-component of the second endpoint of the line
     * @param r the radius of the circle
     * @param xC the x-component of the center of the circle
     * @param yC the y-component of the center of the circle
     * @return true if an intersection exists, otherwise false.
     */
    public static boolean isLineSegmentCircleIntersection(double x1, double y1, double x2, double y2, double r, double xC, double yC)
    {
        /*
        DERIVATION:
        http://math.stackexchange.com/questions/103556/circle-and-line-segment-intersection

        The points (x, y) on the line segment that joins (x1, y1) and (x2, y2) can be
        represented parametrically by x = t * x1 + (1 - t) * x2, y = t * y1 + (1 - t) * y2,
        where 0 <= t <= 1. Substitute in the equation of the circle, solve the resulting
        quadratic for t. If 0 <= t <= 1, we have an intersection point, otherwise we don't.
        The value(s) of t between 0 and 1 (if any) determine the intersection point(s).

        If we want a simple yes/no answer, we can use the coefficients of the quadratic
        in t to determine the answer without taking any square roots.

        Equation of a circle:
        (x - xC)^2 + (y - yC)^2 - r^2 = 0
        (x^2 - 2 * xC * x + xC^2) + (y^2 - 2 * yC * y + yC^2) - r^2 = 0
        (x^2 - 2 * xC * x + y^2 - 2 * yC * y) + xC^2 + yC^2 - r^2 = 0

        x = t * x1 + x2 - t * x2
        y = t * y1 + y2 - t * y2

        ((t * x1 + x2 - t * x2)^2 - 2 * xC * (t * x1 + x2 - t * x2) + (t * y1 + y2 - t * y2)^2 - 2 * yC * (t * y1 + y2 - t * y2)) + xC^2 + yC^2 - r^2 = 0

        (x1^2 * t^2 + x2^2 * t^2 - 2 * x1 * x2 * t^2 + y1^2 * t^2 + y2^2 * t^2 - 2 * y1 * y2 * t^2)
        + (2 * x1 * x2 * t - 2 * x2^2 * t - 2 * xC * x1 * t + 2 * xC * x2 * t + 2 * y1 * y2 * t - 2 * y2^2 * t - 2 * yC * y1 * t + 2 * yC * y2 * t)
        + (x2^2 - 2 * xC * x2 + y2^2 - 2 * yC * y2 + xC^2 + yC^2 - r^2) = 0

        (x1^2 + x2^2 - 2 * x1 * x2 + y1^2 + y2^2 - 2 * y1 * y2) * t^2
        + (2 * x1 * x2 - 2 * x2^2 - 2 * xC * x1 + 2 * xC * x2 + 2 * y1 * y2 - 2 * y2^2 - 2 * yC * y1 + 2 * yC * y2) * t
        + (x2^2 - 2 * xC * x2 + y2^2 - 2 * yC * y2 + xC^2 + yC^2 - r^2) = 0

        a = (x1^2 + x2^2 - 2 * x1 * x2 + y1^2 + y2^2 - 2 * y1 * y2)
        a = x1 * x1 + x2 * x2 - 2 * x1 * x2 + y1 * y1 + y2 * y2 - 2 * y1 * y2

        b = (2 * x1 * x2 - 2 * x2^2 - 2 * xC * x1 + 2 * xC * x2 + 2 * y1 * y2 - 2 * y2^2 - 2 * yC * y1 + 2 * yC * y2)
        b = 2 * x1 * x2 - 2 * x2 * x2 - 2 * xC * x1 + 2 * xC * x2 + 2 * y1 * y2 - 2 * y2 * y2 - 2 * yC * y1 + 2 * yC * y2

        c = (x2^2 - 2 * xC * x2 + y2^2 - 2 * yC * y2 + xC^2 + yC^2 - r^2)
        c = x2 * x2 - 2 * xC * x2 + y2 * y2 - 2 * yC * y2 + xC * xC + yC * yC - r * r

        */
        
        double a = x1 * x1 + x2 * x2 - 2 * x1 * x2 + y1 * y1 + y2 * y2 - 2 * y1 * y2;
        double b = 2 * x1 * x2 - 2 * x2 * x2 - 2 * xC * x1 + 2 * xC * x2 + 2 * y1 * y2 - 2 * y2 * y2 - 2 * yC * y1 + 2 * yC * y2;
        double c = x2 * x2 - 2 * xC * x2 + y2 * y2 - 2 * yC * y2 + xC * xC + yC * yC - r * r;
                        
        double sqrtRadicand = b * b - 4 * a * c;

        if (sqrtRadicand >= 0.0)
        {
            double twoTimesA = 2.0 * a;
            double negativeB = -b;
            double sqrtTerm = Math.sqrt(sqrtRadicand);
            double t1 = (negativeB + sqrtTerm) / twoTimesA;
            double t2 = (negativeB - sqrtTerm) / twoTimesA;

            if ((t1 >= 0.0 && t1 <= 1.0) || (t2 >= 0.0 && t2 <= 1.0))
            {
                System.out.println("circleIntersection!");
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Tests whether the specified point is inside the triangle specified by three vertices.
     * Uses the barycentric coordinate method.
     * @param point the point to be tested for containment inside the triangle
     * @param triangleVertex0 the first vertex of the triangle
     * @param triangleVertex1 the second vertex of the triangle
     * @param triangleVertex2 the third vertex of the triangle
     * @return true if the point is contained within the triangle, else false
     */
    public static boolean isPointInsideTriangle(Point2D point, Point2D triangleVertex0, Point2D triangleVertex1, Point2D triangleVertex2)
    {
        // http://www.blackpawn.com/texts/pointinpoly/
        // Also, algorithms follows one used in Real-Time Collision Detection.
        
        // Compute vectors
        Point2D v0 = triangleVertex2.subtract(triangleVertex0);
        Point2D v1 = triangleVertex1.subtract(triangleVertex0);
        Point2D v2 = point.subtract(triangleVertex0);
        
        // Compute dot products
        double dot00 = v0.dotProduct(v0);
        double dot01 = v0.dotProduct(v1);
        double dot02 = v0.dotProduct(v2);
        double dot11 = v1.dotProduct(v1);
        double dot12 = v1.dotProduct(v2);

        // Compute barycentric coordinates
        double invDenom = 1.0 / (dot00 * dot11 - dot01 * dot01);
        double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        double v = (dot00 * dot12 - dot01 * dot02) * invDenom;

        // Check if point is in triangle
        return (u >= 0) && (v >= 0) && (u + v < 1);
    }
    
    public static boolean isHorizontalVerticalLineIntersection(Line horizontalLine, Line verticalLine)
    {
        if (horizontalLine.getStartY() != horizontalLine.getEndY())
            throw new IllegalArgumentException("The provided horizontal line is not horizontal.");
        else if (verticalLine.getStartX() != verticalLine.getEndX())
            throw new IllegalArgumentException("The provided vertical line is not vertical.");
        
        double horizontalY = horizontalLine.getStartY();
        double verticalX = verticalLine.getStartX();
        double verticalMin, verticalMax, horizontalMin, horizontalMax;
        
        if (verticalLine.getStartY() < verticalLine.getEndY())
        {
            verticalMin = verticalLine.getStartY();
            verticalMax = verticalLine.getEndY();
        }
        else
        {
            verticalMin = verticalLine.getEndY();
            verticalMax = verticalLine.getStartY();
        }
        
        if (horizontalLine.getStartX() < horizontalLine.getEndX())
        {
            horizontalMin = horizontalLine.getStartX();
            horizontalMax = horizontalLine.getEndX();
        }
        else
        {
            horizontalMin = horizontalLine.getEndX();
            horizontalMax = horizontalLine.getStartX();
        }
        
        return (horizontalY >= verticalMin && horizontalY <= verticalMax
                && verticalX >= horizontalMin && verticalX <= horizontalMax);
    }
}
