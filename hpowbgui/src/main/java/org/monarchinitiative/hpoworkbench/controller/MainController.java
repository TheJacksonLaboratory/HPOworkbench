package org.monarchinitiative.hpoworkbench.controller;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.graph.data.DirectedGraph;
import com.github.phenomics.ontolib.graph.data.Edge;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermPrefix;
import com.github.phenomics.ontolib.ontology.data.TermId;
import com.github.phenomics.ontolib.ontology.data.TermPrefix;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.Main;
import org.monarchinitiative.hpoworkbench.excel.HierarchicalExcelExporter;
import org.monarchinitiative.hpoworkbench.excel.Hpo2ExcelExporter;
import org.monarchinitiative.hpoworkbench.exception.HPOWorkbenchException;
import org.monarchinitiative.hpoworkbench.gui.GitHubPopup;
import org.monarchinitiative.hpoworkbench.gui.PlatformUtil;
import org.monarchinitiative.hpoworkbench.gui.PopUps;
import org.monarchinitiative.hpoworkbench.gui.WidthAwareTextFields;
import org.monarchinitiative.hpoworkbench.io.Downloader;
import org.monarchinitiative.hpoworkbench.model.Model;
import org.monarchinitiative.hpoworkbench.github.GitHubPoster;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Controller for HPO Workbench
 */
public class MainController {
    private static final Logger logger = LogManager.getLogger();

    /** Download address for {@code hp.obo}. */
    private final static String HP_OBO_URL ="https://raw.githubusercontent.com/obophenotype/human-phenotype-ontology/master/hp.obo";
    private final static String PHENOTYPE_ANNOTATION_URL="http://compbio.charite.de/jenkins/job/hpo.annotations/lastStableBuild/artifact/misc/phenotype_annotation.tab";
    private Model model=null;
    @FXML private Button goButton;
    /** Ontology object containing {@link HpoTerm}s and their relationships. */
    private HpoOntology ontology;

    private final TermPrefix HP_PREFIX = new ImmutableTermPrefix("HP");

    /** Key: a term name such as "Myocardial infarction"; value: the corresponding HPO id as a {@link TermId}. */
    private Map<String, TermId> labels = new HashMap<>();
    /** Tree hierarchy of the ontology is presented here. */
    @FXML private TreeView<HpoTermWrapper> ontologyTreeView;
    /** WebView for displaying details of the Term that is selected in the {@link #ontologyTreeView}.*/
    @FXML private WebView infoWebView;
    /** WebEngine backing up the {@link #infoWebView}. */
    private WebEngine infoWebEngine;
    /** Text field with autocompletion for jumping to a particular HPO term in the tree view. */
    @FXML private TextField searchTextField;
    @FXML private Button GoButton;
    @FXML private Label browserlabel;

    private  Stage primarystage;

    private Main mainApp=null;

    /** Approved {@link HpoTerm} is submitted here. */
    private Consumer<HpoTerm> addHook;

    /** The term that is currently selected in the Browser window. */
    private HpoTerm selectedTerm=null;


