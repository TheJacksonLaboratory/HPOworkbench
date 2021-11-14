package org.monarchinitiative.hpoworkbench.model;


import com.google.common.collect.ImmutableList;

import org.monarchinitiative.hpoworkbench.resources.OptionalResources;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.ontology.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class coordinates the data on diseases and annotations.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class Model {
    private static final Logger logger = LoggerFactory.getLogger(Model.class);

    private List<String> githublabels=new ArrayList<>();


    private final OptionalResources optionalResources;

    public Model(OptionalResources optionalResources) {
      this.optionalResources = optionalResources;
    }

    public void setGithublabels(List<String> lab) { this.githublabels=lab; }

    public List<String> getGithublabels() { return githublabels; }

    public boolean hasLabels(){ return githublabels!=null && githublabels.size()>0; }

    public List<HpoDisease> getDiseaseAnnotations(String hpoTermId, DiseaseDatabase dbase) {
        if (this.optionalResources.indirectAnnotMapProperty() == null) {
            return List.of();
        }
        Map<TermId,List<HpoDisease>> annotmap = optionalResources.getIndirectAnnotMap();
        List<HpoDisease> diseases=new ArrayList<>();
        Optional<TermId> opt = string2TermId(hpoTermId);
        if (opt.isEmpty()) return List.of();
        TermId tid = opt.get();
        diseases=annotmap.get(tid);
        if (diseases==null) return List.of();
        if (dbase== DiseaseDatabase.ALL) return diseases;
        // TODO can we replace dbase.toString() ?
        return diseases.stream().filter(d -> d.getDatabase().equals(dbase.toString())).collect(Collectors.toList());
    }


    private Optional<TermId> string2TermId(String termstring) {
        Ontology hpo = optionalResources.getHpoOntology(); // assume we will not call this unless HPO initialized
        if (termstring.length()!=10) {
            logger.error("Malformed termstring: "+termstring);
            return Optional.empty();
        }
        TermId tid = TermId.of(termstring);
        if (! hpo.containsTerm(tid)) {
            logger.error("Unknown TermId "+tid.getValue());
            return Optional.empty();
        }
        return Optional.of(tid);
    }

    /**
     * @param dmod disease of interest
     * @return List with all HPO terms that annotate the disease
     */
    public List<Term> getAnnotationTermsForDisease(HpoDisease dmod) {
        Map<TermId,List<HpoDisease>> directAnnotMap = optionalResources.getDirectAnnotMap();
        if (directAnnotMap == null) {
            logger.error("Could not retrieve direct annotation map");
            return List.of();
        }
        Ontology hpo = optionalResources.getHpoOntology();
        if (hpo==null) {
            logger.error("Could not retrieve direct annotation map because HPO not initialized");
            return List.of();
        }
        // the map contains a list with key = HPO id and value = list of annotated diseases
        // in the following we filter for HPO ids that annotate the argument dmod
        List<TermId> hpoIdsAnnotatedToDisease = directAnnotMap.entrySet()
                .stream()
                .filter(e -> e.getValue().contains(dmod))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        // get the corresponding Terms
        List<Term> terms = new ArrayList<>();
       for (TermId tid : hpoIdsAnnotatedToDisease) {
           if (hpo.containsTerm(tid)) {
               terms.add(hpo.getTermMap().get(tid));
           }
       }
       return List.copyOf(terms);
    }


    public Map<String,HpoDisease> getDiseases() {
        Map<TermId,List<HpoDisease>> directAnnotMap = optionalResources.getDirectAnnotMap();
        if (directAnnotMap == null) {
            logger.error("Could not retrieve direct annotation map");
            return Map.of();
        }
        Map<String,HpoDisease> mods = new HashMap<>();
        for (TermId tid : directAnnotMap.keySet()) {
            List<HpoDisease> ls = directAnnotMap.get(tid);
            for (HpoDisease dmod : ls) {
                mods.put(dmod.getName(),dmod);
            }
        }
        return mods;
    }
}
