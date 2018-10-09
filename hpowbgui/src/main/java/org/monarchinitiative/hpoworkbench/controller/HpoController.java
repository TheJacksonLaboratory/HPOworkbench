package org.monarchinitiative.hpoworkbench.controller;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
import org.monarchinitiative.hpoworkbench.gui.GitHubPopup;
import org.monarchinitiative.hpoworkbench.gui.PopUps;
import org.monarchinitiative.hpoworkbench.gui.WidthAwareTextFields;
import org.monarchinitiative.hpoworkbench.model.DiseaseDatabase;
import org.monarchinitiative.hpoworkbench.model.Model;
import org.monarchinitiative.hpoworkbench.resources.OptionalResources;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

import static org.monarchinitiative.hpoworkbench.controller.MainController.mode.*;
import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.*;

/**
 * Controller for the {@link Tab} that displays HPO. The {@link Tab} is a part of the {@link MainController}
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 * @version 0.1.12
 * @since 0.1
 */

public final class HpoController {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String EVENT_TYPE_CLICK = "click";
    private static final String EVENT_TYPE_MOUSEOVER = "mouseover";
    private static final String EVENT_TYPE_MOUSEOUT = "mouseclick";

    private final Stage primaryStage;
    /**
     * Object that stores the ontology data etc. if available.
     */
    private final OptionalResources optionalResources;

    /**
     * Current behavior of HPO Workbench. See {@link MainController.mode}.
     */
    private MainController.mode currentMode = MainController.mode.BROWSE_HPO;
    @FXML
    private TextField hpoAutocompleteTextfield;
    @FXML
    private TextField diseaseAutocompleteTextfield;
    @FXML
    private Button goButton;
    /**
     * The tree view that shows the HPO Ontology hierarchy
     */
    @FXML
    private TreeView<HpoTermWrapper> ontologyTreeView;
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
    private Button exportHierarchicalSummaryButton;
    @FXML
    private Button exportToExcelButton;
    @FXML
    private Button suggestCorrectionToTermButton;
    @FXML
    private Button suggestNewChildTermButton;
    @FXML
    private Button suggestNewAnnotationButton;
    @FXML
    private Button reportMistakenAnnotationButton;
    @FXML
    private RadioButton allDatabaseButton;
    @FXML
    private RadioButton orphanetButton;
    @FXML
    private RadioButton omimButton;
    @FXML
    private RadioButton decipherButton;
    @FXML
    private Label currentDiseaseLabel;
    /**
     * Current disease shown in the browser. Any suggested changes will refer to this disease.
     */
    private HpoDisease selectedDisease = null;
    /**
     * This determines which disease as shown (OMIM, Orphanet, DECIPHER, or all). Default: ALL.
     */
    private DiseaseDatabase selectedDatabase = DiseaseDatabase.ALL;
    /**
     * Key: a term name such as "Myocardial infarction"; value: the corresponding HPO id as a {@link TermId}.
     */
    private final Map<String, TermId> labelsAndHpoIds = new HashMap<>();

    private Model model;
    /**
     * The term that is currently selected in the Browser window.
     */
    private Term selectedTerm = null;
    /**
     * Users can create a github issue. Username and password will be stored for the current session only.
     */
    private String githubUsername = null;
    /**
     * Github password. Username and password will be stored for the current session only.
     */
    private String githubPassword;

    @Inject
    public HpoController(OptionalResources optionalResources, @Named("mainWindow") Stage primaryStage) {
        this.optionalResources = optionalResources;
        this.primaryStage = primaryStage;
    }

    @FXML
    public void goButtonAction() {
            TermId id = labelsAndHpoIds.get(hpoAutocompleteTextfield.getText());
            if (id == null) return; // button was clicked while field was hasTermsUniqueToOnlyOneDisease, no need to do anything
            expandUntilTerm(optionalResources.getHpoOntology().getTermMap().get(id));
            hpoAutocompleteTextfield.clear();
       /* } else if (currentMode == MainController.mode.NEW_ANNOTATION) {
            TermId id = labelsAndHpoIds.get(hpoAutocompleteTextfield.getText());
            if (id == null) return; // button was clicked while field was hasTermsUniqueToOnlyOneDisease, no need to do anything
            expandUntilTerm(optionalResources.getHpoOntology().getTermMap().get(id));
            hpoAutocompleteTextfield.clear();
        }*/
    }

