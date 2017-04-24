package org.monarch.hpoapi.association;

import java.util.*;

import org.monarch.hpoapi.ontology.TermID;
import org.monarch.hpoapi.types.ByteString;

/**
 * <P>
 * Gene2Associations objects store all the gene ontology associations for one
 * gene.
 * </P>
 * <P>
 * Note that duplicate entries are possible in the association files. For this
 * reason, we make sure there is only one entry for each GO:id number. We do
 * this by storing a list of all goIDs seen in the arrayList goIDs.
 * </P>
 * <P>
 * This class implements the Iterable interface, so you easly can iterate
 * over the associations to this gene. Class is derived from Gene2Association.java from the Ontologizer
 *
 * @author Peter Robinson, Sebastian Bauer
 * @version 0.2.0 (April 24,2017)
 */

public class Disease2Association implements Iterable<Association>
{
    /** Name of the gene for which this object stores 0 - n associations.
     * The disease will be refered to by its Object ID (e.g., the OMIM or Orphanet
     * accession number). */
    private ByteString disease;

    /** List of HPO phenotype annotations */
    private ArrayList<Association> associations;

    /**
     * @param objectID The accession number (e.g., in Orphanet or OMIM) of the disease being annotated.
     */
    public Disease2Association(ByteString objectID)
    {
        associations = new ArrayList<Association>();
        disease = objectID;
    }

    /**
     * Add a new association (an HPO term) to the disease.
     *
     * @param a defines the {@link Association} to be added.
     */
    public void add(Association a)
    {
		/* Only add, if association is really associated with the disease */
        if (disease.equals(a.getObjectID()))
        {
            if (containsID(a.getTermID()))
                return;

            associations.add(a);
        }
    }

    public ByteString name()
    {
        return disease;
    }

    /**
     * Get an arraylist of all HPO Ids to which this disease is directly
     * annotated by extracting the information from the Association object(s)
     * belonging to the disease.
     * @return List of HPO term ids for this disease
     */
    public ArrayList<TermID> getAssociations()
    {
        ArrayList<TermID> a = new ArrayList<TermID>();
        Iterator<Association> it = associations.iterator();
        while (it.hasNext())
        {
            Association assoc = it.next();
            a.add(assoc.getTermID());
        }
        return a;
    }

    /**
     * @return An iterator to iterate over all HPO associations for this disease.
     */
    public Iterator<Association> iterator()
    {
        return associations.iterator();
    }

    /**
     * Returns whether the given HPO term id is associated with this disease.
     *
     * @param tid the id of the term that should be checked.
     * @return true if tid is contained in this mapping.
     */
    public boolean containsID(TermID tid)
    {
        for (Association assoc : associations)
        {
            if (assoc.getTermID().equals(tid))
                return true;
        }
        return false;
    }
}