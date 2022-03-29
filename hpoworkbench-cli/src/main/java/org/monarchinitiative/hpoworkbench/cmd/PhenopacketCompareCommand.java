package org.monarchinitiative.hpoworkbench.cmd;


import picocli.CommandLine;

import java.util.concurrent.Callable;


/**
 * To use this command, run the simonsq app to create a directory with
 * phenopackets. Pass the location of the identifier with the --identifier argument
 * (e.g., disease
 *  "diseases": [{
 *     "term": {
 *       "id": "MONDO:0013847",
 *       "label": "chromosome 16p11.2 duplication syndrome"
 *     }
 *   }],
 *   )
 *
 *   and pass --groupA MONDO:0013847 --groupB MONDO:0013848 etc.
 *
 * java -jar target/HPOworkbench.jar  compare
 * --phenopackets
 * /Users/robinp/IdeaProjects/simonsq/TMP
 * --identifier
 * disease
 * --groupA
 * MONDO:0013847
 * --groupB MONDO:0013267
 */
@CommandLine.Command(name = "phenopacketCompare",
        mixinStandardHelpOptions = true,
        description = "Compare two groups of patients in a colection of phenopackets")
public class PhenopacketCompareCommand implements Callable<Integer> {

    ///Users/robinp/IdeaProjects/simonsq
    @CommandLine.Option(names = {"-p", "--phenopackets"},required = true, description = "TSV file with terms")
    private String phenopacketDirectory;

    @CommandLine.Option(names = {"-i", "--identifier"},required = true, description = "name of phenopacket element that has the group name")
    private String identifier;

    @CommandLine.Option(names = {"-a", "--groupA"},required = true, description = "name of groupA")
    private String groupA;

    @CommandLine.Option(names = {"-b", "--groupB"},required = true, description = "name of groupA")
    private String groupB;

    @Override
    public Integer call() throws Exception {
        return null;
    }
}
