package org.monarch.hpoapi.ontology;


/**
 * Simple class for storing an external cross reference for an HPO term
*/
public class TermXref {


    /**
     * The external database name, e.g. MeSH, ICD-10, UMLS
     */
    private String database;
    /**
     * The ID in the external DB, D012587, C2077312, Q20.4
     */
    private String xrefId;
    /**
     * The name of the referenced entity e.g. "Asymmetric lower limb shortness" for UMLS - C1844734
     */
    private String xrefName;

    public TermXref(String database, String xrefId) {

        this.database 	= database;
        this.xrefId		= xrefId;

    }

    public TermXref(String database, String xrefId, String xrefName) {

        this.database 	= database;
        this.xrefId		= xrefId;
        this.xrefName	= xrefName;
    }

    @Override
    public int hashCode() {
        return database.hashCode() + xrefId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if ( ! (obj instanceof TermXref))
            return false;

        TermXref otherXref = (TermXref) obj;

        if (this.database.equals(otherXref.database) && this.xrefId.equals(otherXref.xrefId))
            return true;

        return false;


    }

    public String getDatabase() {
        return database;
    }

    public String getXrefId() {
        return xrefId;
    }

    public String getXrefName() {
        return xrefName;
    }

    /**
     * Returns 'db' - 'db-ID' if no name was given <br>
     * Returns 'db' - 'db-ID' - 'db-name' if  name was given
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuffer returnString = new StringBuffer();

        returnString.append(database);
        returnString.append(" - ");
        returnString.append(xrefId);

        if (xrefName != null){
            returnString.append(" - ");
            returnString.append(xrefName);
        }

        return returnString.toString();
    }

}
