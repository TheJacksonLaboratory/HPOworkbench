package org.monarchinitiative.hpoapi;

import org.apache.log4j.Logger;
import org.monarchinitiative.hpoapi.argparser.ArgumentParser;
import org.monarchinitiative.hpoapi.argparser.ArgumentParserException;
import org.monarchinitiative.hpoapi.argparser.Arguments;
import org.monarchinitiative.hpoapi.cmd.*;

/**
 * Created by peter on 08.05.17.
 */
public class HPOAPI {
    private static Logger LOGGER = Logger.getLogger(HPOAPI.class);
    public static void main(String[] argv){
        ArgumentParser parser = new org.monarchinitiative.hpoapi.argparser.ArgumentParser("hpoapi");
        parser.setVersion(getVersion());
        parser.addArgument("--version").setShortFlag("-v").help("Show HPOAPI version").action(Arguments.version(getVersion()));
        parser.addArgument("--input").setShortFlag("-i").help("path to input file").required();
        parser.addCommand(new HPO2CSVCommand()).setDefaultValue("input","data/hp.obo");
        parser.addCommand(new DownloadCommand()).setDefaultValue("input","data");
        parser.addCommand(new NeurologyCommand()).setDefaultValue("input","data");
        parser.debugPrint();
        try {
            parser.parseArgs(argv);
            HPOCommand cmd=null;
            cmd = parser.getCommand();
            LOGGER.trace("HPOAPI command="+cmd.getName());
            cmd.run();
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }


       // parser.defaultHelp(true);
       // parser.epilog("You can find out more at http://TODO.rtfd.org");


    }

    public static String getVersion() {
        return HPOAPI.class.getPackage().getSpecificationVersion();
    }
}
