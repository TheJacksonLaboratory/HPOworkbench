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
public class PhenotypeDownloadOptions extends PhenotypeDBOptions {

    /** Path to download directory */
    private String downloadDir = null;

    /** Names of the databases to download */
    private List<String> databaseNames = new ArrayList<>();

    /**
     * Setup {@link ArgumentParser}
     *
     * @param subParsers
     *            {@link Subparsers} to setup
     */
    public static void setupParser(Subparsers subParsers) {
        BiFunction<String[], Namespace, DownloadCommand> handler = (argv, args) -> {
            try {
                return new DownloadCommand(argv, args);
            } catch (CommandLineParsingException e) {
                throw new UncheckedException("Could not parse command line", e);
            }
        };

        Subparser subParser = subParsers.addParser("download", true).help("download phenotype databases")
                .setDefault("cmd", handler);
        subParser.description("Download OBO/Association files for phenotype ontology");

        ArgumentGroup requiredGroup = subParser.addArgumentGroup("Required arguments");
        requiredGroup.addArgument("-d", "--database").help("Name of database to download, can be given multiple times")
                .setDefault(new ArrayList<String>()).action(Arguments.append()).required(true);

        ArgumentGroup optionalGroup = subParser.addArgumentGroup("Optional Arguments");
        optionalGroup.addArgument("-s", "--io-source-list").help("INI file with io source list")
                .setDefault(Lists.newArrayList("bundle:///default_sources.ini")).action(Arguments.append());
        optionalGroup.addArgument("--download-dir").help("Path to download directory").setDefault("io");

        PhenotypeBaseOptions.setupParser(subParser);
    }

    @Override
    public void setFromArgs(Namespace args) throws CommandLineParsingException {
        super.setFromArgs(args);
        System.err.println("setfrom args" + args);
        downloadDir = args.getString("download_dir");
        databaseNames = args.getList("database");
        for (String n : databaseNames){
            System.out.println("I GOT "+n);
        }
    }

    public String getDownloadDir() {
        return downloadDir;
    }

    public void setDownloadDir(String downloadDir) {
        this.downloadDir = downloadDir;
    }

    public List<String> getDatabaseNames() {
        return databaseNames;
    }

    public void setDatabaseNames(List<String> databaseNames) {
        this.databaseNames = databaseNames;
    }

    @Override
    public String toString() {
        return "PhenotypeDownloadOptions [downloadDir=" + downloadDir + ", getDataSourceFiles()=" + getDataSourceFiles()
                + ", isReportProgress()=" + isReportProgress() + ", getHttpProxy()=" + getHttpProxy()
                + ", getHttpsProxy()=" + getHttpsProxy() + ", getFtpProxy()=" + getFtpProxy() + "]";
    }

}