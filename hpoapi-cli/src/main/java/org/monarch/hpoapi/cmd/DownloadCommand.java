package org.monarch.hpoapi.cmd;

import net.sourceforge.argparse4j.inf.Namespace;

import org.monarch.hpoapi.data.DataSource;
import org.monarch.hpoapi.data.DataSourceFactory;
import org.monarch.hpoapi.data.DatasourceOptions;
import org.monarch.hpoapi.data.PhenotypeData;
import org.monarch.hpoapi.exception.HPOException;
import org.monarch.hpoapi.util.PathUtil;


/**
 * Implementation of download step in HPOAPI.
 * Code modified from Download command in Jannovar.
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.0.1 (May 10, 2017)
 */
public final class DownloadCommand extends HPOCommand {

    private PhenotypeDownloadOptions options;

    public DownloadCommand(String argv[], Namespace args) throws CommandLineParsingException {
        this.options = new PhenotypeDownloadOptions();
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

        System.err.println("options:"+options);
        System.err.println("dsOptions:"+dsOptions);

        DataSourceFactory factory = new DataSourceFactory(dsOptions, options.dataSourceFiles);
        for (String name : options.getDatabaseNames()) {
            System.err.println("Downloading/parsing for io source \"" + name + "\"");
            DataSource ds = factory.getDataSource(name);
            System.err.println("Datasource : "+ds);
            PhenotypeData data = factory.getDataSource(name).getDataFactory().build(options.getDownloadDir(),
                    options.isReportProgress());
            String filename = PathUtil.join(options.getDownloadDir(),
                    name.replace('/', '_').replace('\\', '_') + ".ser");
            //JannovarDataSerializer serializer = new JannovarDataSerializer(filename);
            //serializer.save(io);
        }
    }

}