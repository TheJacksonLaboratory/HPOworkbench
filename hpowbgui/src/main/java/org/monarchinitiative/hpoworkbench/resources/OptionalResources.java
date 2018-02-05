package org.monarchinitiative.hpoworkbench.resources;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.ontology.data.TermId;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.monarchinitiative.hpoworkbench.model.DiseaseModel;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The aim of this class is to group the optional resources required for GUI. An optional resource is a resource which
 * may or may not be available during the start of the GUI. If the resource is not available, some functions of GUI
 * should be disabled (e.g. ontology tree view should be disabled, if ontology OBO file has not been downloaded yet).
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

    private final ObjectProperty<HpoOntology> ontology = new SimpleObjectProperty<>(this, "ontology", null);

    private final ObjectProperty<Map<TermId, List<DiseaseModel>>> annotmap =
            new SimpleObjectProperty<>(this, "annotmap", null);

    private final ObjectProperty<Map<TermId, List<DiseaseModel>>> directAnnotMap =
            new SimpleObjectProperty<>(this, "directAnnotMap", null);

    /**
     * This binding evaluates to false, if any of ontology, annotMap or directAnnotMap are missing/null.
     *
     * @return {@link BooleanBinding}
     */
    public BooleanBinding someResourceMissing() {
        return Bindings.createBooleanBinding(() -> Stream.of(ontologyProperty(), annotmapProperty(),
                directAnnotMapProperty()).anyMatch(op -> op.get() == null),
                ontologyProperty(), annotmapProperty(), directAnnotMapProperty());
    }

    public Map<TermId, List<DiseaseModel>> getAnnotmap() {
        return annotmap.get();
    }

    public void setAnnotmap(Map<TermId, List<DiseaseModel>> annotmap) {
        this.annotmap.set(annotmap);
    }

    public ObjectProperty<Map<TermId, List<DiseaseModel>>> annotmapProperty() {
        return annotmap;
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

    public HpoOntology getOntology() {
        return ontology.get();
    }

    public void setOntology(HpoOntology ontology) {
        this.ontology.set(ResourceValidators.ontologyResourceValidator().isValid(ontology) ? ontology : null);
    }

    public ObjectProperty<HpoOntology> ontologyProperty() {
        return ontology;
    }

    @Override
    public int hashCode() {

        return Objects.hash(ontology, annotmap, directAnnotMap);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptionalResources that = (OptionalResources) o;
        return Objects.equals(ontology, that.ontology) &&
                Objects.equals(annotmap, that.annotmap) &&
                Objects.equals(directAnnotMap, that.directAnnotMap);
    }


}
