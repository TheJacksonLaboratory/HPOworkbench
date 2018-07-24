package org.monarchinitiative.hpoworkbench.cmd;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Post multiple issues to the HPO GitHub tracker. THe input is a CSV file with two columns -- the
 * issue title and the body. The user can pass one or more labels for the issues.
 * @author Peter Robinson
 */
public class BatchGitPostCommand extends HPOCommand {
    private static final Logger logger = Logger.getLogger(BatchGitPostCommand.class.getName());

    private final String issueLabel;
    private final String inputFilePath;


    /**
     * Create a word document with up to 30 open issues for the label. This is intended to be used
     * to make a summary of open documents for collaborators but unfortunately is limited to up to
     * 30 GitHub issues.
     * @param label GitHub label
     */
    public BatchGitPostCommand(String label, String inputFile) {
        this.issueLabel=label;
        this.inputFilePath=inputFile;

    }

    public void run() {
        logger.error("Running batch command with "+issueLabel + " and file " + inputFilePath);
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.inputFilePath));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public String getName(){ return "batch-git-post";}

}
