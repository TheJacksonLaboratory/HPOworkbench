package org.monarchinitiative.hpoworkbench.model;


import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class coordinates the data on diseases and annotations.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class Model {
    private static final Logger logger = LogManager.getLogger();
    private final TermPrefix HP_PREFIX = new ImmutableTermPrefix("HP");
    /** Ontology model for full HPO ontology (all subhierarchies). */
    private final HpoOntology ontology;
    /** List of annotated diseases (direct annotations) */
    private final Map<TermId,List<DiseaseModel>> annotmap;
    /** List of all indirected annotations (by annotation propagation rule). */
    private final Map<TermId,List<DiseaseModel>> directAnnotMap;

    private List<String> githublabels=new ArrayList<>();


    public Model(HpoOntology ontology, Map<TermId, List<DiseaseModel>> annotMap,
                 Map<TermId, List<DiseaseModel>> directAnnotMap) {
        this.ontology = ontology;
        this.annotmap = annotMap;
        this.directAnnotMap = directAnnotMap;
    }

    public void setGithublabels(List<String> lab) { this.githublabels=lab; }

    public List<String> getGithublabels() { return githublabels; }

    public boolean hasLabels(){ return githublabels!=null && githublabels.size()>0; }

    public List<DiseaseModel> getDiseaseAnnotations(String hpoTermId, DiseaseModel.database dbase) {
        List<DiseaseModel> diseases=new ArrayList<>();
        TermId id = string2TermId(hpoTermId);
        if (id!=null) {
            diseases=annotmap.get(id);
        }
        if (diseases==null) return new ArrayList<>();// return hasTermsUniqueToOnlyOneDisease
        // user wan't all databases, just pass through
        if (dbase== DiseaseModel.database.ALL) return diseases;
        // filter for desired database
        ImmutableList.Builder<DiseaseModel> builder = new ImmutableList.Builder<>();
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


    public List<Term> getAnnotationTermsForDisease(DiseaseModel dmod) {
        List<Term> annotating=new ArrayList<>();
        for (TermId tid : directAnnotMap.keySet()) {
            if (directAnnotMap.get(tid).contains(dmod)) {
                Term term = ontology.getTermMap().get(tid);
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



    public HpoOntology getHpoOntology() {
        return ontology;
    }



}
