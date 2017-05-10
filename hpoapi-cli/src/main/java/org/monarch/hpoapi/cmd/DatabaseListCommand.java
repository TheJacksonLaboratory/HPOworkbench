package org.monarch.hpoapi.cmd;


import org.monarch.hpoapi.data.DataSourceFactory;
import org.monarch.hpoapi.data.DatasourceOptions;
import org.monarch.hpoapi.exception.HPOException;

import net.sourceforge.argparse4j.inf.Namespace;

public class DatabaseListCommand extends HPOCommand {

    /**
     * Configuration
     */
    private PhenotypeDBListOptions options;

    /**
     *
     * @param argv
     * @param args A wrapper around the command line arguments.
     * @throws CommandLineParsingException
     */
    public DatabaseListCommand(String argv[], Namespace args) throws CommandLineParsingException {
        this.options = new PhenotypeDBListOptions();
        this.options.setFromArgs(args);
        System.err.println("Namespace args: "+ args);
    }

    /**
     * Perform the downloading.
     */
    @Override
    public void run() throws HPOException {
        System.err.println("Options");
        System.err.println(options.toString());

        DatasourceOptions dsOptions = new DatasourceOptions(options.getHttpProxy(), options.getHttpsProxy(),
                options.getFtpProxy(), options.isReportProgress());

        DataSourceFactory factory = new DataSourceFactory(dsOptions, options.getDataSourceFiles());
        System.err.println("Available data sources:\n");
        for (String name : factory.getNames())
            System.err.println(String.format("    %s", name));
    }

}