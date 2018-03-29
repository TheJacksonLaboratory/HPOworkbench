package org.monarchinitiative.hpoworkbench;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.controller.HpoController;
import org.monarchinitiative.hpoworkbench.controller.MainController;
import org.monarchinitiative.hpoworkbench.controller.MondoController;
import org.monarchinitiative.hpoworkbench.controller.StatusController;
import org.monarchinitiative.hpoworkbench.gui.PlatformUtil;
import org.monarchinitiative.hpoworkbench.gui.PopUps;
import org.monarchinitiative.hpoworkbench.io.DirectIndirectHpoAnnotationParser;
import org.monarchinitiative.hpoworkbench.io.HPOParser;
import org.monarchinitiative.hpoworkbench.io.MondoParser;
import org.monarchinitiative.hpoworkbench.io.UTF8Control;
import org.monarchinitiative.hpoworkbench.resources.OptionalResources;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Here, the Guice module is defined. The module contains dependencies, which are used in construction of classes
 * managed by Guice injector (dependency injection container).
 * <p>
 * As defined in {@link Main}, Guice is used to create dependencies of the {@link MainController}.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 * @version 0.1.10
 * @since 0.1
 */
public final class HpoWorkbenchGuiModule extends AbstractModule {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String HPO_OBO_FILE="hp.obo";
    private static final String MONDO_OBO_FILE="mondo.obo";
    private static final String HP_ANNOTATION_FILE="phenotype.hpoa";

    /**
     * This is the main stage/window of the GUI provided by JavaFX in
     * {@link javafx.application.Application#start(Stage)} method. We keep it here to ensure a nice GUI behaviour -
     * if this stage is closed, all other stages (e.g. file choosers) should be closed as well.
     */
    private final Stage window;

    HpoWorkbenchGuiModule(Stage window) {
        this.window = window;
    }

    @Override
    protected void configure() {
        bind(Stage.class)
                .annotatedWith(Names.named("mainWindow"))
                .toInstance(window);

        bind(ExecutorService.class)
                .toInstance(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));

        bind(ResourceBundle.class)
                .toInstance(ResourceBundle.getBundle("resource_bundle.ResourceBundle",
                        new Locale("en", "US"), new UTF8Control()));

        // file where e.g. paths to hp.obo file or URLs are defined. The file is expected to be next to the JAR file
        bind(File.class)
                .annotatedWith(Names.named("propertiesFilePath"))
                .toInstance(new File(codeHomeDir(), PlatformUtil.HPO_WORKBENCH_SETTINGS_FILENAME));


        // ------ CONTROLLERS ------
        // We bind the controller in order to inject the dependencies to the controller's constructor
        bind(MainController.class).asEagerSingleton();

        bind(StatusController.class).asEagerSingleton();

        bind(MondoController.class).asEagerSingleton();

        bind(HpoController.class).asEagerSingleton();

