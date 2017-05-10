package org.monarch.hpoapi;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.monarch.hpoapi.cmd.HPOCommand;
import org.monarch.hpoapi.cmd.HPODBListOptions;
import org.monarch.hpoapi.cmd.PhenotypeDownloadOptions;
import org.monarch.hpoapi.exception.HPOException;

import java.util.function.BiFunction;

/**
 * Created by peter on 08.05.17.
 */
public class HPOAPI {




    public static void main(String[] argv){
        System.out.println("HPOAPI");
        // Setup command line parser
        ArgumentParser parser = ArgumentParsers.newArgumentParser("hpoapi-cli");
        parser.version(getVersion());
        parser.addArgument("--version").help("Show HPOAPI version").action(Arguments.version());
        parser.description("HPOAPI CLI performs a series of Human Phenotype Ontology (HPO) and HPO-annotation tasks.");
        Subparsers subParsers = parser.addSubparsers();
        HPODBListOptions.setupParser(subParsers);
        PhenotypeDownloadOptions.setupParser(subParsers);
        parser.defaultHelp(true);
        parser.epilog("You can find out more at http://TODO.rtfd.org");

        // Parse command line arguments
        Namespace args = null;
        try {
            args = parser.parseArgs(argv);
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
    }

    public static String getVersion() {
        return HPOAPI.class.getPackage().getSpecificationVersion();
    }
}
