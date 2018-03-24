package org.monarchinitiative.hpoworkbench.resources;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.Ontology;


/**
 * The aim of this class is to provide validators for optional resources as static methods.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 * @version 0.1.10
 * @see OptionalResources
 * @see ResourceValidator
 * @since 0.1
 */
public final class ResourceValidators {

    private static final Logger LOGGER = LogManager.getLogger();


    /**
     * Validate the {@link HpoOntology} object to make sure, that it can be used in GUI.
     *
     * @return <code>true</code> if the {@link HpoOntology} is valid
     */
    static ResourceValidator<HpoOntology> ontologyResourceValidator() {
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
