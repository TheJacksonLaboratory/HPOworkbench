package org.monarchinitiative.hpoworkbench.controller;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.exception.HPOWorkbenchException;
import org.monarchinitiative.hpoworkbench.github.GitHubPoster;
import org.monarchinitiative.hpoworkbench.gui.GitHubPopup;
import org.monarchinitiative.hpoworkbench.gui.PopUps;
import org.monarchinitiative.hpoworkbench.gui.WidthAwareTextFields;
import org.monarchinitiative.hpoworkbench.resources.OptionalResources;
import org.monarchinitiative.phenol.formats.hpo.HpoAnnotation;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.ontology.data.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;

import javax.inject.Inject;
import javax.inject.Named;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.*;
import java.util.List;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.*;


/**
 * Controller for the Tab pane that shows Mondo
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public final class MondoController {
    private static final Logger logger = LogManager.getLogger();

    private final OptionalResources optionalResources;

    private final TermId MONDO_ROOT_ID = TermId.of("MONDO:0000001");

    /**
     * Unused, but still required.
     */
    private final File hpoWorkbenchDir;
    private static final String EVENT_TYPE_CLICK = "click";
    private static final String EVENT_TYPE_MOUSEOVER = "mouseover";
    private static final String EVENT_TYPE_MOUSEOUT = "mouseclick";
    //TODO migrate these variables into somewhere else, they are now duplicated between the HPO and MONDO controller
    /**
     * Users can create a github issue. Username and password will be stored for the current session only.
     */
    private String githubUsername = null;
    /**
     * Github password. Username and password will be stored for the current session only.
     */
    private String githubPassword;
    /** Holds the generated HTML string that is displayed in the JavaFX browser. */
    private String htmlContent=null;


    /**
     * Application-specific properties (not the System properties!) defined in the 'application.properties' file that
     * resides in the classpath.
     */
    private final Properties properties;

    /**
     * Reference to the primary stage of the App.
     */
    private final Stage primaryStage;

    @FXML
    private RadioButton hpoTermRadioButton;

    public RadioButton diseaseRadioButton;

    public RadioButton newAnnotationRadioButton;

    public Button goButton;
    @FXML
    public Button copyToClipboardButton;

    public Button exportToExcelButton;

    public Button suggestCorrectionToTermButton;

    public Button suggestNewChildTermButton;

    public Button suggestNewAnnotationButton;

    public Button reportMistakenAnnotationButton;

    public RadioButton allDatabaseButton;

    public RadioButton orphanetButton;

    public RadioButton omimButton;

    public RadioButton decipherButton;

    /**
     * The MONDO term that is currently selected in the Browser window.
     */
    private Term selectedTerm = null;

    /**
     * Tree hierarchy of the ontology is presented here.
     */
    @FXML
    private TreeView<GenericTermWrapper> mondoOntologyTreeView;

    /**
     * Key: a term name such as "Myocardial infarction"; value: the corresponding HPO id as a {@link TermId}.
     */
    private final Map<String, TermId> labelsAndMondoIds = new HashMap<>();

    /**
     * Text field with autocompletion for jumping to a particular HPO term in the tree view.
     */
    @FXML
    private TextField searchTextField;

    /**
     * WebView for displaying details of the Term that is selected in the {@link #mondoOntologyTreeView}.
     */
    @FXML
    private WebView infoWebView;

    /**
     * WebEngine backing up the {@link #infoWebView}.
     */
    private WebEngine infoWebEngine;


    @Inject
    public MondoController(OptionalResources optionalResources, Properties properties,
                           @Named("mainWindow") Stage primaryStage, @Named("hpoWorkbenchDir") File hpoWorkbenchDir) {
        this.optionalResources = optionalResources;
        this.properties = properties;
        this.primaryStage = primaryStage;
        this.hpoWorkbenchDir = hpoWorkbenchDir;
    }

    @FXML
    public void initialize() {

        // this binding evaluates to true, if ontology or annotations files are missing (null)
        BooleanBinding mondoResourceIsMissing = optionalResources.mondoResourceMissing();


        logger.error("Initializing MondoController, missing = " + mondoResourceIsMissing.toString());
        hpoTermRadioButton.disableProperty().setValue(false);
        diseaseRadioButton.disableProperty().setValue(false);
        newAnnotationRadioButton.disableProperty().setValue(false);
        goButton.disableProperty().bind(mondoResourceIsMissing);
        copyToClipboardButton.disableProperty().setValue(false);
        exportToExcelButton.disableProperty().setValue(false);
        suggestCorrectionToTermButton.disableProperty().bind(mondoResourceIsMissing);
        suggestNewChildTermButton.disableProperty().setValue(false);
        suggestNewAnnotationButton.disableProperty().setValue(false);
        reportMistakenAnnotationButton.disableProperty().setValue(false);
        allDatabaseButton.disableProperty().setValue(false);
        orphanetButton.disableProperty().setValue(false);
        omimButton.disableProperty().setValue(false);
        decipherButton.disableProperty().setValue(false);

        mondoResourceIsMissing.addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // nothing is missing anymore
                activate();
            } else { // invalidate model and anything in the background. Controls should be disabled automatically
                deactivate();
            }
        });


        if (!mondoResourceIsMissing.get()) {
            activate();
        }
    }

    private void activate() {
        initTree(optionalResources.getMondoOntology());

    }

    private void deactivate() {
        initTree(null);
    }


    /**
     * Initialize the ontology browser-tree in the left column of the app.
     *
     * @param ontology Reference to the HPO
     */
    private void initTree(Ontology ontology) {
        // populate the TreeView with top-level elements from ontology hierarchy
        if (ontology == null) {
            mondoOntologyTreeView.setRoot(null);
            return;
        }
        TermId rootId = TermId.of("MONDO:0000001");
        logger.trace("Initializing Mondo tree with root id {}", rootId.getValue());
        Term rootTerm = ontology.getTermMap().get(rootId);
        if (rootTerm == null) {
            logger.error("Mondo root term was null");
            return;
        }
        logger.trace("Initializing Mondo tree with RootTerm {}", rootTerm.toString());
        TreeItem<GenericTermWrapper> root = new MondoController.GenericTermTreeItem(new GenericTermWrapper(rootTerm));
        root.setExpanded(true);
        mondoOntologyTreeView.setShowRoot(false);
        mondoOntologyTreeView.setRoot(root);
        mondoOntologyTreeView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        logger.error("New value is null");
                        return;
                    }
                    GenericTermWrapper w = newValue.getValue();
                    TreeItem<GenericTermWrapper> item = new MondoController.GenericTermTreeItem(w);
                    updateMondoDescription(item);
                });
        // create Map for lookup of the terms in the ontology based on their Name
        ontology.getTermMap().values().forEach(term -> {
            labelsAndMondoIds.put(term.getName(), term.getId());
            labelsAndMondoIds.put(term.getId().getValue(), term.getId());
        });
        WidthAwareTextFields.bindWidthAwareAutoCompletion(searchTextField, labelsAndMondoIds.keySet());

        // show intro message in the infoWebView
        Platform.runLater(() -> {
            infoWebEngine = infoWebView.getEngine();
            infoWebEngine.loadContent("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>MONDO " +
                    "tree browser</title></head>" +
                    "<body><p>Click on MONDO term in the tree browser to display additional " +
                    "information</p></body></html>");
        });
    }


    private TermId getOMIMid(Term gterm) {
        List<Dbxref> dbxlst = gterm.getXrefs();
        if (dbxlst == null) return null;
        for (Dbxref dbx : dbxlst) {
//           logger.trace("Name=" + dbx.getName());
//           logger.trace("Description = "+ dbx.getDescription());
            if (dbx.getName().startsWith("OMIM:"))
                return TermId.of(dbx.getName());
        }
        return null;
    }

    private TermId getOrphanetid(Term gterm) {
        List<Dbxref> dbxlst = gterm.getXrefs();
        if (dbxlst == null) return null;
        for (Dbxref dbx : dbxlst) {
            if (dbx.getName().startsWith("Orphanet:")) {
                String id = dbx.getName().replaceAll("Orphanet", "ORPHA");
                return TermId.of(id);
            }
        }
        return null;
    }

    /**
     * Update content of the {@link #infoWebView} with currently selected MONDO {@link Term}.
     *
     * @param treeItem currently selected {@link TreeItem} containing {@link Term}
     */
    private void updateMondoDescription(TreeItem<GenericTermWrapper> treeItem) {
        if (treeItem == null)
            return;

        Term mondoTerm = treeItem.getValue().term;
        TermId omimTermId = getOMIMid(mondoTerm);
        TermId orphaTermId = getOrphanetid(mondoTerm);

        Map<TermId, HpoDisease> disease2AnnotationMap = optionalResources.getDisease2AnnotationMap();
        if (disease2AnnotationMap == null) {
            PopUps.showInfoMessage("Error: disease annotation map could not be initialized. Consider restarting the app.", "Error");
            return;
        }
        HpoDisease omimDisease = disease2AnnotationMap.get(omimTermId);
        HpoDisease orphaDisease = disease2AnnotationMap.get(orphaTermId);
        //debugDisease(omimDisease);
        //debugDisease(orphaDisease);

        if (omimDisease == null || orphaDisease == null) {
            logger.warn("Could not init diseases");
        } else {
            logger.trace("Got mim " + omimDisease.toString());
            logger.trace("Got orph " + orphaDisease.toString());
        }


        this.htmlContent = MondoHtmlPageGenerator.getHTML(mondoTerm, omimDisease, orphaDisease, optionalResources.getHpoOntology());
        infoWebEngine.loadContent(this.htmlContent);
        infoWebEngine.getLoadWorker().stateProperty().addListener( // ChangeListener
                (ov, oldState, newState) -> {
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

                                        logger.trace("Got click from webview");
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
                });

    }

    private void debugDisease(HpoDisease disease) {
        System.err.println("DEBUG DISEASE PRINT MONDO CONTROLLER");
        if (disease == null) {
            System.err.println("disease null");
            return;
        }
        System.err.println(disease.getName() + " [" + disease.getDatabase() + ":" + disease.getDiseaseDatabaseId() + "]");
        List<HpoAnnotation> termlist = disease.getPhenotypicAbnormalities();
        for (HpoAnnotation annot : termlist) {
            System.err.println("\t" + annot.toString());
        }
    }


    /**
     * Get the parents of "term"
     *
     * @param term HPO Term of interest
     * @return parents of term (not including term itself).
     */
    private Set<Term> getTermParents(Term term) {
        Ontology ontology = optionalResources.getMondoOntology();
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
        Ontology ontology = optionalResources.getMondoOntology();
        if (ontology == null) {
            PopUps.showInfoMessage("Error: Could not initialize Mondo Ontology", "ERROR");
            return false;
        }
        TermId tid = term.getId();
        return existsPath(ontology, tid, MONDO_ROOT_ID);
    }


    /**
     * Find the path from the root term to given {@link Term}, expand the tree and set the selection model of the
     * TreeView to the term position.
     *
     * @param term {@link Term} to be displayed
     */
    private void expandUntilTerm(Term term) {
        // logger.trace("expand until term " + term.toString());
        // switchToMode(BROWSE_HPO);
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
            List<TreeItem<GenericTermWrapper>> children = mondoOntologyTreeView.getRoot().getChildren();
            termStack.pop(); // get rid of 'All' node which is hidden
            TreeItem<GenericTermWrapper> target = mondoOntologyTreeView.getRoot();
            while (!termStack.empty()) {
                Term current = termStack.pop();
                for (TreeItem<GenericTermWrapper> child : children) {
                    if (child.getValue().term.equals(current)) {
                        child.setExpanded(true);
                        target = child;
                        children = child.getChildren();
                        break;
                    }
                }
            }
            mondoOntologyTreeView.getSelectionModel().select(target);
            mondoOntologyTreeView.scrollTo(mondoOntologyTreeView.getSelectionModel().getSelectedIndex());
        } else {
            TermId rootId = optionalResources.getMondoOntology().getRootTermId();
            Term rootTerm = optionalResources.getMondoOntology().getTermMap().get(rootId);
            logger.warn(String.format("Unable to find the path from %s to %s", rootTerm.toString(), term.getName()));
        }
        selectedTerm = term;
    }

    @FXML
    public void goButtonAction() {
        TermId tid = labelsAndMondoIds.get(searchTextField.getText());
        Term term = optionalResources.getMondoOntology().getTermMap().get(tid);
        if (term == null) {
            String msg = String.format("Could not find ontology term for search result: %s", searchTextField.getText());
            PopUps.showInfoMessage(msg, "Warning");
            return;
        }
        expandUntilTerm(term);
        TreeItem<GenericTermWrapper> titem = new TreeItem<>(new GenericTermWrapper(term));
        updateMondoDescription(titem);
    }

    @FXML
    public void copyHtmlToSystemClipboard(Event e) {
        String str;
        if (this.htmlContent==null) {
            str="no MONDO disease seleced";
        } else {
            str=this.htmlContent;
        }
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        StringSelection strSel = new StringSelection(str);
        clipboard.setContents(strSel, null);
        e.consume();
    }

    /**
     * Get currently selected Term. Used in tests.
     *
     * @return {@link HpoController.HpoTermTreeItem} that is currently selected
     */
    private GenericTermTreeItem getSelectedTerm() {
        return (mondoOntologyTreeView.getSelectionModel().getSelectedItem() == null) ? null
                : (GenericTermTreeItem) mondoOntologyTreeView.getSelectionModel().getSelectedItem();
    }

    private void postGitHubIssue(String message, String title, String uname, String pword) {
        GitHubPoster poster = new GitHubPoster(uname, pword, title, message);
        this.githubUsername = uname;
        this.githubPassword = pword;
        try {
            poster.postMondoIssue();
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
     * Post an issue on the MONDO tracker to suggest a correction to a term.
     */
    @FXML
    private void suggestCorrectionToTerm() {
        if (getSelectedTerm() == null) {
            logger.error("Select a term before creating GitHub issue");
            PopUps.showInfoMessage("Please select a MONDO term before creating GitHub issue",
                    "Error: No MONDO Term selected");
            return;
        } else {
            selectedTerm = getSelectedTerm().getValue().term;
        }
        selectedTerm = getSelectedTerm().getValue().term;
        GitHubPopup popup = new GitHubPopup(selectedTerm);
        // initializeGitHubLabelsIfNecessary();
        // popup.setLabels(model.getGithublabels());
        popup.setupGithubUsernamePassword(githubUsername, githubPassword);
        popup.displayWindow(primaryStage);
        String githubissue = popup.retrieveGitHubIssue();
        if (popup.wasCancelled()) {
            return;
        }
        if (githubissue == null) {
            logger.trace("got back null GitHub issue");
            return;
        }
        String title = String.format("Correction to term %s", selectedTerm.getName());
        postGitHubIssue(githubissue, title, popup.getGitHubUserName(), popup.getGitHubPassWord());
    }


    /**
     * Get the children of "term"
     *
     * @param term HPO Term of interest
     * @return children of term (not including term itself).
     */
    private Set<Term> getTermChildren(Term term) {
        Ontology ontology = optionalResources.getMondoOntology();
        if (ontology == null) {
            PopUps.showInfoMessage("Error: Could not initialize Mondo Ontology", "ERROR");
            return new HashSet<>(); // return hasTermsUniqueToOnlyOneDisease set
        }
        if (term == null) {
            PopUps.showInfoMessage("Error: term==null in getTermChildren", "ERROR");
            return new HashSet<>(); // return hasTermsUniqueToOnlyOneDisease set
        }
        TermId parentTermId = term.getId();
        Set<TermId> childrenIds = getChildTerms(ontology, parentTermId, false);
        Set<Term> kids = new HashSet<>();
        childrenIds.forEach(tid -> {
            Term gt = ontology.getTermMap().get(tid);
            kids.add(gt);
        });
        return kids;
    }


    /**
     * Inner class that defines a bridge between hierarchy of {@link Term}s and {@link TreeItem}s of the
     * {@link TreeView}.
     */
    class GenericTermTreeItem extends TreeItem<GenericTermWrapper> {

        /**
         * List used for caching of the children of this term
         */
        private ObservableList<TreeItem<GenericTermWrapper>> childrenList;

        /**
         * Default & only constructor for the TreeItem.
         *
         * @param term {@link Term} that is represented by this TreeItem
         */
        GenericTermTreeItem(GenericTermWrapper term) {
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
        public ObservableList<TreeItem<GenericTermWrapper>> getChildren() {
            if (childrenList == null) {
                // logger.debug(String.format("Getting children for term %s", getValue().term.getName()));
                childrenList = FXCollections.observableArrayList();
                Set<Term> children = getTermChildren(getValue().term);
                children.stream()
                        .sorted(Comparator.comparing(Term::getName))
                        .map(term -> new MondoController.GenericTermTreeItem(new GenericTermWrapper(term)))
                        .forEach(childrenList::add);
                super.getChildren().setAll(childrenList);
            }
            return super.getChildren();
        }
    }


}