    @FXML
    private void goDiseaseAutocomplete() {
        HpoDisease dmod = model.getDiseases().get(diseaseAutocompleteTextfield.getText());
        if (dmod == null) {
            LOGGER.warn("disease page could not be shown because disease model was null");
            return;
        } else {
            LOGGER.error("got disease "+dmod.getName());
        }
        updateDescriptionToDiseaseModel(dmod);
        selectedDisease = dmod;
        this.currentDiseaseLabel.setText(dmod.getName());
        diseaseAutocompleteTextfield.clear();
    }

    @FXML
    private void clearCurrentDisease() {
        this.currentDiseaseLabel.setText("");
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
            LOGGER.error("Select a term before exporting hierarchical summary TODO show error window");
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
            LOGGER.trace(String.format("Setting path to hierarchical export file to %s", path));
        } else {
            LOGGER.error("Unable to obtain path to Excel export file");
            return;
        }
        LOGGER.trace(String.format("Exporting hierarchical summary starting from term %s", selectedTerm.toString()));
        HierarchicalExcelExporter exporter = new HierarchicalExcelExporter(model.getHpoOntology(), selectedTerm);
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
        LOGGER.trace("exporting to excel");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export HPO as Excel-format file");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel file (*.xlsx)", "*.xlsx");
        chooser.getExtensionFilters().add(extFilter);
        chooser.setInitialFileName("hpo.xlsx");
        File f = chooser.showSaveDialog(null);
        if (f != null) {
            String path = f.getAbsolutePath();
            LOGGER.trace(String.format("Setting path to export HPO as excel file at: %s", path));
            Hpo2ExcelExporter exporter = new Hpo2ExcelExporter(model.getHpoOntology());
            exporter.exportToExcelFile(path);
        } else {
            LOGGER.error("Unable to obtain path to Excel export file");
        }
    }

    @FXML
    public void suggestCorrectionToTerm() {
        if (getSelectedTerm() == null) {
            LOGGER.error("Select a term before creating GitHub issue");
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
        popup.displayWindow(primaryStage);
        String githubissue = popup.retrieveGitHubIssue();
        if (popup.wasCancelled()) {
            return;
        }
        if (githubissue == null) {
            LOGGER.trace("got back null GitHub issue");
            return;
        }
        String title = String.format("Correction to term %s", selectedTerm.getName());
        postGitHubIssue(githubissue, title, popup.getGitHubUserName(), popup.getGitHubPassWord(), popup.getGitHubLabels());
    }

    @FXML
    public void suggestNewChildTerm() {
        if (getSelectedTerm() == null) {
            LOGGER.error("Select a term before creating GitHub issue");
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
        popup.displayWindow(primaryStage);
        String githubissue = popup.retrieveGitHubIssue();
        if (githubissue == null) {
            LOGGER.trace("got back null github issue");
            return;
        }
        String title = String.format("Suggesting new child term of \"%s\"", selectedTerm.getName());
        postGitHubIssue(githubissue, title, popup.getGitHubUserName(), popup.getGitHubPassWord(), popup.getGitHubLabels());
    }

    @FXML
    public void suggestNewAnnotation() {
        if (getSelectedTerm() == null) {
            LOGGER.error("Select a term before creating GitHub issue");
            PopUps.showInfoMessage("Please select an HPO term before creating GitHub issue",
                    "Error: No HPO Term selected");
            return;
        } else {
            selectedTerm = getSelectedTerm().getValue().term;
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
        popup.displayWindow(primaryStage);
        String githubissue = popup.retrieveGitHubIssue();
        if (githubissue == null) {
            LOGGER.trace("got back null GitHub issue");
            return;
        }
        String title = String.format("New annotation suggestion for %s", selectedDisease.getName());
        postGitHubIssue(githubissue, title, popup.getGitHubUserName(), popup.getGitHubPassWord(), popup.getGitHubLabels());
    }

    @FXML
    public void reportMistakenAnnotation() {
        if (getSelectedTerm() == null) {
            LOGGER.error("Select a term before creating GitHub issue");
            PopUps.showInfoMessage("Please select an HPO term before creating GitHub issue",
                    "Error: No HPO Term selected");
            return;
        } else {
            selectedTerm = getSelectedTerm().getValue().term;
        }
        if (!currentMode.equals(MainController.mode.NEW_ANNOTATION)) {
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
        popup.displayWindow(primaryStage);
        String githubissue = popup.retrieveGitHubIssue();
        if (githubissue == null) {
            LOGGER.trace("got back null GitHub issue");
            return;
        }
        String title = String.format("Erroneous annotation for %s", selectedDisease.getName());
        postGitHubIssue(githubissue, title, popup.getGitHubUserName(), popup.getGitHubPassWord());
    }


    public void initialize() {
        //logger.trace("initialize");
//        // This action will be run after user approves a PhenotypeTerm in the ontologyTreePane
//        Consumer<HpoTerm> addHook = (ph -> logger.trace(String.format("Hook for %s", ph.getName())));

        initRadioButtons();

        // this binding evaluates to true, if ontology or annotations files are missing (null)
        BooleanBinding hpoResourceMissing = optionalResources.hpoResourceMissing();

//        hpoTermRadioButton.disableProperty().bind(hpoResourceMissing);
//        diseaseRadioButton.disableProperty().bind(hpoResourceMissing);
//        newAnnotationRadioButton.disableProperty().bind(hpoResourceMissing);

        hpoAutocompleteTextfield.disableProperty().bind(hpoResourceMissing);
        goButton.disableProperty().bind(hpoResourceMissing);
        ontologyTreeView.disableProperty().bind(hpoResourceMissing);

        exportHierarchicalSummaryButton.disableProperty().bind(hpoResourceMissing);
        exportToExcelButton.disableProperty().bind(hpoResourceMissing);
        suggestCorrectionToTermButton.disableProperty().bind(hpoResourceMissing);
        suggestNewChildTermButton.disableProperty().bind(hpoResourceMissing);
        suggestNewAnnotationButton.disableProperty().bind(hpoResourceMissing);
        reportMistakenAnnotationButton.disableProperty().bind(hpoResourceMissing);


        hpoResourceMissing.addListener(((observable, oldValue, newValue) -> {
            if (!newValue) { // nothing is missing anymore
                activate();
            } else { // invalidate model and anything in the background. Controls should be disabled automatically
                deactivate();
            }
        }));


        if (!hpoResourceMissing.get()) {
            activate();
        }

    }
    /** FUnction is called once all of the resources are found (hp obo, disease annotations, mondo). */
    private void activate() {
        initTree(optionalResources.getHpoOntology(), k -> System.out.println("Consumed " + k));
        this.model = new Model(optionalResources.getHpoOntology(), optionalResources.getIndirectAnnotMap(),
                optionalResources.getDirectAnnotMap());
        WidthAwareTextFields.bindWidthAwareAutoCompletion(hpoAutocompleteTextfield, labelsAndHpoIds.keySet());
        WidthAwareTextFields.bindWidthAwareAutoCompletion(diseaseAutocompleteTextfield, model.getDiseases().keySet());
    }

    private void deactivate() {
        initTree(null, k -> System.out.println("Consumed " + k));
        this.model = new Model(null, null, null);
    }


    /**
     * Update content of the {@link #infoWebView} with currently selected {@link Term}.
     * The function is called when the user is on an HPO Term page and selects a link to
     * a disease.
     *
     * @param dmodel currently selected {@link TreeItem} containing {@link Term}
     */
    private void updateDescriptionToDiseaseModel(HpoDisease dmodel) {
        LOGGER.trace("TOP OF updateDescriptionToDiseaseModel");
//        String dbName = dmodel.getDiseaseDatabaseId().getIdWithPrefix();
//        String diseaseName = dmodel.getName();
//        List<Term> annotatingTerms = model.getAnnotationTermsForDisease(dmodel);
        String content = HpoHtmlPageGenerator.getDiseaseHTML(dmodel, optionalResources.getHpoOntology());
        infoWebEngine.loadContent(content);
        infoWebEngine.getLoadWorker().stateProperty().addListener( // ChangeListener<Worker.State>()
                (ov, oldState, newState) -> {
                    LOGGER.trace("TOP OF CHANGED updateDescriptionToDiseaseModel");
                    if (newState == Worker.State.SUCCEEDED) {
                        org.w3c.dom.events.EventListener listener = // EventListener
                                (ev) -> {
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
                                        if (!href.startsWith("HP:")) {
                                            return;
                                        }
                                        TermId tid = TermId.constructWithPrefix(href);
                                        Term term = optionalResources.getHpoOntology().getTermMap().get(tid);
                                        if (term == null) {
                                            LOGGER.error(String.format("Could not construct term  from termid \"%s\"", tid.getIdWithPrefix()));
                                            return;
                                        }
                                        // set the tree on the left to our new term
                                        expandUntilTerm(term);
                                        // update the Webview browser
                                        LOGGER.trace("ABOUT TO UPDATE DESCRIPTION FOR " + term.getName());
                                        updateDescription(new HpoTermTreeItem(new HpoTermWrapper(term)));
                                        hpoAutocompleteTextfield.clear();
                                        currentMode = MainController.mode.BROWSE_HPO;
                                        //hpoTermRadioButton.setSelected(true);
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
     * Find the path from the root term to given {@link Term}, expand the tree and set the selection model of the
     * TreeView to the term position.
     *
     * @param term {@link Term} to be displayed
     */
    private void expandUntilTerm(Term term) {
        // logger.trace("expand until term " + term.toString());
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
            List<TreeItem<HpoTermWrapper>> children = ontologyTreeView.getRoot().getChildren();
            termStack.pop(); // get rid of 'All' node which is hidden
            TreeItem<HpoTermWrapper> target = ontologyTreeView.getRoot();
            while (!termStack.empty()) {
                Term current = termStack.pop();
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
            TermId rootId = optionalResources.getHpoOntology().getRootTermId();
            Term rootTerm = optionalResources.getHpoOntology().getTermMap().get(rootId);
            LOGGER.warn(String.format("Unable to find the path from %s to %s", rootTerm.toString(), term.getName()));
        }
        selectedTerm = term;
    }


    /**
     * Update content of the {@link #infoWebView} with currently selected {@link Term}.
     *
     * @param treeItem currently selected {@link TreeItem} containing {@link Term}
     */
    private void updateDescription(TreeItem<HpoTermWrapper> treeItem) {
        LOGGER.trace("TOP OF UPDATE DESCRIPTION");
        if (currentMode.equals(MainController.mode.NEW_ANNOTATION)) {
            return;
        }
        if (treeItem == null)
            return;

        Term term = treeItem.getValue().term;
        String termID = term.getId().getIdWithPrefix();
        List<HpoDisease> annotatedDiseases = model.getDiseaseAnnotations(termID, selectedDatabase);
        if (annotatedDiseases == null) {
            LOGGER.error("could not retrieve diseases for " + termID);
        }
        int n_descendents = 42;//getDescendents(model.getHpoOntology(),term.getId()).size();
        //todo--add number of descendents to HTML
        String content = HpoHtmlPageGenerator.getHTML(term, annotatedDiseases);
        //System.out.print(content);
        // infoWebEngine=this.infoWebView.getEngine();
        infoWebEngine.loadContent(content);
        infoWebEngine.getLoadWorker().stateProperty().addListener(// ChangeListener<Worker.State>
                (observableValue, oldState, newState) -> {
                    LOGGER.trace("TOP OF CHANGED  UPDATE DESCRIPTION");
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
                                        // The following line is necessary because sometimes multiple events are triggered
                                        // and we get a "stray" HPO-related link that does not belong here.
                                        if (href.startsWith("HP:")) return;
                                        HpoDisease dmod = model.getDiseases().get(href);
                                        if (dmod == null) {
                                            LOGGER.error("Link to disease model for " + href + " was null");
                                            return;
                                        }
                                        updateDescriptionToDiseaseModel(dmod);
                                        selectedDisease = dmod;
                                        hpoAutocompleteTextfield.clear();
                                        currentMode = BROWSE_DISEASE;
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
     * @param ontology Reference to the HPO
     * @param addHook  function hook (currently unused)
     */
    private void initTree(HpoOntology ontology, Consumer<Term> addHook) {
        // populate the TreeView with top-level elements from ontology hierarchy
        if (ontology == null) {
            ontologyTreeView.setRoot(null);
            return;
        }
        TermId rootId = ontology.getRootTermId();
        Term rootTerm = ontology.getTermMap().get(rootId);
        TreeItem<HpoTermWrapper> root = new HpoTermTreeItem(new HpoTermWrapper(rootTerm));
        root.setExpanded(true);
        ontologyTreeView.setShowRoot(false);
        ontologyTreeView.setRoot(root);
        ontologyTreeView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        LOGGER.error("New value is null");
                        return;
                    }
                    HpoTermWrapper w = newValue.getValue();
                    TreeItem<HpoTermWrapper> item = new HpoTermTreeItem(w);
                    updateDescription(item);
                });
        // create Map for lookup of the terms in the ontology based on their Name
        ontology.getTermMap().values().forEach(term -> {
            labelsAndHpoIds.put(term.getName(), term.getId());
            labelsAndHpoIds.put(term.getId().getIdWithPrefix(), term.getId());
        });
        WidthAwareTextFields.bindWidthAwareAutoCompletion(hpoAutocompleteTextfield, labelsAndHpoIds.keySet());

        // show intro message in the infoWebView
        Platform.runLater(() -> {
            infoWebEngine = infoWebView.getEngine();
            infoWebEngine.loadContent("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>HPO tree browser</title></head>" +
                    "<body><p>Click on HPO term in the tree browser to display additional information</p></body></html>");
        });
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
                            LOGGER.warn("Could not retrieve user data for database radio buttons");
                        }
                        if (userdata==null) return;
                        switch (userdata) {
                            case "orphanet":
                                selectedDatabase = DiseaseDatabase.ORPHANET;
                                break;
                            case "omim":
                                selectedDatabase = DiseaseDatabase.OMIM;
                                break;
                            case "all":
                                selectedDatabase = DiseaseDatabase.ALL;
                                break;
                            case "decipher":
                                selectedDatabase = DiseaseDatabase.DECIPHER;
                                break;
                            default:
                                LOGGER.warn("did not recognize database " + userdata);
                                break;
                        }
                    }
                });

    }


    /**
     * For the GitHub new issues, we want to allow the user to choose a pre-existing label for the issue.
     * For this, we first go to GitHub and retrieve the labelsAndHpoIds with
     * {@link org.monarchinitiative.hpoworkbench.github.GitHubLabelRetriever}. We only do this
     * once per session though.
     */
    private void initializeGitHubLabelsIfNecessary() {
        if (model.hasLabels()) {
            return; // we only need to retrieve the labelsAndHpoIds from the server once per session!
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

    private void postGitHubIssue(String message, String title, String uname, String pword) {
        GitHubPoster poster = new GitHubPoster(uname, pword, title, message);
        this.githubUsername = uname;
        this.githubPassword = pword;
        try {
            poster.postHpoIssue();
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

    private void postGitHubIssue(String message, String title, String uname, String pword, List<String> labels) {
        GitHubPoster poster = new GitHubPoster(uname, pword, title, message);
        this.githubUsername = uname;
        this.githubPassword = pword;
        if (labels != null && !labels.isEmpty()) {
            poster.setLabel(labels);
        }
        try {
            poster.postHpoIssue();
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
     * Get the children of "term"
     *
     * @param term HPO Term of interest
     * @return children of term (not including term itself).
     */
    private Set<Term> getTermChildren(Term term) {
        HpoOntology ontology = optionalResources.getHpoOntology();
        if (ontology == null) {
            PopUps.showInfoMessage("Error: Could not initialize HPO Ontology", "ERROR");
            return new HashSet<>();
        }
        TermId parentTermId = term.getId();
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
        HpoOntology ontology = optionalResources.getHpoOntology();
        if (ontology == null) {
            PopUps.showInfoMessage("Error: Could not initialize HPO Ontology", "ERROR");
            return new HashSet<>(); // return hasTermsUniqueToOnlyOneDisease set
        }
        Set<TermId> parentIds = getParentTerms(ontology, term.getId(), false);
        Set<Term> eltern = new HashSet<>();
        parentIds.forEach(tid -> {
            Term ht = ontology.getTermMap().get(tid);
            eltern.add(ht);
        });
        return eltern;
    }

    private boolean existsPathFromRoot(Term term) {
        HpoOntology ontology = optionalResources.getHpoOntology();
        if (ontology == null) {
            PopUps.showInfoMessage("Error: Could not initialize HPO Ontology", "ERROR");
            return false;
        }
        TermId rootId = ontology.getRootTermId();
        TermId tid = term.getId();
        return existsPath(ontology, tid, rootId);
    }


    /**
     * Inner class that defines a bridge between hierarchy of {@link Term}s and {@link TreeItem}s of the
     * {@link TreeView}.
     */
    class HpoTermTreeItem extends TreeItem<HpoTermWrapper> {
        /** List used for caching of the children of this term */
        private ObservableList<TreeItem<HpoTermWrapper>> childrenList;

        /**
         * Default & only constructor for the TreeItem.
         *
         * @param term {@link Term} that is represented by this TreeItem
         */
        HpoTermTreeItem(HpoTermWrapper term) {
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
        public ObservableList<TreeItem<HpoTermWrapper>> getChildren() {
            if (childrenList == null) {
                // logger.debug(String.format("Getting children for term %s", getValue().term.getName()));
                childrenList = FXCollections.observableArrayList();
                Set<Term> children = getTermChildren(getValue().term);
                children.stream()
                        .sorted(Comparator.comparing(Term::getName))
                        .map(term -> new HpoTermTreeItem(new HpoTermWrapper(term)))
                        .forEach(childrenList::add);
                super.getChildren().setAll(childrenList);
            }
            return super.getChildren();
        }
    }
}
