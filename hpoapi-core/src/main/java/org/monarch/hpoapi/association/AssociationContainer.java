package org.monarch.hpoapi.association;


import java.util.*;

import org.monarch.hpoapi.types.ByteString;

/**
 * After AssociationParser is used to parse the phenotype association file, this
 * class is used to store and process the information about Associations.
 * @author  Peter Robinson
 * @version 0.2.0 (April 24, 2017)
 */
public class AssociationContainer implements Iterable<Disease2Association>
{
    /** Associations keys by the unique id */
    private Disease2Association [] associations;

    /** Mapping */
    private AnnotationContext annotationMapping;

    /**
     * Constructs the container using a list of association and an annotation mapping created from it.
     *
     * @param assocs
     * @param annotationMapping
     */
    public AssociationContainer(List<Association> assocs, AnnotationContext annotationMapping)
    {
        this.annotationMapping = annotationMapping;
        associations = new Disease2Association[annotationMapping.getSymbols().length];
        for (Association a : assocs)
            addAssociation(a);
    }

    /**
     * Adds a new association. Note that this will not read out synonyms or any other field
     * than the object symbol.
     *TODO check logic
     * @param a the associated to be added
     */
    private void addAssociation(Association a)
    {
        ByteString symbol = a.getObjectID();
        int index = annotationMapping.mapSymbol(a.getObjectID());
        if (index == Integer.MAX_VALUE)
            return;

        Disease2Association g2a = associations[index];
        if (g2a == null)
        {
            g2a = new Disease2Association(symbol);
            associations[index] = g2a;
        }
        g2a.add(a);
    }

    /**
     * get a Gene2Associations object corresponding to a given gene name. If the
     * name is not initially found as dbObject Symbol, (which is usually a
     * database name with meaning to a biologist), try dbObject (which may be an
     * accession number or some other term from the association database), and
     * finally, look for a synonym (another entry in the gene_association file
     * that will have been parsed into the present object).
     *
     * @param diseaseID name of the disease whose associations are interesting
     * @return associations for the given disease
     */
    public Disease2Association get(ByteString diseaseID)
    {
        int index = annotationMapping.mapSymbol(diseaseID);
        if (index == Integer.MAX_VALUE)
        {
            index = annotationMapping.mapObjectID(diseaseID);
            if (index == Integer.MAX_VALUE)
            {
                index = annotationMapping.mapSynonym(diseaseID);
            }
        }

        if (index == Integer.MAX_VALUE)
            return null;

        return associations[index];
    }

    /**
     * Returns whether the given name is an object symbol.
     *
     * @param name defines the name that should be checked
     * @return whether name is an object symbol or not
     */
    public boolean isObjectSymbol(ByteString name)
    {
        return annotationMapping.mapSymbol(name) != Integer.MAX_VALUE;
    }

    /**
     * Returns whether the given name is an object id.
     *
     * @param name defines the name that should be checked
     * @return whether name is an object id or not
     */
    public boolean isObjectID(ByteString name)
    {
        return annotationMapping.mapObjectID(name) != Integer.MAX_VALUE;
    }

    /**
     * Returns whether the given name is a synonym.
     *
     * @param name defines the name that should be checked
     * @return whether name is a synonym
     */
    public boolean isSynonym(ByteString name)
    {
        return annotationMapping.mapSynonym(name) != Integer.MAX_VALUE;
    }

    /**
     * A way to get all annotated genes in the container
     *
     * @return The annotated genes as a Set
     */
    public Set<ByteString> getAllAnnotatedGenes()
    {
        Set<ByteString> symbols = new HashSet<ByteString>();
        for (ByteString bs : annotationMapping.getSymbols())
            symbols.add(bs);

        return symbols;
    }

    public boolean containsGene(ByteString g1)
    {
        return get(g1) != null;
    }

    public Iterator<Disease2Association> iterator()
    {
        return new Iterator<Disease2Association>()
        {
            int current;

            @Override
            public boolean hasNext()
            {
                if (current == associations.length)
                    return false;
                return true;
            }

            @Override
            public Disease2Association next()
            {
                Disease2Association value = associations[current];
                current++;
                return value;
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * @return all evidence codes and their occurrence count that can be found in the container.
     */
    public Map<String,Integer> getAllEvidenceCodes()
    {
        Map<String,Integer> evidenceCounts = new HashMap<String, Integer>();
        for (Disease2Association g2a : associations)
        {
            for (Association a : g2a)
            {
                String ev = a.getEvidence().toString();
                int count;

                if (evidenceCounts.containsKey(ev)) count = evidenceCounts.get(ev) + 1;
                else count = 1;

                evidenceCounts.put(ev, count);
            }
        }
        return evidenceCounts;
    }
}