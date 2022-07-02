package org.monarchinitiative.hpoworkbench.analysis;


import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseaseAnnotation;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationTlc {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationTlc.class);
    private String hpopath;
    private String annotpath;
    private final Ontology hpoOntology;
    /** All disease annotations for the entire ontology. */
    private final Map<TermId, HpoDisease> diseaseMap;

    private Map<String,Integer> underannotatedDiseases;
    private Map<String,String> diseasesWithTooGeneralAnnotations;


    public AnnotationTlc(Ontology ontolog,Map<TermId, HpoDisease> d2amap)  {
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
            String db = disease.id().getPrefix();
            if (! db.equals("OMIM")) continue;  // just look at OMIM entries
            String label = String.format("%s [%s]",disease.diseaseName(),disease.id().getValue());
            if (disease.annotationCount()<3) {
                underannotatedDiseases.put(label,disease.annotationCount());
            } else {
                for (HpoDiseaseAnnotation ann : disease.annotations()) {
                    TermId tid=ann.id();
                    String lab = hpoOntology.getTermMap().get(tid).getName();
                    if (lab.contains("Abnormality of")) {
                        String s = String.format("%s [%s]",lab,tid.getValue());
                        diseasesWithTooGeneralAnnotations.put(label,s);
                    }
                }
            }
        }
        LOGGER.info("diseasesWithTooGeneralAnnotations: n={}", diseasesWithTooGeneralAnnotations.size());
    }

}
