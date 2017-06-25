package org.monarch.hpoapi;

import org.apache.log4j.Logger;
import org.monarch.hpoapi.argparser.ArgumentParser;
import org.monarch.hpoapi.argparser.ArgumentParserException;
import org.monarch.hpoapi.argparser.Arguments;
import org.monarch.hpoapi.cmd.*;

/**
 * Created by peter on 08.05.17.
 */
public class HPOAPI {
    private static Logger LOGGER = Logger.getLogger(HPOAPI.class);
    public static void main(String[] argv){
        ArgumentParser parser = new org.monarch.hpoapi.argparser.ArgumentParser("hpoapi");
        parser.setVersion(getVersion());
        parser.addArgument("--version").setShortFlag("-v").help("Show HPOAPI version").action(Arguments.version(getVersion()));
        parser.addArgument("--input").setShortFlag("-i").help("path to input file").required();
        parser.addCommand(new HPO2CSVCommand()).setDefaultValue("input","data/hp.obo");
        parser.addCommand(new DownloadCommand()).setDefaultValue("directory","data");
        //parser.debugPrint();
        try {
            parser.parseArgs(argv);
            HPOCommand cmd=null;
            cmd = parser.getCommand();
            System.out.println("HPOAPI command="+cmd);
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
