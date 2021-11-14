package org.monarchinitiative.hpoworkbench.resources;


import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Tab;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The aim of this class is to group the optional resources required for GUI. An optional resource is a resource which
 * may or may not be available during the start of the GUI. If the resource is not available, some functions of GUI
 * should be disabled (e.g. hpoOntology tree view should be disabled, if hpoOntology OBO file has not been downloaded yet).
 * <p>
 * Controllers of GUI that depend on optional resource should create listeners in their <code>initialize</code>
 * methods, such that the listeners will disable controls if the resource is <code>null</code>.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 * @version 0.1.10
 * @see ResourceValidators
 * @see ResourceValidator
 * @since 0.1
 */

public final class OptionalResources {
    private static final Logger LOGGER = LoggerFactory.getLogger(OptionalResources.class);
    public static final String HP_JSON_PATH_PROPERTY = "hp.json.path";
    public static final String MONDO_PATH_PROPERTY = "mondo.json.path";
    public static final String HPOA_PATH_PROPERTY = "hpoa.path";
    static public final String HPOA_URL_KEY = "hpo.phenotype.annotations.url";
    /**
     * This binding evaluates as true when a resource required for a {@link Tab} controlled by
     * {@link org.monarchinitiative.hpoworkbench.controller.MondoController} is missing.
     */
    private final BooleanBinding mondoResourceIsMissing;

    /**
     * This binding evaluates as true when a resource required for a {@link Tab} controlled by
     * {@link org.monarchinitiative.hpoworkbench.controller.HpoController} is missing.
     */
    private final BooleanBinding hpoResourceIsMissing;

    private final BooleanBinding hpoaResourceIsMissing;

    private final ObjectProperty<Ontology> hpoOntology = new SimpleObjectProperty<>(this, "hpoOntology", null);

    private final ObjectProperty<Ontology> mondoOntology
            = new SimpleObjectProperty<>(this, "mondoOntology", null);

    private final StringProperty statusLabelProperty =
            new SimpleStringProperty(this, "status", "");

    private final ObjectProperty<Map<TermId, List<HpoDisease>>> indirectAnnotMap =
            new SimpleObjectProperty<>(this, "indirectAnnotMap", null);

    private final ObjectProperty<Map<TermId, List<HpoDisease>>> directAnnotMap =
            new SimpleObjectProperty<>(this, "directAnnotMap", null);

    private final ObjectProperty<Map<TermId, HpoDisease>> disease2annotationMap =
            new SimpleObjectProperty<>(this,"disease2annotationMap", null);

    private final StringProperty annotationPath =
            new SimpleStringProperty(this,"annotationPath",null);


    public OptionalResources() {
        hpoResourceIsMissing = Bindings.createBooleanBinding(() -> Stream.of(hpoOntologyProperty(),
                indirectAnnotMapProperty(),
                directAnnotMapProperty()).anyMatch(op -> op.get() == null),
                hpoOntologyProperty(), indirectAnnotMapProperty(), directAnnotMapProperty());

        mondoResourceIsMissing = Bindings.createBooleanBinding(() -> Stream.of(mondoOntologyProperty(),
                indirectAnnotMapProperty(),
                directAnnotMapProperty()).anyMatch(op -> op.get() == null),
                mondoOntologyProperty(), indirectAnnotMapProperty(), directAnnotMapProperty());

        hpoaResourceIsMissing =Bindings.createBooleanBinding(() -> Stream.of(directAnnotMapProperty())
                .anyMatch(op -> op.get() == null));
    }

    /**
     * This binding evaluates to false, if any of hpoOntology, annotMap or directAnnotMap are missing/null.
     *
     * @return {@link BooleanBinding}
     */
    public BooleanBinding hpoResourceMissing() {
        return hpoResourceIsMissing;
    }

    /**
     * This binding evaluates to false, if any of mondoOntology, annotMap or directAnnotMap are missing/null.
     *
     * @return {@link BooleanBinding}
     */
    public BooleanBinding mondoResourceMissing() {
        return mondoResourceIsMissing;
    }


    public BooleanBinding hpoaResourceMissing() { return hpoaResourceIsMissing; }

    public Map<TermId, List<HpoDisease>> getIndirectAnnotMap() {
        return indirectAnnotMap.get();
    }

    public void setIndirectAnnotMap(Map<TermId, List<HpoDisease>> indirectAnnotMap) {
        this.indirectAnnotMap.set(indirectAnnotMap);
    }

    public void setDisease2annotationMap(Map<TermId, HpoDisease> d2amap) {
        this.disease2annotationMap.set(d2amap);
    }
    public ObjectProperty<Map<TermId, HpoDisease>> disease2annotationMapProperty() { return disease2annotationMap; }

    public Map<TermId, HpoDisease> getDisease2AnnotationMap() { return disease2annotationMap.get(); }

    public ObjectProperty<Map<TermId, List<HpoDisease>>> indirectAnnotMapProperty() {
        return indirectAnnotMap;
    }

    public Map<TermId, List<HpoDisease>> getDirectAnnotMap() {
        return directAnnotMap.get();
    }

    public void setDirectAnnotMap(Map<TermId, List<HpoDisease>> directAnnotMap) {
        this.directAnnotMap.set(directAnnotMap);
    }

    public void setAnnotationPath(String path) {
        this.annotationPath.setValue(path);
    }
    public String getAnnotationPath() {
        return annotationPath.getValue();
    }



    public Ontology getHpoOntology() {
        return hpoOntology.get();
    }

    public void setHpoOntology(Ontology hpoOntology) {
        this.hpoOntology.set(ResourceValidators.ontologyResourceValidator().isValid(hpoOntology) ? hpoOntology : null);
    }

    public Ontology getMondoOntology() {
        return mondoOntology.get();
    }

    public void setMondoOntology(Ontology mondoOnt) {
        this.mondoOntology.set(mondoOnt);
    }

    public ObjectProperty<Ontology> mondoOntologyProperty() {
        return mondoOntology;
    }

    public ObjectProperty<Ontology> hpoOntologyProperty() {
        return hpoOntology;
    }

    public ObjectProperty<Map<TermId, List<HpoDisease>>> directAnnotMapProperty() {
        return directAnnotMap;
    }

}
