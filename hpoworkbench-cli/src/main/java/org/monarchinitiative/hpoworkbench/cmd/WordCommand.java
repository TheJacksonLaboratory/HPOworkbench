package org.monarchinitiative.hpoworkbench.cmd;


import java.io.IOException;
import java.util.concurrent.Callable;

import org.monarchinitiative.hpoworkbench.word.Hpo2Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

/**
 * A command class to coordinate the production and output of an RTF file containing information about
 * a subhierarchy of the HPO.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.1.0
 */
@CommandLine.Command(name = "word",
        mixinStandardHelpOptions = true,
        description = "Output subontology as word file (experimental)")
public class WordCommand implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(WordCommand.class);

    private final String DEFAULT_OUTPUTNAME="hpotest.word";
    private static String DEFAULT_START_TERM="HP:0000118";

    @CommandLine.Option(names={"--startterm"})
    private String startTerm=DEFAULT_START_TERM;
    private String IMMUNOLOGY_STAERT_TERM="HP:0002715";


    private Hpo2Word hpo2Word =null;


    public WordCommand() {
    }


    @Override
    public Integer call() {
        logger.trace("running RTF command");
        try {
            this.hpo2Word = new Hpo2Word(DEFAULT_OUTPUTNAME, startTerm);
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

}
