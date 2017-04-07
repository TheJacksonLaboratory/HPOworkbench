package org.monarch.hpoapi.types;

import java.util.ArrayList;

/**
 * This class is a byte string which act similar to the
 * java String class but stores the string using bytes rather
 * than chars and so require less memory.
 *
 * Note that the supplied strings should be ascii-7 only.
 *
 * Like java's String class objects of this class are immutable.
 *
 * @author Sebastian Bauer
 */
public final class ByteString
{
    public static final ByteString EMPTY = b("");

    private byte [] bytes;
    private transient int hashVal;

    public ByteString(String str)
    {
        bytes = str.getBytes();
    }

    public ByteString(String str, int length)
    {
        bytes = new byte[length];
        for (int i=0;i<length;i++)
            bytes[i] = (byte)str.charAt(i);
    }

    public ByteString(byte [] bytes)
    {
        this.bytes = new byte[bytes.length];
        System.arraycopy(bytes,0,this.bytes,0,bytes.length);
    }

    public ByteString(byte [] bytes, int length)
    {
        this.bytes = new byte[length];
        System.arraycopy(bytes,0,this.bytes,0,length);
    }

    /**
     *
     * @param bytes
     * @param from this position in inclusive
     * @param to this position is exclusive
     */
    public ByteString(byte [] bytes, int from, int to)
    {
        this.bytes = new byte[to-from];
        System.arraycopy(bytes,from,this.bytes,0,to-from);
    }


    private ByteString()
    {
    }

