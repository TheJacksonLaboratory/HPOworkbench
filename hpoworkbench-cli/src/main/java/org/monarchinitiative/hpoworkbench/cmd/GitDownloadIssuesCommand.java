package org.monarchinitiative.hpoworkbench.cmd;

import org.monarchinitiative.hpoworkbench.github.GitHubIssue;
import org.monarchinitiative.hpoworkbench.github.GitHubIssueRetriever;
import org.monarchinitiative.hpoworkbench.word.GitIssue2Doc4J;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;


@CommandLine.Command(name = "git",
        mixinStandardHelpOptions = true,
        description = "Download GitHub issues and create a Word doc.")
public class GitDownloadIssuesCommand extends HPOCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitDownloadIssuesCommand.class.getName());
    @CommandLine.Option(names={"-l","--label"},required = true,description = "git issue label")
    private  String issueLabel;

    @CommandLine.Option(names={"--closed"}, description = "if set, retrieve closed issues")
    Boolean closed;

    /**
     * Create a word document with up to 30 open issues for the label. This is intended to be used
     * to make a summary of open documents for collaborators but unfortunately is limited to up to
     * 30 GitHub issues.
     */
    public GitDownloadIssuesCommand() {
    }

    public Integer call() {
        LOGGER.trace("git get issues for " + issueLabel);

        GitHubIssueRetriever iretriever = new GitHubIssueRetriever(issueLabel, closed);
        List<GitHubIssue> issues = iretriever.getIssues();
        GitIssue2Doc4J gi2w = new GitIssue2Doc4J(issues,issueLabel);
        String filename=String.format("%s-open-issues.docx",issueLabel);
        gi2w.outputFile(filename);
        return 0;
    }

    public String getName(){ return "git";}
}
