package org.monarchinitiative.hpoworkbench.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.log4j.Logger;
import org.monarchinitiative.hpoworkbench.github.GitHubPoster;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Post multiple issues to the HPO GitHub tracker. THe input is a CSV file with two columns -- the
 * issue title and the body. The user can pass one or more labels for the issues.
 * @author Peter Robinson
 */
@Parameters(commandDescription = "batch. Post a batch of GitHub issues to the HPO tracker.")
public class BatchGitPostCommand extends HPOCommand {
    private static final Logger logger = Logger.getLogger(BatchGitPostCommand.class.getName());
    @Parameter(names={"-l","--label"},required = true,description = "github issue label")
    private String issueLabel;
    @Parameter(names={"-i","--input-file"},required = true,description = "path to input file")
    private String inputFilePath;
    /** Github user name */
    @Parameter(names={"-u","--username"},required = true,description = "github username")
    private String gitUname;
    /** Github password */
    @Parameter(names={"-p","--password"},required = true,description = "github password")
    private String gitPword;
    @Parameter(names={"--forreal"},description="execute for real (otherwise, we do a dry run)")
    private boolean forReal=false;
    @Parameter(names={"--onentr"},description="just submit one NTR (for testing)")
    private boolean onentr=false;

    /**
     * Create a word document with up to 30 open issues for the label. This is intended to be used
     * to make a summary of open documents for collaborators but unfortunately is limited to up to
     * 30 GitHub issues.
     */
    public BatchGitPostCommand() {
    }

    public void run() {
        logger.error("Running batch command with "+issueLabel + " and file " + inputFilePath);
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.inputFilePath));
            String line;
            while ((line=br.readLine())!=null) {
                System.out.println(line);
                String[] F = line.split("\t");
                if (F.length<2) {
                    System.err.println("Malformed line, skipping: + line");
                    System.err.println("Input for batch-git must have two tab-separated fields");
                    continue;
                }
                String title = F[0];
                String messagebody=F[1].replaceAll("\\\\n","\n");
                GitHubPoster poster = new GitHubPoster(gitUname,gitPword, title, messagebody);
                List<String> labs  = new ArrayList<>();
                labs.add(this.issueLabel);
               /// labs.add("NIAID"); add as many as desired.
                poster.setLabel(labs);
                if (! forReal) {
                    poster.setDryRun();
                }
                try {
                    poster.postHpoIssue();
                    Thread.sleep(1000);
                    System.out.println("Issue: " + title + "; response=" + poster.getHttpResponse());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (onentr) {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public String getName(){ return "batch-git-post";}

}
