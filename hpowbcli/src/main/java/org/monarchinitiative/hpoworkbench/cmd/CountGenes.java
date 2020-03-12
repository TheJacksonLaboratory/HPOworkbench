package org.monarchinitiative.hpoworkbench.cmd;

import com.beust.jcommander.Parameters;
import org.apache.log4j.Logger;

@Parameters(commandDescription = "count.  Count and compare gene to disease associations")
public class CountGenes extends HPOCommand {
    private static final Logger LOGGER = Logger.getLogger(CountGenes.class.getName());



    public void run() {
        LOGGER.trace("Count genes command");
    }

    @Override
    public String getName() {
        return "count";
    }
}
