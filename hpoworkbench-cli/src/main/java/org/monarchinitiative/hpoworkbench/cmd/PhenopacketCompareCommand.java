package org.monarchinitiative.hpoworkbench.cmd;


import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "phenopacketCompare",
        mixinStandardHelpOptions = true,
        description = "Compare two groups of patients in a colection of phenopackets")
public class PhenopacketCompareCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        return null;
    }
}
