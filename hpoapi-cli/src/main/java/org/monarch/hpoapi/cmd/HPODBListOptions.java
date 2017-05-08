package org.monarch.hpoapi.cmd;

import java.util.function.BiFunction;

import com.google.common.collect.Lists;

import org.monarch.hpoapi.cmd.CommandLineParsingException;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * Configuration for the <tt>db-list</tt> command
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
public class HPODBListOptions extends HPOAPIDBOptions {

    /**
     * Setup {@link ArgumentParser}
     *
     * @param subParsers {@link Subparsers} to setup
     */
    public static void setupParser(Subparsers subParsers) {
        BiFunction<String[], Namespace, DatabaseListCommand> handler = (argv, args) -> {
            try {
                return new DatabaseListCommand(argv, args);
            } catch (CommandLineParsingException e) {
                System.out.println("Could not parse command line (TODO improve error handling" );
                e.printStackTrace();
                System.exit(1);
            }
        };

        Subparser subParser = subParsers.addParser("db-list", true).help("list databases available for download")
                .setDefault("cmd", handler);
        subParser.description("List databases available for download");

        ArgumentGroup optionalGroup = subParser.addArgumentGroup("Optional Arguments");
        optionalGroup.addArgument("-s", "--data-source-list").help("INI file with data source list")
                .setDefault(Lists.newArrayList("bundle:///default_sources.ini")).action(Arguments.append());

        HPOAPIBaseOptions.setupParser(subParser);
    }

}

