package org.monarchinitiative.hpoworkbench;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.cmd.*;
import org.monarchinitiative.hpoworkbench.io.Commandline;

/**
 * A collection of utilities for working with the HPO.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.2.0
 */
public class HpoWorkbench {
    private static final Logger LOGGER = LogManager.getLogger();
    public static void main(String[] argv){
        Commandline clp = new Commandline(argv);
        HPOCommand command = clp.getCommand();
        LOGGER.trace(String.format("running command %s",command));
        command.run();


    }

    public static String getVersion() {
        return HpoWorkbench.class.getPackage().getSpecificationVersion();
    }
}
