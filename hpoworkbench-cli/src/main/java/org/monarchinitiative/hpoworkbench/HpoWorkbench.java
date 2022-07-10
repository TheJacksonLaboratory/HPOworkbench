package org.monarchinitiative.hpoworkbench;


import org.monarchinitiative.hpoworkbench.cmd.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * A collection of utilities for working with the HPO.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.2.0
 */
@CommandLine.Command(name = "lcp", mixinStandardHelpOptions = true, version = "lcp 0.0.1",
        description = "long covid phenottype")
public class HpoWorkbench implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(HpoWorkbench.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            // if the user doesn't pass any command or option, add -h to show help
            args = new String[]{"-h"};
        }
        CommandLine cline = new CommandLine(new HpoWorkbench())
                .addSubcommand("word", new WordCommand())
                .addSubcommand("stats", new HpoStatsCommand())
                .addSubcommand("qc", new AnnotQcCommand())
                .addSubcommand("download", new DownloadCommand())
                .addSubcommand("batch", new BatchGitPostCommand())
                .addSubcommand("countfreq", new CountFrequencyCommand())
                .addSubcommand("git", new GitCommand())
                .addSubcommand("descendents", new HpoListDescendentsCommand())
                .addSubcommand("hpo2hpo", new Hpo2HpoCommand())
                .addSubcommand("csv", new HPO2CSVCommand())
                .addSubcommand("convert", new PhenopacketConvertCommand())
                .addSubcommand("compare", new PhenopacketCompareCommand())
                .addSubcommand("matchterms", new MatchTermsCommand())
                .addSubcommand("count", new CountGenes())
                .addSubcommand("encoding", new EncodingCheckCommand())
                .addSubcommand("onset", new OnsetCommand())
                .addSubcommand("ptools", new MapToPtools())
                .addSubcommand("ranges", new CountHpoIdRanges());
        cline.setToggleBooleanFlags(false);
        int exitCode = cline.execute(args);
        System.exit(exitCode);
    }


    public static String getVersion() {
        String version = "0.0.0";// default, should be overwritten by the following.
        try {
            Package p = HpoWorkbench.class.getPackage();
            version = p.getImplementationVersion();
        } catch (Exception e) {
            // do nothing
        }
        return version;
    }


    @Override
    public Integer call() {
        // work done in subcommands
        return 0;
    }


}
