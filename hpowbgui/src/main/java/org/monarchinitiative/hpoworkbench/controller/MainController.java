package org.monarchinitiative.hpoworkbench.controller;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.graph.data.DirectedGraph;
import com.github.phenomics.ontolib.graph.data.Edge;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermId;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermPrefix;
import com.github.phenomics.ontolib.ontology.data.TermId;
import com.github.phenomics.ontolib.ontology.data.TermPrefix;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.excel.HierarchicalExcelExporter;
import org.monarchinitiative.hpoworkbench.excel.Hpo2ExcelExporter;
import org.monarchinitiative.hpoworkbench.exception.HPOException;
import org.monarchinitiative.hpoworkbench.exception.HPOWorkbenchException;
import org.monarchinitiative.hpoworkbench.github.GitHubLabelRetriever;
import org.monarchinitiative.hpoworkbench.github.GitHubPoster;
import org.monarchinitiative.hpoworkbench.gui.*;
import org.monarchinitiative.hpoworkbench.io.DirectIndirectHpoAnnotationParser;
import org.monarchinitiative.hpoworkbench.io.Downloader;
import org.monarchinitiative.hpoworkbench.io.HPOParser;
import org.monarchinitiative.hpoworkbench.model.DiseaseModel;
import org.monarchinitiative.hpoworkbench.model.Model;
import org.monarchinitiative.hpoworkbench.resources.OptionalResources;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;

import static org.monarchinitiative.hpoworkbench.controller.MainController.mode.BROWSE_DISEASE;
import static org.monarchinitiative.hpoworkbench.controller.MainController.mode.BROWSE_HPO;
import static org.monarchinitiative.hpoworkbench.controller.MainController.mode.NEW_ANNOTATION;


