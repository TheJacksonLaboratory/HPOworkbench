package org.monarch.hpoapi.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.monarch.hpoapi.association.AnnotationContext;
import org.monarch.hpoapi.association.Association;
import org.monarch.hpoapi.ontology.PrefixPool;
import org.monarch.hpoapi.ontology.Term;
import org.monarch.hpoapi.ontology.TermID;
import org.monarch.hpoapi.ontology.TermMap;
import org.monarch.hpoapi.types.ByteString;
import org.monarch.hpoapi.collections.ObjectIntHashMap;

/**
 * A GAF Line scanner.
 *
 * @author Sebastian Bauer, Peter Robinson
 * @version 0.2.2 (24 April, 2017)
 */
class GAFByteLineScanner extends AbstractByteLineScanner
{
    private static Logger logger = Logger.getLogger(GAFByteLineScanner.class.getName());

    /** The wrapped input */
    private IParserInput input;

    /** Contains all items whose associations should gathered or null if all should be gathered */
    private Set<ByteString> names;

    /** All known terms */
    private TermMap terms;

    /** Set of evidences that shall be considered or null if all should be considered */
    private Set<ByteString> evidences;

    /** Monitor progress */
    private IAssociationParserProgress progress;

    private int lineno = 0;
    private long millis = 0;
    public int good = 0;
    public int bad = 0;
    public int skipped = 0;
    public int nots = 0;
    public int evidenceMismatch = 0;
    public int kept = 0;
    public int obsolete = 0;
    private int symbolWarnings = 0;
    private int dbObjectWarnings = 0;

    /** Mapping from gene (or gene product) names to Association objects */
    private ArrayList<Association> associations = new ArrayList<Association>();

    /** Our prefix pool */
    private PrefixPool prefixPool = new PrefixPool();

    private HashMap<TermID, Term> altTermID2Term = null;
    private HashSet<TermID> usedGoTerms = new HashSet<TermID>();

    /**********************************************************************/

    /** Unique list of items seen so far */
    private List<ByteString> items = new ArrayList<ByteString>();

    /** And the corresponding object ids. These would be the database IDs of diseases or models, e.g., 600123 for OMIM. */
    private List<ByteString> objectIds = new ArrayList<ByteString>();

    /** Maps object symbols to item indices within the items list */
    private ObjectIntHashMap<ByteString> objectSymbolMap = new ObjectIntHashMap<ByteString>();

    /** Maps object ids to item indices within the items list */
    private ObjectIntHashMap<ByteString> objectIdMap = new ObjectIntHashMap<ByteString>();

    /** Maps synonyms to item indices within the items list */
    private ObjectIntHashMap<ByteString> synonymMap = new ObjectIntHashMap<ByteString>();

    public GAFByteLineScanner(IParserInput input, byte [] head, Set<ByteString> names, TermMap terms, Set<ByteString> evidences, IAssociationParserProgress progress)
    {
        super(input.inputStream());

        push(head);

        this.input = input;
        this.names = names;
        this.terms = terms;
        this.evidences = evidences;
        this.progress = progress;
    }

