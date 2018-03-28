package org.monarchinitiative.hpoworkbench.controller;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.gui.PopUps;
import org.monarchinitiative.hpoworkbench.gui.WidthAwareTextFields;
import org.monarchinitiative.hpoworkbench.resources.OptionalResources;
import org.monarchinitiative.phenol.formats.generic.GenericRelationship;
import org.monarchinitiative.phenol.formats.generic.GenericTerm;
import org.monarchinitiative.phenol.formats.hpo.HpoTerm;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.*;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.*;


/**
 * Controller for the Tab pane that shows Mondo
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public final class MondoController {

    private static final Logger logger = LogManager.getLogger();

    private final OptionalResources optionalResources;

    /** Unused, but still required. */
    private final File hpoWorkbenchDir;

    /**
     * Application-specific properties (not the System properties!) defined in the 'application.properties' file that
     * resides in the classpath.
     */
    private final Properties properties;

    /** Reference to the primary stage of the App. */
    private final Stage primaryStage;

    @FXML
    public RadioButton hpoTermRadioButton;

    public RadioButton diseaseRadioButton;

    public RadioButton newAnnotationRadioButton;

    public Button goButton;

    public Button exportHierarchicalSummaryButton;

    public Button exportToExcelButton;

    public Button suggestCorrectionToTermButton;

    public Button suggestNewChildTermButton;

    public Button suggestNewAnnotationButton;

    public Button reportMistakenAnnotationButton;

    public RadioButton allDatabaseButton;

    public RadioButton orphanetButton;

    public RadioButton omimButton;

    public RadioButton decipherButton;

    /** The MONDO term that is currently selected in the Browser window. */
    private GenericTerm selectedTerm = null;

    /** Tree hierarchy of the ontology is presented here. */
    @FXML
    private TreeView<GenericTermWrapper> mondoOntologyTreeView;

    /** Key: a term name such as "Myocardial infarction"; value: the corresponding HPO id as a {@link TermId}. */
    private Map<String, TermId> labelsAndMondoIds = new HashMap<>();

    /** Text field with autocompletion for jumping to a particular HPO term in the tree view. */
    @FXML
    private TextField searchTextField;

    /** WebView for displaying details of the Term that is selected in the {@link #mondoOntologyTreeView}. */
    @FXML
    private WebView infoWebView;

    /** WebEngine backing up the {@link #infoWebView}. */
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
        hpoTermRadioButton.disableProperty().bind(mondoResourceIsMissing);
        diseaseRadioButton.disableProperty().bind(mondoResourceIsMissing);
        newAnnotationRadioButton.disableProperty().bind(mondoResourceIsMissing);
        goButton.disableProperty().bind(mondoResourceIsMissing);
        exportHierarchicalSummaryButton.disableProperty().bind(mondoResourceIsMissing);
        exportToExcelButton.disableProperty().bind(mondoResourceIsMissing);
        suggestCorrectionToTermButton.disableProperty().bind(mondoResourceIsMissing);
        suggestNewChildTermButton.disableProperty().bind(mondoResourceIsMissing);
        suggestNewAnnotationButton.disableProperty().bind(mondoResourceIsMissing);
        reportMistakenAnnotationButton.disableProperty().bind(mondoResourceIsMissing);
        allDatabaseButton.disableProperty().bind(mondoResourceIsMissing);
        orphanetButton.disableProperty().bind(mondoResourceIsMissing);
        omimButton.disableProperty().bind(mondoResourceIsMissing);
        decipherButton.disableProperty().bind(mondoResourceIsMissing);

        mondoResourceIsMissing.addListener(((observable, oldValue, newValue) -> {
            if (!newValue) { // nothing is missing anymore
                activate();
            } else { // invalidate model and anything in the background. Controls should be disabled automatically
                deactivate();
            }
            System.out.println("MONDO LISTEBNER old="+oldValue+" new="+newValue);
        }));


        if (!mondoResourceIsMissing.get()) {
            activate();
        }
    }

    private void activate() {
        initTree(optionalResources.getMondoOntology());

    }

    private void deactivate() {
        initTree(null);
        //this.model = new Model(null, null, null);
    }


    /**
     * Initialize the ontology browser-tree in the left column of the app.
     *
     * @param ontology Reference to the HPO
     */
    private void initTree(Ontology<GenericTerm, GenericRelationship> ontology) {
        // populate the TreeView with top-level elements from ontology hierarchy
        if (ontology == null) {
            mondoOntologyTreeView.setRoot(null);
            return;
        }
        TermId rootId = ontology.getRootTermId();
        logger.trace("root id = " + rootId.getIdWithPrefix());
        GenericTerm rootTerm = ontology.getTermMap().get(rootId);
        logger.trace("RootTerm is " + rootTerm.toString());
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
                    TreeItem item = new MondoController.GenericTermTreeItem(w);
                    // updateDescription(item);
                });
        // create Map for lookup of the terms in the ontology based on their Name
        ontology.getTermMap().values().forEach(term -> {
            labelsAndMondoIds.put(term.getName(), term.getId());
            labelsAndMondoIds.put(term.getId().getIdWithPrefix(), term.getId());
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


    /**
     * Get the parents of "term"
     *
     * @param term HPO Term of interest
     *
     * @return parents of term (not including term itself).
     */
    private Set<GenericTerm> getTermParents(GenericTerm term) {
        Ontology ontology = optionalResources.getMondoOntology();
        if (ontology == null) {
            PopUps.showInfoMessage("Error: Could not initialize HPO Ontology", "ERROR");
            return new HashSet<>(); // return empty set
        }
        Set<TermId> parentIds = getParentTerms(ontology, term.getId(), false);
        Set<GenericTerm> eltern = new HashSet<>();
        parentIds.forEach(tid -> {
            GenericTerm ht = (GenericTerm) ontology.getTermMap().get(tid);
            eltern.add(ht);
        });
        return eltern;
    }

    private boolean existsPathFromRoot(GenericTerm term) {
        Ontology ontology = optionalResources.getMondoOntology();
        if (ontology == null) {
            PopUps.showInfoMessage("Error: Could not initialize Mondo Ontology", "ERROR");
            return false;
        }
        TermId rootId = ontology.getRootTermId();
        TermId tid = term.getId();
        return existsPath(ontology, tid, rootId);
    }


    /**
     * Find the path from the root term to given {@link HpoTerm}, expand the tree and set the selection model of the
     * TreeView to the term position.
     *
     * @param term {@link HpoTerm} to be displayed
     */
    private void expandUntilTerm(GenericTerm term) {
        // logger.trace("expand until term " + term.toString());
        // switchToMode(BROWSE_HPO);
        if (existsPathFromRoot(term)) {
            // find root -> term path through the tree
            Stack<GenericTerm> termStack = new Stack<>();
            termStack.add(term);
            Set<GenericTerm> parents = getTermParents(term);
            while (parents.size() != 0) {
                GenericTerm parent = parents.iterator().next();
                termStack.add(parent);
                parents = getTermParents(parent);
            }

            // expand tree nodes in top -> down direction
            List<TreeItem<GenericTermWrapper>> children = mondoOntologyTreeView.getRoot().getChildren();
            termStack.pop(); // get rid of 'All' node which is hidden
            TreeItem<GenericTermWrapper> target = mondoOntologyTreeView.getRoot();
            while (!termStack.empty()) {
                GenericTerm current = termStack.pop();
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
            GenericTerm rootTerm = optionalResources.getMondoOntology().getTermMap().get(rootId);
            logger.warn(String.format("Unable to find the path from %s to %s", rootTerm.toString(), term.getName()));
        }
        selectedTerm = term;
    }


    /**
     * Get the children of "term"
     *
     * @param term HPO Term of interest
     *
     * @return children of term (not including term itself).
     */
    private Set<GenericTerm> getTermChildren(GenericTerm term) {
        Ontology ontology = optionalResources.getMondoOntology();
        if (ontology == null) {
            PopUps.showInfoMessage("Error: Could not initialize Mondo Ontology", "ERROR");
            return new HashSet<>(); // return empty set
        }
        if (term == null) {
            PopUps.showInfoMessage("Error: term==null in getTermChildren", "ERROR");
            return new HashSet<>(); // return empty set
        }
        TermId parentTermId = term.getId();
        Set<TermId> childrenIds = getChildTerms(ontology, parentTermId, false);
        Set<GenericTerm> kids = new HashSet<>();
        childrenIds.forEach(tid -> {
            GenericTerm gt = (GenericTerm) ontology.getTermMap().get(tid);
            kids.add(gt);
        });
        return kids;
    }


    /**
     * Inner class that defines a bridge between hierarchy of {@link HpoTerm}s and {@link TreeItem}s of the
     * {@link TreeView}.
     */
    class GenericTermTreeItem extends TreeItem<GenericTermWrapper> {

        /** List used for caching of the children of this term */
        private ObservableList<TreeItem<GenericTermWrapper>> childrenList;

        /**
         * Default & only constructor for the TreeItem.
         *
         * @param term {@link HpoTerm} that is represented by this TreeItem
         */
        GenericTermTreeItem(GenericTermWrapper term) {
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
        public ObservableList<TreeItem<GenericTermWrapper>> getChildren() {
            if (childrenList == null) {
                // logger.debug(String.format("Getting children for term %s", getValue().term.getName()));
                childrenList = FXCollections.observableArrayList();
                Set<GenericTerm> children = getTermChildren(getValue().term);
                children.stream()
                        .sorted(Comparator.comparing(GenericTerm::getName))
                        .map(term -> new MondoController.GenericTermTreeItem(new GenericTermWrapper(term)))
                        .forEach(childrenList::add);
                super.getChildren().setAll(childrenList);
            }
            return super.getChildren();
        }
    }


}
