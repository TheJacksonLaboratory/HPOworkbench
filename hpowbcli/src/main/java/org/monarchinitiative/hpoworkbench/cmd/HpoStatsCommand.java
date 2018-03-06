package org.monarchinitiative.hpoworkbench.cmd;


import org.apache.log4j.Logger;
import org.monarchinitiative.hpoworkbench.hpo.Disease;
import org.monarchinitiative.hpoworkbench.io.HPOAnnotationParser;
import org.monarchinitiative.hpoworkbench.io.HPOParser;
import org.monarchinitiative.phenol.formats.hpo.*;
import org.monarchinitiative.phenol.ontology.data.ImmutableTermId;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.util.*;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getDescendents;

/**
 * Extract descriptive statistics about a a certain subhierarchy of the HPO.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class HpoStatsCommand extends HPOCommand  {
    private static Logger LOGGER = Logger.getLogger(HpoStatsCommand.class.getName());
    private final String hpopath;
    private final String annotpath;
    private HpoOntology hpoOntology=null;
    /** All disease annotations for the entire ontology. */
    private Map<String,HpoDiseaseWithMetadata> annotationMap=null;
    /** the root of the subhierarchy for which we are calculating the descriptive statistics. */
    private TermId termOfInterest;
    /** Set of all HPO terms that are descencents of {@link #termOfInterest}. */
    private Set<TermId> descendentsOfTheTermOfInterest =null;
    private Set<TermId> adultOnset=null;
    private Set<TermId> childhoodOnset=null;


    private List<HpoDiseaseWithMetadata> omim;
    private List<HpoDiseaseWithMetadata> orphanet;
    private List<HpoDiseaseWithMetadata> decipher;



    public HpoStatsCommand(String hpo,String annotations,String term) {
        this.hpopath=hpo;
        this.annotpath=annotations;
        if (! term.startsWith("HP:")) {
            LOGGER.error(String.format("Malformed HPO id: \"%s\". Terminating program...",term ));
            System.exit(1);
        }
        this.termOfInterest=ImmutableTermId.constructWithPrefix(term);
        omim=new ArrayList<>();
        orphanet=new ArrayList<>();
        decipher=new ArrayList<>();
        inputHPOdata();
    }

    @Override
    public  void run() {
        getDescendentsOfTermOfInterest();
        filterDiseasesAccordingToDatabase();
        initializeAdultOnsetTerms();
        initializeChildhoodOnsetTerms();
        LOGGER.trace("Getting OMIM diseases according to onset");
        filterNeuroDiseasesAccordingToOnset(this.omim);
        LOGGER.trace("Getting ORPHANET diseases according to onset");
        filterNeuroDiseasesAccordingToOnset(this.orphanet);
        LOGGER.trace("Getting DECIPER diseases according to onset");
        filterNeuroDiseasesAccordingToOnset(this.decipher);
    }


    private void inputHPOdata() {
        File f = new File(hpopath);
        if (! f.exists()) {
            LOGGER.error(String.format("Could not find hpo ontology file at\"%s\". Terminating program...", hpopath ));
            System.exit(1);
        }
        f=new File(annotpath);
        if (! f.exists()) {
            LOGGER.error(String.format("Could not find phenotype annotation file at\"%s\". Terminating program...", annotpath ));
            System.exit(1);
        }
        LOGGER.trace(String.format("inputting data with files %s and %s",hpopath,annotpath));
        HPOParser parser = new HPOParser(hpopath);
        hpoOntology=parser.getHPO();
        HPOAnnotationParser annotparser=new HPOAnnotationParser(annotpath,hpoOntology);
        annotationMap=annotparser.getAnnotationMap();
    }

    private void getDescendentsOfTermOfInterest() {
        descendentsOfTheTermOfInterest = getDescendents(hpoOntology,termOfInterest);
        LOGGER.trace(String.format("We found a total of %d neurology terms", descendentsOfTheTermOfInterest.size()));
    }


    private void initializeAdultOnsetTerms() {
        adultOnset=new HashSet<>();
        adultOnset.add(HpoOnsetTermIds.ADULT_ONSET);
        adultOnset.add(HpoOnsetTermIds.LATE_ONSET);
        adultOnset.add(HpoOnsetTermIds.MIDDLE_AGE_ONSET);
        adultOnset.add(HpoOnsetTermIds.YOUNG_ADULT_ONSET);
        LOGGER.trace(String.format("We found a total of %d adult onset terms",adultOnset.size()));
    }

    private void initializeChildhoodOnsetTerms() {
        childhoodOnset = new HashSet<>();
        childhoodOnset.add(HpoOnsetTermIds.CHILDHOOD_ONSET);
        childhoodOnset.add(HpoOnsetTermIds.ANTENATAL_ONSET);
        childhoodOnset.add( HpoOnsetTermIds.CONGENITAL_ONSET);
        childhoodOnset.add(HpoOnsetTermIds.EMBRYONAL_ONSET);
        childhoodOnset.add(HpoOnsetTermIds.FETAL_ONSET);
        childhoodOnset.add(HpoOnsetTermIds.INFANTILE_ONSET);
        childhoodOnset.add(HpoOnsetTermIds.JUVENILE_ONSET);
        childhoodOnset.add(HpoOnsetTermIds.NEONATAL_ONSET);
    }


    boolean diseaseAnnotatedToTermOfInterest(HpoDiseaseWithMetadata d) {
        List<TermIdWithMetadata> tiwmlist= d.getPhenotypicAbnormalities();
        for (TermIdWithMetadata id:tiwmlist) {
          if (this.descendentsOfTheTermOfInterest.contains(id.getTermId()))
              return true;
        }
        return false;
    }


    private void filterDiseasesAccordingToDatabase() {
        for (HpoDiseaseWithMetadata d:this.annotationMap.values()) {
            if (!diseaseAnnotatedToTermOfInterest(d)) {
                continue;
            }
            String database=d.getDiseaseDatabaseId();
            if (database.startsWith("OMIM")) {
                omim.add(d);
            } else if (database.startsWith("ORPHA")){
                orphanet.add(d);
            } else if (database.startsWith("DECIPHER")) {
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


    private boolean hasAdultOnset(HpoDiseaseWithMetadata d) {
        List<TermIdWithMetadata> ids=d.getPhenotypicAbnormalities();
        for (TermIdWithMetadata id:ids) {
            if (this.adultOnset.contains(id.getTermId()))
                return  true;
        }
        return false;
    }

    private boolean hasChildhoodOnset(HpoDiseaseWithMetadata d) {
        List<TermIdWithMetadata> ids=d.getPhenotypicAbnormalities();
        for (TermIdWithMetadata id:ids) {
            if (this.childhoodOnset.contains(id.getTermId()))
                return  true;
        }
        return false;
    }



    private void filterNeuroDiseasesAccordingToOnset(List<HpoDiseaseWithMetadata> diseases) {
        int no_onset=0;
        int early_onset=0;
        int adult_onset=0;
        for (HpoDiseaseWithMetadata d:diseases) {
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
    public String getName() {return "stats";}

}
