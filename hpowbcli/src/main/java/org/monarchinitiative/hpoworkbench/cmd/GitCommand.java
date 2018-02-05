package org.monarchinitiative.hpoworkbench.cmd;

import org.apache.log4j.Logger;
import org.monarchinitiative.hpoworkbench.github.GitHubLabelRetriever;

import java.util.List;

public class GitCommand  extends HPOCommand {
    private static Logger LOGGER = Logger.getLogger(DownloadCommand.class.getName());


    public GitCommand() {

    }

    public void run() {
        LOGGER.trace("git run");
        GitHubLabelRetriever retriever=new GitHubLabelRetriever();
        List<String> labels = retriever.getLabels();
        for (String label: labels) {
            System.out.println(label);
        }
    }

    public String getName(){ return "git";};
}
