package org.monarchinitiative.hpoworkbench.model;

import org.monarchinitiative.hpoworkbench.resources.OptionalResources;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class HpoWbModel {

    private Ontology hpo = null;

    private Ontology mondo = null;

    private Map<TermId, List<HpoDisease>> directMap;
    private Map<TermId, List<HpoDisease>> indirectMap;

    private Map<String, TermId> name2diseaseIdMap;
    private Map<TermId, HpoDisease> id2diseaseModelMap;



    public HpoWbModel() {
        directMap = new HashMap<>();
        indirectMap = new HashMap<>();
        name2diseaseIdMap = new HashMap<>();
        id2diseaseModelMap = new HashMap<>();
    }

    public void fromOptionalResources(OptionalResources optionalResources) {
        if (optionalResources.getHpoOntology() != null) {
            hpo = optionalResources.getHpoOntology();
        }
        if (optionalResources.getMondoOntology() != null) {
            mondo = optionalResources.getMondoOntology();
        }

        if (optionalResources.getIndirectAnnotMap() != null) {
            indirectMap = optionalResources.getIndirectAnnotMap();
        }
        if (optionalResources.getDirectAnnotMap() != null) {
            directMap = optionalResources.getDirectAnnotMap();
            Set<HpoDisease> diseaseSet = directMap.values().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            name2diseaseIdMap = diseaseSet.stream()
                    .collect(Collectors.toMap(HpoDisease::getName, HpoDisease::getDiseaseDatabaseId));
            id2diseaseModelMap = diseaseSet.stream()
                    .collect(Collectors.toMap(HpoDisease::getDiseaseDatabaseId, Function.identity()));
        }
    }

    public List<String> hpoDiseaseNames() {
        return new ArrayList<>(name2diseaseIdMap.keySet());
    }

    public Optional<HpoDisease> getDisease(String diseaseName) {
        if (name2diseaseIdMap.containsKey(diseaseName)) {
            TermId diseaseId = name2diseaseIdMap.get(diseaseName);
            return Optional.ofNullable(id2diseaseModelMap.get(diseaseId));
        }
        return Optional.empty();
    }

    public Optional<HpoDisease> getDiseaseById(TermId diseaseId) {
        if (id2diseaseModelMap.containsKey(diseaseId)) {
            return Optional.of(id2diseaseModelMap.get(diseaseId));
        } else {
            return Optional.empty();
        }
    }

    public Map<TermId, HpoDisease> getId2diseaseMap() {
        return this.id2diseaseModelMap;
    }


    public Optional<Term> getTermFromHpoId(TermId id) {
        if (hpo == null) {
            return Optional.empty();
        } else  if (! hpo.containsTerm(id)) {
            return Optional.empty();
        } else {
            return Optional.of(hpo.getTermMap().get(id));
        }
    }

    public Optional<Ontology> getHpo() {
        return Optional.ofNullable(hpo);
    }


    public Optional<Ontology> getMondo() {
        return Optional.ofNullable(mondo);
    }

    public List<HpoDisease> getDiseasesByHpoTermId(TermId id) {
        return indirectMap.getOrDefault(id, List.of());
    }
}
