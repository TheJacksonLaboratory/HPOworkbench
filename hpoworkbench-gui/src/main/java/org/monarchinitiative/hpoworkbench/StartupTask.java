package org.monarchinitiative.hpoworkbench;

import javafx.concurrent.Task;
import org.monarchinitiative.hpoworkbench.exception.HPOWorkbenchException;
import org.monarchinitiative.hpoworkbench.exception.HpoWorkbenchRuntimeException;
import org.monarchinitiative.hpoworkbench.resources.OptionalResources;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Properties;

/**
 * Initialization of the GUI resources is being done here. Information from {@link Properties} parsed from
 * <code>hpo-case-annotator.properties</code> are being read and following resources are initialized:
 * <ul>
 * <li>Human phenotype ontology OBO file</li>
 * </ul>
 * <p>
 * Changes made by user are stored for the next run in
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 * @version 0.0.2
 * @since 0.0
 */
public final class StartupTask extends Task<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartupTask.class);
    @Autowired
    private OptionalResources optionalResources;

    private final Properties pgProperties;


    public StartupTask(Properties pgProperties) {
        this.pgProperties = pgProperties;
    }

    /**
     * Read {@link Properties} and initialize app resources in the {@link OptionalResources}:
     *
     * <ul>
     * <li>HPO ontology</li>
     * </ul>
     *
     * @return nothing
     */
    @Override
    protected Void call() {
        /*
        This is the place where we deserialize HPO ontology if we know path to the OBO file.
        We need to make sure to set ontology property of `optionalResources` to null if loading fails.
        This way we ensure that GUI elements dependent on ontology presence (labels, buttons) stay disabled
        and that the user will be notified about the fact that the ontology is missing.
         */
        String ontologyPath = pgProperties.getProperty(OptionalResources.ONTOLOGY_PATH_PROPERTY);
        if (ontologyPath != null) {
            final File hpJsonFile = new File(ontologyPath);
            if (hpJsonFile.isFile()) {
                String msg = String.format("Loading HPO from file '%s'", hpJsonFile.getAbsoluteFile());
                updateMessage(msg);
                LOGGER.info(msg);
                final Ontology ontology = OntologyLoader.loadOntology(hpJsonFile);
                optionalResources.setHpoOntology(ontology);
                updateMessage("Ontology loaded");
            } else {
                optionalResources.setHpoOntology(null);
            }
        } else {
            String msg = "Need to set path to hp.obo file (See edit menu)";
            updateMessage(msg);
            LOGGER.info(msg);
            optionalResources.setHpoOntology(null);
        }
        return null;
    }
}
