package org.monarchinitiative.hpoworkbench.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.monarchinitiative.hpoworkbench.StartupTask;
import org.monarchinitiative.hpoworkbench.analysis.AnnotationTlc;
import org.monarchinitiative.hpoworkbench.analysis.HpoStats;
import org.monarchinitiative.hpoworkbench.excel.HierarchicalExcelExporter;
import org.monarchinitiative.hpoworkbench.excel.Hpo2ExcelExporter;
import org.monarchinitiative.hpoworkbench.exception.HPOException;
import org.monarchinitiative.hpoworkbench.gui.HelpViewFactory;
import org.monarchinitiative.hpoworkbench.gui.PopUps;
import org.monarchinitiative.hpoworkbench.gui.WidthAwareTextFields;
import org.monarchinitiative.hpoworkbench.gui.webpopup.SettingsPopup;
import org.monarchinitiative.hpoworkbench.gui.webviewerutil.WebViewerFactory;
import org.monarchinitiative.hpoworkbench.gui.webviewerutil.WebViewerPopup;
import org.monarchinitiative.hpoworkbench.html.AnnotationTlcHtmlGenerator;
import org.monarchinitiative.hpoworkbench.html.HpoStatsHtmlGenerator;
import org.monarchinitiative.hpoworkbench.io.*;
import org.monarchinitiative.hpoworkbench.resources.OptionalHpoResource;
import org.monarchinitiative.hpoworkbench.resources.OptionalHpoaResource;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.*;


