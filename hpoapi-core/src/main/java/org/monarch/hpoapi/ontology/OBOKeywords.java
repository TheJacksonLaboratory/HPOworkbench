package org.monarch.hpoapi.ontology;

/**
 *
 */
public class OBOKeywords
{
    /* Stanza types */
    public final static byte [] TERM_KEYWORD = "term".getBytes();
    public final static byte [] TYPEDEF_KEYWORD = "typedef".getBytes();

    /* Supported header types */
    public final static byte [] FORMAT_VERSION_KEYWORD = "format-version".getBytes();
    public final static byte [] DATE_KEYWORD = "date".getBytes();
    public final static byte [] DATA_VERSION_KEYWORD = "data-version".getBytes();
    public final static byte [] SUBSETDEF_KEYWORD = "subsetdef".getBytes();

    /* Supported term types */
    public final static byte [] ID_KEYWORD = "id".getBytes();
    public final static byte [] NAME_KEYWORD = "name".getBytes();
    public final static byte [] IS_A_KEYWORD = "is_a".getBytes();
    public final static byte [] RELATIONSHIP_KEYWORD = "relationship".getBytes();
    public final static byte [] SYNONYM_KEYWORD = "synonym".getBytes();
    public final static byte [] DEF_KEYWORD = "def".getBytes();
    public final static byte [] NAMESPACE_KEYWORD = "namespace".getBytes();
    public final static byte [] ALT_ID_KEYWORD = "alt_id".getBytes();
    public final static byte [] EQUIVALENT_TO_KEYWORD = "equivalent_to".getBytes();
    public final static byte [] IS_OBSOLETE_KEYWORD = "is_obsolete".getBytes();
    public final static byte [] XREF_KEYWORD = "xref".getBytes();
    public final static byte [] SUBSET_KEYWORD = "subset".getBytes();
    public final static byte [] TRUE_KEYWORD = "true".getBytes();

    public final static byte[][] TERM_KEYWORDS =
            {
                    ID_KEYWORD,
                    NAME_KEYWORD,
                    IS_A_KEYWORD,
                    RELATIONSHIP_KEYWORD,
                    SYNONYM_KEYWORD,
                    DEF_KEYWORD,
                    NAMESPACE_KEYWORD,
                    EQUIVALENT_TO_KEYWORD,
                    IS_OBSOLETE_KEYWORD,
                    XREF_KEYWORD,
                    SUBSET_KEYWORD
            };

    /* Supported relationship types */
    public final static byte [] PART_OF_KEYWORD = "part_of".getBytes();
    public final static byte [] REGULATES_KEYWORD = "regulates".getBytes();
    public final static byte [] NEGATIVELY_REGULATES_KEYWORD = "negatively_regulates".getBytes();
    public final static byte [] POSITIVELY_REGULATES_KEYWORD = "positively_regulates".getBytes();

}