    public MainController() {
        this.model=new Model();
        ensureUserDirectoryExists();
    }


    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
        logger.trace("Set main app to " + mainApp.toString());

    }



    /**
     * This function will create the .hpoworkbench directory in the user's home directory if it does not yet exist.
     */
    private void ensureUserDirectoryExists() {
        File userDirectory = PlatformUtil.getHpoWorkbenchDir();
        if (!userDirectory.exists()) {
            File fck = new File(userDirectory.getAbsolutePath());
            if (!fck.mkdir()) { // make sure config directory is created, exit if not
                logger.fatal("Unable to create HPOworkbench config directory.\n"
                        + "Even though this is a serious problem I'm exiting gracefully. Bye.");
                System.exit(1);
            }
        }
        logger.trace("Created HpoWorkench user directory at "+userDirectory);
    }


    @FXML private void close(ActionEvent e) {
        logger.trace("Closing down");
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void downloadHPO(ActionEvent e) {
        String dirpath= PlatformUtil.getHpoWorkbenchDir().getAbsolutePath();
        File f = new File(dirpath);
        if (f==null || ! (f.exists() && f.isDirectory())) {
            logger.trace("Cannot download hp.obo, because directory not existing at " + f.getAbsolutePath());
            return;
        }
        String BASENAME="hp.obo";

        ProgressIndicator pb = new ProgressIndicator();
        javafx.scene.control.Label label=new javafx.scene.control.Label("downloading hp.obo...");
        FlowPane root = new FlowPane();
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.getChildren().addAll(label,pb);
        Scene scene = new Scene(root, 400, 100);
        Stage window = new Stage();
        window.setTitle("HPO download");
        window.setScene(scene);

        Task hpodownload = new Downloader(dirpath, HP_OBO_URL,BASENAME,pb);
        new Thread(hpodownload).start();
        window.show();
        hpodownload.setOnSucceeded(event -> {
            window.close();
            logger.trace(String.format("Successfully downloaded hpo to %s",dirpath));
            String fullpath=String.format("%s%shp.obo",dirpath,File.separator);
        });
        hpodownload.setOnFailed(event -> {
            window.close();
            logger.error("Unable to download HPO obo file");
        });
        Thread thread = new Thread(hpodownload);
        thread.start();

        e.consume();
    }

    @FXML
    private void downloadHPOAnnotations(ActionEvent e) {
        String dirpath= PlatformUtil.getHpoWorkbenchDir().getAbsolutePath();
        File f = new File(dirpath);
        if (f==null || ! (f.exists() && f.isDirectory())) {
            logger.trace("Cannot download phenotype_annotation.tab, because directory not existing at " + f.getAbsolutePath());
            return;
        }
        String BASENAME="phenotype_annotation.tab";

        ProgressIndicator pb = new ProgressIndicator();
        javafx.scene.control.Label label=new javafx.scene.control.Label("downloading phenotype_annotation.tab...");
        FlowPane root = new FlowPane();
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.getChildren().addAll(label,pb);
        Scene scene = new Scene(root, 400, 100);
        Stage window = new Stage();
        window.setTitle("HPO annotation download");
        window.setScene(scene);

        Task hpodownload = new Downloader(dirpath, PHENOTYPE_ANNOTATION_URL,BASENAME,pb);
        new Thread(hpodownload).start();
        window.show();
        hpodownload.setOnSucceeded(event -> {
            window.close();
            logger.trace(String.format("Successfully downloaded %s to %s",BASENAME,dirpath));
            String fullpath=String.format("%s%s%s",dirpath,File.separator,BASENAME);
        });
        hpodownload.setOnFailed(event -> {
            window.close();
            logger.error("Unable to download phenotype_annotation.tab file");
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
        TermId id = labels.get(searchTextField.getText());
        if (id==null) return; // button was clicked while field was empty, no need to do anything
        logger.trace("go button for term %s [%s]",searchTextField.getText(),id.getIdWithPrefix());
        if (id != null) {
            expandUntilTerm(ontology.getTermMap().get(id));
            searchTextField.clear();
        }
    }

    public static String getVersion() {
        String version="0.0.0";// default, should be overwritten by the following.
        try {
            Package p = MainController.class.getPackage();
            logger.trace("got package p="+p.toString());
            logger.trace("got i p="+p.getImplementationVersion());
            version = p.getImplementationVersion();
        } catch (Exception e) {
            // do nothing
        }
        if (version==null) version = "0.1.1"; // this works on a maven build but needs to be reassigned in intellij
        return version;
    }

    @FXML
    private void initialize()
    {
        logger.trace("initialize");
        // This action will be run after user approves a PhenotypeTerm in the ontologyTreePane
        Consumer<HpoTerm> addHook = (ph -> logger.trace(String.format("Hook for %s",ph.getName())));
        if (model.getOntology()==null) {
            logger.error("Need to initialize ontology before we can start the application.");
            return;
        } else {
            logger.trace("retrieved ontology");
        }
        if (GoButton==null) {
            logger.fatal("Go Button null");
        } else {
            logger.trace("Go Button OK");
        }
        initTree(model.getOntology(), addHook);
        logger.trace("done init");
        browserlabel.setAlignment(Pos.BOTTOM_RIGHT);
        String ver = getVersion();
        browserlabel.setText("HPO Workbench, v. "+ver+", \u00A9 Monarch Initiative 2018");
        this.primarystage=Main.primarystage;
    }


    public void initTree(HpoOntology ontology, Consumer<HpoTerm> addHook) {
        this.ontology=ontology;
        this.addHook = addHook;
        // populate the TreeView with top-level elements from ontology hierarchy
        TermId rootId = ontology.getRootTermId();
        HpoTerm rootTerm = ontology.getTermMap().get(rootId);
        TreeItem<HpoTermWrapper> root = new HpoTermTreeItem(new HpoTermWrapper(rootTerm));
        root.setExpanded(true);
        if (ontologyTreeView==null) {
            logger.fatal("Tree view is not initialized");
            return;
        }
        ontologyTreeView.setShowRoot(false);
        ontologyTreeView.setRoot(root);
        ontologyTreeView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
            HpoTermWrapper w =newValue.getValue();
            TreeItem item = new HpoTermTreeItem(w);
            updateDescription(item);
                });
        // create Map for lookup of the terms in the ontology based on their Name
        ontology.getTermMap().values().forEach(term -> labels.put(term.getName(), term.getId()));
        WidthAwareTextFields.bindWidthAwareAutoCompletion(searchTextField, labels.keySet());

        // show intro message in the infoWebView
        Platform.runLater(()->{
        infoWebEngine = infoWebView.getEngine();
        infoWebEngine.loadContent("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>HPO tree browser</title></head>" +
                "<body><p>Click on HPO term in the tree browser to display additional information</p></body></html>");
        });
    }



    /**
     * Focus on the HPO term with given ID if the term is contained in the ontology.
     *
     * @param termId String with HPO term id (e.g. HP:0002527 for Falls)
     */
    void focusOnTerm(String termId) {

        HpoTerm term = ontology.getTermMap().get(termId);
        if (term == null) {
            logger.warn("Unable to focus on term with id {} because it is not defined in the ontology", termId);
            return;
        }
        expandUntilTerm(term);
    }


    /**
     * Find the path from the root term to given {@link HpoTerm}, expand the tree and set the selection model of the
     * TreeView to the term position.
     *
     * @param term {@link HpoTerm} to be displayed
     */
    private void expandUntilTerm(HpoTerm term) {
        //if (ontology.existsPath(ontology.getRootTerm().getID(), term.getID())) {
        logger.trace("expand until term " + term.toString());
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
            TermId rootId=ontology.getRootTermId();
            HpoTerm rootTerm = ontology.getTermMap().get(rootId);
            logger.warn(String.format("Unable to find the path from %s to %s", rootTerm.toString(), term.getName()));
        }
        selectedTerm=term;
    }


    /**
     * Get currently selected Term. Used in tests.
     *
     * @return {@link HpoTermTreeItem} that is currently selected
     */
    HpoTermTreeItem getSelectedTerm() {
        return (ontologyTreeView.getSelectionModel().getSelectedItem() == null) ? null
                : (HpoTermTreeItem) ontologyTreeView.getSelectionModel().getSelectedItem();
    }


    /**
     * Update content of the {@link #infoWebView} with currently selected {@link HpoTerm}.
     *
     * @param treeItem currently selected {@link TreeItem} containing {@link HpoTerm}
     */
    private void updateDescription(TreeItem<HpoTermWrapper> treeItem) {
        if (treeItem == null)
            return;
        HpoTerm term = treeItem.getValue().term;
        String HTML_TEMPLATE = "<!DOCTYPE html>" +
                "<html lang=\"en\"><head><meta charset=\"UTF-8\"><title>HPO tree browser</title></head>" +
                "<body>" +
                "<p><b>Term ID:</b> %s</p>" +
                "<p><b>Term Name:</b> %s</p>" +
                "<p><b>Synonyms:</b> %s</p>" +
                "<p><b>Definition:</b> %s</p>" +
                "<p><b>Comment:</b> %s</p>" +
                "</body></html>";

        String termID = term.getId().getIdWithPrefix();
        String synonyms = (term.getSynonyms() == null) ? "" : term.getSynonyms().stream().map(s->s.getValue())
                .collect(Collectors.joining("; "));
        // Synonyms
        String definition = (term.getDefinition() == null) ? "" : term.getDefinition().toString();
        String comment = (term.getComment() == null) ? "-" : term.getComment();
        List<String> annotatedDiseases = model.getDiseaseAnnotations(termID);
        if (annotatedDiseases==null) {
            logger.error("could not retrieve diseases for " + termID);
        }
        String content = HpoHtmlPageGenerator.getHTML(term,annotatedDiseases);
        infoWebEngine.loadContent(content);
    }


    @FXML private void exportToExcel(ActionEvent event) {
        logger.trace("exporting to excel");

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export HPO as Excel-format file");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel file (*.xlsx)", "*.xlsx");
        chooser.getExtensionFilters().add(extFilter);
        chooser.setInitialFileName("hpo.xlsx");
        File f = chooser.showSaveDialog(null);
        if (f != null) {
            String path = f.getAbsolutePath();
            logger.trace(String.format("Setting path to LOINC Core Table file to %s",path));
            Hpo2ExcelExporter exporter = new Hpo2ExcelExporter(model.getOntology());
            exporter.exportToExcelFile(path);
        } else {
            logger.error("Unable to obtain path to Excel export file");
        }
        event.consume();
    }


    @FXML private void exportHierarchicalSummary(ActionEvent event) {
        HpoTerm t = getSelectedTerm().getValue().term;
        logger.trace("export hierarchy I found term " + t.getName());

        if (selectedTerm==null) {
            logger.error("Select a term before exporting hierarchical summary TODO show error window");
            PopUps.showInfoMessage("Please select an HPO term in order to export a term with its subhierarchy",
                    "Error: No HPO Term selected");
            return; // to do throw exceptio
        }
        selectedTerm=getSelectedTerm().getValue().term;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export HPO as Excel-format file");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel file (*.xlsx)", "*.xlsx");
        chooser.getExtensionFilters().add(extFilter);
        chooser.setInitialFileName(String.format("%s.xlsx",selectedTerm.getName()));
        File f = chooser.showSaveDialog(null);
        if (f != null) {
            String path = f.getAbsolutePath();
            logger.trace(String.format("Setting path to hierarchical export file to %s",path));
        } else {
            logger.error("Unable to obtain path to Excel export file");
            return;
        }
        logger.trace(String.format("Exporting hierarchical summary starting from term %s", selectedTerm.toString()));
        HierarchicalExcelExporter exporter = new HierarchicalExcelExporter(model.getOntology(),selectedTerm);
        exporter.exportToExcel(f.getAbsolutePath());
    }

    @FXML private void suggestCorrectionToTerm(ActionEvent e) {


        if (getSelectedTerm()==null) {
            logger.error("Select a term before creating GitHub issue");
            PopUps.showInfoMessage("Please select an HPO term before creating GitHub issue",
                    "Error: No HPO Term selected");
            return;
        } else {
            selectedTerm = getSelectedTerm().getValue().term;
        }
        selectedTerm=getSelectedTerm().getValue().term;
        logger.trace("Will suggest correction to "+selectedTerm.getName());
        GitHubPopup popup = new GitHubPopup(selectedTerm);
        //Stage thisStage = (Stage) goButton.getScene().getWindow();
        popup.displayWindow(primarystage);
        String githubissue=popup.retrieveGitHubIssue();
        if (githubissue==null) {
            logger.trace("got back null githuib issue");
            return;
        }
        String pword=popup.getGitHubPassWord();
        String uname=popup.getGitHubUserName();
        logger.trace("I got github issue: "+githubissue);
        GitHubPoster poster = new GitHubPoster(uname,pword,String.format("Correction to term %s",selectedTerm.getName()),githubissue);
        try {
            poster.postIssue();
        } catch (HPOWorkbenchException he) {
            PopUps.showException("GitHub error","Bad Request (400)","Could not post issue", he);
        }
        catch (Exception ex) {
            PopUps.showException("GitHub error","GitHub error","Could not post issue", ex);
        }

    }

    @FXML private void suggestNewChildTerm(ActionEvent e) {
        selectedTerm=getSelectedTerm().getValue().term;
        logger.trace("Will suggest new child term of "+selectedTerm.getName());
    }



    /** Show the about message */
    @FXML private void aboutWindow(ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("HPO Workbench");
        alert.setHeaderText("Human Phenotype Ontology Workbench");
        String s = "A tool for working with the HPO.";
        alert.setContentText(s);
        alert.showAndWait();
        e.consume();
    }


    /**
     * Inner class that defines a bridge between hierarchy of {@link HpoTerm}s and {@link TreeItem}s of the
     * {@link TreeView}.
     */
    class HpoTermTreeItem extends TreeItem<HpoTermWrapper> {

        /**
         * List used for caching of the children of this term
         */
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
            return getTermChildren(getValue().term).size()==0;
        }


        /**
         * Get list of children of the {@link HpoTerm} that is represented by this TreeItem.
         * <p>
         * {@inheritDoc}
         */
        @Override
        public ObservableList<TreeItem<HpoTermWrapper>> getChildren() {
            if (childrenList == null) {
                logger.debug(String.format("Getting children for term %s", getValue().term.getName()));
                childrenList = FXCollections.observableArrayList();
                Set<HpoTerm> children = getTermChildren(getValue().term) ;
                children.stream()
                        .sorted((l, r) -> l.getName().compareTo(r.getName()))
                        .map(term -> new HpoTermTreeItem(new HpoTermWrapper(term)))
                        .forEach(childrenList::add);
                super.getChildren().setAll(childrenList);
            }
            return super.getChildren();
        }

    }


    public  Set<HpoTerm> getTermChildren(HpoTerm term) {
        Set<HpoTerm> kids = new HashSet<>();
        Iterator it =  ontology.getGraph().inEdgeIterator(term.getId());
        while (it.hasNext()) {
            Edge<TermId> edge = (Edge<TermId>) it.next();
            TermId sourceId=edge.getSource();
            HpoTerm sourceTerm = ontology.getTermMap().get(sourceId);
            kids.add(sourceTerm);
        }
        return kids;
    }

    private Set<HpoTerm> getTermParents(HpoTerm term) {
        Set<HpoTerm> eltern = new HashSet<>();
        Iterator it =  ontology.getGraph().outEdgeIterator(term.getId());
        while (it.hasNext()) {
            Edge<TermId> edge = (Edge<TermId>) it.next();
            TermId destId=edge.getDest();
            HpoTerm destTerm = ontology.getTermMap().get(destId);
            eltern.add(destTerm);
        }
        return eltern;
    }


    public boolean existsPathFromRoot(HpoTerm term) {
        TermId rootId = ontology.getRootTermId();
        TermId tid = term.getId();
        DirectedGraph dag = ontology.getGraph();
        Stack<TermId> stack = new Stack<>();
        stack.push(rootId);
        while (! stack.empty()) {
            TermId id = stack.pop();
            Iterator it = dag.inEdgeIterator(id);
            while (it.hasNext()) {
                Edge<TermId> edge = (Edge<TermId>) it.next();
                TermId source = edge.getSource();
                if (source.equals(tid)) { return true; }
                stack.push(source);
            }
        }
        return false; // if we get here, there was no path.
    }

}
