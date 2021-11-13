package org.monarchinitiative.hpoworkbench.model;


public enum DiseaseDatabase {
        ORPHANET("ORPHA"), OMIM("OMIM"), DECIPHER("DECIPHER"), ALL("ALL");


    private final String dbasename;




    DiseaseDatabase(String databas) {
        this.dbasename=databas;
    }


    public boolean isOmim() {
        return this==OMIM;
    }
    public boolean isOrphanet() { return this==ORPHANET; }
    public boolean isDecipher() {
        return this==DECIPHER;
    }

    public DiseaseDatabase fromString(String db) {
        return switch (db) {
            case "OMIM", "omim" -> OMIM;
            case "ORPHA", "ORPHANET" -> ORPHANET;
            case "DECIPHER" -> DECIPHER;
            default -> ALL;
        };
    }

}
