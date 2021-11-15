package org.monarchinitiative.hpoworkbench;

import javafx.application.Platform;
import javafx.concurrent.Task;
import org.monarchinitiative.hpoworkbench.controller.MainController;
import org.monarchinitiative.hpoworkbench.io.DirectIndirectHpoAnnotationParser;
import org.monarchinitiative.hpoworkbench.model.Model;
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

    private final OptionalResources optionalResources;

    private final Properties pgProperties;

    @Autowired
    private MainController mainController;

    public StartupTask(OptionalResources optionalResources, Properties pgProperties) {
        this.pgProperties = pgProperties;
        this.optionalResources = optionalResources;
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
        if (hpoJsonPath != null) {
            final File hpJsonFile = new File(hpoJsonPath);
            if (hpJsonFile.isFile()) {
                String msg = String.format("Loading HPO from file '%s'", hpJsonFile.getAbsoluteFile());
                updateMessage(msg);
                LOGGER.info(msg);
                final Ontology ontology = OntologyLoader.loadOntology(hpJsonFile);
                optionalResources.setHpoOntology(ontology);
                updateMessage("HPO loaded");
                LOGGER.info("Loaded HPO ontology");
            } else {
                optionalResources.setHpoOntology(null);
            }
        } else {
            String msg = "Need to set path to hp.json file (See edit menu)";
            updateMessage(msg);
            LOGGER.info(msg);
            optionalResources.setHpoOntology(null);
        }
        if (mondoJsonPath != null) {
            final File mondoJsonFile = new File(mondoJsonPath);
            if (mondoJsonFile.isFile()) {
                String msg = String.format("Loading Mondo from file '%s'", mondoJsonFile.getAbsoluteFile());
                updateMessage(msg);
                LOGGER.info(msg);
                final Ontology mondo = OntologyLoader.loadOntology(mondoJsonFile);
                optionalResources.setMondoOntology(mondo);
                updateMessage("Mondo loaded");
                LOGGER.info("Loaded Mondo ontology");
            } else {
                optionalResources.setHpoOntology(null);
            }
        }
        if (hpoAnnotPath != null) {
            String msg = String.format("Loading phenotype.hpoa from file '%s'", hpoAnnotPath);
            updateMessage(msg);
            LOGGER.info(msg);
            final File hpoAnnotFile = new File(hpoAnnotPath);
            if (optionalResources.getHpoOntology() == null) {
                LOGGER.error("Cannot load phenotype.hpoa because HP ontology not loaded");
                return null;
            }
            if (hpoAnnotFile.isFile()) {
                DirectIndirectHpoAnnotationParser parser =
                        new DirectIndirectHpoAnnotationParser(hpoAnnotPath, optionalResources.getHpoOntology());
                optionalResources.setDirectAnnotMap(parser.getDirectAnnotMap());
                optionalResources.setIndirectAnnotMap(parser.getTotalAnnotationMap());
                //optionalResources.setDisease2annotationMap(parser.);
                LOGGER.info("Loaded annotation maps");
            } else {
                optionalResources.setDirectAnnotMap(null);
                optionalResources.setIndirectAnnotMap(null);
                LOGGER.error("Cannot load phenotype.hpoa File was null");
            }
        } else {
            LOGGER.error("Cannot load phenotype.hpoa File path not found");
        }
        return null;
    }
}
