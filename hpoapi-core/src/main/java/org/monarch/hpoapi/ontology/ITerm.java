package org.monarch.hpoapi.ontology;

import org.monarch.hpoapi.types.ByteString;

/**
 * The read-only interface for terms.
 *
 * @author Sebastian Bauer
 */
public interface ITerm
{
    /**
     * Returns the id as vanilla TermID object.
     *
     * @return the id
     */
    public TermID getID();

    /**
     * Returns the name of the term.
     *
     * @return the name
     */
    public ByteString getName();

    /**
     * Returns the associated namespace of the term.
     *
     * @return the namespace
     */
    public Namespace getNamespace();

    /**
     * Returns the parent term ids and their relation.
     *
     * @return the parental terms and there relation.
     */
    public ParentTermID[] getParents();

    /**
     * @return whether term is declared as obsolete
     */
    public boolean isObsolete();

    /**
     * Returns the definition of this term. Might be null if none is available.
     *
     * @return the definition or null.
     */
    public ByteString getDefinition();

    /**
     * Return the term id of terms that are declared as equivalent.
     * @return equivalent terms
     */
    public TermID[] getEquivalents();

    /**
     * Returns the terms that have been declared as alternatives of this term.
     *
     * @return the alternatives.
     */
    public TermID[] getAlternatives();

    /**
     * @return the subsets.
     */
    public Subset[] getSubsets();

    /**
     * Returns the synonyms.
     *
     * @return the synonyms.
     */
    public ByteString[] getSynonyms();

    /**
     * Returns the associated xrefs
     *
     * @return the associated xrefs
     */
    public TermXref[] getXrefs();
}