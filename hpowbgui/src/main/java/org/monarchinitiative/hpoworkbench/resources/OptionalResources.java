package org.monarchinitiative.hpoworkbench.resources;


import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.monarchinitiative.hpoworkbench.model.DiseaseModel;
import org.monarchinitiative.phenol.formats.generic.GenericRelationship;
import org.monarchinitiative.phenol.formats.generic.GenericTerm;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

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

    private final BooleanBinding someResourceIsMissing;

    private final ObjectProperty<HpoOntology> hpoOntology = new SimpleObjectProperty<>(this, "hpoOntology", null);

    private final ObjectProperty<Ontology<GenericTerm, GenericRelationship>> mondoOntology = new SimpleObjectProperty<>(this,"mondoOntology", null);

    private final ObjectProperty<Map<TermId, List<DiseaseModel>>> indirectAnnotMap =
            new SimpleObjectProperty<>(this, "indirectAnnotMap", null);

    private final ObjectProperty<Map<TermId, List<DiseaseModel>>> directAnnotMap =
            new SimpleObjectProperty<>(this, "directAnnotMap", null);

    public OptionalResources() {
        someResourceIsMissing = Bindings.createBooleanBinding(() -> Stream.of(hpoOntologyProperty(),
                indirectAnnotMapProperty(),
                directAnnotMapProperty()).anyMatch(op -> op.get() == null),
                hpoOntologyProperty(), indirectAnnotMapProperty(), directAnnotMapProperty());
    }

    /**
     * This binding evaluates to false, if any of hpoOntology, annotMap or directAnnotMap are missing/null.
     *
     * @return {@link BooleanBinding}
     */
    public BooleanBinding someResourceMissing() {
        return someResourceIsMissing;
    }

    public Map<TermId, List<DiseaseModel>> getIndirectAnnotMap() {
        return indirectAnnotMap.get();
    }

    public void setIndirectAnnotMap(Map<TermId, List<DiseaseModel>> indirectAnnotMap) {
        this.indirectAnnotMap.set(indirectAnnotMap);
    }

    public ObjectProperty<Map<TermId, List<DiseaseModel>>> indirectAnnotMapProperty() {
        return indirectAnnotMap;
    }

    public Map<TermId, List<DiseaseModel>> getDirectAnnotMap() {
        return directAnnotMap.get();
    }

    public void setDirectAnnotMap(Map<TermId, List<DiseaseModel>> directAnnotMap) {
        this.directAnnotMap.set(directAnnotMap);
    }

    public ObjectProperty<Map<TermId, List<DiseaseModel>>> directAnnotMapProperty() {
        return directAnnotMap;
    }

    public HpoOntology getHpoOntology() {
        return hpoOntology.get();
    }

    public Ontology<GenericTerm, GenericRelationship> getMondoOntology() { return mondoOntology.get(); }

    public void setHpoOntology(HpoOntology hpoOntology) {
        this.hpoOntology.set(ResourceValidators.ontologyResourceValidator().isValid(hpoOntology) ? hpoOntology : null);
    }

    public ObjectProperty<Ontology<GenericTerm, GenericRelationship>> mondoOntologyProperty() {
        return mondoOntology;
    }

    public void setMondoOntology(Ontology mondoOntology) {
        this.mondoOntology.set(ResourceValidators.mondoResourceValidator().isValid(mondoOntology) ? mondoOntology : null);
    }

    public ObjectProperty<HpoOntology> hpoOntologyProperty() {
        return hpoOntology;
    }

    @Override
    public int hashCode() {

        return Objects.hash(hpoOntology, indirectAnnotMap, directAnnotMap);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptionalResources that = (OptionalResources) o;
        return Objects.equals(hpoOntology, that.hpoOntology) &&
                Objects.equals(indirectAnnotMap, that.indirectAnnotMap) &&
                Objects.equals(directAnnotMap, that.directAnnotMap);
    }


}
