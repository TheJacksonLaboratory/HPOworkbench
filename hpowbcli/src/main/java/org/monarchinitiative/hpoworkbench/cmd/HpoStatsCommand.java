package org.monarchinitiative.hpoworkbench.cmd;


import org.apache.logging.log4j.LogManager;
import org.monarchinitiative.hpoworkbench.io.HPOAnnotationParser;
import org.monarchinitiative.hpoworkbench.io.HPOParser;
import org.monarchinitiative.phenol.base.PhenolException;
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
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    private final String hpopath;
    private final String annotpath;
    private HpoOntology hpoOntology=null;
    /** All disease annotations for the entire ontology. */
    private Map<String,HpoDisease> diseaseMap =null;
    /** the root of the subhierarchy for which we are calculating the descriptive statistics. */
    private TermId termOfInterest;
    /** Set of all HPO terms that are descencents of {@link #termOfInterest}. */
    private Set<TermId> descendentsOfTheTermOfInterest =null;
    private Set<TermId> adultOnset=null;
    private Set<TermId> childhoodOnset=null;


    private List<HpoDisease> omim;
    private List<HpoDisease> orphanet;
    private List<HpoDisease> decipher;



    public HpoStatsCommand(String hpo,String annotations,String term) {
        this.hpopath=hpo;
        this.annotpath=annotations;
        LOGGER.trace(String.format("HPO path: %s, annotations: %s",hpopath,annotpath ));
        if (! term.startsWith("HP:") || term.length()!=10) {
            LOGGER.error(String.format("Malformed HPO id: \"%s\". Terminating program...",term ));
            System.exit(1);
        }
        this.termOfInterest=ImmutableTermId.constructWithPrefix(term);
        LOGGER.trace("Term of interest: "+termOfInterest.getIdWithPrefix());
        omim=new ArrayList<>();
        orphanet=new ArrayList<>();
        decipher=new ArrayList<>();
        inputHPOdata();
    }

    @Override
    public  void run() {
        getDescendentsOfTermOfInterest();
        filterDiseasesAccordingToDatabase();
        /*initializeAdultOnsetTerms();
        initializeChildhoodOnsetTerms();
        LOGGER.trace("Getting OMIM diseases according to onset");
        filterDiseasesAccordingToOnset(this.omim);
        LOGGER.trace("Getting ORPHANET diseases according to onset");
        filterDiseasesAccordingToOnset(this.orphanet);
        LOGGER.trace("Getting DECIPER diseases according to onset");
        filterDiseasesAccordingToOnset(this.decipher);
        */
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
        try {
            HPOAnnotationParser annotparser = new HPOAnnotationParser(annotpath, hpoOntology);
            diseaseMap = annotparser.getDisdeaseMap();
        } catch (PhenolException pe) {
            pe.printStackTrace();
        }
    }

    private void getDescendentsOfTermOfInterest() {
        String name = String.format("%s [%s]",hpoOntology.getTermMap().get(termOfInterest).getName(),termOfInterest.getIdWithPrefix() );
        descendentsOfTheTermOfInterest = getDescendents(hpoOntology,termOfInterest);
        int n_textual_def = getNumberOfTermsWithDefinition(hpoOntology,descendentsOfTheTermOfInterest);
        int n_synonyms = getTotalNumberOfSynonyms(hpoOntology,descendentsOfTheTermOfInterest);
        LOGGER.trace(String.format("We found a total of %d terms annotated to %s or descendents", descendentsOfTheTermOfInterest.size(), name));
        LOGGER.trace(String.format("Of these terms, %d has a textual definition. There were a total of %d synonyms.",n_textual_def,n_synonyms));
    }

    private int getTotalNumberOfSynonyms(HpoOntology ontology, Set<TermId> terms) {
        int n=0;
        for (TermId tid : terms) {
            n+=ontology.getTermMap().get(tid).getSynonyms().size();
        }
        return n;
    }

    private int getNumberOfTermsWithDefinition(HpoOntology ontology, Set<TermId> terms) {
        int n=0;
        for (TermId tid : terms) {
            String def=ontology.getTermMap().get(tid).getDefinition();
            if (def!=null && def.length()>0) n++;
        }
        return n;
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


    boolean diseaseAnnotatedToTermOfInterest(HpoDisease d) {
        List<HpoAnnotation> tiwmlist= d.getPhenotypicAbnormalities();
        for (HpoAnnotation id:tiwmlist) {
          if (this.descendentsOfTheTermOfInterest.contains(id.getTermId()))
              return true;
        }
        return false;
    }


    private void filterDiseasesAccordingToDatabase() {
        for (HpoDisease d:this.diseaseMap.values()) {
            if (!diseaseAnnotatedToTermOfInterest(d)) {
                continue;
            }
            String database=d.getDatabase();
            if (database.startsWith("OMIM")) {
                omim.add(d);
            } else if (database.startsWith("ORPHA")){
                orphanet.add(d);
            } else if (database.startsWith("DECIPHER")) {
                decipher.add(d);
            } else {
                LOGGER.error("Did not recognize data base"+ database);
                continue;
            }
        }
        String termname=hpoOntology.getTermMap().get(termOfInterest).getName();
        LOGGER.trace(String.format("We found %d diseases in OMIM annotated to %s or descendents",omim.size(),termname));
        LOGGER.trace(String.format("We found %d diseases in Orphanet annotated to %s or descendents",orphanet.size(),termname));
        LOGGER.trace(String.format("We found %d diseases in DECIPHER annotated to %s or descendents",decipher.size(),termname));
    }


    private boolean hasAdultOnset(HpoDisease d) {
        List<HpoAnnotation> ids=d.getPhenotypicAbnormalities();
        for (HpoAnnotation id:ids) {
            if (this.adultOnset.contains(id.getTermId()))
                return  true;
        }
        return false;
    }

    private boolean hasChildhoodOnset(HpoDisease d) {
        List<HpoAnnotation> ids=d.getPhenotypicAbnormalities();
        for (HpoAnnotation id:ids) {
            if (this.childhoodOnset.contains(id.getTermId()))
                return  true;
        }
        return false;
    }



    private void filterDiseasesAccordingToOnset(List<HpoDisease> diseases) {
        int no_onset=0;
        int early_onset=0;
        int adult_onset=0;
        for (HpoDisease d:diseases) {
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