    /** Parse one line of the annotation file. If all is successful it adds an
     *  {@link Association} object.*/
    @Override
    public boolean newLine(byte[] buf, int start, int len)
    {
		/* Progress stuff */
        if (progress != null)
        {
            long newMillis = System.currentTimeMillis();
            if (newMillis - millis > 250)
            {
                progress.update(input.getPosition());
                millis = newMillis;
            }
        }

        lineno++;

		/* Ignore comments */
        if (len < 1 || buf[start]=='!')
            return true;

        Association assoc = Association.createFromGAFLine(buf,start,len,prefixPool);

        TermID currentTermID = assoc.getTermID();

        Term currentTerm;

        good++;

        if (assoc.hasNotQualifier())
        {
            skipped++;
            nots++;
            return true;
        }

        if (evidences != null)
        {
			/*
			 * Skip if evidence of the annotation was not supplied as
			 * argument
			 */
            if (!evidences.contains(assoc.getEvidence()))
            {
                skipped++;
                evidenceMismatch++;
                return true;
            }
        }

        currentTerm = terms.get(currentTermID);
        if (currentTerm == null)
        {
            if (altTermID2Term == null)
            {
				/* Create the alternative ID to Term map */
                altTermID2Term = new HashMap<TermID, Term>();

                for (Term t : terms)
                    for (TermID altID : t.getAlternatives())
                        altTermID2Term.put(altID, t);
            }

			/* Try to find the term among the alternative terms before giving up. */
            currentTerm = altTermID2Term.get(currentTermID);
            if (currentTerm == null)
            {
                System.err.println("Skipping association of item \"" + assoc.getObjectID() + "\" to " + currentTermID + " because the term was not found!");
                System.err.println("(Are the obo file and the association " + "file both up-to-date?)");
                skipped++;
                return true;
            } else
            {
				/* Okay, found, so set the new attributes */
                currentTermID = currentTerm.getID();
                assoc.setTermID(currentTermID);
            }
        } else
        {
			/* Reset the term id so a unique id is used */
            currentTermID = currentTerm.getID();
            assoc.setTermID(currentTermID);
        }

        usedGoTerms.add(currentTermID);

        if (currentTerm.isObsolete())
        {
            System.err.println("Skipping association of item \"" + assoc.getObjectID() + "\" to " + currentTermID + " because term is obsolete!");
            System.err.println("(Are the obo file and the association file in sync?)");
            skipped++;
            obsolete++;
            return true;
        }

        ByteString[] synonyms;

		/* populate synonym string field */
        if (assoc.getSynonym() != null && assoc.getSynonym().length() > 2)
        {
			/* Note that there can be multiple synonyms, separated by a pipe */
            synonyms = assoc.getSynonym().splitBySingleChar('|');
        } else
            synonyms = null;

        if (names != null)
        {
			/* We are only interested in associations to given genes */
            boolean keep = false;

			/* Check if synonyms are contained */
            if (synonyms != null)
            {
                for (int i = 0; i < synonyms.length; i++)
                {
                    if (names.contains(synonyms[i]))
                    {
                        keep = true;
                        break;
                    }
                }
            }

            if (keep || names.contains(assoc.getObjectID()) || names.contains(assoc.getObjectID()))
            {
                kept++;
            } else
            {
                skipped++;
                return true;
            }
        } else
        {
            kept++;
        }

		/* Add the Association to ArrayList */
        associations.add(assoc);

		/* And throw them in item buckets */
        ByteString objectSymbol = assoc.getObjectID();

		/* New code */
        int potentialObjectIndex = items.size();
        int objectIndex = objectSymbolMap.getIfAbsentPut(objectSymbol, potentialObjectIndex);
        if (objectIndex == potentialObjectIndex)
        {
			/* Object symbol was not seen before */
            items.add(objectSymbol);
            objectIds.add(assoc.getObjectID());
        } else
                   /* Object symbol was seen before */
            if (!assoc.getObjectID().equals(objectIds.get(objectIndex)))
            {
				/* Record this as a synonym now */
                synonymMap.put(assoc.getObjectID(), objectIndex);

				/* Warn about that the same symbol is used with at least two object ids */
                dbObjectWarnings++;
                if (dbObjectWarnings < 1000)
                {
                    String warning = "Line " + lineno + ": Expected that symbol \"" + assoc.getObjectID() + "\" maps to \"" + objectIds.get(objectIndex) + "\" but it maps to \"" + assoc.getDatabase() + "\"";
                    if (progress != null)
                        progress.warning(warning);
                    logger.warning(warning);
                }
            }


		/* Get how the object id is mapped to our id space */
        int objectIdIndex = objectIdMap.getIfAbsentPut(assoc.getObjectID(), objectIndex);
        if (objectIdIndex != objectIndex)
        {
			/* The same object id is is used for two object symbols, warn about it */
            symbolWarnings++;
            if (symbolWarnings < 1000)
            {
                String warning = "Line " + lineno + ": Expected that dbObject \"" + assoc.getObjectID() + "\" maps to symbol \"" + items.get(objectIdIndex) + "\" but it maps to \"" + assoc.getObjectID() + "\"";
                if (progress != null)
                    progress.warning(warning);
                logger.warning(warning);
            }
        }

        if (synonyms != null)
        {
            for (ByteString synonym : synonyms)
                synonymMap.put(synonym, objectIndex);
        }
        return true;
    }

    /**
     * @return the number of terms used by the import.
     */
    public int getNumberOfUsedTerms()
    {
        return usedGoTerms.size();
    }

    public ArrayList<Association> getAssociations()
    {
        return associations;
    }

    /**
     * @return the annotation context.
     */
    public AnnotationContext getAnnotationContext()
    {
        return new AnnotationContext(items, objectIds, objectSymbolMap, objectIdMap, synonymMap);
    }
};