    public int length()
    {
        return bytes.length;
    }

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < bytes.length; i++)
            str.append((char)bytes[i]);
        return str.toString();
    }

    public boolean startsWith(String string)
    {
        int l = string.length();
        if (bytes.length < l)
            return false;
        for (int i = 0; i < l; i++)
        {
            if ((byte)string.charAt(i) != bytes[i])
                return false;
        }
        return true;
    }

    public boolean startsWithIgnoreCase(String string)
    {
        int l = string.length();
        if (bytes.length < l)
            return false;
        for (int i = 0; i < l; i++)
        {
            char c = string.charAt(i);

            if ((byte)c != bytes[i])
                if (Character.toLowerCase(c) != Character.toLowerCase(bytes[i] & 0xff))
                    return false;
        }
        return true;
    }

    /**
     * Return the specified substring.
     *
     * @param beginIndex specifies beginning of the substring (inclusive)
     * @param endIndex specifies the end of the substring (exclusive)
     * @return the substring
     * @see java.lang.String#substring(int, int)
     */
    public ByteString substring(int beginIndex, int endIndex)
    {
        ByteString bs = new ByteString();
        bs.bytes = new byte[endIndex - beginIndex];
        System.arraycopy(bytes, beginIndex, bs.bytes, 0, endIndex - beginIndex);
        return bs;
    }

    /**
     * Copy the bytes of the string of the given extent to a given destination.
     *
     * @param beginIndex specifies beginning of the substring to be copied (inclusive)
     * @param endIndex specifies the end of the substring to be copied (exclusive)
     * @param dest the destination array
     * @param offset the offset within the destination
     */
    public void copyTo(int beginIndex, int endIndex, byte [] dest, int offset)
    {
        System.arraycopy(bytes, beginIndex, dest, offset, endIndex - beginIndex);
    }

    /**
     * Tests if this string is a prefix of the given string.
     *
     * @param string specifies the string that should be checked
     * @return true if this string is a prefix of the given string, else false.
     */
    public boolean isPrefixOf(String string)
    {
        if (bytes.length > string.length())
            return false;

        for (int i=0;i<bytes.length;i++)
        {
            if (bytes[i] != string.charAt(i))
                return false;
        }
        return true;
    }

    public boolean contains(String string)
    {
        return indexOf(string) != -1;
    }

    public ByteString trimmedSubstring(int from, int to)
    {
        byte [] sub = new byte[to - from];
        int bytesW = 0;
        for (int i = from; i < to; i++)
        {
            byte b = bytes[i];
            if (b == ' ' || b == '\t' || b == '\n' || b == '\r')
                continue;
            sub[bytesW] = b;
            bytesW++;
        }
        if (to - from != bytesW)
            return new ByteString(sub,bytesW);

        ByteString bs = new ByteString();
        bs.bytes = sub;
        return bs;
    }

    public int indexOf(ByteString string)
    {
        byte [] stringBytes = string.bytes;
        for (int i = 0; i < bytes.length; i++)
        {
            if (i + stringBytes.length > bytes.length)
                return -1;

            if (stringBytes[0] == bytes[i])
            {
                int j;
                for (j = 1; j < stringBytes.length; j++)
                {
                    if (stringBytes[j] != bytes[j + i])
                        break;
                }
                if (j == stringBytes.length)
                    return i;
            }
        }
        return -1;
    }

    public int indexOf(String string)
    {
        byte [] stringBytes = string.getBytes();
        for (int i = 0; i < bytes.length; i++)
        {
            if (i + stringBytes.length > bytes.length)
                return -1;

            if (stringBytes[0] == bytes[i])
            {
                int j;
                for (j = 1; j < stringBytes.length; j++)
                {
                    if (stringBytes[j] != bytes[j + i])
                        break;
                }
                if (j == stringBytes.length)
                    return i;
            }
        }
        return -1;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ByteString)
        {
            ByteString bStr = (ByteString) obj;
            if (bStr.bytes.length != bytes.length)
                return false;
            for (int i=0;i<bytes.length;i++)
            {
                if (bytes[i] != bStr.bytes[i])
                    return false;
            }
            return true;
        }
        if (obj instanceof String)
        {
            String str = (String)obj;
            if (str.length() != bytes.length)
                return false;
            for (int i=0;i<bytes.length;i++)
            {
                if (str.charAt(i) != bytes[i])
                    return false;
            }
            return true;
        }
        if (obj == null)
            return false;
        return equals(obj);
    }

    @Override
    public int hashCode()
    {
        if (hashVal != 0)
            return hashVal;

        for (int i = 0; i < bytes.length; i++)
            hashVal = 31*hashVal + bytes[i];
        return hashVal;
    }

    public ByteString[] splitBySingleChar(char c)
    {
        int from = 0;
        int to;

        ArrayList<ByteString> bl = new ArrayList<ByteString>();

        for (to = 0;to<bytes.length;to++)
        {
            if (bytes[to] == (byte)c)
            {
                ByteString bs = new ByteString(bytes,from,to);
                bl.add(bs);
                from = to+1;
            }
        }

        ByteString bs = new ByteString(bytes,from,to);
        bl.add(bs);

        ByteString [] bsArray = new ByteString[bl.size()];
        bl.toArray(bsArray);
        return bsArray;
    }

    /**
     * Parse the first appearing decimal integer.
     *
     * @param buf
     * @param off
     * @param len
     * @return the parsed integer.
     */
    public static int parseFirstInt(byte [] buf, int off, int len)
    {
        int number = 0;
        boolean converting = false;

        for (int i=off;i<off+len;i++)
        {
            byte b = buf[i];
            if (b>='0' && b<='9')
            {
                number *= 10;
                number += b - '0';
                converting = true;
            } else if (converting)
            {
                break;
            }
        }
        if (!converting) throw new NumberFormatException();
        return number;
    }

    /**
     * Parse the first appearing decimal integer.
     *
     * @param byteString
     * @return the converted number.
     * @throws NumberFormatException if nothing has been converted.
     */
    public static int parseFirstInt(ByteString byteString)
    {
        return parseFirstInt(byteString.bytes,0,byteString.length());
    }

    public int compareTo(ByteString name)
    {/* FIXME: Provide a better implementation */
        return this.toString().compareTo(name.toString());
    }


    /**
     * Return a ByteString representation from the given str.
     *
     * @param str the string whose ByteString representation should be returned.
     * @return the ByteString representation.
     */
    public static ByteString b(String str)
    {
        return new ByteString(str);
    }

    /**
     * Convert a ByteString to a String. Accepts null pointer in which case a null pointer
     * is returned as well.
     *
     * @param bStr the byte string to be converted
     * @return the converted String
     */
    public static String toString(ByteString bStr)
    {
        if (bStr == null)
            return null;
        return bStr.toString();
    }
}