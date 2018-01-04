package org.monarchinitiative.hpoworkbench.model;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermId;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermPrefix;
import com.github.phenomics.ontolib.ontology.data.TermId;
import com.github.phenomics.ontolib.ontology.data.TermPrefix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.io.HPOParser;
import org.monarchinitiative.hpoworkbench.io.HpoAnnotationParser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.monarchinitiative.hpoworkbench.gui.PlatformUtil.getLocalHPOPath;
import static org.monarchinitiative.hpoworkbench.gui.PlatformUtil.getLocalPhenotypeAnnotationPath;

public class Model {
    private static final Logger logger = LogManager.getLogger();
    /** We save a few settings in a file that we store in ~/.loinc2hpo/loinc2hpo.settings. This variable should
     * be initialized to the absolute path of the file. */
    private String pathToSettingsFile=null;
    /** Path to {@code hp.obo}. */
    private String pathToHpoOboFile=null;
    /** Path to the file we are creating with LOINC code to HPO annotations. */
    private String pathToAnnotationFile=null;
    private final TermPrefix HP_PREFIX = new ImmutableTermPrefix("HP");
    /** Ontology model for full HPO ontology (all subhierarchies). */
    private HpoOntology ontology=null;
    /** List of annotated diseases */
    private Map<TermId,List<String>> annotmap=null;


    public Model(){
        initPaths();
        importData();
    }





    private void initPaths() {
        this.pathToHpoOboFile=getLocalHPOPath();
        this.pathToAnnotationFile=getLocalPhenotypeAnnotationPath();
    }

    private void importData() {
        if (ontology==null) {
            if (pathToHpoOboFile==null) {
                logger.error("Path to hp.obo was not initialized");
                return;
            }
            HPOParser parser = new HPOParser(pathToHpoOboFile);
            this.ontology=parser.getHPO();
        }
        if (annotmap==null) {
            HpoOntology ontology = getOntology();
            HpoAnnotationParser parser = new HpoAnnotationParser(this.pathToAnnotationFile,ontology);
            annotmap=parser.parse();
        }
        logger.trace("Ingested annot map with size="+annotmap.size());

    }


    public void setPathToHpOboFile(String p) { pathToHpoOboFile=p; }
    public void setPathToSettingsFile(String p) { this.pathToSettingsFile=p;}

    /** Write a few settings to a file in the user's .hpoworkbench directory. */
    public void writeSettings() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(pathToSettingsFile));

            if (pathToAnnotationFile!=null) {
                bw.write(String.format("annotationFile:%s\n",pathToAnnotationFile));
            }
            if (pathToHpoOboFile!=null) {
                bw.write(String.format("hp-obo:%s\n",pathToHpoOboFile));
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Could not write settings at " + pathToSettingsFile);
        }
    }


    public List<String> getDiseaseAnnotations(String hpoTermId) {
        List<String> diseases=new ArrayList<>();
        TermId id = string2TermId(hpoTermId);
        if (id!=null) {
            diseases=annotmap.get(id);
        }
        if (diseases==null) diseases=new ArrayList<>();// return empty

        return diseases;
    }


    private TermId string2TermId(String termstring) {
        if (termstring.startsWith("HP:")) {
            termstring=termstring.substring(3);
        }
        if (termstring.length()!=7) {
            logger.error("Malformed termstring: "+termstring);
            return null;
        }
        TermId tid = new ImmutableTermId(HP_PREFIX,termstring);
        if (! ontology.getAllTermIds().contains(tid)) {
            logger.error("Unknown TermId "+tid.getIdWithPrefix());
            return null;
        }
        return tid;
    }





    public HpoOntology getOntology() {
        if (ontology==null) {
            if (pathToHpoOboFile==null) {
                logger.error("Path to hp.obo was not initialized");
                return null;
            }
            HPOParser parser = new HPOParser(pathToHpoOboFile);
            this.ontology=parser.getHPO();
        }
        return ontology;
    }



}
