package org.monarchinitiative.hpoworkbench;

import javafx.concurrent.Task;
import org.monarchinitiative.hpoworkbench.resources.OptionalHpoResource;
import org.monarchinitiative.hpoworkbench.resources.OptionalHpoaResource;
import org.monarchinitiative.hpoworkbench.resources.OptionalMondoResource;
import org.monarchinitiative.hpoworkbench.resources.OptionalResources;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 2.0.0
 * @since 0.0
 */
public final class StartupTask extends Task<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartupTask.class);

    private final OptionalHpoResource optionalHpoResource;

    private final OptionalMondoResource optionalMondoResource;


    private final OptionalHpoaResource optionalHpoaResource;


    private final Properties pgProperties;

    public StartupTask(OptionalHpoResource hpoResource, OptionalMondoResource mondoResource,
                       OptionalHpoaResource hpoaResource, Properties pgProperties) {
        this.pgProperties = pgProperties;
        this.optionalHpoResource = hpoResource;
        this.optionalMondoResource = mondoResource;
        this.optionalHpoaResource = hpoaResource;
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
        String hpoJsonPath = pgProperties.getProperty(OptionalResources.HP_JSON_PATH_PROPERTY);
        String hpoAnnotPath = pgProperties.getProperty(OptionalResources.HPOA_PATH_PROPERTY);
        String mondoJsonPath = pgProperties.getProperty(OptionalResources.MONDO_PATH_PROPERTY);
        updateProgress(0.02, 1);
        if (hpoJsonPath != null) {
            final File hpJsonFile = new File(hpoJsonPath);
            updateProgress(0.03, 1);
            if (hpJsonFile.isFile()) {
                String msg = String.format("Loading HPO from file '%s'", hpJsonFile.getAbsoluteFile());
                updateMessage(msg);
                LOGGER.info(msg);
                final Ontology ontology = OntologyLoader.loadOntology(hpJsonFile);
                updateProgress(0.25, 1);
                optionalHpoResource.setOntology(ontology);
                updateProgress(0.30, 1);
                updateMessage("HPO loaded");
                LOGGER.info("Loaded HPO ontology");
            } else {
                optionalHpoResource.setOntology(null);
            }
        } else {
            String msg = "Need to set path to hp.json file (See edit menu)";
            updateMessage(msg);
            LOGGER.info(msg);
            optionalHpoResource.setOntology(null);
        }
        if (mondoJsonPath != null) {
            final File mondoJsonFile = new File(mondoJsonPath);
            updateProgress(0.33, 1);
            if (mondoJsonFile.isFile()) {
                String msg = String.format("Loading Mondo from file '%s'", mondoJsonFile.getAbsoluteFile());
                updateMessage(msg);
                LOGGER.info(msg);
                final Ontology mondo = OntologyLoader.loadOntology(mondoJsonFile);
                updateProgress(0.62, 1);
                optionalMondoResource.setOntology(mondo);
                updateProgress(0.68, 1);
                updateMessage("Mondo loaded");
                LOGGER.info("Loaded Mondo ontology");
            } else {
                optionalMondoResource.setOntology(null);
            }
        }
        if (hpoAnnotPath != null) {
            String msg = String.format("Loading phenotype.hpoa from file '%s'", hpoAnnotPath);
            updateMessage(msg);
            LOGGER.info(msg);
            final File hpoAnnotFile = new File(hpoAnnotPath);
            updateProgress(0.71, 1);
            if (optionalHpoResource.getOntology() == null) {
                LOGGER.error("Cannot load phenotype.hpoa because HP ontology not loaded");
                return null;
            }
            if (hpoAnnotFile.isFile()) {
                updateProgress(0.78, 1);
                this.optionalHpoaResource.setAnnotationResources(hpoAnnotPath, optionalHpoResource.getOntology());
                updateProgress(0.95, 1);
                LOGGER.info("Loaded annotation maps");
            } else {
                optionalHpoaResource.initializeWithEmptyMaps();
                LOGGER.error("Cannot load phenotype.hpoa File was null");
            }
        } else {
            LOGGER.error("Cannot load phenotype.hpoa File path not found");
        }
        updateProgress(1, 1);
        return null;
    }
}
