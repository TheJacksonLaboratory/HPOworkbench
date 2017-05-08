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
import org.monarch.hpoapi.HPOAPI;


/**
 * Configuration for the <tt>download</tt> command
 *
 * @author Peter Robinson, adapted from Jannovar code by <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
public class HPODownloadOptions extends HPOAPIDBOptions {

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
                //throw new UncheckedJannovarException("Could not parse command line", e);
                System.err.println("Could not parse command line");
                e.printStackTrace();
                System.exit(1);
            }
        };

        Subparser subParser = subParsers.addParser("download", true).help("download transcript databases")
                .setDefault("cmd", handler);
        subParser.description("Download transcript database");

        ArgumentGroup requiredGroup = subParser.addArgumentGroup("Required arguments");
        requiredGroup.addArgument("-d", "--database").help("Name of database to download, can be given multiple times")
                .setDefault(new ArrayList<String>()).action(Arguments.append()).required(true);

        ArgumentGroup optionalGroup = subParser.addArgumentGroup("Optional Arguments");
        optionalGroup.addArgument("-s", "--data-source-list").help("INI file with data source list")
                .setDefault(Lists.newArrayList("bundle:///default_sources.ini")).action(Arguments.append());
        optionalGroup.addArgument("--download-dir").help("Path to download directory").setDefault("data");

        HPOAPIBaseOptions.setupParser(subParser);
    }

    @Override
    public void setFromArgs(Namespace args) throws CommandLineParsingException {
        super.setFromArgs(args);

        downloadDir = args.getString("download_dir");
        databaseNames = args.getList("database");
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
        return "HPODownloadOptions [downloadDir=" + downloadDir + ", getDataSourceFiles()=" + getDataSourceFiles()
                + ", isReportProgress()=" + isReportProgress() + ", getHttpProxy()=" + getHttpProxy()
                + ", getHttpsProxy()=" + getHttpsProxy() + ", getFtpProxy()=" + getFtpProxy() + "]";
    }

}