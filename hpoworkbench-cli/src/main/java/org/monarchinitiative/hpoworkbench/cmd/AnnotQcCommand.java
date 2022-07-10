package org.monarchinitiative.hpoworkbench.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "qc",
        mixinStandardHelpOptions = true,
        description = "QC for annotations.")
public class AnnotQcCommand implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadCommand.class.getName());

    @CommandLine.Option(names={"-s","--small"},required = true,description = "Small File Dir")
    private String smallFileDir;

    @Override
    public Integer call() throws Exception {
        File folder = new File(smallFileDir);
        if (! folder.isDirectory()) {
            System.err.println("Need to pass directory with small files");
            return 1;
        }

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile() && fileEntry.getAbsolutePath().endsWith(".tab")) {
                try (BufferedReader br = new BufferedReader(new FileReader(fileEntry))){
                     String line;
                     while ((line = br.readLine()) != null) {
                         if (line.startsWith("#"))
                             continue;
                         String [] fields = line.split("\t");
                         String freq = fields[6];
                         String id = fields[0];
                         if (freq.startsWith("/")) {
                             System.out.println(id + ": " + freq);
                         }
                         if (freq.endsWith("/")) {
                             System.out.println(id + ": " + freq);
                         }

                     }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return 0;
    }
}
