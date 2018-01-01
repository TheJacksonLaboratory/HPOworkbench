package org.monarchinitiative.hpoapi;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoapi.argparser.ArgumentParser;
import org.monarchinitiative.hpoapi.argparser.ArgumentParserException;
import org.monarchinitiative.hpoapi.argparser.Arguments;
import org.monarchinitiative.hpoapi.cmd.*;

/**
 * A collection of utilities for working with the HPO.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.1.0
 */
public class HpoWorkbench {
    private static final Logger LOGGER = LogManager.getLogger();
    public static void main(String[] argv){
        ArgumentParser parser = new org.monarchinitiative.hpoapi.argparser.ArgumentParser("hpoapi");
        parser.setVersion(getVersion());
        parser.addArgument("--version").setShortFlag("-v").help("Show HpoWorkbench version").action(Arguments.version(getVersion()));
        parser.addArgument("--input").setShortFlag("-i").help("path to input file").required();
        parser.addArgument("--startterm").setShortFlag("-s").help("start HPO term").required();
        parser.addCommand(new HPO2CSVCommand()).setDefaultValue("input","data/hp.obo");
        parser.addCommand(new DownloadCommand()).setDefaultValue("directory","data");
        parser.addCommand(new NeurologyCommand()).setDefaultValue("input","data");
        parser.addCommand(new RtfCommand()).setDefaultValue("input","data");
        //parser.debugPrint();
        try {
            parser.parseArgs(argv);
            HPOCommand cmd = parser.getCommand();
            LOGGER.trace("HpoWorkbench command="+cmd.getName());
            cmd.run();
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }


       // parser.defaultHelp(true);
       // parser.epilog("You can find out more at http://TODO.rtfd.org");


    }

    public static String getVersion() {
        return HpoWorkbench.class.getPackage().getSpecificationVersion();
    }
}
