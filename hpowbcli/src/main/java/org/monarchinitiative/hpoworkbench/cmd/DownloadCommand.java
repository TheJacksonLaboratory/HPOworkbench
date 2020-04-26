package org.monarchinitiative.hpoworkbench.cmd;


import com.beust.jcommander.Parameters;
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
@Parameters(commandDescription = "download. Download HPO/MONDO/medgen files.")
public final class DownloadCommand extends HPOCommand {
    private static final Logger LOGGER = Logger.getLogger(DownloadCommand.class.getName());


    private final String PHENOTYPE_HPOA_URL="http://compbio.charite.de/jenkins/job/hpo.annotations.current/lastSuccessfulBuild/artifact/current/phenotype.hpoa";

    public String getName() { return "download"; }

    /**

     */
    public DownloadCommand()  {
    }

    /**
     * Perform the downloading.
     */
    @Override
    public void run()  {
        createDownloadDir(downloadDirectory);
        downloadHpObo();
        downloadPhenotypeDotHpoa();
        downloadMedgen();
        downloadGeneInfo();
    }

    /** Download the phenotype.hpoa file. */
    private void downloadPhenotypeDotHpoa() {
        // Now the same for the phenotype.hpoa file
        String downloadLocation=String.format("%s%sphenotype.hpoa",downloadDirectory, File.separator);
        File f = new File(downloadLocation);
        if (f.exists()) {
            LOGGER.trace("cowardly refusing to download phenotype.hpoa, since it is already there");
            return;
        }
        try {
            URL url = new URL(PHENOTYPE_HPOA_URL);
            FileDownloader downloader = new FileDownloader();
            boolean result = downloader.copyURLToFile(url,f);
            if (result) {
                LOGGER.trace("Downloaded phenotype.hpoa to "+ downloadLocation);
            } else {
                LOGGER.error("[ERROR] Could not download phenotype.hpoa to " + downloadLocation);
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

    private void downloadMedgen() {
        final  String MIM2GENE_MEDGEN_URL = "ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/mim2gene_medgen";
        String filename = "mim2gene_medgen";
        String downloadLocation=String.format("%s%s%s",downloadDirectory, File.separator,filename);
        File f = new File(downloadLocation);
        try {
            URL url = new URL(MIM2GENE_MEDGEN_URL);
            FileDownloader downloader = new FileDownloader();
            boolean result = downloader.copyURLToFile(url,f);
            if (result) {
                LOGGER.trace("Downloaded mim2gene_medgen to "+ downloadLocation);
            } else {
                LOGGER.error("Could not download mim2gene_medgen to " + downloadLocation);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private void downloadGeneInfo() {
         final  String GENE_INFO = "Homo_sapiens_gene_info.gz";
         final  String GENE_INFO_URL = "ftp://ftp.ncbi.nih.gov/gene/DATA/GENE_INFO/Mammalia/Homo_sapiens.gene_info.gz";
        String downloadLocation=String.format("%s%s%s",downloadDirectory, File.separator,GENE_INFO);
        File f = new File(downloadLocation);
        try {
            URL url = new URL(GENE_INFO_URL);
            FileDownloader downloader = new FileDownloader();
            boolean result = downloader.copyURLToFile(url,f);
            if (result) {
                LOGGER.trace("Downloaded gene info to "+ downloadLocation);
            } else {
                LOGGER.error("Could not download gene info to " + downloadLocation);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }





    /**
     * @param dir directory to which we will download files (default: 'data')
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