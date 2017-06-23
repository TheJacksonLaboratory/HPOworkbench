package org.monarch.hpoapi.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import com.google.common.collect.Lists;
//import de.charite.compbio.jannovar.UncheckedJannovarException;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.monarch.hpoapi.exception.UncheckedException;



/**
 * Configuration for the <tt>download</tt> command
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>, adapted from Jannovar code.
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 * @version 0.0.1 (May 10,2017)
 */
public class HPO2CSVOptions extends PhenotypeBaseOptions {






    @Override
    public void setFromArgs(Namespace args) throws CommandLineParsingException {
        super.setFromArgs(args);
        //downloadDir = args.getString("download_dir");
        //databaseNames = args.getList("database");
    }



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
                throw new UncheckedException("Could not parse command line",e );
            }
        };

        Subparser subParser = subParsers.addParser("csv", true).help("output CSV file")
                .setDefault("cmd", handler);
        subParser.description("Output a CSV version of the HPO");

        ArgumentGroup optionalGroup = subParser.addArgumentGroup("Optional Arguments");
        optionalGroup.addArgument("-i", "--inputfile").help("path to hp.obo file (TODO REFACTOR)");

        PhenotypeBaseOptions.setupParser(subParser);
    }

    /*

        Subparser subParser = subParsers.addParser("db-list", true).help("list databases available for download")
                .setDefault("cmd", handler);
        subParser.description("List databases available for download");

        ArgumentGroup optionalGroup = subParser.addArgumentGroup("Optional Arguments");
        optionalGroup.addArgument("-s", "--data-source-list").help("INI file with data source list")
                .setDefault(Lists.newArrayList("bundle:///default_sources.ini")).action(Arguments.append());

        PhenotypeBaseOptions.setupParser(subParser);
     */



}
