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

import java.io.BufferedWriter;
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

    private int DISEASE_ID_INDEX;
    private int DISEASE_NAME_INDEX;
    private int GENE_ID_INDEX;
    private int GENE_NAME_INDEX;
    private int GENOTYPE_INDEX;
    private int GENE_SYMBOL_INDEX;
    private int PHENOTYPE_ID_INDEX;
    private int PHENOTYPE_NAME_INDEX;
    private int AGE_OF_ONSET_ID_INDEX;
    private int AGE_OF_ONSET_NAME_INDEX;
    private int EVIDENCE_ID_INDEX;
    private int EVIDENCE_NAME_INDEX;
    private int FREQUENCY_INDEX;
    private int SEX_ID_INDEX;
    private int SEX_NAME_INDEX;
    private int NEGATION_ID_INDEX;
    private int NEGATION_NAME_INDEX;
    private int DESCRIPTION_INDEX;
    private int PUB_INDEX;
    private int ASSIGNED_BY_INDEX;
    private int DATE_CREATED_INDEX;

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
        logger.trace("We will convert the small files at " + pathToSmallFileDir);
        List<String> files=getListOfSmallFiles();
        logger.trace("We found " + files.size() + " small files");
        Map<String,Integer> descriptionCount=new HashMap<>();
        try {
            out = new BufferedWriter(new FileWriter("small-file.log"));
            int c=1;
            for (String path : files ) {
                if (c==0) {/*getFirstEntry(path); TODO */ }
                else {
                    c++;
                    OldSmallFile osf = new OldSmallFile(path);
                   // osfList.add(osf);
                   // logger.error("Got total of " + osfList.size() + " small files");
                    //System.exit(3);
                  // if (c>250)break;
                    List<OldSmallFileEntry> osfe = osf.getEntrylist();
                    for (OldSmallFileEntry entry : osfe) {
                        if (!descriptionCount.containsKey(entry.getDescription())) {
                            descriptionCount.put(entry.getDescription(),0);
                        }
                        descriptionCount.put(entry.getDescription(),descriptionCount.get(entry.getDescription())+1);
                    }
                }
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        for (String s : descriptionCount.keySet()) {
            System.out.println(descriptionCount.get(s)+": "+s );
        }
       // convertToNewSmallFiles();
    }

    private void convertToNewSmallFiles() {
        osfList.stream().forEach(old -> {
            V2SmallFile v2 = new V2SmallFile(old);
            v2sfList.add(v2);
        });

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
