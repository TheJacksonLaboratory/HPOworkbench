package org.monarch.hpoapi.data;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.ini4j.Profile.Section;
//import org.monarch.hpoapi.association.Association;
import org.monarch.hpoapi.association.AssociationContainer;
import org.monarch.hpoapi.io.FileDownloadException;
import org.monarch.hpoapi.io.FileDownloader;
import org.monarch.hpoapi.io.OBOParserException;
import org.monarch.hpoapi.ontology.Ontology;
import org.monarch.hpoapi.util.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import org.monarch.hpoapi.io.FileDownloader.ProxyOptions;



/**
 * Interface for io factories, allowing to create {@link PhenotypeData} objects from {@link DataSource}s.
 * Phenotype io consists of an OBO ontology as well as Annotations.
 * We are now (May 9, 2017) implementing this for just the HPO, but inted to extend this so
 * that it will also work with MPO and potentially other phenotype ontologies and would like to use a
 * single interface.
 * This code is based on JannovarDataFactory for Jannovar by Manuel Holtgrewe.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public abstract class PhenotypeDataFactory {

    /**
     * the logger object to use
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PhenotypeDataFactory.class);

    /**
     * the {@link DatasourceOptions} to use for proxy settings
     */
    protected final DatasourceOptions options;
    /**
     * the {@link DataSource} to use
     */
    protected final DataSource dataSource;
    /**
     * configuration section from INI file
     */
    protected final Section iniSection;

    /**
     * Construct the factory with the given {@link DataSource}.
     *
     * @param options    configuration for proxy settings
     * @param dataSource the io source to use.
     * @param iniSection {@link Section} with configuration from INI file
     */
    public PhenotypeDataFactory(DatasourceOptions options, DataSource dataSource, Section iniSection) {
        this.options = options;
        this.dataSource = dataSource;
        this.iniSection = iniSection;
    }

    /**
     * @param downloadDir       path of directory to download files to
     * @param printProgressBars whether or not to print progress bars
     * @return {@link PhenotypeData} object for the factory's state.
     * @throws InvalidDataSourceException on problems with the io source or io source file
     * @throws FileDownloadException      on problems while downloading files.
     */
    public final PhenotypeData build(String downloadDir, boolean printProgressBars)
            throws InvalidDataSourceException, FileDownloadException {
        String targetDir = PathUtil.join(downloadDir, dataSource.getName());

        FileDownloader downloader = new FileDownloader(buildOptions(printProgressBars));

        // Download files.
        LOGGER.info("Downloading io...");
        try {
            for (String url : dataSource.getDownloadURLs()) {
                LOGGER.info("Downloading {}", url);
                URL src = new URL(url);
                String fileName = new File(src.getPath()).getName();
                File dest = new File(PathUtil.join(targetDir, fileName));
                downloader.copyURLToFile(src, dest);

                if (dest.getName().endsWith(".gz")) {
                    checkGZ(dest);
                    LOGGER.info("Downloaded file {} looks like a valid gzip'ed file", new Object[]{dest.getName()});
                }
            }
        } catch (MalformedURLException e) {
            throw new FileDownloadException("Invalid URL.", e);
        }

        // Parse files for building Ontology objects.
        LOGGER.info("Building Ontology...");
        System.out.println("Datasource: "+dataSource);
        final String oboPath = PathUtil.join(downloadDir, dataSource.getName(),
                dataSource.getFileName("obo"));
        final String associationsPath = PathUtil.join(downloadDir, dataSource.getName(),
                dataSource.getFileName("annotation"));
        Ontology ontology=null;
        AssociationContainer container=null;
        try {
            LOGGER.info("Parsing obo file...");
            ontology = parseOntology(oboPath);
            LOGGER.info("Parsing associations...");
            container = parseAssociations(associationsPath);
        } catch (OBOParserException e) {
            e.printStackTrace();
            System.exit(1); //TOOD -- clean this up
        }
        return new PhenotypeData(ontology, container);
    }

    /**
     * Check whether the given file is a valid gzip file.
     *
     * @throws FileDownloadException in case of problems with downloaded gzip file
     */
    private void checkGZ(File dest) throws FileDownloadException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(dest.getAbsolutePath()))) {
            in.mark(2);
            int magic = 0;
            magic = in.read() & 0xff | ((in.read() << 8) & 0xff00);
            in.reset();
            if (magic != GZIPInputStream.GZIP_MAGIC) {
                throw new FileDownloadException("The downloaded file " + dest.getAbsolutePath()
                        + " is not a valid gzip file. " + "Is your proxy configuration correct?");
            }
        } catch (FileNotFoundException e) {
            throw new FileDownloadException("File " + dest.getAbsolutePath() + " not found. Did the download fail?", e);
        } catch (IOException e) {
            throw new FileDownloadException(
                    "Reading from " + dest.getAbsolutePath() + " failed. Did the download fail?", e);
        }
    }

    /**
     * @return {@link FileDownloader.Options} with proxy settings from {@link #options} and environment.
     */
    private FileDownloader.Options buildOptions(boolean printProgressBars) {
        FileDownloader.Options result = new FileDownloader.Options();

        // Get proxy settings from options.
        result.printProgressBar = printProgressBars;
        updateProxyOptions(result.http, options.getHTTPProxy());
        updateProxyOptions(result.https, options.getHTTPSProxy());
        updateProxyOptions(result.ftp, options.getFTPProxy());

        return result;
    }

    private void updateProxyOptions(ProxyOptions proxyOptions, URL url) {
        if (url != null && url.getHost() != null && !url.getHost().equals("")) {
            proxyOptions.host = url.getHost();
            proxyOptions.port = url.getPort();
            if (proxyOptions.port == -1)
                proxyOptions.port = 80;
            String userInfo = url.getUserInfo();
            if (userInfo != null && userInfo.indexOf(':') != -1) {
                String[] userPass = userInfo.split(":", 2);
                proxyOptions.user = userPass[0];
                proxyOptions.password = userPass[1];
            }
        }
    }


    /**
     * @param targetDir
     *            path where the downloaded files are
     * @return {@link Ontology} object representing a phenotype ontology
     * * @throws OBOParserException
     *             on problems with parsing the obo file
     */
    protected abstract Ontology parseOntology(String targetDir)
            throws OBOParserException;

    /**
     * @param targetDir
     *            path where the downloaded files are
     * @return {@link AssociationContainer} object representing list of associations to a phenotype ontology
     * @throws OBOParserException
     *             on problems with parsing the obo file
     */
    protected abstract AssociationContainer parseAssociations(String targetDir)
            throws OBOParserException;
}
