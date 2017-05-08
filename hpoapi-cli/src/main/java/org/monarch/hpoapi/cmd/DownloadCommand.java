package org.monarch.hpoapi.cmd;


import org.monarch.hpoapi.exception.HPOException;

//import de.charite.compbio.jannovar.data.JannovarData;
//import de.charite.compbio.jannovar.data.JannovarDataSerializer;
//import de.charite.compbio.jannovar.datasource.DataSourceFactory;
//import de.charite.compbio.jannovar.datasource.DatasourceOptions;
import org.monarch.hpoapi.util.PathUtil;
import net.sourceforge.argparse4j.inf.Namespace;



/**
 * Implementation of download step in Jannovar.
 *
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 */
public final class DownloadCommand extends HPOCommand {

    private HPODownloadOptions options;

    public DownloadCommand(String argv[], Namespace args) throws CommandLineParsingException {
        this.options = new HPODownloadOptions();
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

        DataSourceFactory factory = new DataSourceFactory(dsOptions, options.dataSourceFiles);
        for (String name : options.getDatabaseNames()) {
            System.err.println("Downloading/parsing for data source \"" + name + "\"");
            JannovarData data = factory.getDataSource(name).getDataFactory().build(options.getDownloadDir(),
                    options.isReportProgress());
            String filename = PathUtil.join(options.getDownloadDir(),
                    name.replace('/', '_').replace('\\', '_') + ".ser");
            JannovarDataSerializer serializer = new JannovarDataSerializer(filename);
            serializer.save(data);
        }
    }

}