package org.monarchinitiative.hpoworkbench.cmd;


import org.apache.log4j.Logger;
import org.monarchinitiative.hpoworkbench.io.FileDownloader;

import java.io.File;
import java.net.URL;


/**
 * Implementation of download in HpoWorkbench. The command is intended to download
 * both the OBO file and the association file. For HPO, this is {@code hp.obo} and
 * {@code phenotype_annotation.tab}.
 * Code modified from Download command in Jannovar.
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.0.1 (May 10, 2017)
 */
public final class DownloadCommand extends HPOCommand {
    private static Logger LOGGER = Logger.getLogger(DownloadCommand.class.getName());
    private final String downloadDirectory;

    public String getName() { return "download"; }

    /**

     */
    public DownloadCommand(String downloadDir)  {
        this.downloadDirectory=downloadDir;
    }

    /**
     * Perform the downloading.
     */
    @Override
    public void run()  {
        createDownloadDir(downloadDirectory);
        downloadHpObo();
        downloadPhenotypeAnnotationDotTab();
    }


    private void downloadPhenotypeAnnotationDotTab() {
        // Now the same for the phenotype.hpoa file
        String downloadLocation=String.format("%s%sphenotype.hpoa",downloadDirectory, File.separator);
        File f = new File(downloadLocation);
        if (f.exists()) {
            LOGGER.trace("cowardly refusing to download phenotype.hpoa, since it is already there");
            return;
        }
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


    private void downloadHpObo() {
        String downloadLocation=String.format("%s%shp.obo",downloadDirectory, File.separator);
        File f = new File(downloadLocation);
        if (f.exists()) {
            LOGGER.trace("cowardly refusing to download hp.obo, since it is already there");
            return;
        }
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