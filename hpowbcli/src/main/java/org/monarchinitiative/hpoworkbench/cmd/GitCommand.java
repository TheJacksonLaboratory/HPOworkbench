package org.monarchinitiative.hpoworkbench.cmd;

import org.apache.log4j.Logger;
import org.monarchinitiative.hpoworkbench.github.GitHubIssue;
import org.monarchinitiative.hpoworkbench.github.GitHubIssueRetriever;
import org.monarchinitiative.hpoworkbench.github.GitHubLabelRetriever;
import org.monarchinitiative.hpoworkbench.word.GitIssue2Doc4J;
//import org.monarchinitiative.hpoworkbench.word.GitIssue2Word;

import java.util.List;

public class GitCommand  extends HPOCommand {
    private static Logger LOGGER = Logger.getLogger(DownloadCommand.class.getName());

    private final String issueLabel;

    /**
     * Create a word document with up to 30 open issues for the label. This is intended to be used
     * to make a summary of open documents for collaborators but unfortunately is limited to up to
     * 30 GitHub issues.
     * @param label GitHub label
     */
    public GitCommand(String label) {
        issueLabel=label;
    }

    public void run() {
        LOGGER.trace("git get issues for " + issueLabel);

        GitHubIssueRetriever iretriever = new GitHubIssueRetriever(issueLabel);
        List<GitHubIssue> issues = iretriever.getIssues();
        //GitIssue2Word gi2w = new GitIssue2Word(issues, issueLabel);
        GitIssue2Doc4J gi2w = new GitIssue2Doc4J(issues,issueLabel);
        String filename=String.format("%s-open-issues.docx",issueLabel);
        gi2w.outputFile(filename);
    }

    public String getName(){ return "git";}
}
