package org.monarchinitiative.hpoworkbench.cmd;


import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.monarchinitiative.hpoworkbench.word.Hpo2Word;

/**
 * A command class to coordinate the production and output of an RTF file containing information about
 * a subhierarchy of the HPO.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.1.0
 */
public class WordCommand extends HPOCommand  {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger();

    private final String DEFAULT_OUTPUTNAME="hpotest.word";
    private static String DEFAULT_START_TERM="HP:0000118";


    private String startTerm=DEFAULT_START_TERM;
    private String IMMUNOLOGY_STAERT_TERM="HP:0002715";


    private Hpo2Word hpo2Word =null;


    public WordCommand(String dir, String startTerm) {
        String hpopath = dir;
        startTerm=startTerm;
    }


    @Override
    public void run() {
        logger.trace("running RTF command");

        try {
            this.hpo2Word = new Hpo2Word(DEFAULT_OUTPUTNAME, startTerm);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
















    @Override
    public String getName() { return "word";}
}
