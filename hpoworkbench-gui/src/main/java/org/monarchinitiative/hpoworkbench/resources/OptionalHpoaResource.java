package org.monarchinitiative.hpoworkbench.resources;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.monarchinitiative.hpoworkbench.StartupTask;
import org.monarchinitiative.hpoworkbench.io.DirectIndirectHpoAnnotationParser;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OptionalHpoaResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(OptionalHpoaResource.class);
    private final BooleanBinding hpoaResourceIsMissing;

    private final ObjectProperty<Map<TermId, List<HpoDisease>>> indirectAnnotMap =
            new SimpleObjectProperty<>(this, "indirectAnnotMap", null);

    private final ObjectProperty<Map<TermId, List<HpoDisease>>> directAnnotMap =
            new SimpleObjectProperty<>(this, "directAnnotMap", null);

    private Map<String, TermId> name2diseaseIdMap;
    private Map<TermId, HpoDisease> id2diseaseModelMap;


    public OptionalHpoaResource(){
        System.err.println("COTR");
        hpoaResourceIsMissing = Bindings.createBooleanBinding(() -> Stream.of( indirectAnnotMapProperty(),
                directAnnotMapProperty()).anyMatch(op -> op.get() == null));
    }

    public ObjectProperty<Map<TermId, List<HpoDisease>>> directAnnotMapProperty() {
        return directAnnotMap;
    }

    public ObjectProperty<Map<TermId, List<HpoDisease>>> indirectAnnotMapProperty() {
        return indirectAnnotMap;
    }

    public void setAnnotationResources(String phenotypeDotHpoaPath, Ontology hpo){
        System.err.println("OptionalHpoaResources TOP" + this.toString());
        DirectIndirectHpoAnnotationParser parser =
                new DirectIndirectHpoAnnotationParser(phenotypeDotHpoaPath, hpo);
        Map<TermId, List<HpoDisease>> directMap = parser.getDirectAnnotMap();
        LOGGER.info("Setting direct annottion map with size {}", directMap.size());
        this.directAnnotMap.set(directMap);
        System.err.println("OptionalHpoaResources 2" + this.toString());
        this.indirectAnnotMap.set(parser.getTotalAnnotationMap());
        System.err.println("OptionalHpoaResources 3" + this.toString());
        if (getDirectAnnotMap() != null) {

            Set<HpoDisease> diseaseSet = directAnnotMap.get().values().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            name2diseaseIdMap = diseaseSet.stream()
                    .collect(Collectors.toMap(HpoDisease::getName, HpoDisease::getDiseaseDatabaseId));
            id2diseaseModelMap = diseaseSet.stream()
                    .collect(Collectors.toMap(HpoDisease::getDiseaseDatabaseId, Function.identity()));
        }
        System.err.println("OptionalHpoaResources " + this.toString());
    }

    public Map<TermId, List<HpoDisease>> getDirectAnnotMap() {
        return directAnnotMap.get();
    }
    public Map<TermId, List<HpoDisease>> getIndirectAnnotMap() {
        return indirectAnnotMap.get();
    }

    public Map<String, TermId> getName2diseaseIdMap() {
        return name2diseaseIdMap;
    }

    public Map<TermId, HpoDisease> getId2diseaseModelMap() {
        return id2diseaseModelMap;
    }

    /**
     * If we cannot initialize these resources, create empty maps to avoid null pointer errors.
     */
    public void initializeWithEmptyMaps() {
        directAnnotMap.set(Map.of());
        indirectAnnotMap.set(Map.of());
        name2diseaseIdMap = Map.of();
        id2diseaseModelMap = Map.of();
    }


    @Override
    public String toString() {
        return String.format("OptionalHpoaResource\n\tdirectAnnotMap: n=%d\n\tindirectAnnotMap: n=%d\n\tname2diseaseIdMap: n=%d\n\tid2diseaseModelMap: n=%d\n",
                    directAnnotMap.get() == null ? 0 : directAnnotMap.get().size(),
                indirectAnnotMap.get() == null ? 0 : indirectAnnotMap.get().size(),
                name2diseaseIdMap == null ? 0 : name2diseaseIdMap.size(),
                id2diseaseModelMap == null ? 0 :  id2diseaseModelMap.size()
        );
    }
}
