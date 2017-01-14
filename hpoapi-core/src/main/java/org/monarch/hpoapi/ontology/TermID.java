package org.monarch.hpoapi.ontology;

/**
 * Created by robinp on 1/15/17.
 */

import java.util.HashMap;

import org.monarch.hpoapi.types.ByteString;
import org.monarch.hpoapi.util.Util;

/**
 * This is a simple wrapper class for representing a term identifier such
 * as GO:0001004.
 *
 * The class is immutable.
 *
 * @author Sebastian Bauer
 *
 */
public class TermID
{
    /** The default prefix. Only used with no prefix is specified */
    public static final Prefix DEFAULT_PREFIX = new Prefix("GO");

    public static final ByteString COLON = new ByteString(":");

    /** Term's prefix */
    private final Prefix prefix;

    /** Its integer part */
    public final int id;

    /** Map arbitrary ids to integer ids. Used for ontologies like Uberpheno */
    private static final HashMap<String, Integer> string2id = new HashMap<String, Integer>();

    /** The id to be used for the next string id. This is decreasing. */
    private static int nextId = Integer.MAX_VALUE;

    /**
     * Constructs the TermID from a plain integer value. The prefix defaults
     * to DEFAULT_PREFIX. For example, when DEFAULT_PREFIX is GO, provide the
     * integer 8150 to get the term id representing the term "biological_process"
     * that has id "GO:0008150".
     *
     * @param id
     *
     * @deprecated as it lacks the specification of the prefix (assumes DEFAULT_PREFIX)
     */
    public TermID(int id)
    {
        this.id = id;
        this.prefix = DEFAULT_PREFIX;
    }

    /**
     * Constructs the TermID.
     *
     * @param prefix defines the prefix part of the identifier
     * @param id defines the integer part of the identifier.
     */
    public TermID(Prefix prefix, int id)
    {
        this.id = id;
        this.prefix = prefix;
    }

    /**
     * Constructs the TermID from a string value assumed in the format defined
     * by the OBO foundry.
     *
     * @param stringID
     *            specifies the term id string.
     *
     * @throws IllegalArgumentException
     *             if the string could not be parsed.
     */
    public TermID(String stringID)
    {
        this(stringID,null);
    }

    /**
     * Constructs the TermID from a string value assumed in the format defined
     * by the OBO foundry.
     *
     * @param stringID specifies the term id string.
     * @param prefixPool specifies the prefix pool which is used to map the prefix
     *
     * @throws IllegalArgumentException
     *             if the string could not be parsed.
     */
    public TermID(String stringID, PrefixPool prefixPool)
    {
        int colon = stringID.indexOf(':');

		/* Ensure that there is a proper prefix */
        if (colon < 1) throw new IllegalArgumentException("Failed to find a proper prefix of termid: \"" + stringID + "\"");

        Prefix newPrefix = new Prefix(stringID,colon);
        if (prefixPool != null) prefix = prefixPool.map(newPrefix);
        else prefix = newPrefix;

        int parsedId;
        String parseIdFrom = stringID.substring(colon+1);
        try
        {
            parsedId = Integer.parseInt(parseIdFrom);
        } catch(NumberFormatException ex)
        {
			/* This was no integer id, so we create an own integer id */
            parsedId = makeIdFromString(parseIdFrom);
        }
        id = parsedId;
    }


    public TermID(ByteString stringID, PrefixPool prefixPool)
    {
        int colon = stringID.indexOf(COLON);

		/* Ensure that there is a proper prefix */
        if (colon < 1) throw new IllegalArgumentException("Failed to find a proper prefix of termid: \"" + stringID.toString() + "\"");

        Prefix newPrefix = new Prefix(stringID.substring(0, colon));
        if (prefixPool != null) prefix = prefixPool.map(newPrefix);
        else prefix = newPrefix;

        try
        {
            int parsedId = ByteString.parseFirstInt(stringID);
            id = parsedId;
        } catch(NumberFormatException ex)
        {
            throw new IllegalArgumentException("Failed to parse the integer part of termid: \"" + stringID.toString() + "\"");
        }
    }

    /**
     * Constructs the term id from the given byte buffer.
     *
     * @param id
     * @param start
     * @param len
     * @param prefixPool
     */
    public TermID(byte [] id, int start, int len, PrefixPool prefixPool)
    {
        int i;
        int colon = -1;

        for (i=start;i<start+len;i++)
        {
            if (id[i] == ':')
            {
                colon = i;
                break;
            }
        }

		/* Ensure that there is a proper prefix */
        if (colon < 1)
        {
            throw new IllegalArgumentException("Failed to find a proper prefix of termid: \"" + new String(id,start,len) + "\"");
        }

        Prefix newPrefix = new Prefix(new ByteString(id,start,colon));
        if (prefixPool != null) prefix = prefixPool.map(newPrefix);
        else prefix = newPrefix;

        int tid;

        try
        {
            tid = ByteString.parseFirstInt(id,colon,start+len-colon);
        } catch(NumberFormatException ex)
        {
			/* This was no integer id, so we create an own integer id */
            String strID = new String(id,colon+1,start+len-colon-1);
            tid = makeIdFromString(strID);
        }
        this.id = tid;
    }

    /**
     * Make an unique integer id from an arbitray string.
     *
     * @param id
     * @return the id referencing the the id.
     */
    private int makeIdFromString(String id)
    {
        if (string2id.containsKey(id))
            return string2id.get(id);

        nextId--;
        string2id.put(id, nextId);
        return nextId;
    }

    /**
     * @return the term's prefix.
     */
    public Prefix getPrefix()
    {
        return prefix;
    }

    /**
     * Return the string representation of this term ID.
     */
    public String toString()
    {
        String idString = Integer.toString(id);
        StringBuffer buf = new StringBuffer(16);
        buf.append(prefix.toString());
        buf.append(":");
        for (int i=0;i<7-idString.length();i++)
            buf.append('0');
        buf.append(idString);
        return buf.toString();
		/*
		 * Luckily java has support for sprintf() functions as known from ANSI-C
		 * since 1.5.
		 *
		 * Disabled as TeaVM's class lib doesn't support it for now.
		 */
		/*return String.format("%s:%07d", prefix.toString(), id);*/
    }

    /**
     * @return the ByteString representation of this term ID.
     */
    public ByteString toByteString()
    {
        ByteString prefixByteString = prefix.getByteString();
        int pl = prefixByteString.length();
        int idlen = Util.lengthOf(id);
        byte [] idBytes = new byte[pl + 1 + Math.max(7,idlen)];
        prefixByteString.copyTo(0, pl, idBytes, 0);
        idBytes[pl] = ':';
        if (idlen < 7)
        {
			/* Gap with 0, if needed */
            for (int i=0;i<7-idlen;i++)
            {
                idBytes[i+pl+1] = '0';
            }
            pl += 7 - idlen;
        }
        Util.intToByteArray(id, idBytes, pl + 1);
        return new ByteString(idBytes);
    }

    @Override
    public int hashCode()
    {
		/* We simply use the Term ID as a hash value neglecting the prefix */
        return id;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof TermID)
        {
            TermID goTermID = (TermID) obj;
            if (goTermID.id != id) return false;
            return goTermID.prefix.equals(prefix);
        }
        return super.equals(obj);
    }
}
