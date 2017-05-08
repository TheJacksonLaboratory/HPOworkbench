package org.monarch.hpoapi.cmd;


import org.monarch.hpoapi.exception.HPOException;
import de.charite.compbio.jannovar.cmd.CommandLineParsingException;
import de.charite.compbio.jannovar.cmd.JannovarCommand;
import de.charite.compbio.jannovar.datasource.DataSourceFactory;
import de.charite.compbio.jannovar.datasource.DatasourceOptions;
import net.sourceforge.argparse4j.inf.Namespace;

public class DatabaseListCommand extends HPOCommand {

    /**
     * Configuration
     */
    private HPODBListOptions options;

    public DatabaseListCommand(String argv[], Namespace args) throws CommandLineParsingException {
        this.options = new HPODBListOptions();
        this.options.setFromArgs(args);
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