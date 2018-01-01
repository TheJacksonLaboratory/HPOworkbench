package org.monarchinitiative.hpoworkbench.hpo;

import com.github.phenomics.ontolib.ontology.data.TermId;

import java.util.ArrayList;
import java.util.List;

public class Disease {



    public List<TermId> termIds;

    private String diseaseID;
    private String diseaseName;
    private String diseaseDatabase;


    public Disease(String database, String name,String id) {
        diseaseID = id;
        diseaseName = name;
        diseaseDatabase=database;
        termIds = new ArrayList<>();
    }


    public void addHpo(TermId hpo) {
        this.termIds.add(hpo);
    }

    public List<TermId> getHpoIds() { return this.termIds; }
    public String getDiseaseDatabase() { return diseaseDatabase; }
    public String getName() {return diseaseName;}

    /*
     String db = ann.getDb();
            String disease = ann.getDbName();
            String id = ann.getDbObjectId();
            TermId hpo=ann.getHpoId();
            String key=String.format("%s_%s",id,disease);
            Disease d=null;
            if (neurodiseases.containsKey(key)) {
                d=neurodiseases.get(key);
            } else {
                d=new Disease(db,disease,id,hpo);
     */


}