        // ------ CONTROLLERS ------
    }

    @Provides
    @Singleton
    private OptionalResources optionalResources(Properties properties) {
        OptionalResources optionalResources = new OptionalResources();
        // load ontology & annotation file, if available
        String obofile = properties.getProperty("hpo.obo.path");
        if (obofile != null && new File(obofile).isFile()) {
            LOGGER.trace("Loading HPO ontology from {}", obofile);
            HPOParser parser = new HPOParser(obofile);
            optionalResources.setHpoOntology(parser.getHPO());
        }

        String annots = properties.getProperty("hpo.annotations.path");
        if (annots != null && new File(annots).isFile()) {
            LOGGER.trace("Loading HPO annotations file from {}", annots);
            DirectIndirectHpoAnnotationParser parser =
                    new DirectIndirectHpoAnnotationParser(annots, optionalResources.getHpoOntology());
            parser.doParse();
            optionalResources.setDirectAnnotMap(parser.getDirectAnnotMap());
            optionalResources.setIndirectAnnotMap(parser.getIndirectAnnotMap());
            HpoOntology hpoontology = optionalResources.getHpoOntology();

            if (hpoontology!=null) {
                HpoDiseaseAnnotationParser annotparser = new HpoDiseaseAnnotationParser(annots,hpoontology);
                try {
                    Map<String,HpoDisease> diseasemap =annotparser.parse();
                    for (String d : diseasemap.keySet()) {
                        System.err.print(d);
                    }
                    optionalResources.setDisease2annotationMap(diseasemap);
                } catch (PhenolException pe) {
                    PopUps.showException("Error","Could not parse annotation file", pe);
                }
            }

        }
        String mondoOboFile = properties.getProperty("mondo.obo.path");
        if (mondoOboFile!=null && new File(mondoOboFile).isFile()) {
            LOGGER.trace("Loading MONDO ontology from {}",mondoOboFile);
            try {
                MondoParser mparser = new MondoParser(mondoOboFile);
                optionalResources.setMondoOntology(mparser.getMondo());
            } catch (PhenolException pe) {
                pe.printStackTrace();
            }
        }

        return optionalResources;
    }

    /**
     * Return {@link Properties} with paths to resources. At first, file <code>application.properties</code> will be
     * tried. If the file doesn't exist, we will fall back to the <code>application.properties</code> that is
     * bundled in the JAR file.
     *
     * @param hpoWorkbenchDir {@link File} pointing to the users .hpoworkbench directory
     *
     * @return application {@link Properties}
     */
    @Provides
    @Singleton
    private Properties properties(@Named("hpoWorkbenchDir") File hpoWorkbenchDir) {
        Properties properties = new Properties();
        String propertiesPath=String.format("%s%sapplication.properties",hpoWorkbenchDir,File.separator);
        File propfile=new File(propertiesPath);
        if (propfile.isFile()) {
            try {
                LOGGER.info("Loading app properties from {}", propertiesPath);
                properties.load(new FileReader(propertiesPath));
            } catch (IOException e) {
                LOGGER.warn(e);
            }
        } else {
            try {
                URL propertiesUrl = Main.class.getResource("/" + PlatformUtil.HPO_WORKBENCH_SETTINGS_FILENAME);
                LOGGER.info("Loading app properties from bundled file {}", propertiesUrl.getPath());
                properties.load(Main.class.getResourceAsStream("/" + PlatformUtil.HPO_WORKBENCH_SETTINGS_FILENAME));
            } catch (IOException e) {
                LOGGER.warn(e);
            }
        }
        // property for the downloaded Mondo file, if available
        String mondoDownloadPath = String.format("%s%s%s",hpoWorkbenchDir.getAbsolutePath(),File.separator,MONDO_OBO_FILE);
        if (properties.getProperty("mondo.obo.path")==null) {
            properties.setProperty("mondo.obo.path",mondoDownloadPath);
        }
        String hpoDownloadPath = String.format("%s%s%s",hpoWorkbenchDir.getAbsolutePath(),File.separator,HPO_OBO_FILE);
        if (properties.getProperty("hpo.obo.path")==null) {
            properties.setProperty("hpo.obo.path",hpoDownloadPath);
        }
        String annotatDownloadPath= String.format("%s%s%s",hpoWorkbenchDir.getAbsolutePath(),File.separator,HP_ANNOTATION_FILE);
        if (properties.getProperty("hpo.annotations.path")==null) {
            properties.setProperty("hpo.annotations.path",annotatDownloadPath);
        }

//        for (String pr : properties.stringPropertyNames()) {
//            LOGGER.trace("{} = {}", pr, properties.getProperty(pr));
//        }

        return properties;
    }

    /**
     * Get path to parent directory of the JAR file (or classes). Note, this is <em>NOT</em> the path to the JAR file
     * . The method ensures, that the parent directory is created, otherwise, the application will shut down.
     *
     * @return {@link File} path of the directory with code
     */
    @Provides
    @Named("codeHomeDir")
    private File codeHomeDir() {
        File codeHomeDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getFile())
                .getParentFile();
        if (codeHomeDir.exists() || codeHomeDir.mkdirs()) // ensure that the home dir exists
            return codeHomeDir;

        Platform.exit();
        return null; // shouldn't get here, but we need to return something
    }


    @Provides
    @Named("hpoWorkbenchDir")
    private File hpoWorkbenchDir() {
        File workbenchdir = PlatformUtil.getHpoWorkbenchDir();
        if (workbenchdir != null) {
            if (workbenchdir.isDirectory() || workbenchdir.mkdirs())
                return workbenchdir;
        }

        LOGGER.fatal("Unable to create HPO workbench directory at {}", workbenchdir);
        Platform.exit();
        return null; // shouldn't get here, but we need to return something
    }

}
