package org.monarch.hpoapi.association;

import static org.monarch.hpoapi.types.ByteString.b;
import static org.monarch.hpoapi.types.ByteString.EMPTY;

import java.util.regex.*;

import org.monarch.hpoapi.ontology.PrefixPool;
import org.monarch.hpoapi.ontology.TermID;
import org.monarch.hpoapi.types.ByteString;

/**
 * Objects of this class represent individual associations as defined by HPO
 * association files (Version March 2017). The meaning of the attributes is described in detail at
 * http://human-phenotype-ontology.github.io/documentation.html#annot.
 *
 * The file format is (in brief)
 * <OL>
 * <LI> DB (database contributing the association file; cardinality=1; example:
 * MIM)</LI>
 * <LI> DB_Object_ID (unique identifier in DB for the item ebing annotated;
 * cardinality=1; example 154700)</LI>
 * <LI> DB_Object_Name (name of a disease; Cardinality=1, example: Achondrogenesis, type IB</LI>
 * <LI> NOT: annotators are allowed to prefix NOT if a disease is <B>not</B>
 * associated with some HPO term. cardinality=0,1, example "NOT GO:nnnnnnnn"</LI>
 * <LI> HPOid: The HPO identifier. Cardinality=1, example = HP:0002487</LI>
 * <LI> DB:Reference database ref. Cardinality 1, &gt;1 (separate by |),
 * example:OMIM:154700 or PMID:15517394</LI>
 * <LI> Evidence: one of IEA, PCS, ITM, TAS.
 * Cardinality = 1</LI>
 * <LI> Onset modifier, cardinality 0,1,&gt;1. Example: A term-id from the HPO-sub-ontology below the term “Age of onset” (HP:0003674).</LI>
 * <LI> Frequency modifier:A term-id from the HPO-sub-ontology below the term “Frequency” (HP:0040279). Cardinality=1</LI>
 * <LI> With: This field is not currently used.</LI>
 * <LI> Aspect: one of O (Phenotypic abnormality), I (inheritance), C (onset and clinical course) or M (Mortality/Aging).
 * This field is mandatory; cardinality 1. </LI>
 * <LI> Synonym: This optional field can be used for a common abbreviation for the disease referred to by
 * the DB_Object_ID such as “NF1” for neurofibromatosis type 1 or “MFS” for Marfan syndrome.
 * </LI>
 * <LI> Date, Date on which the annotation was made; format is YYYY.MM.DD this field is mandatory, cardinality 1</LI>
 * <LI> Assigned_by The database & curator making the annotation. Cardinality 1.</LI>
 * </OL>
 * Objects of this class are used to represent one line of an annotation file. We are
 * interested in parsing the DB_Object_Symbol, NOT, aspect, and synonyms. The
 * English label of an HPO term corresponding to the HPOid is not provided in the
 * association file.
 *
 * @author Peter Robinson, Sebastian Bauer
 * @version 0.2 (April 5, 2017)
 */

public class Association
{
    /** A unique identifier in the database such as an accession number */
    private ByteString DB_Object;

    /** A unique symbol such as a gene name (primary id) */
    private ByteString DB_Object_Symbol;

    /** The evidence */
    private ByteString evidence;

    /** The aspect */
    private ByteString aspect;

    /** e.g., HP:0015888 */
    private TermID termID;

    /** Has a not qualifier? */
    private boolean notQualifier;

	/** A synonym for the identifier */
    private ByteString synonym;

    /** Used to hold the tab-separated fields of each line during parsing */
    private final static String DELIM = "\t";

    /** Number of fields in each gene_association.*** line */
    private final static int FIELDS = 14;

    /** Index of db field */
    private final static int DBFIELD = 0;

    /** Index of dbObject_ID field */
    private final static int DBOBJECTIDFIELD = 1;

    /** Index of dbName field */
    private final static int DBNAMEFIELD = 2;

    /** Index of NOT field */
    private final static int QUALIFIERFIELD = 3;

    /** Index of HP:id field */
    private final static int HPOFIELD = 4;

