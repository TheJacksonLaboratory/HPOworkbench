package org.monarch.hpoapi.ontology;


/**
 * This class is used to specify a parent accompanied with
 * the kind of relationship.
 *
 * @author sba
 */
public class ParentTermID
{
    public TermID termid;
    public TermRelation relation;

    public ParentTermID(TermID parent, TermRelation relation)
    {
        this.termid = parent;
        this.relation = relation;
    }
}