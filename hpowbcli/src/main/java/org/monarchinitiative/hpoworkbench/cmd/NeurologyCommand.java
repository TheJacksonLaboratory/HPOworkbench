package org.monarchinitiative.hpoworkbench.cmd;


import org.apache.log4j.Logger;
import org.monarchinitiative.hpoworkbench.hpo.Disease;
import org.monarchinitiative.hpoworkbench.io.HPOAnnotationParser;
import org.monarchinitiative.hpoworkbench.io.HPOParser;
import org.monarchinitiative.phenol.formats.hpo.HpoDiseaseAnnotation;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.formats.hpo.HpoTerm;
import org.monarchinitiative.phenol.ontology.data.ImmutableTermId;
import org.monarchinitiative.phenol.ontology.data.ImmutableTermPrefix;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermPrefix;

import java.io.File;
import java.util.*;

/** This is a prototype to test usage of the ontolib library. It should be factored into an App.
 *
 */
public class NeurologyCommand extends HPOCommand  {
    private static Logger LOGGER = Logger.getLogger(NeurologyCommand.class.getName());
    private String hpopath=null;
    private String annotpath=null;
    HpoOntology hpoOntology=null;
    List<HpoDiseaseAnnotation>annots=null;
    /** Set of all HPO terms that are descencents of {@code Abnormality of the nervous system}, HP:0000707. */
    Set<TermId> neurologyDescendents=null;
    Set<TermId> adultOnset=null;
    Set<TermId> childhoodOnset=null;
    /** Directory where hp.obo and phenptype_annoptation.tab have been downloaded. */
    private final String datadirectory;


    Map<String,Disease> diseases =null;

    List<Disease> omim;
    List<Disease> orphanet;
    List<Disease> decipher;



    public NeurologyCommand(String directory) {
        datadirectory=directory;
        diseases =new HashMap<>();
        omim=new ArrayList<>();
        orphanet=new ArrayList<>();
        decipher=new ArrayList<>();
        this.hpopath=String.format("%s%shp.obo",this.datadirectory, File.separator);
        this.annotpath=String.format("%s%sphenotype_annotation.tab",this.datadirectory, File.separator);
    }





    private void inputHPOdata(String hpo, String annot) {

        if (hpo==null)hpo="data/hp.obo";
        if (annot==null)annot="data/phenotype_annotation.tab";

        LOGGER.trace(String.format("inputting data with files %s and %s",hpo,annot));
        HPOParser parser = new HPOParser(hpo);
        hpoOntology=parser.getHPO();
        HPOAnnotationParser annotparser=new HPOAnnotationParser(annot);
        annots=annotparser.getAnnotations();
    }

    private void getNeurologyTerms() {
        TermPrefix tp =new ImmutableTermPrefix("HP");
        TermId neuroId = new ImmutableTermId(tp, "0000707");
        neurologyDescendents=new HashSet<>() ;
        for (HpoTerm t: this.hpoOntology.getTerms()) {
            Set<TermId> ancs= hpoOntology.getAncestorTermIds(t.getId());
            if (ancs.contains(neuroId)) {
                neurologyDescendents.add(t.getId());
            }
        }
        LOGGER.trace(String.format("We found a total of %d neurology terms",neurologyDescendents.size()));
    }


    private void getAdultOnsetTerms() {

        // Adult onset HP:0003581 (has various children)
        TermPrefix tp =new ImmutableTermPrefix("HP");
        TermId adultOnsetId = new ImmutableTermId(tp, "0003581");
        adultOnset=new HashSet<>();
        adultOnset.add(adultOnsetId);
        for (HpoTerm t: this.hpoOntology.getTerms()) {
            Set<TermId> ancs= hpoOntology.getAncestorTermIds(t.getId());
            if (ancs.contains(adultOnsetId)) {
                adultOnset.add(t.getId());
            }
        }
        LOGGER.trace(String.format("We found a total of %d adult onset terms",adultOnset.size()));
    }

