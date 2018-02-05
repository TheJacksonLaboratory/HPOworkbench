package org.monarchinitiative.hpoworkbench.resources;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * The aim of this class is to provide validators for optional resources as static methods.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 * @version 0.2.1
 * @see OptionalResources
 * @see ResourceValidator
 * @since 0.2
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

}
