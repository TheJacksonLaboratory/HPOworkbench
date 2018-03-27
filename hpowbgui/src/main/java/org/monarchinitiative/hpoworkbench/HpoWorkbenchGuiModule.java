package org.monarchinitiative.hpoworkbench;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.controller.HpoController;
import org.monarchinitiative.hpoworkbench.controller.MainController;
import org.monarchinitiative.hpoworkbench.controller.MondoController;
import org.monarchinitiative.hpoworkbench.controller.StatusController;
import org.monarchinitiative.hpoworkbench.gui.PlatformUtil;
import org.monarchinitiative.hpoworkbench.io.DirectIndirectHpoAnnotationParser;
import org.monarchinitiative.hpoworkbench.io.HPOParser;
import org.monarchinitiative.hpoworkbench.io.MondoParser;
import org.monarchinitiative.hpoworkbench.io.UTF8Control;
import org.monarchinitiative.hpoworkbench.resources.OptionalResources;
import org.monarchinitiative.phenol.base.PhenolException;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
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

    /**
     * This is the main stage/window of the GUI provided by JavaFX in
     * {@link javafx.application.Application#start(Stage)} method. We keep it here to ensure a nice GUI behaviour -
     * if this stage is closed, all other stages (e.g. file choosers) should be closed as well.
     */
    private final Stage window;

    public HpoWorkbenchGuiModule(Stage window) {
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
     * @param propertiesPath {@link File} pointing to the <code>application.properties</code> file
     *
     * @return application {@link Properties}
     */
    @Provides
    @Singleton
    private Properties properties(@Named("propertiesFilePath") File propertiesPath) {
        Properties properties = new Properties();
        if (propertiesPath.isFile()) {
            try {
                LOGGER.info("Loading app properties from {}", propertiesPath.getAbsolutePath());
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
