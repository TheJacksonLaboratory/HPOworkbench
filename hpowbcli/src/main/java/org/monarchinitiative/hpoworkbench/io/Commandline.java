package org.monarchinitiative.hpoworkbench.io;


import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.monarchinitiative.hpoworkbench.cmd.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Class to capture options and command from the command line.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.0.2 (2018-01-05)
 */
public class Commandline {

    private HPOCommand command = null;
    /**
     * The default name of the file that is produced by the {@code digest} command.
     */
    private final static String DEFAULT_DOWNLOAD_DIRECTORY = "data";

    private final static String DEFAULT_HPO_OBOPATH = String.format("%s%s%s",
            DEFAULT_DOWNLOAD_DIRECTORY, File.separator,"hp.obo");

    private final static String DEFAULT_ANNOTATION_OBOPATH = String.format("%s%s%s",
            DEFAULT_DOWNLOAD_DIRECTORY, File.separator,"phenotype_annotation.tab");


    private final static String DEFAULT_TRUNCATION_SUFFIX = "truncated";

    private final static String DEFAULT_OUTPUT_BAM_NAME = "diachromatic-processed";

    private String downloadDirectory;
    private String hpoOboPath = null;
    private String annotationPath = null;
    private String termid = null;

    private String gitHubIssueLabel=null;


    private String inputFilePath = null;
    private String outputDirectory = null;



    public Commandline(String args[]) {
        final CommandLineParser cmdLineGnuParser = new DefaultParser();

        final Options gnuOptions = constructGnuOptions();
        org.apache.commons.cli.CommandLine commandLine;

        String mycommand = null;
        String clstring = "";
        if (args != null && args.length > 0) {
            clstring = Arrays.stream(args).collect(Collectors.joining(" "));
        }
        try {
            commandLine = cmdLineGnuParser.parse(gnuOptions, args);
            String category[] = commandLine.getArgs();
            if (category.length < 1) {
                printUsage("command missing");
            } else {
                mycommand = category[0];

            }
            if (commandLine.getArgs().length < 1) {
                printUsage("no arguments passed");
                return;
            }
            if (commandLine.hasOption("d")) {
                this.downloadDirectory = commandLine.getOptionValue("d");
            } else {
                this.downloadDirectory = DEFAULT_DOWNLOAD_DIRECTORY;
            }
            if (commandLine.hasOption("h")) {
                this.hpoOboPath = commandLine.getOptionValue("h");
            } else {
                this.hpoOboPath=DEFAULT_HPO_OBOPATH;
            }
            if (commandLine.hasOption("a")) {
                annotationPath = commandLine.getOptionValue("a");
            } else {
                annotationPath =  DEFAULT_ANNOTATION_OBOPATH;
            }
            if (commandLine.hasOption("g")) {
                gitHubIssueLabel=commandLine.getOptionValue("g");
            }
            if (commandLine.hasOption("t")) {
                this.termid = commandLine.getOptionValue("t");
            }
            if (commandLine.hasOption("i")) {
                this.inputFilePath = commandLine.getOptionValue("i");
            }
        } catch (ParseException parseException)  // checked exception
        {
            String msg = String.format("Could not parse options %s [%s]", clstring, parseException.toString());
            printUsage(msg);
        }
        switch (mycommand) {
            case "download":
                this.command = new DownloadCommand(this.downloadDirectory);
                break;
            case "stats":
                if (termid == null) {
                    printUsage("[ERROR] stats command requires -t option");
                }
                this.command = new HpoStatsCommand(this.hpoOboPath, this.annotationPath, this.termid);
                break;
            case "csv":
                this.command = new HPO2CSVCommand(this.hpoOboPath);
                break;
            case "countfreq":
                if (this.termid == null) {
                    printUsage("-t HP:0000123 option required for countfreq");
                }
                this.command = new CountFrequencyCommand(this.hpoOboPath, this.annotationPath, this.termid);
                break;
            case "hpo2hpo":
                if (this.termid == null) {
                    printUsage("-t HP:0000123 option required for hpo2hpo");
                }
                this.command = new Hpo2HpoCommand(this.hpoOboPath, this.annotationPath, this.termid);
                break;
            case "word":
                this.command = new WordCommand(this.downloadDirectory, this.hpoOboPath);
                break;
            case "git":
                if (gitHubIssueLabel == null) {
                    printUsage("-g (github issue) required for git command");
                }
                this.command = new GitCommand(gitHubIssueLabel);
                break;
            case "batch":
                if (gitHubIssueLabel == null) {
                    printUsage("-g (github laqbel) required for batch command");
                }
                if (inputFilePath == null) {
                    printUsage("-i (input file) required for batch command");
                }
                this.command = new BatchGitPostCommand(gitHubIssueLabel,inputFilePath);
                break;
            default:
                printUsage(String.format("Did not recognize command: %s", mycommand));
        }

    }


