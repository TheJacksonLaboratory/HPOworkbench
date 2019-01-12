package org.monarchinitiative.hpoworkbench.io;


import org.apache.log4j.Logger;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;

import java.io.File;

/**
 * Use <a href="https://github.com/Phenomics/ontolib">ontolib</a> to parse the HPO OBO file.
 */
public class HPOParser {
    private static Logger LOGGER = Logger.getLogger(HPOParser.class.getName());

    private Ontology hpo=null;

    public HPOParser(String absolutePathToHpoObo) {
        LOGGER.trace(String.format("Initializing HPO obo parser for %s",absolutePathToHpoObo));
        parse(absolutePathToHpoObo);
    }

    private void parse(String path) {
        File f=new File(path);
        if (!f.exists()) {
            LOGGER.error(String.format("Unable to find HPO file at %s",path));
            return;
        }
        this.hpo = OntologyLoader.loadOntology(new File(path));
    }

    /** @return an initiliazed HPO ontology or null in case of errors. */
    public Ontology getHPO() { return this.hpo; }

}
