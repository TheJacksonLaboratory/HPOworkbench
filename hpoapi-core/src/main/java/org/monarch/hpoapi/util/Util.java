package org.monarch.hpoapi.util;



import java.io.File;
import java.util.Arrays;

/**
 *
 * This class implements some utility functions.
 *
 * @author Sebastian Bauer
 *
 */
public final class Util
{
    /**
     * Hides the default constructor.
     */
    private Util()
    {
    }

    /**
     * Returns the File object for the directory where the application
     * can store persistent data. If directory doesn't exist it will
     * be created.
     *
     * @param appName specifies the name of the application, from which
     *        the name of the directory is derived.
     *
     * @return the path where application dependend files can be stored.
     */
    static public File getAppDataDirectory(final String appName)
    {
		/* On windows we can try the APPDATA path */
        String dirName = System.getenv("APPDATA");
        if (dirName == null)
        {
			/* If not defined we simply take the user directory */
            dirName = System.getProperty("user.home");
        }

        File app = new File(dirName);
        if (app.exists() && app.isDirectory())
        {
            File onto = new File(app, "." + appName);
            if (!onto.exists())	onto.mkdir();
            if (onto.exists()) return onto;
            return onto;
        }
        return null;
    }

    /**
     * Wraps a single line of text.
     * Called by wrapText() to do the real work of wrapping.
     *
     * @param line  a line which is in need of word-wrapping
     * @param newline  the characters that define a newline
     * @param wrapColumn  the column to wrap the words at
     * @return a line with newlines inserted
     */
	/* Taken from WordWrapUtils.java of the jakarta project */
    public static String wrapLine(String line, String newline, int wrapColumn) {
        StringBuilder wrappedLine = new StringBuilder();

        while (line.length() > wrapColumn) {
            int spaceToWrapAt = line.lastIndexOf(' ', wrapColumn);

            if (spaceToWrapAt >= 0) {
                wrappedLine.append(line.substring(0, spaceToWrapAt));
                wrappedLine.append(newline);
                line = line.substring(spaceToWrapAt + 1);
            }

            // This must be a really long word or URL. Pass it
            // through unchanged even though it's longer than the
            // wrapColumn would allow. This behavior could be
            // dependent on a parameter for those situations when
            // someone wants long words broken at line length.
            else {
                spaceToWrapAt = line.indexOf(' ', wrapColumn);

                if (spaceToWrapAt >= 0) {
                    wrappedLine.append(line.substring(0, spaceToWrapAt));
                    wrappedLine.append(newline);
                    line = line.substring(spaceToWrapAt + 1);
                } else {
                    wrappedLine.append(line);
                    line = "";
                }
            }
        }

        // Whatever is left in line is short enough to just pass through
        wrappedLine.append(line);

        return (wrappedLine.toString());
    }

    /**
     * Returns the number of characters the given value would occupy of printed
     * using decimal representation.
     *
     * @param value the number to check. Should not be negative.
     *
     * @return the number of characters
     */
    public static int lengthOf(int value)
    {
        if (value < 0) throw new IllegalArgumentException();

        /* FIXME: Binary search would be much better here */
        if (value <= 9) return 1;
        if (value <= 99) return 2;
        if (value <= 999) return 3;
        if (value <= 9999) return 4;
        if (value <= 99999) return 5;
        if (value <= 999999) return 6;
        if (value <= 9999999) return 7;
        if (value <= 99999999) return 8;
        if (value <= 999999999) return 9;
        return 10;
    }

    private static byte [] digits = new byte[]{'0','1','2','3','4','5','6','7','8','9'};

    /**
     * Converts the given positive integer value to a byte representation.
     *
     * @param value the value to be converted
     * @param dest the destination array
     * @param offset the start offset within the destination array.
     */
    public static void intToByteArray(int value, byte [] dest, int offset)
    {
        if (value < 0) throw new IllegalArgumentException();

        int current = offset + lengthOf(value);

        while (value > 0)
        {
            dest[--current] = digits[value % 10];
            value = value / 10;
        }
    }

    /**
     * Intersection of a and b with result in c.
     * @param a sorted array number one
     * @param b sorted array number two
     * @param c destination array or null.
     * @return number of valid entries of c (number of common elements).
     */
    private static int commonIntsWithResult(int [] a, int [] b, int c[])
    {
        int numCommon = 0;
        for (int i = 0, j = 0; i < a.length && j < b.length;)
        {
            if (a[i] > b[j])
            {
                j++;
            } else if (a[i] < b[j])
            {
                i++;
            } else
            {
                if (c != null)
                {
                    c[numCommon] = a[i];
                }
                i++;
                j++;
                numCommon++;
            }
        }
        return numCommon;
    }

    /**
     * Determine the number of integer values that are common in the given sorted arrays.
     *
     * @param a sorted arrays
     * @return number of ints that are common.
     */
    public static int commonInts(int [] ... a)
    {
        if (a.length == 0) return 0;
        if (a.length == 1) return a[0].length;
        if (a.length == 2) return commonIntsWithResult(a[0], a[1], null);

		/* Slice array */
        int [][] newA = Arrays.copyOfRange(a, 1, a.length);
        int [] tmp = new int[Math.max(a[0].length, a[1].length)];
        int tmpLen = commonIntsWithResult(a[0], a[1], tmp);
        newA[0] = Arrays.copyOf(tmp, tmpLen);
        return commonInts(newA);
    }

    /**
     * Returns the union of both sorted arrays. It is basically a merge without
     * duplication.
     *
     * @param a sorted array number one (no duplicates allowed)
     * @param b sorted array number two (no duplicates allowed)
     * @return union of a and b
     */
    public static int [] union(int [] a, int [] b)
    {
        int [] c = new int[a.length + b.length];
        int i = 0, j = 0, k = 0;

        for (; i < a.length && j < b.length;k++)
        {
            if (a[i] > b[j])
            {
                c[k] = b[j];
                j++;
            } else if (a[i] < b[j])
            {
                c[k] = a[i];
                i++;
            } else
            {
                c[k] = a[i];
                i++;
                j++;
            }
        }

        for (; i < a.length; i++)
        {
            c[k++] = a[i];
        }

        for (; j < b.length; j++)
        {
            c[k++] = b[j];
        }


        if (k != c.length)
        {
            int [] nc = new int[k];
            System.arraycopy(c, 0, nc, 0, k);
            return nc;
        }
        return c;
    }

    /**
     * Calculates the cardinality of a union (b1 intersect b2 ... intersect bn)
     *
     * @param unionCardinality where the cardinality of the union is stored
     * @param a sorted array a
     * @param b many sorted arrays b
     * @return the cardinality of the resulting set.
     */
    public static int commonIntsWithUnion(int [] unionCardinality, int [] a, int [] ...b)
    {
        if (b == null || b.length == 0)
        {
            return a.length;
        }

        int [] bAll = b[0];
        for (int i = 1; i < b.length; i++)
        {
            bAll = union(bAll, b[i]);
        }

        unionCardinality[0] = bAll.length;
        return commonInts(a, bAll);
    }

    public static class CommonIntSet
    {
        public int numberOfCommonInts;
        public int [] common;
    };

    /**
     * Determine the set of common ints (i.e., the intersection of both sets).
     *
     * @param a sorted array number one
     * @param b sorted array number two
     * @return ints that are common.
     */
    public static CommonIntSet commonIntsSet(int a [], int [] b)
    {
        CommonIntSet cis = new CommonIntSet();
        cis.common = new int[Math.min(a.length, b.length)];
        cis.numberOfCommonInts = commonIntsWithResult(a, b, cis.common);
        return cis;
    }
}