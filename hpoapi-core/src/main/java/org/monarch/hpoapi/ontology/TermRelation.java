package org.monarch.hpoapi.ontology;

/**
 * The relation a term can have to another.
 *
 * @author sba
 */
public enum TermRelation
{
    IS_A,
    PART_OF_A,
    REGULATES,
    NEGATIVELY_REGULATES,
    POSITIVELY_REGULATES,
    UNKOWN
}