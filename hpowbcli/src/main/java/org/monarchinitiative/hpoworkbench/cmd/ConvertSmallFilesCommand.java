package org.monarchinitiative.hpoworkbench.cmd;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.formats.hpo.HpoTermRelation;
import com.github.phenomics.ontolib.ontology.data.Ontology;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.io.HpoOntologyParser;
import org.monarchinitiative.hpoworkbench.smallfile.OldSmallFile;
import org.monarchinitiative.hpoworkbench.smallfile.OldSmallFileEntry;
import org.monarchinitiative.hpoworkbench.smallfile.V2SmallFile;
import org.monarchinitiative.hpoworkbench.smallfile.V2SmallFileEntry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Created by peter on 1/20/2018.
 * This command coordinates the conversion of the "small" annotation files from the old format (pre 2018) to
 * the new format and does some Q/C.
 * FOrmat should be something line this
 *  Disease ID	Disease Name	Gene ID	Gene Name	Genotype	Gene Symbol(s)	Phenotype ID	Phenotype Name
 Age of Onset ID	Age of Onset Name	Evidence ID	Evidence Name	Frequency	Sex ID	Sex Name	Negation ID
 Negation Name	Description	Pub	Assigned by	Date Created
 */

public class ConvertSmallFilesCommand  extends HPOCommand {
    private static final Logger logger = LogManager.getLogger();
    /** name of this command */
    private final static String name = "convertSmallFiles";

    private BufferedWriter out;

    private int n_corrected_date=0;
    private int n_no_evidence=0;
    private int n_gene_data=0;
    private int n_alt_id=0;
    private int n_update_label=0;
    private int n_created_modifier=0;
    private int n_EQ_item=0;


    private HpoOntology ontology=null;
    private Ontology<HpoTerm, HpoTermRelation> inheritanceSubontology=null;
    private Ontology<HpoTerm, HpoTermRelation> abnormalPhenoSubOntology=null;

    private List<OldSmallFile> osfList=new ArrayList<>();

    private List<V2SmallFile> v2sfList = new ArrayList<>();

    public String getName() { return name; }

    private final String pathToSmallFileDir;

    public ConvertSmallFilesCommand(String path, String hpoOboPath) {
        this.pathToSmallFileDir=path;
        initOntology(hpoOboPath);
    }

    /** Parse the hp.obo file. Set the static ontology variables in OldSmallFileEntry that we will
     * use to check the entries.
     * @param path
     */
    private void initOntology(String path) {
        if (path==null) {
            logger.fatal("Cannot start app, ontology path is null");
            System.exit(1);
        }
        try {
            HpoOntologyParser parser = new HpoOntologyParser(path);
            ontology = parser.getOntology();
            inheritanceSubontology = parser.getInheritanceSubontology();
            abnormalPhenoSubOntology = parser.getPhenotypeSubontology();
            OldSmallFileEntry.setOntology(ontology, inheritanceSubontology, abnormalPhenoSubOntology);
        } catch (Exception e) {
            logger.fatal(String.format("Could not parse ontology file at %s",path));
            logger.fatal(e.getMessage());
        }
    }

    public void run() {
        if (ontology==null) {
            logger.fatal("We were unable to initialize the Ontology object and will terminate this program...");
            System.exit(1);
        }
        List<String> files=getListOfSmallFiles();
        logger.trace("We found " + files.size() + " small files at " + pathToSmallFileDir);
        Map<String, Integer> descriptionCount = new HashMap<>();
        try {
            out = new BufferedWriter(new FileWriter("small-file.log"));
            int c = 1;
            for (String path : files) {
                OldSmallFile osf = new OldSmallFile(path);
                this.n_alt_id += osf.getN_alt_id();
                this.n_corrected_date += osf.getN_corrected_date();
                n_no_evidence+= osf.getN_no_evidence();
                n_gene_data+= osf.getN_gene_data();
                n_update_label += osf.getN_update_label();
                n_created_modifier += osf.getN_created_modifier();
                n_EQ_item+= osf.getN_EQ_item();

                osfList.add(osf);
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

       convertToNewSmallFiles();
        dumpQCtoShell();
    }

    private void convertToNewSmallFiles() {
        osfList.stream().forEach(old -> {
            V2SmallFile v2 = new V2SmallFile(old);
            v2sfList.add(v2);
        });
        try {
            for (V2SmallFile v2 : v2sfList) {
                outputV2file(v2);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    private void dumpQCtoShell() {
        System.out.println("\n\n################################################\n\n");
        System.out.println(String.format("We converted %d \"old\" small files into %d new (V2) small files",
                osfList.size(),v2sfList.size()));
        System.out.println();
        System.out.println("Summary of Q/C results:");
        System.out.println("\tNumber of lines with corrected date formats: " + n_corrected_date);
        System.out.println("\tNumber of lines with \"Gene\" data that was discarded for the V2 files: " + n_gene_data);
        System.out.println("\tNumber of lines with \"E/Q\" data that was discarded for the V2 files: " + n_EQ_item);
        System.out.println("\tNumber of lines with alt_ids updated to current ids: " + n_alt_id);
        System.out.println("\tNumber of lines with labels updated to current labels: " + n_update_label);
        System.out.println("\tNumber of lines for which no Evidence code was found: "+ n_no_evidence);
        System.out.println("\tNumber of lines for which a Clinical modifer was extracted: "+n_created_modifier);
       System.out.println();
        System.out.println("Lines that were Q/C'd or updated have been written to the log (before/after)");
        System.out.println();
    }


    private void outputV2file(V2SmallFile v2) throws IOException {
        String outdir="v2files";
        if (! new File(outdir).exists()) {
            new File(outdir).mkdir();
        }
        String filename = String.format("%s%s%s",outdir,File.separator,v2.getBasename());
        logger.trace("Writing v2 to file " + filename);
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(V2SmallFileEntry.getHeader()+"\n");
        List<V2SmallFileEntry> entryList = v2.getEntryList();
        for (V2SmallFileEntry v2e:entryList) {
            writer.write(v2e.getRow() + "\n");
        }
        writer.close();

    }




    /** The purpose of this function is to set up an "archetype" of the OldEntries. We will compare all
     * other small files against this one. If all is well, then all files with be compatible.
     * @param path
     */
    public void getFirstEntry(String path) throws IOException {
        out.write("First old small file was " + path);
        OldSmallFile osf = new OldSmallFile(path);
        int n_fields = osf.getN_fields();
        out.write("Number of fields="+n_fields);
    }


    public List<String> getListOfSmallFiles() {
        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(pathToSmallFileDir))) {
            for (Path path : directoryStream) {
                if (path.toString().endsWith(".tab")) {
                    fileNames.add(path.toString());
                }
               // fileNames.add(path.toString());
            }
        } catch (IOException ex) {
        }
        return fileNames;
    }
}
