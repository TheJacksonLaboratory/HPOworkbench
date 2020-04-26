package org.monarchinitiative.hpoworkbench.model;


import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
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
    /** Ontology model for full HPO ontology (all subhierarchies). */
    private final Ontology ontology;
    /** List of annotated diseases (direct annotations) */
    private final Map<TermId,List<HpoDisease>> annotmap;
    /** List of all indirected annotations (by annotation propagation rule). */
    private final Map<TermId,List<HpoDisease>> directAnnotMap;

    private List<String> githublabels=new ArrayList<>();


    public Model(Ontology ontology, Map<TermId, List<HpoDisease>> annotMap,
                 Map<TermId, List<HpoDisease>> directAnnotMap) {
        this.ontology = ontology;
        this.annotmap = annotMap;
        this.directAnnotMap = directAnnotMap;
    }

    public void setGithublabels(List<String> lab) { this.githublabels=lab; }

    public List<String> getGithublabels() { return githublabels; }

    public boolean hasLabels(){ return githublabels!=null && githublabels.size()>0; }

    public List<HpoDisease> getDiseaseAnnotations(String hpoTermId, DiseaseDatabase dbase) {
        List<HpoDisease> diseases=new ArrayList<>();
        TermId id = string2TermId(hpoTermId);
        if (id!=null) {
            diseases=annotmap.get(id);
        }
        if (diseases==null) return new ArrayList<>();// return hasTermsUniqueToOnlyOneDisease
        // user wan't all databases, just pass through
        if (dbase== DiseaseDatabase.ALL) return diseases;
        // filter for desired database
        ImmutableList.Builder<HpoDisease> builder = new ImmutableList.Builder<>();
        for (HpoDisease dm : diseases) {
           // if (dm.getDatabase().equals(dbase)) {
                builder.add(dm);
          // }*/ //TODO ADD DATABASE FILTER
        }
        return builder.build();
    }


    private TermId string2TermId(String termstring) {

        if (termstring.length()!=10) {
            logger.error("Malformed termstring: "+termstring);
            return null;
        }
        TermId tid = TermId.of(termstring);
        if (! ontology.getAllTermIds().contains(tid)) {
            logger.error("Unknown TermId "+tid.getValue());
            return null;
        }
        return tid;
    }


    public List<Term> getAnnotationTermsForDisease(HpoDisease dmod) {
        ImmutableList.Builder<Term> builder=new ImmutableList.Builder<>();
        for (TermId tid : directAnnotMap.keySet()) {
            if (directAnnotMap.get(tid).contains(dmod)) {
                Term term = ontology.getTermMap().get(tid);
                builder.add(term);
            }
        }
        return builder.build();
    }


    public HashMap<String,HpoDisease> getDiseases() {
        HashMap<String,HpoDisease> mods = new HashMap<>();
        for (TermId tid : directAnnotMap.keySet()) {
            List<HpoDisease> ls = directAnnotMap.get(tid);
            for (HpoDisease dmod : ls) {
                mods.put(dmod.getName(),dmod);
            }
        }
        return mods;
    }



    public Ontology getHpoOntology() {
        return ontology;
    }



}
