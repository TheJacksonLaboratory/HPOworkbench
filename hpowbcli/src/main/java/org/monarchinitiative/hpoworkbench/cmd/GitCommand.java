package org.monarchinitiative.hpoworkbench.cmd;

import org.apache.log4j.Logger;
import org.monarchinitiative.hpoworkbench.github.GitHubIssue;
import org.monarchinitiative.hpoworkbench.github.GitHubIssueRetriever;
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
        System.out.println("Now issues");
        GitHubIssueRetriever iretriever = new GitHubIssueRetriever();
        List<GitHubIssue> issues = iretriever.getIssues();
        int c=0;
        for (GitHubIssue issue : issues) {

            System.out.println(++c + ") "+issue.toString());
        }
    }

    public String getName(){ return "git";};
}
