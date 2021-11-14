package org.monarchinitiative.hpoworkbench;


import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;



/**
 * Main class of the fenominal GUI app
 * @author Peter N Robinson
 */
@SpringBootApplication
public class StockUiApplication {
    public static void main(String[] args) {
        Application.launch(HpoWorkbenchApplication.class, args);
    }
    /*
     private static final String WINDOW_TITLE = "Human Phenotype Ontology Workbench";



    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage window) throws Exception {
        injector = Guice.createInjector(new HpoWorkbenchGuiModule(window));
        ResourceBundle bundle = injector.getInstance(ResourceBundle.class);
        Parent rootNode = FXMLLoader.load(Main.class.getResource("/fxml/main.fxml"), bundle,
                new JavaFXBuilderFactory(), injector::getInstance);

        window.setScene(new Scene(rootNode, 1200, 800));

        Image image = new Image(Main.class.getResourceAsStream("/img/icon.jpg"));
        window.getIcons().add(image);
        window.setTitle(WINDOW_TITLE);
        if (PlatformUtil.isMacintosh()) {
            try {
                URL iconURL = Main.class.getResource("/img/icon.jpg");
                java.awt.Image macimage = new ImageIcon(iconURL).getImage();
                com.apple.eawt.Application.getApplication().setDockIconImage(macimage);
            } catch (Exception e) {
                // Not for Windows or Linux. Just skip it!
            }
        }

        window.show();

    }


    @Override
    public void stop() throws Exception {
        super.stop();
        injector.getInstance(ExecutorService.class).shutdown();

        // save properties
        Properties properties = injector.getInstance(Properties.class);
        File where = injector.getInstance(Key.get(File.class, Names.named("propertiesFilePath")));
        properties.store(new FileWriter(where), "HPOworkbench properties");
        LOGGER.trace("Properties saved to {}", where.getAbsolutePath());
    }

     */
}

