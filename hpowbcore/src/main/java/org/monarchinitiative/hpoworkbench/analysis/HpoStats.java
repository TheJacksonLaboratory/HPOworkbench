package org.monarchinitiative.hpoworkbench.analysis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.exception.HPOException;
import org.monarchinitiative.hpoworkbench.io.HPOAnnotationParser;
import org.monarchinitiative.hpoworkbench.io.HPOParser;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoAnnotation;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.formats.hpo.HpoTerm;
import org.monarchinitiative.phenol.ontology.data.ImmutableTermId;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getDescendents;

/**
 * Get all the numbers we can for the HPO and annotations to it. This will be used to display these numbers
 * in the GUI.
 */
public class HpoStats {

    private static final Logger LOGGER = LogManager.getLogger();
    private String hpopath;
    private String annotpath;
    private HpoOntology hpoOntology=null;
    /** All disease annotations for the entire ontology. */
    private Map<String,HpoDisease> diseaseMap =null;
    /** Set of all HPO terms that are descendents of {@link #termIdOfInterest}. */
    private Set<TermId> descendentsOfTheTermOfInterest =null;
    private Set<TermId> adultOnset=null;
    private Set<TermId> childhoodOnset=null;


    private List<HpoDisease> omim;
    private List<HpoDisease> orphanet;
    private List<HpoDisease> decipher;
    /** Id for root (All) */
    private static final String rootHpoTerm="HP:0000001";

    private TermId termIdOfInterest;
    /** Total number of terms that descend from the term of interest */
    private int n_terms;
    /** Total number of terms that have a textual definition. */
    private int n_textual_def;
    /** Total number of synonyms. */
    private int n_synonyms;
    /** Number of OMIM diseases with at least one annotation to the term of interest or its descendents */
    private int n_omim;
    /** Number of Orphanet diseases with at least one annotation to the term of interest or its descendents */
    private int n_orphanet;
    /** Number of DECIPHER diseases with at least one annotation to the term of interest or its descendents */
    private int n_decipher;


    public HpoStats(String hpopath, String annotationpath) throws HPOException{
        this(hpopath,annotationpath,rootHpoTerm);
    }


    public String getHpoTerm() {
        HpoTerm term = hpoOntology.getTermMap().get(termIdOfInterest);
        return String.format("%s [%s]",term.getName(),termIdOfInterest.getIdWithPrefix());
    }

    public String getHpoDefinition() {
        HpoTerm term = hpoOntology.getTermMap().get(termIdOfInterest);
        return term.getDefinition();
    }



    public HpoStats(HpoOntology ontolog,Map<String, HpoDisease> d2amap) throws HPOException {
        termIdOfInterest=ImmutableTermId.constructWithPrefix(rootHpoTerm);
        hpoOntology=ontolog;
        omim=new ArrayList<>();
        orphanet=new ArrayList<>();
        decipher=new ArrayList<>();
        this.diseaseMap=d2amap;
        getDescendentsOfTermOfInterest();
        filterDiseasesAccordingToDatabase();
    }


    /**
     *
     * @param hpo Path to hp.obo file
     * @param annotations Path to phenotype.hpoa
     * @param term a string such as "HP:0000123"
     * @throws HPOException
     */
    public HpoStats(String hpo,String annotations,String term) throws HPOException {
        this.hpopath = hpo;
        this.annotpath = annotations;
        LOGGER.trace("HPO path: {}, annotation path: {}", hpopath, annotpath);
        if (!term.startsWith("HP:") || term.length() != 10) {
            throw new HPOException(String.format("Malformed HPO id: \"%s\"", term));
        } else {
            termIdOfInterest = ImmutableTermId.constructWithPrefix(term);
        }
        omim=new ArrayList<>();
        orphanet=new ArrayList<>();
        decipher=new ArrayList<>();
        inputHPOdata();
        getDescendentsOfTermOfInterest();
        filterDiseasesAccordingToDatabase();
    }

