package org.monarchinitiative.hpoworkbench.cmd;


import java.io.IOException;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.monarchinitiative.hpoworkbench.word.Hpo2Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A command class to coordinate the production and output of an RTF file containing information about
 * a subhierarchy of the HPO.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.1.0
 */
@Parameters(commandDescription = "Output subontology as word file (experimental)")
public class WordCommand extends HPOCommand  {
    private static final Logger logger = LoggerFactory.getLogger(WordCommand.class);

    private final String DEFAULT_OUTPUTNAME="hpotest.word";
    private static String DEFAULT_START_TERM="HP:0000118";

    @Parameter(names={"--startterm"})
    private String startTerm=DEFAULT_START_TERM;
    private String IMMUNOLOGY_STAERT_TERM="HP:0002715";


    private Hpo2Word hpo2Word =null;


    public WordCommand() {
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