    public HPOCommand getCommand() {
        return command;
    }

    /**
     * Construct and provide GNU-compatible Options.
     *
     * @return Options expected from command-line of GNU form.
     */
    private static Options constructGnuOptions() {
        final Options options = new Options();
        options.addOption("a", "annot", true, "path to HPO annotation file")
                .addOption("d", "download", true, "directory to download HPO data (default \"data\")")
                .addOption("g","github-issue",true,"GitHub issue label")
                .addOption("h", "hpo", true, "path to hp.obo")
                .addOption("i","input-file",true,"path to input file")
                .addOption("o", "out", true, "name/path of output file/directory")
                .addOption("t", "term", true, "HPO id (e.g., HP:0000123)");
        return options;
    }

    private static String getVersion() {
        String version = "0.0.0";// default, should be overwritten by the following.
        try {
            Package p = Commandline.class.getPackage();
            version = p.getImplementationVersion();
        } catch (Exception e) {
            // do nothing
        }
        return version;
    }

    /**
     * Print usage information to provided OutputStream.
     */
    private static void printUsage(String message) {


        String version = getVersion();
        final PrintWriter writer = new PrintWriter(System.out);
        // final HelpFormatter usageFormatter = new HelpFormatter();
        // final String applicationName="java -jar diachromatic.jar command";
        // final Options options=constructGnuOptions();
        writer.println(message);
        writer.println();
        //usageFormatter.printUsage(writer, 120, applicationName, options);
        writer.println("Program: HPOWorkbench (Human Phenotype Ontology Workbench)");
        writer.println("Version: " + version);
        writer.println();
        writer.println("Usage: java -jar HPOWorkbench.jar <command> [options]");
        writer.println();
        writer.println("Available commands:");
        writer.println();
        writer.println("download:");
        writer.println("\tjava -jar HPOWorkbench.jar download  [-d <directory>]");
        writer.println("\t<directory>: name of directory to which HPO data will be downloaded (default:\"data\")");
        writer.println();
        writer.println("countfreq:");
        writer.println("\tjava -jar HPOWorkbench.jar countfreq [-h <hpo.obo>] [-a <pheno_annot.tab>] -t <term id> \\");
        writer.println("\t<hp.obo>: path to hp.obo file (default: \"data/hp.obo\")");
        writer.println("\t<pheno_annot.tab>: path to annotation file (default \"data/phenotype_annotation.tab\")");
        writer.println("\t<term>: HPO term id (e.g., HP:0000123)");
        writer.println();
        writer.println("hpo2hpo:");
        writer.println("\tjava -jar HPOWorkbench.jar hpo2hpo [-h <hpo.obo>] [-a <pheno_annot.tab>] -t <term id> \\");
        writer.println("\t<hp.obo>: path to hp.obo file (default: \"data/hp.obo\")");
        writer.println("\t<pheno_annot.tab>: path to annotation file (default \"data/phenotype_annotation.tab\")");
        writer.println("\t<term>: HPO term id (e.g., HP:0000123)");
        writer.println();
        writer.println("csv:");
        writer.println("\tjava -jar HPOWorkbench.jar csv -h <hpo> \\");
        writer.println("\t<hpo>: path to hp.obo file");
        writer.println();
        writer.println("convert:");
        writer.println("\tjava -jar HPOWorkbench.jar convert -h <hpo> -d <directory> \\");
        writer.println("\t<hpo>: path to hp.obo file");
        writer.println("\t<directory>: path to directory with RD annotation files");
        writer.println(String.format("\t<outfile>: optional name of output file (Default: \"%s.bam\")", DEFAULT_OUTPUT_BAM_NAME));
        writer.println();
        writer.println("word:");
        writer.println("\tjava -jar HPOWorkbench.jar word -h <hpo> -t <start-term> \\");
        writer.println("\t<hpo>: path to hp.obo file");
        writer.println("\t<start-term>: HPO start term for the Word document");
        writer.println();
        writer.println("stats:");
        writer.println("\tjava -jar HPOWorkbench.jar stats -h <hpo> -a <pheno_annot.tab> -t <start-term> \\");
        writer.println("\t<hpo>: path to hp.obo file");
        writer.println("\t<pheno_annot.tab>: path to annotation file (default \"data/phenotype_annotation.tab\")");
        writer.println("\t<start-term>: HPO term of interest");
        writer.println();
        writer.println("git:");
        writer.println("\tjava -jar HPOWorkbench.jar git -g <label> ");
        writer.println("\t<label>: github label");
        writer.println();
        writer.println("batch:");
        writer.println("\tjava -jar HPOWorkbench.jar batch -i <file> -g <label> ");
        writer.println("\t<file>: CSV file with GitHub issues");
        writer.println("\t<label>: github label");
        writer.println();
        writer.close();
        System.exit(0);
    }

}