    /** Index of dbReference:id field */
    private final static int DBREFERENCEFIELD = 5;

    /** Index of evidence field */
    private final static int EVIDENCEFIELD = 6;

    /** Index of aspect field */
    private final static int ASPECTFIELD = 10;

    /** Index of synonym field */
    private final static int SYNONYMFIELD = 11;

    /** Index fo dbObjectType field */
    private final static int DATEFIELD = 12;

    /** Index fo AssignedBy field */
    private final static int ASSSIGNEDBYFIELD = 13;

    /** Use this pattern to split tab-separated fields on a line */
    private static final Pattern pattern = Pattern.compile(DELIM);

    private static final ByteString emptyString = EMPTY;

    private static final ByteString notString = b("NOT");

    /**
     * @param line :
     *            line from a gene_association file
     * @throws Exception which contains a failure message
     */
    public Association(String line) throws Exception
    {
        initFromLine(this, line, null);
    }

    /**
     * Constructs a new association object.
     *
     * @param db_object_symbol the name of the object
     * @param goIntID the of the term to which this object is annotated
     *
     * @deprecated as it works only for Gene Ontology IDs.

    public Association(ByteString db_object_symbol, int goIntID)
    {
        DB_Object = synonym = ByteString.EMPTY;
        DB_Object_Symbol = db_object_symbol;
        termID = new TermID(goIntID);
    }
     */


    /**
     * Constructs a new association object annotating
     * the given db_object_symbol to the given termID.
     *
     * @param db_object_symbol the object symbol of the association
     * @param termID specifies the id of the term to which the object is associated
     */
    public Association(ByteString db_object_symbol, TermID termID)
    {
        DB_Object = synonym = EMPTY;
        DB_Object_Symbol = db_object_symbol;
        this.termID = termID;
    }

    /**
     * Constructs a new association object annotating
     * the given db_object_symbol with the given term.
     *
     * @param db_object_symbol the object symbol of the association
     * @param term specifies the term to which the object is associated
     */
    public Association(ByteString db_object_symbol, String term)
    {
        DB_Object = synonym = EMPTY;
        DB_Object_Symbol = db_object_symbol;
        termID = new TermID(term);
    }

    private Association() {};

    /**
     * Returns the Term ID of this association.
     *
     * @return the term id.
     */
    public TermID getTermID()
    {
        return termID;
    }

    /**
     * @return the objects symbol (primary id).
     */
    public ByteString getObjectSymbol()
    {
        return DB_Object_Symbol;
    }

    /**
     * @return the association's synonym.
     */
    public ByteString getSynonym()
    {
        return synonym;
    }

    /**
     * @return whether this association is qualified as "NOT".
     */
    public boolean hasNotQualifier()
    {
        return notQualifier;
    }

    /**
     * @return name of DB_Object, usually a name that has meaning in a database,
     *         for instance, a swissprot accession number
     */
    public ByteString getDB_Object()
    {
        return DB_Object;
    }

    /**
     * @return the associations's aspect.
     */
    public ByteString getAspect()
    {
        return aspect;
    }

    /**
     * @return the evidence code of the annotation.
     */
    public ByteString getEvidence()
    {
        return evidence;
    }


    /**
     * Sets the term id of this association.
     *
     * @param termID
     */
    void setTermID(TermID termID)
    {
        this.termID = termID;
    }