/**
 * Main Controller for HPO Workbench
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
@Component
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private static final String EVENT_TYPE_CLICK = "click";

    private final OptionalHpoResource optionalHpoResource;

    private final OptionalHpoaResource optionalHpoaResource;

    /**
     * Directory, where ontologies and HPO annotation files are being stored.
     */
    private final File hpoWorkbenchDir;

    /**
     * Application-specific properties (not the System properties!) defined in the 'application.properties' file that
     * resides in the classpath.
     */
    private final Properties pgProperties;

    private final ExecutorService executor;

    @Autowired
    DownloaderFactory factory;
    /**
     * Place at the bottom of the window for showing messages to user
     */
    @FXML
    private Label copyrightLabel;

    @FXML
    public HBox statusHBox;

    @FXML
    private TextField autocompleteTextfield;

    /**
     * The tree view that shows the HPO Ontology hierarchy
     */
    @FXML
    private TreeView<OntologyTermWrapper> ontologyTreeView;
    /**
     * WebView for displaying details of the Term that is selected in the {@link #ontologyTreeView}.
     */
    @FXML
    private WebView infoWebView;
    /**
     * WebEngine backing up the {@link #infoWebView}.
     */
    private WebEngine infoWebEngine;
    @FXML
    private RadioButton hpoRadioButton;
    @FXML
    private RadioButton mondoRadioButton;

    /**
     * Key: a term name such as "Myocardial infarction"; value: the corresponding HPO id as a {@link TermId}.
     */
    private final Map<String, TermId> ontologyLabelsAndTermIdMap = new HashMap<>();
    /**
     * The term that is currently selected in the Browser window.
     */
    private Term selectedTerm = null;
    /**
     * Github password. Username and password will be stored for the current session only.
     */
    private String githubPassword;


    @Autowired
    public MainController(OptionalHpoResource optionalHpoResource,
                          OptionalHpoaResource optionalHpoaResource,
                          @Qualifier("configProperties") Properties properties,
                          @Qualifier("appHomeDir") File hpoWorkbenchDir,
                          ExecutorService executorService) {
        this.optionalHpoResource = optionalHpoResource;
        this.optionalHpoaResource = optionalHpoaResource;
        this.pgProperties = properties;
        this.hpoWorkbenchDir = hpoWorkbenchDir;
        this.executor = executorService;
    }

    @FXML
    private void initialize() {
        logger.info("Initializing main controller");
        StartupTask task = new StartupTask(optionalHpoResource, optionalHpoaResource, pgProperties);
        publishMessage("Loading resources");
        ProgressIndicator pb = new ProgressIndicator();
        pb.setProgress(0);
        pb.progressProperty().unbind();
        pb.progressProperty().bind(task.progressProperty());
        Stage window = PopUps.setupProgressDialog("Initializing", "Loading resources...", pb);
        window.show();
        window.toFront();
        task.setOnSucceeded(e -> {
            publishMessage("Successfully loaded files");
            window.close();
        });
        task.setOnFailed(e -> {
            publishMessage("Unable to load ontologies/annotations", MessageType.ERROR);
            window.close();
        });
        this.executor.submit(task);
        String ver = MainController.getVersion();
        copyrightLabel.setText("HPO Workbench, v. " + ver + ", \u00A9 Monarch Initiative 2017-2021");

        ChangeListener<? super Object> listener = (obs, oldval, newval) -> activateIfResourcesAvailable();
        optionalHpoResource.ontologyProperty().addListener(listener);
        optionalHpoaResource.directAnnotMapProperty().addListener(listener);
        optionalHpoaResource.indirectAnnotMapProperty().addListener(listener);
        logger.info("Done initialization");
        checkAll();
        logger.info("done activate");
    }

    private void activateIfResourcesAvailable() {
        if (optionalHpoResource.getOntology() != null) { // hpo obo file is missing
            activateOntologyTree();
        } else {
            logger.error("Could not activate resource");
        }
    }


    /**
     * Check availability of tracked resources and publish an appropriate message.
     */
    private void checkAll() {
        if (optionalHpoResource.getOntology() == null) { // hpo obo file is missing
            publishMessage("hpo json file is missing", MessageType.ERROR);
        } else if (optionalHpoaResource.getDirectAnnotMap() == null) {
            publishMessage("phenotype.hpoa file is missing", MessageType.ERROR);
        } else {
            logger.info("All three resources loaded");
            publishMessage("Ready to go", MessageType.INFO);
        }
    }


    /**
     * Post information message to the status bar.
     *
     * @param msg String with message to be displayed
     */
    void publishMessage(String msg) {
        publishMessage(msg, MessageType.INFO);
    }

    /**
     * Post the message to the status bar. Color of the text is determined by the message <code>type</code>.
     *
     * @param msg  String with message to be displayed
     * @param type message type
     */
    private void publishMessage(String msg, MessageType type) {
        int MAX_MESSAGES = 1;
        Platform.runLater(() -> {
            if (statusHBox.getChildren().size() == MAX_MESSAGES) {
                statusHBox.getChildren().remove(MAX_MESSAGES - 1);
            }
            Label label = prepareContainer(type);
            label.setText(msg);
            statusHBox.getChildren().add(0, label);
        });
    }

    /**
     * Make label for displaying message in the {@link #statusHBox}. The style of the text depends on given
     * <code>type</code>
     *
     * @param type of the message to be displayed
     * @return {@link Label} styled according to the message type
     */
    private Label prepareContainer(MessageType type) {
        Label label = new Label();
        label.setPrefHeight(30);
        HBox.setHgrow(label, Priority.ALWAYS);
        label.setPadding(new Insets(5));
        switch (type) {
            case WARNING:
                label.setStyle("-fx-text-fill: orange; -fx-font-weight: bolder");
                break;
            case ERROR:
                label.setStyle("-fx-text-fill: red; -fx-font-weight: bolder");
                break;
            case INFO:
            default:
                label.setStyle("-fx-text-fill: black; -fx-font-weight: bolder");
                break;
        }


        return label;
    }

    public static String getVersion() {
        String version = "0.0.0";// default, should be overwritten by the following.
        try {
            Package p = MainController.class.getPackage();
            version = p.getImplementationVersion();
        } catch (Exception e) {
            // do nothing
        }
        if (version == null) version = "1.6.0"; // this works on a maven build but needs to be reassigned in intellij
        return version;
    }

    /**
     * This is called from the Edit menu and allows the user to import a local copy of
     * hp.obo (usually because the local copy is newer than the official release version of hp.obo).
     *
     * @param e event
     */
    @FXML
    private void importLocalHpObo(ActionEvent e) {
        e.consume();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import local hp.obo file");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("HPO OBO file (*.obo)", "*.obo");
        chooser.getExtensionFilters().add(extFilter);
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        File f = chooser.showOpenDialog(stage);
        if (f == null) {
            logger.error("Unable to obtain path to local HPO OBO file");
            PopUps.showInfoMessage("Unable to obtain path to local HPO OBO file", "Error");
            return;
        }
        String hpoOboPath = f.getAbsolutePath();

        HPOParser parser = new HPOParser(hpoOboPath);
        optionalHpoResource.setOntology(parser.getHPO());
        pgProperties.setProperty("hpo.obo.path", hpoOboPath);
    }

    @FXML
    private void close(ActionEvent e) {
        logger.trace("Closing down");
        Platform.exit();
    }

    @FXML
    private void downloadHPO(ActionEvent e) {
        factory.downloadHpoJson();
        e.consume();
    }

    @FXML
    private void downloadHPOAnnotations(ActionEvent e) {
        factory.downloadHPOAnnotations();
        e.consume();
    }

    @FXML
    private void showSettings(ActionEvent e) {
        Stage stage = (Stage) this.statusHBox.getScene().getWindow();
        SettingsPopup popup = new SettingsPopup(pgProperties, optionalHpoResource, optionalHpoaResource, stage);
        popup.popup();
    }

    /**
     * Show the help dialog
     */
    @FXML
    private void helpWindow(ActionEvent e) {
        HelpViewFactory.openBrowser();
        e.consume();
    }

    /**
     * Show the about message
     */
    @FXML
    private void aboutWindow(ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("HPO Workbench");
        alert.setHeaderText("Human Phenotype Ontology Workbench");
        String s = "A tool for working with the HPO.\n\u00A9 Monarch Initiative 2021";
        alert.setContentText(s);
        alert.showAndWait();
        e.consume();
    }




    /// for the analysis menu
    @FXML
    private void showHpoStatistics(ActionEvent e) {
        Ontology hpo = optionalHpoResource.getOntology();
        if (hpo == null) {
            logger.error("Attempt to show HPO stats before initializing HPO ontology object");
            return;
        }
        try {
            HpoStats stats = new HpoStats(hpo, optionalHpoaResource.getId2diseaseModelMap());
            Stage stage = (Stage) this.copyrightLabel.getScene().getWindow();
            String html = HpoStatsHtmlGenerator.getHTML(stats);
            WebViewerPopup popup = WebViewerFactory.hpoStats(html, stage);
            popup.popup();

        } catch (HPOException ex) {
            PopUps.showException("Error","Could not retrieve HPO Stats",ex);
        }
        e.consume();
    }


    @FXML
    private void showEntriesNeedingMoreAnnotations(ActionEvent e) {
        e.consume();
        Ontology hpo = optionalHpoResource.getOntology();
        if (hpo == null) {
            logger.error("Attempt to show HPO stats before initializing HPO ontology object");
            return;
        }
        AnnotationTlc tlc = new AnnotationTlc(hpo, optionalHpoaResource.getId2diseaseModelMap());
        String html = AnnotationTlcHtmlGenerator.getHTML(tlc);
        Stage stage = (Stage) this.copyrightLabel.getScene().getWindow();
        WebViewerPopup popup = WebViewerFactory.entriesNeedingMoreAnnotations(html, stage);
        popup.popup();
    }



    @FXML
    private void showEntriesNeedingMoreSpecificAnnotation(ActionEvent e) {
        e.consume();
        Ontology hpo = optionalHpoResource.getOntology();
        if (hpo == null) {
            logger.error("Attempt to show HPO stats before initializing HPO ontology object");
            return;
        }
        if (optionalHpoaResource.getId2diseaseModelMap() == null) {
            logger.error("Attempt to show HPO stats but id2diseaseModel map was empty");
            return;
        }
        AnnotationTlc tlc = new AnnotationTlc(hpo, optionalHpoaResource.getId2diseaseModelMap());
        String html = AnnotationTlcHtmlGenerator.getHTMLSpecificTerms(tlc);
        Stage stage = (Stage) this.copyrightLabel.getScene().getWindow();
        WebViewerPopup popup = WebViewerFactory.entriesNeedingSpecificAnnotations(html, stage);
        popup.popup();

    }

    // from HPO Tab Controller

    @FXML
    public void goButtonAction() {
        TermId id = ontologyLabelsAndTermIdMap.get(autocompleteTextfield.getText());
        if (id == null) return; // button was clicked while field was hasTermsUniqueToOnlyOneDisease, no need to do anything
        Ontology hpo = optionalHpoResource.getOntology();
        if (hpo == null) {
            logger.error("goButtonAction: hpo is null");
            return;
        }
        Term term = hpo.getTermMap().get(id);
        if (term == null) {
            logger.error("Could not retrieve HPO term from {}", id.getValue());
            return;
        }
        expandUntilTerm(term);
        autocompleteTextfield.clear();
    }

    /**
     * Export a hierarchical summary of part of the HPO as an Excel file.
     */
    @FXML
    public void exportHierarchicalSummary() {
        if (selectedTerm == null && getSelectedTerm()!=null) {
            selectedTerm = getSelectedTerm().getValue().term;
        }
        if (selectedTerm == null) { // should only happen if the user hasn't selected anything at all.
            logger.error("Select a term before exporting hierarchical summary TODO show error window");
            PopUps.showInfoMessage("Please select an HPO term in order to export a term with its subhierarchy",
                    "Error: No HPO Term selected");
            return; // to do throw exceptio
        }

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
        Ontology hpo = optionalHpoResource.getOntology();
        if (hpo == null) {
            logger.error("HPO was null (not initialized)");
            return;
        }
        HierarchicalExcelExporter exporter = new HierarchicalExcelExporter(hpo, selectedTerm);
        try {
            exporter.exportToExcel(f.getAbsolutePath());
        } catch (HPOException e) {
            PopUps.showException("Error", "could not export excel file", e);
        }
    }

    /**
     * Export the entire HPO ontology as an excel file.
     */
    @FXML
    public void exportToExcel() {
        logger.trace("exporting to excel");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export HPO as Excel-format file");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel file (*.xlsx)", "*.xlsx");
        chooser.getExtensionFilters().add(extFilter);
        chooser.setInitialFileName("hpo.xlsx");
        File f = chooser.showSaveDialog(null);
        if (f != null) {
            Ontology hpo = optionalHpoResource.getOntology();
            if (hpo == null) {
                logger.error("HPO was null (not initialized)");
                return;
            }
            String path = f.getAbsolutePath();
            logger.trace(String.format("Setting path to export HPO as excel file at: %s", path));
            Hpo2ExcelExporter exporter = new Hpo2ExcelExporter(hpo);
            exporter.exportToExcelFile(path);
        } else {
            logger.error("Unable to obtain path to Excel export file");
        }
    }

    /** Function is called once all of the resources are found (hp obo, disease annotations, mondo). */
    public void activateOntologyTree() {
            if (optionalHpoResource.getOntology() == null) {
                logger.error("activateOntologyTree: HPO null");
            } else {
                final Ontology hpo = optionalHpoResource.getOntology();
                Platform.runLater(()->{
                    initTree(hpo, k -> System.out.println("Consumed " + k));
                    WidthAwareTextFields.bindWidthAwareAutoCompletion(autocompleteTextfield, ontologyLabelsAndTermIdMap.keySet());
                });
            }
    }

    /**
     * Find the path from the root term to given {@link Term}, expand the tree and set the selection model of the
     * TreeView to the term position.
     *
     * @param term {@link Term} to be displayed
     */
    private void expandUntilTerm(Term term) {
        // logger.trace("expand until term " + term.toString());
        Ontology ontology = optionalHpoResource.getOntology();
        if (ontology == null) {
            logger.error("expandUntilTerm not possible because ontology was null");
            return;
        }
        if (existsPathFromRoot(term)) {
            // find root -> term path through the tree
            Stack<Term> termStack = new Stack<>();
            termStack.add(term);
            Set<Term> parents = getTermParents(term);
            while (parents.size() != 0) {
                Term parent = parents.iterator().next();
                termStack.add(parent);
                parents = getTermParents(parent);
            }

            // expand tree nodes in top -> down direction
            List<TreeItem<OntologyTermWrapper>> children = ontologyTreeView.getRoot().getChildren();
            termStack.pop(); // get rid of 'All' node which is hidden
            TreeItem<OntologyTermWrapper> target = ontologyTreeView.getRoot();
            while (!termStack.empty()) {
                Term current = termStack.pop();
                for (TreeItem<OntologyTermWrapper> child : children) {
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
            TermId rootId = ontology.getRootTermId();
            Term rootTerm = ontology.getTermMap().get(rootId);
            logger.warn(String.format("Unable to find the path from %s to %s", rootTerm.toString(), term.getName()));
        }
        selectedTerm = term;
    }


    /**
     * Update content of the {@link #infoWebView} with currently selected {@link Term}.
     *
     * @param treeItem currently selected {@link TreeItem} containing {@link Term}
     */
    private void updateDescription(TreeItem<OntologyTermWrapper> treeItem) {
        if (treeItem == null)
            return;
        Term term = treeItem.getValue().term;
        if (optionalHpoaResource.getIndirectAnnotMap() == null) {
            logger.error("Attempt to get Indirect annotation map but it was null");
            return;
        }
        List<HpoDisease> annotatedDiseases =  optionalHpoaResource.getIndirectAnnotMap().getOrDefault(term.id(), List.of());
        int n_descendents = 42;//getDescendents(model.getHpoOntology(),term.getId()).size();
        //todo--add number of descendents to HTML
        String content = HpoHtmlPageGenerator.getHTML(term, annotatedDiseases);
        //System.out.print(content);
        // infoWebEngine=this.infoWebView.getEngine();
        infoWebEngine.loadContent(content);
        infoWebEngine.getLoadWorker().stateProperty().addListener(// ChangeListener<Worker.State>
                (observableValue, oldState, newState) -> {
                    logger.trace("TOP OF CHANGED  UPDATE DESCRIPTION");
                    if (newState == Worker.State.SUCCEEDED) {
                        org.w3c.dom.events.EventListener listener = // EventListener
                                (event) -> {
                                    String domEventType = event.getType();
                                    // System.err.println("EventType FROM updateHPO: " + domEventType);
                                    if (domEventType.equals(EVENT_TYPE_CLICK)) {
                                        String href = ((Element) event.getTarget()).getAttribute("href");
                                        // System.out.println("HREF "+href);
                                        if (href.equals("http://www.human-phenotype-ontology.org")) {
                                            return; // the external link is taken care of by the Webengine
                                            // therefore, we do not need to do anything special here
                                        }
                                    }
                                };

                        Document doc = infoWebView.getEngine().getDocument();
                        NodeList nodeList = doc.getElementsByTagName("a");
                        for (int i = 0; i < nodeList.getLength(); i++) {
                            ((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_CLICK, listener, false);
                        }
                    }
                });

    }

    /**
     * Initialize the ontology browser-tree in the left column of the app.
     *
     * @param ontology Reference to the HPO or Mondo
     * @param addHook  function hook (currently unused)
     */
    private void initTree(Ontology ontology, Consumer<Term> addHook) {
        // populate the TreeView with top-level elements from ontology hierarchy
        if (ontology == null) {
            ontologyTreeView.setRoot(null);
            return;
        }
        TermId rootId = ontology.getRootTermId();
        Term rootTerm = ontology.getTermMap().get(rootId);
        TreeItem<OntologyTermWrapper> root = new OntologyTermTreeItem(new OntologyTermWrapper(rootTerm));
        root.setExpanded(true);
        ontologyTreeView.setShowRoot(false);
        ontologyTreeView.setRoot(root);

        ontologyTreeView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    OntologyTermWrapper w;
                    if (newValue == null) {
                        // nothing selected so start are the root.
                        w = new OntologyTermWrapper(rootTerm);
                    } else {
                        w = newValue.getValue();
                    }
                    TreeItem<OntologyTermWrapper> item = new OntologyTermTreeItem(w);
                    updateDescription(item);
                });
        // create Map for lookup of the terms in the ontology based on their Name
        ontology.getTermMap().values().forEach(term -> {
            ontologyLabelsAndTermIdMap.put(term.getName(), term.id());
            ontologyLabelsAndTermIdMap.put(term.id().getValue(), term.id());
        });
        WidthAwareTextFields.bindWidthAwareAutoCompletion(autocompleteTextfield, ontologyLabelsAndTermIdMap.keySet());

        // show intro message in the infoWebView
        Platform.runLater(() -> {
            infoWebEngine = infoWebView.getEngine();
            infoWebEngine.loadContent("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>HPO tree browser</title></head>" +
                    "<body><p>Click on HPO term in the tree browser to display additional information</p></body></html>");
        });
    }

    /**
     * Get currently selected Term. Used in tests.
     *
     * @return {@link OntologyTermTreeItem} that is currently selected
     */
    private OntologyTermTreeItem getSelectedTerm() {
        return (ontologyTreeView.getSelectionModel().getSelectedItem() == null) ? null
                : (OntologyTermTreeItem) ontologyTreeView.getSelectionModel().getSelectedItem();
    }

    /**
     * Get the children of "term"
     *
     * @param term HPO Term of interest
     * @return children of term (not including term itself).
     */
    private Set<Term> getTermChildren(Term term) {
        Ontology  ontology = optionalHpoResource.getOntology();
        if (ontology == null) {
            logger.error("Ontology null");
            PopUps.showInfoMessage("Error: Could not initialize Ontology", "ERROR");
            return Set.of();
        }
        TermId parentTermId = term.id();
        Set<TermId> childrenIds = getChildTerms(ontology, parentTermId, false);
        Set<Term> kids = new HashSet<>();
        childrenIds.forEach(tid -> {
            Term ht = ontology.getTermMap().get(tid);
            kids.add(ht);
        });
        return kids;
    }

    /**
     * Get the parents of "term"
     *
     * @param term HPO Term of interest
     * @return parents of term (not including term itself).
     */
    private Set<Term> getTermParents(Term term) {
        Ontology ontology = optionalHpoResource.getOntology();
        if (ontology == null) {
            logger.error("Ontology null");
            PopUps.showInfoMessage("Error: Could not initialize Ontology", "ERROR");
            return Set.of();
        }
        Set<TermId> parentIds = getParentTerms(ontology, term.id(), false);
        Set<Term> eltern = new HashSet<>();
        parentIds.forEach(tid -> {
            Term ht = ontology.getTermMap().get(tid);
            eltern.add(ht);
        });
        return eltern;
    }

    private boolean existsPathFromRoot(Term term) {
        Ontology ontology = optionalHpoResource.getOntology();
        if (ontology == null) {
            logger.error("Ontology null");
            PopUps.showInfoMessage("Error: Could not initialize Ontology", "ERROR");
            return false;
        }
        return existsPath(ontology, term.id(), ontology.getRootTermId());
    }


    /**
     * Inner class that defines a bridge between hierarchy of {@link Term}s and {@link TreeItem}s of the
     * {@link TreeView}.
     */
    class OntologyTermTreeItem extends TreeItem<OntologyTermWrapper> {
        /** List used for caching of the children of this term */
        private ObservableList<TreeItem<OntologyTermWrapper>> childrenList;
        /**
         * Default & only constructor for the TreeItem.
         *
         * @param term {@link Term} that is represented by this TreeItem
         */
        OntologyTermTreeItem(OntologyTermWrapper term) {
            super(term);
        }
        /**
         * Check that the {@link Term} that is represented by this TreeItem is a leaf term as described below.
         * <p>
         * {@inheritDoc}
         */
        @Override
        public boolean isLeaf() {
            return getTermChildren(getValue().term).size() == 0;
        }


        /**
         * Get list of children of the {@link Term} that is represented by this TreeItem.
         * {@inheritDoc}
         */
        @Override
        public ObservableList<TreeItem<OntologyTermWrapper>> getChildren() {
            if (childrenList == null) {
                // logger.debug(String.format("Getting children for term %s", getValue().term.getName()));
                childrenList = FXCollections.observableArrayList();
                Set<Term> children = getTermChildren(getValue().term);
                children.stream()
                        .sorted(Comparator.comparing(Term::getName))
                        .map(term -> new OntologyTermTreeItem(new OntologyTermWrapper(term)))
                        .forEach(childrenList::add);
                super.getChildren().setAll(childrenList);
            }
            return super.getChildren();
        }
    }

}
