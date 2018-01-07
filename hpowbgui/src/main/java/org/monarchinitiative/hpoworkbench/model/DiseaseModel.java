package org.monarchinitiative.hpoworkbench.model;


public class DiseaseModel {

    public enum database {
        ORPHANET, OMIM, DECIPHER, ALL
    }

    private final database dbase;

    private final String disease_id;

    private final String diseasename;

    public String getDiseaseName() { return diseasename; }

    public String getDiseaseDbAndId() {
        String db;
        if (dbase.equals(database.ORPHANET)) db="ORPHA";
        else if (dbase.equals(database.OMIM)) db="OMIM";
        else if (dbase.equals(database.DECIPHER)) db="DECIPHER";
        else db="?";
        return String.format("%s:%s",db,disease_id);
    }


    public DiseaseModel(String databas, String id, String name) {

        disease_id=id;
        diseasename=name;
        switch (databas) {
            case "OMIM" :
                dbase=database.OMIM;
                break;
            case "DECIPER":
                dbase=database.DECIPHER;
                break;
            case "ORPHA":
                dbase=database.ORPHANET;
                break;
            default:
                dbase=database.ALL;

        }
    }


    public boolean isOmim() {
        return dbase==database.OMIM;
    }

    public boolean isOrphanet() {
        return dbase==database.ORPHANET;
    }

    public boolean isDecipher() {
        return dbase==database.DECIPHER;
    }

    public database database() { return dbase; }



    /** Hash code is based on the end and start positions as well as on the chromosome */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime * diseasename.hashCode();
        result = result * disease_id.hashCode();
        result = result * database().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DiseaseModel other = (DiseaseModel) obj;
        return (database().equals(other.database()) &&
            disease_id.equals(other.disease_id) &&
            diseasename.equals(other.diseasename));
    }



}
