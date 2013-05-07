package decaf.Parallel;

import java.util.Vector;

/**
 * A simple parallelization analysis class. This class provides an easy-to-use
 * interface to getting distance vector information for accessing a
 * single-dimensional arrays in nested loops. As part of the analysis a simple
 * interface for solving linear Diophantine equations is included.
 * 
 * @author 6.035 Staff (<tt>6.035-staff@mit.edu</tt>)
 */

// term is expressed as a vector [const, inner-most-loop-mult, ... ,
// outer-most-loop-mult]
// distance and step are same, but have no const
public class Analyze {
  /**
   * A class for representing a distance vector for accesses of a
   * single-dimensional array using multiple nested loops.
   */
  public static class AccessPattern {
    /**
     * Indicates whether a distance vector exists for the problem
     * posed. If <tt>true</tt>, the distance vector is stored in
     * the <tt>distance</tt> member. Also, if there are infinite
     * distance vectors, <tt>step</tt> provides the minimal
     * distance increment.
     */
    public boolean distanceExists;

    /**
     * Distance vector for the problem posed.
     */
    public Integer[] distance;
    /**
     * Minimal distance increment, if infinite solutions exist. If
     * <tt>null</tt>, only a single solution exists.
     */
    public Integer[] step;
  }

  /**
   * Solves Diophantine equation <tt>c1*x+c2*y+c3=0</tt>
   * 
   * @param c1
   * @param c2
   * @param c3
   * @return y (one possibly infinite set)
   */
  public static int solveLinearInteger(int c1, int c2, int c3) {
    if (c3 == 0)
      return 0;
    if (c2 == 0) {
      assert ((c1 != 0) && (c3 % c1 == 0));
      return 0;
    }

    int r1, r3;
    r1 = c1 % c2;
    r3 = c3 % c2;

    int x = solveLinearInteger(c2, r1, r3);
    int y = solveForX(c2, c1, c3, x);

    return y;
  }

  /**
   * Solves linear equation <tt>c1*x+c2*y+c3=0</tt> for x with all other
   * values known. Solution must be integer!
   * 
   * @param c1
   * @param c2
   * @param c3
   * @param y
   * @return x (must be integer)
   */
  public static int solveForX(int c1, int c2, int c3, int y) {
    assert (c1 != 0);
    int x = (-c3 - c2 * y) / c1;
    assert (c1 * x + c2 * y + c3 == 0);
    return x;
  }

  /**
   * Finds GCD (greatest common divisor) using the Euclid method
   * 
   * @param x >
   *            0
   * @param y >
   *            0
   * @return gcd(x,y)
   */
  public static int getGCD(int x, int y) {
    assert (x > 0 && y >= 0);
    while (y != 0) {
      int t = x % y;
      x = y;
      y = t;
    }
    return x;
  }

  /**
   * Compute a vector distance for accessing an array. Indexes accessed
   * must be computed linearly using only loop variables. Indexes are
   * passed in form of [c_1, c_2, ..., c_n], so that c_1*i+c_2*j+...+c_n
   * is the index accessed;i,j,k are the loop variables and c_k are
   * integer constants
   * 
   * @param firstIndex
   * @param secondIndex
   * @return vector distance information in form of an AccessPattern. null
   *         is returned if no analysis is possible. This does not mean
   *         that the accesses are not parallelizable, but rather that
   *         more advanced analysis may be necessary, which is beyond the
   *         scope of this course.
   */
  public static AccessPattern getAccessPattern(Integer[] firstIndex,
      Integer[] secondIndex) {
    assert (firstIndex != null && secondIndex != null);
    if (firstIndex.length != secondIndex.length)
      return null;

    assert (firstIndex.length > 0);

    Vector<Integer> indexLookup = new Vector<Integer>();
    for (int i = 0; i < firstIndex.length - 1; i++) {
      if (firstIndex[i].compareTo(secondIndex[i]) != 0)
        return null;
      if (firstIndex[i] != 0 || secondIndex[i] != 0)
        indexLookup.add(i);
    }

    if (indexLookup.size() > 2) {
      return null;
    }

    AccessPattern distance = new AccessPattern();

    int numVars = firstIndex.length - 1;

    if (indexLookup.size() == 0) {
      distance.distanceExists = firstIndex[numVars] == secondIndex[numVars];

      if (distance.distanceExists)
        distance.distance = new Integer[numVars];
    } else if (indexLookup.size() == 1) {
      int c1 = firstIndex[indexLookup.elementAt(0)];
      int c3 = -(firstIndex[numVars] - secondIndex[numVars]);

      if (Math.abs(c3) % Math.abs(c1) != 0) {
        distance.distanceExists = false;
        return distance;
      }

      int x = -c3 / c1;

      distance.distanceExists = true;
      distance.distance = new Integer[numVars];

      distance.distance[indexLookup.elementAt(0)] = x;

      assert (c1 * x + c3 == 0);
    } else {
      assert (indexLookup.size() == 2);
      int c1 = firstIndex[indexLookup.elementAt(0)];
      int c2 = firstIndex[indexLookup.elementAt(1)];
      int c3 = -(firstIndex[numVars] - secondIndex[numVars]);

      // check if there ever is an intersection
      int gcdXY = getGCD(java.lang.Math.abs(c1), java.lang.Math.abs(c2));
      if (c3 % gcdXY != 0) {
        distance.distanceExists = false;
        return distance;
      } else {

        int y = solveLinearInteger(c1, c2, c3);
        int x = solveForX(c1, c2, c3, y);
        assert (c1 * x + c2 * y + c3 == 0);

        distance.distance = new Integer[numVars];
        distance.step = new Integer[numVars];

        distance.distanceExists = true;
        distance.distance[indexLookup.elementAt(0)] = x;
        distance.distance[indexLookup.elementAt(1)] = y;

        int stepGCD = getGCD(Math.abs(c1), Math.abs(c2));

        distance.step[indexLookup.elementAt(0)] = c2 / stepGCD;
        distance.step[indexLookup.elementAt(1)] = -c1 / stepGCD;
      }
    }

    return distance;
  }

}


