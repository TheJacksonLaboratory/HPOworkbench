package org.monarchinitiative.hpoapi.cmd;


import org.apache.log4j.Logger;
import org.monarchinitiative.hpoapi.argparser.ArgumentParserException;
import org.monarchinitiative.hpoapi.io.FileDownloader;

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
    private static Logger LOGGER = Logger.getLogger(DownloadCommand.class.getName());
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
        createDownloadDir(downloadDirectory);
        String downloadLocation=String.format("%s%shp.obo",downloadDirectory, File.separator);
        File f = new File(downloadLocation);
        try {
            URL url = new URL("https://raw.githubusercontent.com/obophenotype/human-phenotype-ontology/master/hp.obo");
            FileDownloader downloader = new FileDownloader();
            boolean result = downloader.copyURLToFile(url,f);
            if (result) {
                LOGGER.trace("Downloaded hp.obo to "+ downloadLocation);
            } else {
                LOGGER.error("Could not download hp.obo to " + downloadLocation);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
        // Now the same for the phenotype_annotation.tab file
        downloadLocation=String.format("%s%sphenotype_annotation.tab",downloadDirectory, File.separator);
        f = new File(downloadLocation);
        try {
            URL url = new URL("http://compbio.charite.de/jenkins/job/hpo.annotations/lastStableBuild/artifact/misc/phenotype_annotation.tab");
            FileDownloader downloader = new FileDownloader();
            boolean result = downloader.copyURLToFile(url,f);
            if (result) {
                LOGGER.trace("Downloaded phenotype_annotation.tab to "+ downloadLocation);
            } else {
                LOGGER.error("[ERROR] Could not phenotype_annotation.tab hp.obo to " + downloadLocation);
            }

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Todo make robust
     * @param dir
     */
    private void createDownloadDir(String dir) {
        LOGGER.trace("creating download dir (and deleting previous version) at "+ dir);
        File d =new File(dir);
        if (d.exists()) {
            d.delete();
        }
        d.mkdir();
    }

}