    private void getChildhoodOnsetTerms() {
        childhoodOnset = new HashSet<>();
        TermPrefix tp =new ImmutableTermPrefix("HP");
        childhoodOnset.add(new ImmutableTermId(tp,"0011463"));//Childhood onset
        TermId antenatal=new ImmutableTermId(tp,"0030674"); //Antenatal onset
        childhoodOnset.add(antenatal);
        for (HpoTerm t: this.hpoOntology.getTerms()) {
            Set<TermId> ancs= hpoOntology.getAncestorTermIds(t.getId());
            if (ancs.contains(antenatal)) {
                childhoodOnset.add(t.getId());
            }
        }
        childhoodOnset.add(new ImmutableTermId(tp,"0003577")); // Congenital onset
        childhoodOnset.add(new ImmutableTermId(tp,"0011460")); // Embryonal onset
        childhoodOnset.add(new ImmutableTermId(tp,"0011461")); // Fetal onset
        childhoodOnset.add(new ImmutableTermId(tp,"0003593")); // Infantile onset
        childhoodOnset.add(new ImmutableTermId(tp,"0003621")); // Juvenile onset
        childhoodOnset.add(new ImmutableTermId(tp,"0003623")); // neonatal onset

    }




    private void inputDiseases() {
        for (HpoDiseaseAnnotation ann: annots) {
            String db = ann.getDb();
            String disease = ann.getDbName();
            String id = ann.getDbObjectId();
            TermId hpo=ann.getHpoId();
            String key=String.format("%s_%s",id,disease);
            Disease d=null;
            if (diseases.containsKey(key)) {
                d= diseases.get(key);
            } else {
                d=new Disease(db,disease,id);
                this.diseases.put(key,d);
            }
            d.addHpo(hpo);

        }
    }


    boolean isNeuroDisease(Disease d) {
        List<TermId> ids=d.getHpoIds();
        for (TermId id:ids) {
          if (this.neurologyDescendents.contains(id))
              return true;
        }
        return false;
    }


    private void filterNeuroDiseasesAccordingToDatabase() {
        for (Disease d:this.diseases.values()) {
            if (!isNeuroDisease(d)) {
                continue;
            }
            String database=d.getDiseaseDatabase();
            if (database.equals("OMIM")) {
                omim.add(d);
            } else if (database.equals("ORPHA")){
                orphanet.add(d);
            } else if (database.equals("DECIPHER")) {
                decipher.add(d);
            } else {
                LOGGER.error("Did not recognize data base"+ database);
                System.exit(1);
            }
        }
        LOGGER.trace(String.format("We found %d neurological diseases in OMIM",omim.size()));
        LOGGER.trace(String.format("We found %d neurological diseases in Orphanet",orphanet.size()));
        LOGGER.trace(String.format("We found %d neurological diseases in DECIPHER",decipher.size()));
    }


    private boolean hasAdultOnset(Disease d) {
        List<TermId> ids=d.getHpoIds();
        for (TermId id:ids) {
            if (this.adultOnset.contains(id))
                return  true;
        }
        return false;
    }

    private boolean hasChildhoodOnset(Disease d) {
        List<TermId> ids=d.getHpoIds();
        for (TermId id:ids) {
            if (this.childhoodOnset.contains(id))
                return  true;
        }
        return false;
    }



    private void filterNeuroDiseasesAccordingToOnset(List<Disease> diseases) {
        int no_onset=0;
        int early_onset=0;
        int adult_onset=0;
        for (Disease d:diseases) {
            if (hasAdultOnset(d))
                adult_onset++;
            else if (hasChildhoodOnset(d))
                early_onset++;
            else
                no_onset++;
        }

        LOGGER.trace("ONSET CATEGORIES");
        LOGGER.trace(String.format("\tChildhood: %d",early_onset));
        LOGGER.trace(String.format("\tAdult: %d",adult_onset));
        LOGGER.trace(String.format("\tNo data: %d",no_onset));

    }




   @Override
    public  void run() {
       inputHPOdata(this.hpopath,this.annotpath);
       getNeurologyTerms();
       inputDiseases();
       filterNeuroDiseasesAccordingToDatabase();
       getAdultOnsetTerms();
       getChildhoodOnsetTerms();
       LOGGER.trace("Getting OMIM diseases according to onset");
       filterNeuroDiseasesAccordingToOnset(this.omim);
       LOGGER.trace("Getting ORPHANET diseases according to onset");
       filterNeuroDiseasesAccordingToOnset(this.orphanet);
       LOGGER.trace("Getting DECIPER diseases according to onset");
       filterNeuroDiseasesAccordingToOnset(this.decipher);
    }




    @Override
    public String getName() {return "neuro";}

}
