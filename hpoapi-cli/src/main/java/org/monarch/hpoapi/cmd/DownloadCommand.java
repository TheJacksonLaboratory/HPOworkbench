package org.monarch.hpoapi.cmd;


import org.monarch.hpoapi.argparser.ArgumentParserException;
import org.monarch.hpoapi.data.DataSource;
import org.monarch.hpoapi.data.DataSourceFactory;
import org.monarch.hpoapi.data.DatasourceOptions;
import org.monarch.hpoapi.data.PhenotypeData;
import org.monarch.hpoapi.exception.HPOException;
import org.monarch.hpoapi.io.FileDownloader;
import org.monarch.hpoapi.util.PathUtil;

import java.io.File;
import java.net.URL;
import java.util.Map;


/**
 * Implementation of download in HPOAPI. The command is intended to download
 * both the OBO file and the association file. For HPO, this is {@code hp.obo} and
 * {@code phenotype_annotation.tab}.
 * Code modified from Download command in Jannovar.
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.0.1 (May 10, 2017)
 */
public final class DownloadCommand extends HPOCommand {

    private String downloadDirectory=null;

    public String getName() { return "download"; }

    public void setOptions(Map<String,String> mp) throws ArgumentParserException {
        if (mp.containsKey("directory")) {
            this.downloadDirectory=mp.get("directory");
        }
    }

    /**

     */
    public DownloadCommand()  {

    }

    /**
     * Perform the downloading.
     */
    @Override
    public void run()  {
        if (downloadDirectory==null) {
            downloadDirectory=defaults.get("directory");
        }
        String downloadLocation=String.format("%s%shp.obo",downloadDirectory, File.separator);
        File f = new File(downloadLocation);
        try {
            URL url = new URL("https://raw.githubusercontent.com/obophenotype/human-phenotype-ontology/master/hp.obo");
            FileDownloader downloader = new FileDownloader();
            boolean result = downloader.copyURLToFile(url,f);
            if (result) {
                System.out.println("[INFO] Downloaded hp.obo to "+ downloadLocation);
            } else {
                System.out.println("[ERROR] Could not download hp.obo to " + downloadLocation);
            }

        } catch (Exception e){
            e.printStackTrace();
        }


    }

}