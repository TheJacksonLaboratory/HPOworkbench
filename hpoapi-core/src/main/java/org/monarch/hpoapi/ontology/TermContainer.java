package org.monarch.hpoapi.ontology;

import java.util.*;
import org.monarch.hpoapi.types.ByteString;



/**
 * A Container class for the terms parsed by OBOParser. The class stores the
 * parsed terms as a HashMap. While OBOParser basically has to do with input
 * from the gene_ontology.obo file, this class has to do more with storing and
 * processing the information about the terms.
 *
 * This class implements the Iterable interface so you can iterate over all
 * terms conveniently.
 *
 * @author Peter N. Robinson, Sebastian Bauer, Steffen Grossmann
 */

public class TermContainer extends TermMap implements Iterable<Term>
{
    /** To allow easy iteration over all GO terms */
    private LinkedList<Term> termList;

    /** Format version of the gene_ontology.obo file */
    private ByteString formatVersion;

    /** Date of the OBO file */
    private ByteString date;

    public TermContainer(Iterable<Term> terms, ByteString format, ByteString datum)
    {
        super(terms);

        formatVersion = format;
        date = datum;

		/* Build our io structures linked list */
        termList = new LinkedList<Term>();
        for (Term entry : terms)
            termList.add(entry);
    }

    /**
     * Returns the number of terms stored in this container.
     */
    public int termCount()
    {
        return termList.size();
    }

    public ByteString getFormatVersion()
    {
        return formatVersion;
    }

    public ByteString getDate()
    {
        return date;
    }

    /**
     * Given a GOid such as GO:0001234 get the corresponding English name
     */
    public String getName(String GOid)
    {
        Term got = get(GOid);
        if (got == null)
            return null;
        else
            return got.getName().toString();
    }

    /**
     * Given a GOid get the corresponding English name
     */
    public String getName(TermID id)
    {
        Term got = get(id);
        if (got == null)
            return null;
        else
            return got.getName().toString();
    }

    public Term get(String id)
    {
        TermID tempID = new TermID(id);
        return get(tempID);
    }

    /** The following is intended for debugging purposes. */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("*****\n---Term Container---\n*****\n");
        sb.append("gene_ontology.obo format-version " + getFormatVersion()
                + " from " + getDate() + " was parsed.\n");
        sb.append("A total of " + termCount() + " terms were identified.\n");
        return sb.toString();
    }

    public Iterator<Term> iterator()
    {
        return termList.iterator();
    }

}
