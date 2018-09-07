package org.monarchinitiative.hpoworkbench.io;


import org.apache.log4j.Logger;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpOboParser;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Use <a href="https://github.com/Phenomics/ontolib">ontolib</a> to parse the HPO OBO file.
 */
public class HPOParser {
    private static Logger LOGGER = Logger.getLogger(HPOParser.class.getName());

    private HpoOntology hpo=null;

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

        try {
            final HpOboParser parser = new HpOboParser(f);
            this.hpo = parser.parse();
        } catch (PhenolException | FileNotFoundException e) {
            e.printStackTrace();
            LOGGER.error(String.format("I/O error with HPO file at %s",path));
        }

    }
    /** @return an initiliazed HPO ontology or null in case of errors. */
    public HpoOntology getHPO() { return this.hpo; }

}
