package org.monarchinitiative.hpoworkbench;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.monarchinitiative.hpoworkbench.cmd.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of utilities for working with the HPO.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.2.0
 */
public class HpoWorkbench {
    private static final Logger logger = LoggerFactory.getLogger(HpoWorkbench.class);

    @Parameter(names = {"-h", "--help"}, help = true, arity = 0,description = "display this help message")
    private boolean usageHelpRequested;

    public static void main(String[] args){

        HpoWorkbench workbench = new HpoWorkbench();
        WordCommand word = new WordCommand();
        HpoStatsCommand stats = new HpoStatsCommand();
        DownloadCommand download = new DownloadCommand();
        BatchGitPostCommand batch = new BatchGitPostCommand();
        CountFrequencyCommand countfreq = new CountFrequencyCommand();
        GitCommand git = new GitCommand();
        HpoListDescendentsCommand descendents = new HpoListDescendentsCommand();
        Hpo2HpoCommand hpo2hpo = new Hpo2HpoCommand();
        HPO2CSVCommand csv = new HPO2CSVCommand();
        MatchTermsCommand matchterms = new MatchTermsCommand();
        CountGenes count = new CountGenes();


        JCommander jc = JCommander.newBuilder().
                addObject(workbench).
                addCommand("word",word).
                addCommand("stats",stats).
                addCommand("download",download).
                addCommand("countfreq",countfreq).
                addCommand("batch",batch).
                addCommand("git",git).
                addCommand("hpo2hpo",hpo2hpo).
                addCommand("count", count).
                addCommand("descendents",descendents).
                addCommand("matchterms",matchterms).
                addCommand("csv",csv).
                build();
        jc.setProgramName("java -jar HpoWorkbench.jar");
        try {
            jc.parse(args);
        } catch (ParameterException e) {
            System.err.println("[ERROR] " + e.getMessage());
            System.err.println("[ERROR] Enter java -jar HpoWorkbench.jar -h for help");
            System.exit(1);
        }
        String parsedCommand = jc.getParsedCommand();

        if ( workbench.usageHelpRequested) {
            jc.usage();
            System.exit(1);
        }
        String command = jc.getParsedCommand();
        if (command == null || command.isEmpty()) {
            System.err.println("[ERROR] No command passed");
            return;
        }
        HPOCommand hpocommand=null;
        switch (command) {
            case "download":
                hpocommand= download;
                break;
            case "stats":
                hpocommand = stats;
                break;
            case "word":
                hpocommand = word;
                break;
            case "batch":
                hpocommand = batch;
                break;
            case "countfreq":
                hpocommand = countfreq;
                break;
            case "count":
                hpocommand = count;
                break;
            case "git":
                hpocommand = git;
                break;
            case "descendents":
                hpocommand = descendents;
                break;
            case "hpo2hpo":
                hpocommand = hpo2hpo;
                break;
            case "matchterms":
                hpocommand = matchterms;
                break;
            case "csv":
                hpocommand = csv;
                break;
            default:
                System.err.println(String.format("[ERROR] command \"%s\" not recognized",command));
                jc.usage();
                System.exit(1);
        }
        logger.trace("Running command " + command);

        try {
        hpocommand.run();
    } catch (Exception e) {
        e.printStackTrace();
    }


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


}
