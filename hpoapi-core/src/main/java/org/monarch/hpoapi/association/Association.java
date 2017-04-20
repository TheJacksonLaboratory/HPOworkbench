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
 * <LI> database (database contributing the association file; cardinality=1; example:
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
    /** The name of the database (e.g., DECIPER or OMIM) that goes with the DB_Object_ID (which is an accession number) */
    private ByteString database;

    /** The accession number with the database (e.g., 600123 for an OMIM entry) */
    private ByteString DB_Object_ID;

    /** The name of the disease in the database (e.g., 100050 AARSKOG SYNDROME, AUTOSOMAL DOMINANT) */
    private ByteString DB_Name;

    /** The database authority (reference) supporting this assertion, e.g., PMID. */
    private ByteString DB_Reference;

    /** The evidence */
    private ByteString evidence;

    /** The aspect */
    private ByteString onset;

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

    /** Index of onset modifier field */
    private final static int ONSETFIELD = 7;

    /** Index of onset modifier field */
    private final static int FREQUENCYFIELD = 8;

    /** Index of synonym field */
    private final static int SYNONYMFIELD = 11;

    /** Index fo dbObjectType field (currently not used in this class) */
    private final static int DATEFIELD = 12;

    /** Index fo AssignedBy field (currently not used in this class) */
    private final static int ASSSIGNEDBYFIELD = 13;

    /** Use this pattern to split tab-separated fields on a line */
    private static final Pattern pattern = Pattern.compile(DELIM);

    private static final ByteString emptyString = EMPTY;

    private static final ByteString notString = b("NOT");

    /** Frequency of a disease manifestation */
    private ByteString frequency;

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
     * Constructs a new association object annotating
     * the given db_object_symbol to the given termID.
     *
     * @param db_object_symbol the object symbol of the association
     * @param termID specifies the id of the term to which the object is associated
     */
    public Association(ByteString db_object_symbol, TermID termID)
    {
        this.database = synonym = EMPTY;
       // DB_Object_Symbol = db_object_symbol;
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
        this.database = synonym = EMPTY;
       // DB_Object_Symbol = db_object_symbol;
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
     * @return the object's accession number in {@link #database} (an accession number of the disease).
     */
    public ByteString getObjectID()
    {
        return DB_Object_ID;
    }

    public ByteString getName() {
        return this.DB_Name;
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
     * @return name of database, e.g., ORPHANET
     */
    public ByteString getDatabase()
    {
        return database;
    }

    /**
     * @return the associations's aspect.
     */
    public ByteString getOnset()
    {
        return this.onset;
    }

    /**
     * @return the evidence code of the annotation.
     */
    public ByteString getEvidence()
    {
        return evidence;
    }

    /**
     * @return the database refernece
     */
    public  ByteString getDBReference() {
        return DB_Reference;
    }

    public ByteString getFrequency() {
        return frequency;
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
        /* Split the tab-separated line: */
        String[] fields = pattern.split(line, FIELDS);
        /* field 0--the database, e.g., OMIM or DECIPHER or ORPHANET */
        a.database = new ByteString(fields[DBFIELD].trim());
        a.DB_Object_ID = new ByteString(fields[DBOBJECTIDFIELD].trim());
        a.DB_Name = new ByteString(fields[DBNAMEFIELD].trim());
        a.DB_Reference = new ByteString(fields[DBREFERENCEFIELD].trim());
        a.evidence = new ByteString(fields[EVIDENCEFIELD].trim());
        a.onset = new ByteString(fields[ONSETFIELD].trim());
        a.frequency = new ByteString(fields[FREQUENCYFIELD].trim());
        /* Find HP:nnnnnnn */
        a.termID = new TermID(fields[HPOFIELD].trim(),prefixPool);
        String [] qualifiers = fields[QUALIFIERFIELD].trim().split("\\|");
        a.notQualifier=false;
        for (String qual : qualifiers)
            if (qual.equalsIgnoreCase("not")) a.notQualifier = true;
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
        //a.DB_Object = a.DB_Object_Symbol = a.synonym = emptyString;

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
                    case DBFIELD: 	a.database = new ByteString(byteBuf,fieldOffset,p); break;
                   // case	DBOBJECTSYMBOLFIELD:	a.DB_Object_Symbol = new ByteString(byteBuf,fieldOffset,p); break;
                    case	EVIDENCEFIELD:	a.evidence = new ByteString(byteBuf,fieldOffset,p); break;
                    case	ONSETFIELD:	a.onset = new ByteString(byteBuf,fieldOffset,p); break;
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