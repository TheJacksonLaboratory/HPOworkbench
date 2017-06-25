package org.monarch.hpoapi;

import org.apache.log4j.Logger;
import org.monarch.hpoapi.argparser.ArgumentMap;
import org.monarch.hpoapi.argparser.ArgumentParserException;
import org.monarch.hpoapi.argparser.Arguments;
import org.monarch.hpoapi.cmd.*;

/**
 * Created by peter on 08.05.17.
 */
public class HPOAPI {
    private static Logger LOGGER = Logger.getLogger(HPOAPI.class);
    public static void main(String[] argv){

        System.out.println("HPOAPI");
        org.monarch.hpoapi.argparser.ArgumentParser parser = new org.monarch.hpoapi.argparser.ArgumentParser("hpoapi");
        parser.setVersion(getVersion());
        parser.addArgument("--version").setShortFlag("-v").help("Show HPOAPI version").action(Arguments.version(getVersion()));
        parser.addArgument("--input").setShortFlag("-i").help("path to input file").required();
        parser.addCommand(new HPO2CSVCommand()).setDefaultValue("input","data/hp.obo");
        ArgumentMap args;
        try {
            System.out.println("HPOAPI about to parse args");
            args = parser.parseArgs(argv);
            System.out.println("args="+args.toString());
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
        // The following provides us with the command. The command object has been provided
        //; with all of the command line options.
        HPOCommand cmd=null;
        try {
            cmd = parser.getCommand();
        } catch (ArgumentParserException e) {
            System.err.println("[ERROR] Failure to parse arguments: " + e.toString());
            System.exit(1);
        }

         cmd.run();



        /*
        ArgumentParser parser = ArgumentParsers.newArgumentParser("hpoapi-cli");
        parser.version(getVersion());
        parser.addArgument("--version").help("Show HPOAPI version").action(Arguments.version());
        parser.description("HPOAPI CLI performs a series of Human Phenotype Ontology (HPO) and HPO-annotation tasks.");
        Subparsers subParsers = parser.addSubparsers();
        PhenotypeDBListOptions.setupParser(subParsers);
        PhenotypeDownloadOptions.setupParser(subParsers);
        HPO2CSVOptions.setupParser(subParsers);
        parser.defaultHelp(true);
        parser.epilog("You can find out more at http://TODO.rtfd.org");


        // Parse command line arguments
        Namespace args = null;
        try {
            System.out.println("BEFORE@");
            args = parser.parseArgs(argv);
            System.out.println("args="+args.toString());
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        BiFunction<String[], Namespace, HPOCommand> factory = args.get("cmd");
        HPOCommand cmd = factory.apply(argv, args);
        if (cmd == null)
            System.exit(1);

        // Execute the command.
        try {
            cmd.run();
        } catch (HPOException e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        */
    }

    public static String getVersion() {
        return HPOAPI.class.getPackage().getSpecificationVersion();
    }
}
