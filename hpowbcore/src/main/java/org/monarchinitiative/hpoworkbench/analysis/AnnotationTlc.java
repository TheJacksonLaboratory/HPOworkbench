package org.monarchinitiative.hpoworkbench.analysis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.phenol.formats.hpo.HpoAnnotation;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnnotationTlc {

    private static final Logger LOGGER = LogManager.getLogger();
    private String hpopath;
    private String annotpath;
    private final HpoOntology hpoOntology;
    /** All disease annotations for the entire ontology. */
    private final Map<TermId,HpoDisease> diseaseMap;

    private Map<String,Integer> underannotatedDiseases;
    private Map<String,String> diseasesWithTooGeneralAnnotations;


    public AnnotationTlc(HpoOntology ontolog,Map<TermId, HpoDisease> d2amap)  {
        hpoOntology = ontolog;
        diseaseMap = d2amap;
        lookForUnderannotatedDiseases();
    }


    public Map<String, Integer> getUnderannotatedDiseases() {
        return underannotatedDiseases;
    }

    public Map<String, String> getDiseasesWithTooGeneralAnnotations() {
        return diseasesWithTooGeneralAnnotations;
    }

    private void lookForUnderannotatedDiseases() {
        underannotatedDiseases=new HashMap<>();
        diseasesWithTooGeneralAnnotations=new HashMap<>();
        for (Map.Entry<TermId,HpoDisease> entry : diseaseMap.entrySet()) {
            HpoDisease disease = entry.getValue();
            String db = disease.getDatabase();
            if (! db.equals("OMIM")) continue;  // just look at OMIM entries
            List<HpoAnnotation> annotations = disease.getPhenotypicAbnormalities();
            String label = String.format("%s [%s]",disease.getName(),disease.getDiseaseDatabaseId());
            if (annotations.size()<3) {
                underannotatedDiseases.put(label,annotations.size());
            } else {
                for (HpoAnnotation ann : annotations) {
                    TermId tid=ann.getTermId();
                    String lab = hpoOntology.getTermMap().get(tid).getName();
                    if (lab.contains("Abnormality of")) {
                        String s = String.format("%s [%s]",lab,tid.getIdWithPrefix());
                        diseasesWithTooGeneralAnnotations.put(label,s);

                    }
                }
            }
        }
    }

}