    /**
     * Parse one line and distribute extracted values. Note that we use the
     * String class method trim to remove leading and trailing whitespace, which
     * occasionally is found (mistakenly) in some GO association files (for
     * instance, in 30 entries of one such file that will go nameless :-) ).
     *
     * We are interested in 2) DB_Object, 3) DB_Object_Symbol, NOT, GOid,
     * Aspect, synonym.
     *
     * @param a the object to be initialized
     * @param line a line from a gene_association file
     * @param prefixPool the prefix pool to be used (may be null).
     * @throws Exception which contains a failure message
     */
    private static void initFromLine(Association a, String line, PrefixPool prefixPool)
    {
        a.DB_Object = a.DB_Object_Symbol = a.synonym = emptyString;
        a.termID = null;

		/* Split the tab-separated line: */
        String[] fields = pattern.split(line, FIELDS);

        a.DB_Object = new ByteString(fields[DBFIELD].trim());

		/*
		 * DB_Object_Symbol should always be at 2 (or is missing, then this
		 * entry wont make sense for this program anyway)
		 */
        //a.DB_Object_Symbol = new ByteString(fields[DBOBJECTSYMBOLFIELD].trim());  TODO!!!

        a.evidence = new ByteString(fields[EVIDENCEFIELD].trim());
        a.aspect = new ByteString(fields[ASPECTFIELD].trim());

		/* TODO: There are new fields (colocalizes_with (a component) and
		 * contributes_to (a molecular function term) ), checkout how
		 * these should be fitted into this framework */

        String [] qualifiers = fields[QUALIFIERFIELD].trim().split("\\|");
        for (String qual : qualifiers)
            if (qual.equalsIgnoreCase("not")) a.notQualifier = true;

		/* Find GO:nnnnnnn */
        fields[HPOFIELD] = fields[HPOFIELD].trim();
        a.termID = new TermID(fields[HPOFIELD],prefixPool);

        a.synonym = new ByteString(fields[SYNONYMFIELD].trim());
    }

    /**
     * Create an association from a GAF line. Uses the supplied prefix pool.
     *
     * @param line the GAF line
     * @param pp the prefix pool to be used.
     * @return the created association
     */
    public static Association createFromGAFLine(String line, PrefixPool pp)
    {
        Association a = new Association();
        initFromLine(a, line, pp);
        return a;
    }

    /**
     * Create an association from a GAF line.
     *
     * @param line the GAF line
     * @return the created association
     */
    public static Association createFromGAFLine(String line)
    {
        return createFromGAFLine(line, null);
    }

    /**
     * Create an association from a GAF ByteString line using the given prefix pool.
     *
     * @param line the GAF line
     * @param pp the prefix pool to be used.
     * @return the created association
     */
    public static Association createFromGAFLine(ByteString line, PrefixPool pp)
    {
        return createFromGAFLine(line.toString(),pp);
    }

    /**
     * Create an association from a GAF ByteString line.
     *
     * @param line the GAF line
     * @return the created association
     */
    public static Association createFromGAFLine(ByteString line)
    {
        return createFromGAFLine(line,null);
    }

    /**
     * Create an association from a byte array.
     *
     * @param byteBuf the byteBuf
     * @param offset the offset of the first byte to be considered
     * @param len number of bytes to be considered
     * @param prefixPool the prefix pool that should be used.
     * @return the created association
     */
    public static Association createFromGAFLine(byte[] byteBuf, int offset, int len, PrefixPool prefixPool)
    {
        Association a = new Association();
        a.DB_Object = a.DB_Object_Symbol = a.synonym = emptyString;

        int fieldOffset = offset;
        int p = offset;
        int fieldNo = 0;

        while (p < offset + len)
        {
            if (byteBuf[p] == '\t')
            {
				/* New field */
                switch (fieldNo)
                {
                    case DBFIELD: 	a.DB_Object = new ByteString(byteBuf,fieldOffset,p); break;
                   // case	DBOBJECTSYMBOLFIELD:	a.DB_Object_Symbol = new ByteString(byteBuf,fieldOffset,p); break;
                    case	EVIDENCEFIELD:	a.evidence = new ByteString(byteBuf,fieldOffset,p); break;
                    case	ASPECTFIELD:	a.aspect = new ByteString(byteBuf,fieldOffset,p); break;
                    case	QUALIFIERFIELD: a.notQualifier = new ByteString(byteBuf,fieldOffset,p).indexOf(notString) != -1; break;
                    case	SYNONYMFIELD:	a.synonym = new ByteString(byteBuf,fieldOffset,p); break;
                    case	HPOFIELD:		a.termID = new TermID(new ByteString(byteBuf,fieldOffset,p),prefixPool); break;

                }

                fieldOffset = p + 1;
                fieldNo++;
            }
            p++;
        }
        return a;
    }
}