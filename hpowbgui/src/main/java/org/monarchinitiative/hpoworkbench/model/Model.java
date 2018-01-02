package org.monarchinitiative.hpoworkbench.model;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.io.HPOParser;

import java.io.*;

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
    /** Ontology model for full HPO ontology (all subhierarchies). */
    HpoOntology ontology=null;


    public Model(){
        initPaths();
    }



    private void initPaths() {
        this.pathToHpoOboFile=getLocalHPOPath();
        this.pathToAnnotationFile=getLocalPhenotypeAnnotationPath();
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

    /** Read the loinc2hpo settings file from the user's .loinc2hpo directory. */
    public void inputSettings(final String path) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line = null;
            while ((line = br.readLine()) != null) {
                int idx=line.indexOf(":");
                if (idx<0) {
                    logger.error("Malformed settings line (no semicolon): "+line);
                }
                if (line.length()<idx+2) {
                    logger.error("Malformed settings line (value too short): "+line);
                }
                String key,value;
                key=line.substring(0,idx).trim();
                value=line.substring(idx+1).trim();

                if (key.equals("annotationFile")) this.pathToAnnotationFile = value;
                else if (key.equals("hp-obo")) this.pathToHpoOboFile = value;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Could not open settings at " + path);
        }
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
