package org.monarchinitiative.hpoworkbench.analysis;

import org.monarchinitiative.phenol.annotations.hpo.HpoAnnotationFileParser;
import org.monarchinitiative.phenol.annotations.hpo.HpoAnnotationModel;
import org.monarchinitiative.phenol.ontology.data.Ontology;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SmallFileQc {

    private final Ontology ontology;
    private final String smallFilePath;

    private final String OUTFILENAME = "smallfile-qc.txt";

    private final List<String> errors;


    public SmallFileQc(String smallFilePath, Ontology ontology) {
        this.smallFilePath = smallFilePath;
        this.ontology = ontology;
        errors = new ArrayList<>();
    }

    public void doQualityControl()  {
        File f = new File(this.smallFilePath);
        System.out.println("Searching for small files in " + f.getAbsolutePath());
        String[] pathnames = f.list();
        if (pathnames == null || pathnames.length == 0) {
            System.err.println("Could not extract small files from " + smallFilePath);
            return;
        }
        int n_no_errors = 0;
        int total_omim_files = 0;
        for (String smallFile : pathnames) {
            if (! smallFile.contains("OMIM")) {
                continue;
            }
            total_omim_files++;
            File path = new File(this.smallFilePath + File.separator + smallFile);
            HpoAnnotationFileParser parser = new HpoAnnotationFileParser(path, ontology);
            try {
                HpoAnnotationModel model = parser.parse();
                if (parser.hasErrors()) {
                    errors.addAll(parser.getParseErrors());
                } else {
                    n_no_errors++;                }
            } catch (Exception e) {
                String [] lines =  e.getMessage().split("\n");
                errors.addAll(Arrays.asList(lines));
            }
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(OUTFILENAME))) {
            for (String line : errors) {
                line = line.replace(this.smallFilePath, "");
                bw.write(line + "\n");
            }
            bw.write(String.format("No errors found in %d/%d files.\n", n_no_errors, total_omim_files));
            System.out.printf("No errors found in %d/%d files.\n", n_no_errors, total_omim_files);
        } catch (IOException e) {
            e.printStackTrace();
        }




    }

}
