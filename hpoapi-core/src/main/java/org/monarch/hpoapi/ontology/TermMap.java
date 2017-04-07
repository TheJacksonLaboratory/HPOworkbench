package org.monarch.hpoapi.ontology;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A simple class mapping term ids to actual terms.
 *
 * @author Sebastian Bauer, Peter Robinson
 * @version 0.2.1 (April 8, 2017)
 */
public class TermMap implements Iterable<Term>
{
    /** The set of terms */
    private Map<TermID, Term> map = new HashMap<TermID, Term>();

    private TermMap()
    {
    }

    /**
     * Initialize the map with the given terms. Required for subclasses.
     *
     * @param terms
     */
    protected TermMap(Iterable<Term> terms)
    {
        init(terms);
    }

    /**
     * Initialize the map with the given terms.
     *
     * @param terms
     */
    private void init(Iterable<Term> terms)
    {
        for (Term t : terms)
            map.put(t.getID(), t);
    }

    /**
     * Return the full term reference to the given term id.
     *
     * @param tid the term id for which to get the term.
     * @return the term.
     */
    public Term get(TermID tid)
    {
        return map.get(tid);
    }

    /**
     * Create a term id map.
     *
     * @param terms
     * @return the term map
     */
    public static TermMap create(Iterable<Term> terms)
    {
        TermMap map = new TermMap();
        map.init(terms);
        return map;
    }

    @Override
    public Iterator<Term> iterator()
    {
        return map.values().iterator();
    }
}