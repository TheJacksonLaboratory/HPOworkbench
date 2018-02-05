package org.monarchinitiative.hpoworkbench.model;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermId;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermPrefix;
import com.github.phenomics.ontolib.ontology.data.TermId;
import com.github.phenomics.ontolib.ontology.data.TermPrefix;
import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.io.HPOParser;
import org.monarchinitiative.hpoworkbench.io.DirectIndirectHpoAnnotationParser;

import java.util.*;

import static org.monarchinitiative.hpoworkbench.gui.PlatformUtil.getLocalHPOPath;
import static org.monarchinitiative.hpoworkbench.gui.PlatformUtil.getLocalPhenotypeAnnotationPath;

public class Model {
    private static final Logger logger = LogManager.getLogger();
    /** We save a few settings in a file that we store in ~/.loinc2hpo/loinc2hpo.settings. This variable should
     * be initialized to the absolute path of the file. */
    private String pathToSettingsFile=null;
    /** Path to {@code hp.obo}. */
    private String pathToHpoOboFile=null;
    /** Path to the file we are creating with LOINC code to HPO annotations. */
    private String pathToAnnotationFile=null;
    private final TermPrefix HP_PREFIX = new ImmutableTermPrefix("HP");
    /** Ontology model for full HPO ontology (all subhierarchies). */
    private HpoOntology ontology=null;
    /** List of annotated diseases (direct annotations) */
    private Map<TermId,List<DiseaseModel>> annotmap=null;
    /** List of all indirected annotations (by annotation propagation rule). */
    private Map<TermId,List<DiseaseModel>> directAnnotMap=null;

    private List<String> githublabels=new ArrayList<>();

    public Model(){
        initPaths();
        importData();
    }


    public Model(String hpoOboPath, String phenoAnnotationPath) {
        this.pathToHpoOboFile=hpoOboPath;
        this.pathToAnnotationFile=phenoAnnotationPath;
        importData();
    }

    public void setGithublabels(List<String> lab) { this.githublabels=lab; }

    public List<String> getGithublabels() { return githublabels; }

    public boolean hasLabels(){ return githublabels!=null && githublabels.size()>0; }

    private void initPaths() {
        this.pathToHpoOboFile=getLocalHPOPath();
        this.pathToAnnotationFile=getLocalPhenotypeAnnotationPath();
    }

    private void importData() {
        if (ontology==null) {
            if (pathToHpoOboFile==null) {
                logger.error("Path to hp.obo was not initialized");
                return;
            }
            HPOParser parser = new HPOParser(pathToHpoOboFile);
            this.ontology=parser.getHPO();
        }
        if (annotmap==null) {
            HpoOntology ontology = getOntology();
            DirectIndirectHpoAnnotationParser parser = new DirectIndirectHpoAnnotationParser(this.pathToAnnotationFile,ontology);
            annotmap=parser.parse();
            directAnnotMap=parser.getDirectannotmap();
        }
        logger.trace("Ingested annot map with size="+annotmap.size());

    }




    public List<DiseaseModel> getDiseaseAnnotations(String hpoTermId, DiseaseModel.database dbase) {
        List<DiseaseModel> diseases=new ArrayList<>();
        TermId id = string2TermId(hpoTermId);
        if (id!=null) {
            diseases=annotmap.get(id);
        }
        if (diseases==null) return new ArrayList<>();// return empty
        // user wan't all databases, just pass through
        if (dbase== DiseaseModel.database.ALL) return diseases;
        // filter for desired database
        ImmutableList.Builder<DiseaseModel> builder = new ImmutableList.Builder();
        for (DiseaseModel dm : diseases) {
            if (dm.database().equals(dbase)) {
                builder.add(dm);
            }
        }
        return builder.build();
    }


    private TermId string2TermId(String termstring) {
        if (termstring.startsWith("HP:")) {
            termstring=termstring.substring(3);
        }
        if (termstring.length()!=7) {
            logger.error("Malformed termstring: "+termstring);
            return null;
        }
        TermId tid = new ImmutableTermId(HP_PREFIX,termstring);
        if (! ontology.getAllTermIds().contains(tid)) {
            logger.error("Unknown TermId "+tid.getIdWithPrefix());
            return null;
        }
        return tid;
    }


    public List<HpoTerm> getAnnotationTermsForDisease(DiseaseModel dmod) {
        List<HpoTerm> annotating=new ArrayList<>();
        for (TermId tid : directAnnotMap.keySet()) {
            if (directAnnotMap.get(tid).contains(dmod)) {
                HpoTerm term = ontology.getTermMap().get(tid);
                annotating.add(term);
            }
        }
        return annotating;
    }


    public HashMap<String,DiseaseModel> getDiseases() {
        HashMap<String,DiseaseModel> mods = new HashMap<>();
        for (TermId tid : directAnnotMap.keySet()) {
            List<DiseaseModel> ls = directAnnotMap.get(tid);
            for (DiseaseModel dmod : ls) {
                mods.put(dmod.getDiseaseName(),dmod);
            }
        }
        return mods;
    }



    public HpoOntology getOntology() {
        if (ontology==null) {
            if (pathToHpoOboFile==null) {
                logger.error("Path to hp.obo was not initialized");
                return null;
            }
            HPOParser parser = new HPOParser(pathToHpoOboFile);
            this.ontology=parser.getHPO();
        }
        return ontology;
    }



}