/**
 * Controller for HPO Workbench
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class MainController {

    public static final String HPO_OBO_FILENAME = "hp.obo";

    private static final Logger logger = LogManager.getLogger();

    private static final String EVENT_TYPE_CLICK = "click";

    private static final String EVENT_TYPE_MOUSEOVER = "mouseover";

    private static final String EVENT_TYPE_MOUSEOUT = "mouseclick";

    private final TermPrefix HP_PREFIX = new ImmutableTermPrefix("HP");

    private final OptionalResources optionalResources;

    /**
     * Unused, but still required
     */
    private final File hpoWorkbenchDir;

    /**
     * Application-specific properties (not the System properties!) defined in the 'application.properties' file that
     * resides in the classpath.
     */
    private final Properties properties;

    /** Reference to the primary stage of the App. */
    private final Stage primarystage;

    @FXML
    public Button exportHierarchicalSummaryButton;

    @FXML
    public Button exportToExcelButton;

    @FXML
    public Button suggestCorrectionToTermButton;

    @FXML
    public Button suggestNewChildTermButton;

    @FXML
    public Button suggestNewAnnotationButton;

    @FXML
    public Button reportMistakenAnnotationButton;

    /** Place at the bottom of the window controlled by {@link StatusController} for showing messages to user */
    @FXML
    public StackPane statusStackPane;

    private Model model;


    /** Users can create a github issue. Username and password will be stored for the current session only. */
    private String githubUsername = null;

    private String githubPassword;

    /** This determines which disease as shown (OMIM, Orphanet, DECIPHER, or all). Default: ALL. */
    private DiseaseModel.database selectedDatabase = DiseaseModel.database.ALL;

    /** Current behavior of HPO Workbench. See {@link mode}. */
    private mode currentMode = mode.BROWSE_HPO;

    /** Approved {@link HpoTerm} is submitted here. */
    private Consumer<HpoTerm> addHook;

    /** The term that is currently selected in the Browser window. */
    private HpoTerm selectedTerm = null;

    /** Current disease shown in the browser. Any suggested changes will refer to this disease. */
    private DiseaseModel selectedDisease = null;

    /** Key: a term name such as "Myocardial infarction"; value: the corresponding HPO id as a {@link TermId}. */
    private Map<String, TermId> labels = new HashMap<>();

    /** Tree hierarchy of the ontology is presented here. */
    @FXML
    private TreeView<HpoTermWrapper> ontologyTreeView;

    /** WebView for displaying details of the Term that is selected in the {@link #ontologyTreeView}. */
    @FXML
    private WebView infoWebView;

    /** WebEngine backing up the {@link #infoWebView}. */
    private WebEngine infoWebEngine;

    /** Text field with autocompletion for jumping to a particular HPO term in the tree view. */
    @FXML
    private TextField searchTextField;

    @FXML
    private Button goButton;

    @FXML
    private RadioButton allDatabaseButton, orphanetButton, omimButton, decipherButton;

    @FXML
    private RadioButton hpoTermRadioButton;

    @FXML
    private RadioButton diseaseRadioButton;

    @FXML
    private RadioButton newAnnotationRadioButton;

    @Inject
    public MainController(OptionalResources optionalResources, Properties properties,
                          @Named("mainWindow") Stage primarystage, @Named("hpoWorkbenchDir") File hpoWorkbenchDir) {
        this.optionalResources = optionalResources;
        this.properties = properties;
        this.primarystage = primarystage;
        this.hpoWorkbenchDir = hpoWorkbenchDir;
    }

    public static String getVersion() {
        String version = "0.0.0";// default, should be overwritten by the following.
        try {
            Package p = MainController.class.getPackage();
            version = p.getImplementationVersion();
        } catch (Exception e) {
            // do nothing
        }
        if (version == null) version = "0.1.11"; // this works on a maven build but needs to be reassigned in intellij
        return version;
    }

    /** This is called from the Edit menu and allows the user to import a local copy of
     * hp.obo (usually because the local copy is newer than the official release version of hp.obo).
     * @param e event
     */
    @FXML private void importLocalHpObo(ActionEvent e) {
        e.consume();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import local hp.obo file");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("HPO OBO file (*.obo)", "*.obo");
        chooser.getExtensionFilters().add(extFilter);
        File f = chooser.showOpenDialog(primarystage);
        if (f == null) {
            logger.error("Unable to obtain path to local HPO OBO file");
            PopUps.showInfoMessage("Unable to obtain path to local HPO OBO file", "Error");
            return;
        }
        String hpoOboPath = f.getAbsolutePath();

        HPOParser parser = new HPOParser(hpoOboPath);
        optionalResources.setOntology(parser.getHPO());
        properties.setProperty("hpo.obo.path", hpoOboPath);

    }

    @FXML
    private void close(ActionEvent e) {
        logger.trace("Closing down");
        Platform.exit();
    }

    @FXML
    private void downloadHPO(ActionEvent e) {
        String dirpath = PlatformUtil.getHpoWorkbenchDir().getAbsolutePath();
        File f = new File(dirpath);
        if (!(f.exists() && f.isDirectory())) {
            logger.trace("Cannot download hp.obo, because directory not existing at " + f.getAbsolutePath());
            return;
        }

        ProgressIndicator pb = new ProgressIndicator();
        javafx.scene.control.Label label = new javafx.scene.control.Label("downloading hp.obo...");
        FlowPane root = new FlowPane();
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.getChildren().addAll(label, pb);
        Scene scene = new Scene(root, 400, 100);
        Stage window = new Stage();
        window.initOwner(primarystage);
        window.setTitle("HPO download");
        window.setScene(scene);

        Task hpodownload = new Downloader(dirpath, properties.getProperty("hpo.obo.url"), PlatformUtil.HPO_OBO_FILENAME, pb);
        window.show();
        hpodownload.setOnSucceeded(event -> {
            window.close();
            logger.trace(String.format("Successfully downloaded hpo to %s", dirpath));
            String hpoOboPath = dirpath + File.separator + PlatformUtil.HPO_OBO_FILENAME;
            optionalResources.setOntology(new HPOParser(hpoOboPath).getHPO());
            properties.setProperty("hpo.obo.path", hpoOboPath);

        });
        hpodownload.setOnFailed(event -> {
            window.close();
            logger.error("Unable to download HPO obo file");
            optionalResources.setOntology(null);
            properties.setProperty("hpo.obo.path", null);
        });
        Thread thread = new Thread(hpodownload);
        thread.start();
        e.consume();
    }

    @FXML
    private void downloadHPOAnnotations(ActionEvent e) {
        String dirpath = PlatformUtil.getHpoWorkbenchDir().getAbsolutePath();
        File f = new File(dirpath);
        if (!(f.exists() && f.isDirectory())) {
            logger.trace("Cannot download phenotype_annotation.tab, because directory not existing at " + f.getAbsolutePath());
            return;
        }

        ProgressIndicator pb = new ProgressIndicator();
        javafx.scene.control.Label label = new javafx.scene.control.Label("downloading phenotype_annotation.tab...");
        FlowPane root = new FlowPane();
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.getChildren().addAll(label, pb);
        Scene scene = new Scene(root, 400, 100);
        Stage window = new Stage();
        window.setTitle("HPO annotation download");
        window.setScene(scene);

        Task hpodownload = new Downloader(dirpath, properties.getProperty("hpo.phenotype.annotations.url"),
                PlatformUtil.HPO_ANNOTATIONS_FILENAME, pb);
        window.show();
        hpodownload.setOnSucceeded(event -> {
            window.close();
            logger.trace(String.format("Successfully downloaded %s to %s",
                    PlatformUtil.HPO_ANNOTATIONS_FILENAME, dirpath));
            String hpoAnnotationsFileName = dirpath + File.separator + PlatformUtil.HPO_ANNOTATIONS_FILENAME;
            DirectIndirectHpoAnnotationParser parser = new DirectIndirectHpoAnnotationParser(hpoAnnotationsFileName, optionalResources
                    .getOntology());
            parser.doParse();
            optionalResources.setDirectAnnotMap(parser.getDirectAnnotMap());
            optionalResources.setIndirectAnnotMap(parser.getIndirectAnnotMap());
            properties.setProperty("hpo.annotations.path", hpoAnnotationsFileName);
        });
        hpodownload.setOnFailed(event -> {
            window.close();
            logger.error("Unable to download phenotype_annotation.tab file");
            optionalResources.setIndirectAnnotMap(null);
            optionalResources.setDirectAnnotMap(null);
            properties.setProperty("hpo.annotations.path", null);
        });
        Thread thread = new Thread(hpodownload);
        thread.start();
        e.consume();
    }

    /**
     * Expand & scroll to the term selected in the search text field.
     */
    @FXML
    private void goButtonAction() {
        if (currentMode.equals(BROWSE_DISEASE)) {
            DiseaseModel dmod = model.getDiseases().get(searchTextField.getText());
            if (dmod == null) return;
            updateDescriptionToDiseaseModel(dmod);
            selectedDisease = dmod;
            searchTextField.clear();
        } else if (currentMode.equals(mode.BROWSE_HPO)) {
            TermId id = labels.get(searchTextField.getText());
            if (id == null) return; // button was clicked while field was empty, no need to do anything
            expandUntilTerm(optionalResources.getOntology().getTermMap().get(id));
            searchTextField.clear();
        } else if (currentMode == mode.NEW_ANNOTATION) {
            TermId id = labels.get(searchTextField.getText());
            if (id == null) return; // button was clicked while field was empty, no need to do anything
            expandUntilTerm(optionalResources.getOntology().getTermMap().get(id));
            searchTextField.clear();
        }
    }

    @FXML
    public void initialize() {
        //logger.trace("initialize");
        // This action will be run after user approves a PhenotypeTerm in the ontologyTreePane
        Consumer<HpoTerm> addHook = (ph -> logger.trace(String.format("Hook for %s", ph.getName())));

        initRadioButtons();

        // this binding evaluates to true, if ontology or annotations files are missing (null)
        BooleanBinding someResourceMissing = optionalResources.someResourceMissing();

        hpoTermRadioButton.disableProperty().bind(someResourceMissing);
        diseaseRadioButton.disableProperty().bind(someResourceMissing);
        newAnnotationRadioButton.disableProperty().bind(someResourceMissing);

        searchTextField.disableProperty().bind(someResourceMissing);
        goButton.disableProperty().bind(someResourceMissing);
        ontologyTreeView.disableProperty().bind(someResourceMissing);

        exportHierarchicalSummaryButton.disableProperty().bind(someResourceMissing);
        exportToExcelButton.disableProperty().bind(someResourceMissing);
        suggestCorrectionToTermButton.disableProperty().bind(someResourceMissing);
        suggestNewChildTermButton.disableProperty().bind(someResourceMissing);
        suggestNewAnnotationButton.disableProperty().bind(someResourceMissing);
        reportMistakenAnnotationButton.disableProperty().bind(someResourceMissing);


        someResourceMissing.addListener(((observable, oldValue, newValue) -> {
            if (!newValue) { // nothing is missing anymore
                activate();
            } else { // invalidate model and anything in the background. Controls should be disabled automatically
                deactivate();
            }
        }));


        if (!someResourceMissing.get()) {
            activate();
        }


//        initTree(ontology, addHook);
//        string2diseasemap = model.getDiseases();
    }

    private void activate() {
        initTree(optionalResources.getOntology(), k -> System.out.println("Consumed " + k));
        this.model = new Model(optionalResources.getOntology(), optionalResources.getIndirectAnnotMap(),
                optionalResources.getDirectAnnotMap());
    }

    private void deactivate() {
        initTree(null, k -> System.out.println("Consumed " + k));
        this.model = new Model(null, null, null);
    }

    @Deprecated
    private void initHpoData() {
        if (model.getOntology() == null) {
            logger.error("Need to initialize ontology before we can start the application.");
            return;
        }
        initTree(model.getOntology(), addHook);
        logger.trace("Finished initilizing Human Phenotype Ontology");
//        string2diseasemap = model.getDiseases();
    }

    private void initRadioButtons() {
        ToggleGroup group = new ToggleGroup();
        allDatabaseButton.setSelected(true);
        allDatabaseButton.setToggleGroup(group);
        omimButton.setSelected(false);
        omimButton.setToggleGroup(group);
        orphanetButton.setSelected(false);
        orphanetButton.setToggleGroup(group);
        decipherButton.setSelected(false);
        decipherButton.setToggleGroup(group);
        group.selectedToggleProperty().addListener(
                (ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) -> {
                    if (group.getSelectedToggle() != null) {
                        String userdata = (String) group.getSelectedToggle().getUserData();
                        if (userdata == null) {
                            logger.warn("Could not retrieve user data for database radio buttons");
                        }
                        if (userdata.equals("orphanet"))
                            selectedDatabase = DiseaseModel.database.ORPHANET;
                        else if (userdata.equals("omim"))
                            selectedDatabase = DiseaseModel.database.OMIM;
                        else if (userdata.equals("all"))
                            selectedDatabase = DiseaseModel.database.ALL;
                        else if (userdata.equals("decipher"))
                            selectedDatabase = DiseaseModel.database.DECIPHER;
                        else {
                            logger.warn("did not recognize database " + userdata);
                        }
                    }
                });
        // now the HPO vs disease
        ToggleGroup group2=new ToggleGroup();
        hpoTermRadioButton.setSelected(true);
        hpoTermRadioButton.setToggleGroup(group2);
        diseaseRadioButton.setToggleGroup(group2);
        newAnnotationRadioButton.setToggleGroup(group2);
        group2.selectedToggleProperty().addListener((ov,oldval,newval) ->{
            String userdata=(String)newval.getUserData();
            if (userdata.equals("hpo")) {
                currentMode=mode.BROWSE_HPO;
                if (labels !=null) {
                    WidthAwareTextFields.bindWidthAwareAutoCompletion(searchTextField, labels.keySet());
                } else {
                    logger.error("Attempt to init autocomplete with null list of HPO terms");
                }
            } else if (userdata.equals("disease")){
                currentMode= BROWSE_DISEASE;
                if (this.model.getDiseases()!=null) {
                    WidthAwareTextFields.bindWidthAwareAutoCompletion(searchTextField,model.getDiseases().keySet());
                } else {
                    logger.warn("Attempt to init autocomplete with null list of diseases");
                }
            } else if (userdata.equals("newannotation")) {
                currentMode=NEW_ANNOTATION;
                if (labels !=null) {
                    WidthAwareTextFields.bindWidthAwareAutoCompletion(searchTextField, labels.keySet());
                } else {
                    logger.error("Attempt to init autocomplete with null list of HPO terms");
                }
            }
        });
    }


    private void switchToMode(mode Mode) {
        if(Mode.equals( mode.BROWSE_HPO) ) {
            hpoTermRadioButton.setSelected(true);
            currentMode = mode.BROWSE_HPO;
        } else if (Mode.equals(BROWSE_DISEASE)) {
            diseaseRadioButton.setSelected(true);
            currentMode = mode.BROWSE_DISEASE;
        } else if (Mode.equals(NEW_ANNOTATION)) {
            currentMode = mode.NEW_ANNOTATION;
        }
    }


    /**
     * Initialize the ontology browser-tree in the left column of the app.
     *
     * @param ontology Reference to the HPO
     * @param addHook  function hook (currently unused)
     */
    private void initTree(HpoOntology ontology, Consumer<HpoTerm> addHook) {
//        this.ontology=ontology;
//        this.addHook = addHook;
        // populate the TreeView with top-level elements from ontology hierarchy
        if (ontology == null) {
            ontologyTreeView.setRoot(null);
            return;
        }
        TermId rootId = ontology.getRootTermId();
        HpoTerm rootTerm = ontology.getTermMap().get(rootId);
        TreeItem<HpoTermWrapper> root = new HpoTermTreeItem(new HpoTermWrapper(rootTerm));
        root.setExpanded(true);
//        if (ontologyTreeView==null) {
//            logger.fatal("Tree view is not initialized");
//            return;
//        }
        ontologyTreeView.setShowRoot(false);
        ontologyTreeView.setRoot(root);
        ontologyTreeView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        logger.error("New value is null");
                        return;
                    }
                    HpoTermWrapper w = newValue.getValue();
                    TreeItem item = new HpoTermTreeItem(w);
                    updateDescription(item);
                });
        // create Map for lookup of the terms in the ontology based on their Name
        ontology.getTermMap().values().forEach(term -> labels.put(term.getName(), term.getId()));
        WidthAwareTextFields.bindWidthAwareAutoCompletion(searchTextField, labels.keySet());

        // show intro message in the infoWebView
        Platform.runLater(() -> {
            infoWebEngine = infoWebView.getEngine();
            infoWebEngine.loadContent("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>HPO tree browser</title></head>" +
                    "<body><p>Click on HPO term in the tree browser to display additional information</p></body></html>");
        });
    }

    /**
     * Find the path from the root term to given {@link HpoTerm}, expand the tree and set the selection model of the
     * TreeView to the term position.
     *
     * @param term {@link HpoTerm} to be displayed
     */
    private void expandUntilTerm(HpoTerm term) {
        // logger.trace("expand until term " + term.toString());
        switchToMode(BROWSE_HPO);
        if (existsPathFromRoot(term)) {
            // find root -> term path through the tree
            Stack<HpoTerm> termStack = new Stack<>();
            termStack.add(term);
            Set<HpoTerm> parents = getTermParents(term);//ontology.getTermParents(term);
            while (parents.size() != 0) {
                HpoTerm parent = parents.iterator().next();
                termStack.add(parent);
                parents = getTermParents(parent);//ontology.getTermParents(parent);
            }

            // expand tree nodes in top -> down direction
            List<TreeItem<HpoTermWrapper>> children = ontologyTreeView.getRoot().getChildren();
            termStack.pop(); // get rid of 'All' node which is hidden
            TreeItem<HpoTermWrapper> target = ontologyTreeView.getRoot();
            while (!termStack.empty()) {
                HpoTerm current = termStack.pop();
                for (TreeItem<HpoTermWrapper> child : children) {
                    if (child.getValue().term.equals(current)) {
                        child.setExpanded(true);
                        target = child;
                        children = child.getChildren();
                        break;
                    }
                }
            }
            ontologyTreeView.getSelectionModel().select(target);
            ontologyTreeView.scrollTo(ontologyTreeView.getSelectionModel().getSelectedIndex());
        } else {
            TermId rootId = optionalResources.getOntology().getRootTermId();
            HpoTerm rootTerm = optionalResources.getOntology().getTermMap().get(rootId);
            logger.warn(String.format("Unable to find the path from %s to %s", rootTerm.toString(), term.getName()));
        }
        selectedTerm = term;
    }

    /**
     * Get currently selected Term. Used in tests.
     *
     * @return {@link HpoTermTreeItem} that is currently selected
     */
    private HpoTermTreeItem getSelectedTerm() {
        return (ontologyTreeView.getSelectionModel().getSelectedItem() == null) ? null
                : (HpoTermTreeItem) ontologyTreeView.getSelectionModel().getSelectedItem();
    }

    /**
     * Update content of the {@link #infoWebView} with currently selected {@link HpoTerm}.
     *
     * @param treeItem currently selected {@link TreeItem} containing {@link HpoTerm}
     */
    private void updateDescription(TreeItem<HpoTermWrapper> treeItem) {
        if (currentMode.equals(mode.NEW_ANNOTATION)) {
            return;
        }
        if (treeItem == null)
            return;

        HpoTerm term = treeItem.getValue().term;
        String termID = term.getId().getIdWithPrefix();
        List<DiseaseModel> annotatedDiseases = model.getDiseaseAnnotations(termID, selectedDatabase);
        if (annotatedDiseases == null) {
            logger.error("could not retrieve diseases for " + termID);
        }
        String content = HpoHtmlPageGenerator.getHTML(term, annotatedDiseases);
        //System.out.print(content);
        // infoWebEngine=this.infoWebView.getEngine();
        infoWebEngine.loadContent(content);
        infoWebEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    org.w3c.dom.events.EventListener listener = new EventListener() {
                        @Override
                        public void handleEvent(org.w3c.dom.events.Event ev) {
                            String domEventType = ev.getType();
                           // System.err.println("EventType FROM updateHPO: " + domEventType);
                            if (domEventType.equals(EVENT_TYPE_CLICK)) {
                                String href = ((Element) ev.getTarget()).getAttribute("href");
                                // System.out.println("HREF "+href);
                                if (href.equals("http://www.human-phenotype-ontology.org")) {
                                    return; // the external link is taken care of by the Webengine
                                    // therefore, we do not need to do anything special here
                                }
                                // The following line is necessary because sometimes multiple events are triggered
                                // and we get a "stray" HPO-related link that does not belong here.
                                if (href.startsWith("HP:")) return;
                                DiseaseModel dmod = model.getDiseases().get(href);
                                if (dmod == null) {
                                    logger.error("Link to disease model for " + href + " was null");
                                    return;
                                }
                                updateDescriptionToDiseaseModel(dmod);
                                selectedDisease = dmod;
                                searchTextField.clear();
                                currentMode = BROWSE_DISEASE;
                                diseaseRadioButton.setSelected(true);
                            }
                        }
                    };

                    Document doc = infoWebView.getEngine().getDocument();
                    NodeList nodeList = doc.getElementsByTagName("a");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        ((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_CLICK, listener, false);
                        //((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_MOUSEOVER, listener, false);
                        //((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_MOUSEOVER, listener, false);
                    }
                }
            }
        });

    }

    /**
     * Update content of the {@link #infoWebView} with currently selected {@link HpoTerm}.
     * The function is called when the user is on an HPO Term page and selects a link to
     * a disease.
     *
     * @param dmodel currently selected {@link TreeItem} containing {@link HpoTerm}
     */
    private void updateDescriptionToDiseaseModel(DiseaseModel dmodel) {
        String dbName = dmodel.getDiseaseDbAndId();
        String diseaseName = dmodel.getDiseaseName();
        List<HpoTerm> annotatingTerms = model.getAnnotationTermsForDisease(dmodel);
        String content = HpoHtmlPageGenerator.getDiseaseHTML(dbName, diseaseName, annotatingTerms, optionalResources
                .getOntology());
        infoWebEngine.loadContent(content);
        infoWebEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    org.w3c.dom.events.EventListener listener = new EventListener() {
                        @Override
                        public void handleEvent(org.w3c.dom.events.Event ev) {
                            String domEventType = ev.getType();
                            //System.err.println("EventType from updateToDisease: " + domEventType);
                            if (domEventType.equals(EVENT_TYPE_CLICK)) {
                                String href = ((Element) ev.getTarget()).getAttribute("href");
                                if (href.equals("http://www.human-phenotype-ontology.org")) {
                                    return; // the external link is taken care of by the Webengine
                                    // therefore, we do not need to do anything special here
                                }
                                // The following line is needed because sometimes we get multiple click events
                                // if the user clicks once and some appear to be for the "wrong" link type.
                                if (!href.startsWith("HP:")) { return; }
                                TermId tid = ImmutableTermId.constructWithPrefix(href);
                                if (tid == null) {
                                    logger.error(String.format("Could not construct term id from \"%s\"", href));
                                    return;
                                }
                                HpoTerm term = optionalResources.getOntology().getTermMap().get(tid);
                                if (term == null) {
                                    logger.error(String.format("Could not construct term  from termid \"%s\"", tid.getIdWithPrefix()));
                                    return;
                                }
                                // set the tree on the left to our new term
                                expandUntilTerm(term);
                                // update the Webview browser
                                updateDescription(new HpoTermTreeItem(new HpoTermWrapper(term)));
                                searchTextField.clear();
                                currentMode = mode.BROWSE_HPO;
                                hpoTermRadioButton.setSelected(true);
                            }
                        }
                    };

                    Document doc = infoWebView.getEngine().getDocument();
                    NodeList nodeList = doc.getElementsByTagName("a");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        ((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_CLICK, listener, false);
                        //((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_MOUSEOVER, listener, false);
                        //((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_MOUSEOVER, listener, false);
                    }
                }
            }
        });
    }

    @FXML
    private void exportToExcel(ActionEvent event) {
        logger.trace("exporting to excel");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export HPO as Excel-format file");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel file (*.xlsx)", "*.xlsx");
        chooser.getExtensionFilters().add(extFilter);
        chooser.setInitialFileName("hpo.xlsx");
        File f = chooser.showSaveDialog(null);
        if (f != null) {
            String path = f.getAbsolutePath();
            logger.trace(String.format("Setting path to LOINC Core Table file to %s", path));
            Hpo2ExcelExporter exporter = new Hpo2ExcelExporter(model.getOntology());
            exporter.exportToExcelFile(path);
        } else {
            logger.error("Unable to obtain path to Excel export file");
        }
        event.consume();
    }

    @FXML
    private void exportHierarchicalSummary(ActionEvent event) {
        if (selectedTerm == null) {
            logger.error("Select a term before exporting hierarchical summary TODO show error window");
            PopUps.showInfoMessage("Please select an HPO term in order to export a term with its subhierarchy",
                    "Error: No HPO Term selected");
            return; // to do throw exceptio
        }
        selectedTerm = getSelectedTerm().getValue().term;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export HPO as Excel-format file");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel file (*.xlsx)", "*.xlsx");
        chooser.getExtensionFilters().add(extFilter);
        chooser.setInitialFileName(String.format("%s.xlsx", selectedTerm.getName()));
        File f = chooser.showSaveDialog(null);
        if (f != null) {
            String path = f.getAbsolutePath();
            logger.trace(String.format("Setting path to hierarchical export file to %s", path));
        } else {
            logger.error("Unable to obtain path to Excel export file");
            return;
        }
        logger.trace(String.format("Exporting hierarchical summary starting from term %s", selectedTerm.toString()));
        HierarchicalExcelExporter exporter = new HierarchicalExcelExporter(model.getOntology(), selectedTerm);
        try {
            exporter.exportToExcel(f.getAbsolutePath());
        } catch (HPOException e) {
            PopUps.showException("Error", "could not export excel file", e);
        }
    }

    /**
     * For the GitHub new issues, we want to allow the user to choose a pre-existing label for the issue.
     * For this, we first go to GitHub and retrieve the labels with
     * {@link org.monarchinitiative.hpoworkbench.github.GitHubLabelRetriever}. We only do this
     * once per session though.
     */
    private void initializeGitHubLabelsIfNecessary() {
        if (model.hasLabels()) {
            return; // we only need to retrieve the labels from the server once per session!
        }
        GitHubLabelRetriever retriever = new GitHubLabelRetriever();
        List<String> labels = retriever.getLabels();
        if (labels == null) {
            labels = new ArrayList<>();
        }
        if (labels.size() == 0) {
            labels.add("new term request");
        }
        model.setGithublabels(labels);
    }

    @FXML
    private void suggestCorrectionToTerm(ActionEvent e) {
        if (getSelectedTerm() == null) {
            logger.error("Select a term before creating GitHub issue");
            PopUps.showInfoMessage("Please select an HPO term before creating GitHub issue",
                    "Error: No HPO Term selected");
            return;
        } else {
            selectedTerm = getSelectedTerm().getValue().term;
        }
        selectedTerm = getSelectedTerm().getValue().term;
        GitHubPopup popup = new GitHubPopup(selectedTerm);
        initializeGitHubLabelsIfNecessary();
        popup.setLabels(model.getGithublabels());
        popup.setupGithubUsernamePassword(githubUsername, githubPassword);
        popup.displayWindow(primarystage);
        String githubissue = popup.retrieveGitHubIssue();
        if (popup.wasCancelled()) {
            return;
        }
        if (githubissue == null) {
            logger.trace("got back null GitHub issue");
            return;
        }
        String title = String.format("Correction to term %s", selectedTerm.getName());
        postGitHubIssue(githubissue, title, popup.getGitHubUserName(), popup.getGitHubPassWord(), popup.getGitHubLabels());
    }

    @FXML
    private void suggestNewChildTerm(ActionEvent e) {
        if (getSelectedTerm() == null) {
            logger.error("Select a term before creating GitHub issue");
            PopUps.showInfoMessage("Please select an HPO term before creating GitHub issue",
                    "Error: No HPO Term selected");
            return;
        } else {
            selectedTerm = getSelectedTerm().getValue().term;
        }
        GitHubPopup popup = new GitHubPopup(selectedTerm, true);
        initializeGitHubLabelsIfNecessary();
        popup.setLabels(model.getGithublabels());
        popup.setupGithubUsernamePassword(githubUsername, githubPassword);
        popup.displayWindow(primarystage);
        String githubissue = popup.retrieveGitHubIssue();
        if (githubissue == null) {
            logger.trace("got back null github issue");
            return;
        }
        String title = String.format("Suggesting new child term of \"%s\"", selectedTerm.getName());
        postGitHubIssue(githubissue, title, popup.getGitHubUserName(), popup.getGitHubPassWord(), popup.getGitHubLabels());
    }

    @FXML
    private void suggestNewAnnotation(ActionEvent e) {
        if (getSelectedTerm() == null) {
            logger.error("Select a term before creating GitHub issue");
            PopUps.showInfoMessage("Please select an HPO term before creating GitHub issue",
                    "Error: No HPO Term selected");
            return;
        } else {
            selectedTerm = getSelectedTerm().getValue().term;
        }
        if (!currentMode.equals(mode.NEW_ANNOTATION)) {
            PopUps.showInfoMessage("Please select a disease and then a new HPO term before using this option",
                    "Error: No disease selected");
            return;
        }
        selectedTerm = getSelectedTerm().getValue().term;
        if (selectedDisease == null) {
            PopUps.showInfoMessage("Please select a disease and then a new HPO term before using this option",
                    "Error: No disease selected");
            return;
        }
        GitHubPopup popup = new GitHubPopup(selectedTerm, selectedDisease);
        initializeGitHubLabelsIfNecessary();
        popup.setLabels(model.getGithublabels());
        popup.setupGithubUsernamePassword(githubUsername, githubPassword);
        popup.displayWindow(primarystage);
        String githubissue = popup.retrieveGitHubIssue();
        if (githubissue == null) {
            logger.trace("got back null GitHub issue");
            return;
        }
        String title = String.format("New annotation suggestion for %s", selectedDisease.getDiseaseName());
        postGitHubIssue(githubissue, title, popup.getGitHubUserName(), popup.getGitHubPassWord(), popup.getGitHubLabels());
    }

    @FXML
    private void reportMistakenAnnotation(ActionEvent e) {
        if (getSelectedTerm() == null) {
            logger.error("Select a term before creating GitHub issue");
            PopUps.showInfoMessage("Please select an HPO term before creating GitHub issue",
                    "Error: No HPO Term selected");
            return;
        } else {
            selectedTerm = getSelectedTerm().getValue().term;
        }
        if (!currentMode.equals(mode.NEW_ANNOTATION)) {
            PopUps.showInfoMessage("Please select a disease and then a new HPO term before using this option",
                    "Error: No disease selected");
            return;
        }
        selectedTerm = getSelectedTerm().getValue().term;
        if (selectedDisease == null) {
            PopUps.showInfoMessage("Please select a disease and then a new HPO term before using this option",
                    "Error: No disease selected");
            return;
        }
        GitHubPopup popup = new GitHubPopup(selectedTerm, selectedDisease, true);
        initializeGitHubLabelsIfNecessary();
        popup.setLabels(model.getGithublabels());
        popup.setupGithubUsernamePassword(githubUsername, githubPassword);
        popup.displayWindow(primarystage);
        String githubissue = popup.retrieveGitHubIssue();
        if (githubissue == null) {
            logger.trace("got back null GitHub issue");
            return;
        }
        String title = String.format("Erroneous annotation for %s", selectedDisease.getDiseaseName());
        postGitHubIssue(githubissue, title, popup.getGitHubUserName(), popup.getGitHubPassWord());
    }

    @FXML
    private void postGitHubIssue(String message, String title, String uname, String pword) {
        GitHubPoster poster = new GitHubPoster(uname, pword, title, message);
        this.githubUsername = uname;
        this.githubPassword = pword;
        try {
            poster.postIssue();
        } catch (HPOWorkbenchException he) {
            PopUps.showException("GitHub error", "Bad Request (400): Could not post issue", he);
        } catch (Exception ex) {
            PopUps.showException("GitHub error", "GitHub error: Could not post issue", ex);
            return;
        }
        String response = poster.getHttpResponse();
        PopUps.showInfoMessage(
                String.format("Created issue for %s\nServer response: %s", selectedTerm.getName(), response), "Created new issue");

    }

    @FXML
    private void postGitHubIssue(String message, String title, String uname, String pword, List<String> labels) {
        GitHubPoster poster = new GitHubPoster(uname, pword, title, message);
        this.githubUsername = uname;
        this.githubPassword = pword;
        if (labels != null && !labels.isEmpty()) {
            poster.setLabel(labels);
        }
        try {
            poster.postIssue();
        } catch (HPOWorkbenchException he) {
            PopUps.showException("GitHub error", "Bad Request (400): Could not post issue", he);
        } catch (Exception ex) {
            PopUps.showException("GitHub error", "GitHub error: Could not post issue", ex);
            return;
        }
        String response = poster.getHttpResponse();
        PopUps.showInfoMessage(
                String.format("Created issue for %s\nServer response: %s", selectedTerm.getName(), response), "Created new issue");

    }

    /** Show the help dialog */
    @FXML
    private void helpWindow(ActionEvent e) {
        HelpViewFactory.openBrowser();
        e.consume();
    }

    /** Show the about message */
    @FXML
    private void aboutWindow(ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("HPO Workbench");
        alert.setHeaderText("Human Phenotype Ontology Workbench");
        String s = "A tool for working with the HPO.\n\u00A9 Monarch Initiative 2018";
        alert.setContentText(s);
        alert.showAndWait();
        e.consume();
    }

    private Set<HpoTerm> getTermChildren(HpoTerm term) {
        Set<HpoTerm> kids = new HashSet<>();
        Iterator it = optionalResources.getOntology().getGraph().inEdgeIterator(term.getId());
        while (it.hasNext()) {
            Edge<TermId> edge = (Edge<TermId>) it.next();
            TermId sourceId = edge.getSource();
            HpoTerm sourceTerm = optionalResources.getOntology().getTermMap().get(sourceId);
            kids.add(sourceTerm);
        }
        return kids;
    }

    private Set<HpoTerm> getTermParents(HpoTerm term) {
        Set<HpoTerm> eltern = new HashSet<>();
        Iterator it = optionalResources.getOntology().getGraph().outEdgeIterator(term.getId());
        while (it.hasNext()) {
            Edge<TermId> edge = (Edge<TermId>) it.next();
            TermId destId = edge.getDest();
            HpoTerm destTerm = optionalResources.getOntology().getTermMap().get(destId);
            eltern.add(destTerm);
        }
        return eltern;
    }

    private boolean existsPathFromRoot(HpoTerm term) {
        TermId rootId = optionalResources.getOntology().getRootTermId();
        TermId tid = term.getId();
        DirectedGraph dag = optionalResources.getOntology().getGraph();
        Stack<TermId> stack = new Stack<>();
        stack.push(rootId);
        while (!stack.empty()) {
            TermId id = stack.pop();
            Iterator it = dag.inEdgeIterator(id);
            while (it.hasNext()) {
                Edge<TermId> edge = (Edge<TermId>) it.next();
                TermId source = edge.getSource();
                if (source.equals(tid)) {
                    return true;
                }
                stack.push(source);
            }
        }
        return false; // if we get here, there was no path.
    }

    /** Determines the behavior of the app. Are we browsing HPO terms, diseases, or suggesting new annotations? */
    enum mode {
        BROWSE_HPO, BROWSE_DISEASE, NEW_ANNOTATION
    }

    /**
     * Inner class that defines a bridge between hierarchy of {@link HpoTerm}s and {@link TreeItem}s of the
     * {@link TreeView}.
     */
    class HpoTermTreeItem extends TreeItem<HpoTermWrapper> {

        /** List used for caching of the children of this term */
        private ObservableList<TreeItem<HpoTermWrapper>> childrenList;

        /**
         * Default & only constructor for the TreeItem.
         *
         * @param term {@link HpoTerm} that is represented by this TreeItem
         */
        HpoTermTreeItem(HpoTermWrapper term) {
            super(term);
        }

        /**
         * Check that the {@link HpoTerm} that is represented by this TreeItem is a leaf term as described below.
         * <p>
         * {@inheritDoc}
         */
        @Override
        public boolean isLeaf() {
            return getTermChildren(getValue().term).size() == 0;
        }


        /**
         * Get list of children of the {@link HpoTerm} that is represented by this TreeItem.
         * {@inheritDoc}
         */
        @Override
        public ObservableList<TreeItem<HpoTermWrapper>> getChildren() {
            if (childrenList == null) {
                // logger.debug(String.format("Getting children for term %s", getValue().term.getName()));
                childrenList = FXCollections.observableArrayList();
                Set<HpoTerm> children = getTermChildren(getValue().term);
                children.stream()
                        .sorted(Comparator.comparing(HpoTerm::getName))
                        .map(term -> new HpoTermTreeItem(new HpoTermWrapper(term)))
                        .forEach(childrenList::add);
                super.getChildren().setAll(childrenList);
            }
            return super.getChildren();
        }
    }

}
