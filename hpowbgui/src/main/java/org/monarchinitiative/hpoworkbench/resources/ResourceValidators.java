package org.monarchinitiative.hpoworkbench.resources;


import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The aim of this class is to provide validators for optional resources as static methods.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 * @version 0.1.10
 * @see OptionalResources
 * @see ResourceValidator
 * @since 0.1
 */
final class ResourceValidators {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceValidators.class);


    /**
     * Validate the {@link Ontology} object to make sure, that it can be used in GUI.
     *
     * @return <code>true</code> if the {@link Ontology} is valid
     */
    static ResourceValidator<Ontology> ontologyResourceValidator() {
        return ontology -> {
            // TODO - validate ontology here, if necessary
            return true;
        };
    }

    static ResourceValidator<Ontology> mondoResourceValidator() {
        return ontology -> {
            // TODO - validate ontology here, if necessary
            return true;
        };
    }

}