    private void getDescendentsOfTermOfInterest() throws HPOException {
        if (termIdOfInterest==null) {
            throw new HPOException("The Term of interest was not initialized");
        }
        if (! hpoOntology.getTermMap().containsKey(termIdOfInterest)) {
            throw new HPOException("Could not retrieve term for term id: "+termIdOfInterest);
        }
        String name = String.format("%s [%s]",hpoOntology.getTermMap().get(termIdOfInterest).getName(),termIdOfInterest.getIdWithPrefix() );
        descendentsOfTheTermOfInterest = getDescendents(hpoOntology,termIdOfInterest);
        this.n_textual_def = getNumberOfTermsWithDefinition(hpoOntology,descendentsOfTheTermOfInterest);
        this.n_synonyms = getTotalNumberOfSynonyms(hpoOntology,descendentsOfTheTermOfInterest);
        n_terms=descendentsOfTheTermOfInterest.size();
        LOGGER.trace("We found a total of {} terms annotated to {} or descendents", n_terms, name);
        LOGGER.trace("Of these terms, {} has a textual definition. There were a total of {} synonyms.",n_textual_def,n_synonyms);
    }

    /**
     * Calculates and returns the total number of synonyms
     * @param ontology reference to Hpo ontology object
     * @param terms the set of terms for which we want to calculate the number of synomnyms
     * @return number of synonyms.
     */
    private int getTotalNumberOfSynonyms(HpoOntology ontology, Set<TermId> terms) {
        int n=0;
        for (TermId tid : terms) {
            n+=ontology.getTermMap().get(tid).getSynonyms().size();
        }
        return n;
    }

    /**
     * Calculates and returns the total number of terms that have a textual definition
     * @param ontology reference to Hpo ontology object
     * @param terms the set of terms for which we want to calculate the number of terms with definition
     * @return number of terms with definition.
     */
    private int getNumberOfTermsWithDefinition(HpoOntology ontology, Set<TermId> terms) {
        int n=0;
        for (TermId tid : terms) {
            String def=ontology.getTermMap().get(tid).getDefinition();
            if (def!=null && def.length()>0) n++;
        }
        return n;
    }

    /** is the disease annotated to the term we are interested in? */
    private boolean diseaseAnnotatedToTermOfInterest(HpoDisease d) {
        List<HpoAnnotation> tiwmlist= d.getPhenotypicAbnormalities();
        for (HpoAnnotation id:tiwmlist) {
            if (this.descendentsOfTheTermOfInterest.contains(id.getTermId()))
                return true;
        }
        return false;
    }

    public int getN_terms() {
        return n_terms;
    }

    public int getN_textual_def() {
        return n_textual_def;
    }

    public int getN_synonyms() {
        return n_synonyms;
    }

    public int getN_omim() {
        return n_omim;
    }

    public int getN_orphanet() {
        return n_orphanet;
    }

    public int getN_decipher() {
        return n_decipher;
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
            }
        }
        String termname=hpoOntology.getTermMap().get(termIdOfInterest).getName();
        n_omim=omim.size();
        n_orphanet=orphanet.size();
        n_decipher=decipher.size();

        LOGGER.trace(String.format("We found %d diseases in OMIM annotated to %s or descendents",omim.size(),termname));
        LOGGER.trace(String.format("We found %d diseases in Orphanet annotated to %s or descendents",orphanet.size(),termname));
        LOGGER.trace(String.format("We found %d diseases in DECIPHER annotated to %s or descendents",decipher.size(),termname));
    }


    private void inputHPOdata() throws HPOException{
        File f = new File(hpopath);
        if (! f.exists()) {
            throw new HPOException(String.format("Could not find hpo ontology file at\"%s\". Terminating program...", hpopath ));
        }
        f=new File(annotpath);
        if (! f.exists()) {
            throw new HPOException(String.format("Could not find phenotype annotation file at\"%s\". Terminating program...", annotpath ));
        }
        LOGGER.trace(String.format("inputting data with files %s and %s",hpopath,annotpath));
        HPOParser parser = new HPOParser(hpopath);
        hpoOntology=parser.getHPO();
        try {
            HPOAnnotationParser annotparser = new HPOAnnotationParser(annotpath, hpoOntology);
            diseaseMap = annotparser.getDiseaseMap();
        } catch (PhenolException pe) {
            pe.printStackTrace();
            throw new HPOException(pe.getMessage());
        }
    }
}
