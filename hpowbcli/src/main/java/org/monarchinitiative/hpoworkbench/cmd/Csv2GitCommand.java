package org.monarchinitiative.hpoworkbench.cmd;


import org.apache.log4j.Logger;
import org.monarchinitiative.hpoworkbench.github.GitHubPoster;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Csv2GitCommand  extends HPOCommand {
    private static Logger LOGGER = Logger.getLogger(Csv2GitCommand.class.getName());
    /** GitHub username. */
    private String username;
    /** GitHub password. */
    private String password;

    private String githubLabel=null;

    private String csvFile=null;

    private boolean forreal=false;


    public Csv2GitCommand(String csvpath, String label, String user, String pw, boolean forreal) {
        this.csvFile=csvpath;
        this.githubLabel=label.trim();
        this.username=user;
        this.password=pw;
        this.forreal=forreal;
    }

    /**
     * Perform the downloading.
     */
    @Override
    public void run()  {
        int successful_items=0;
        Map<String,String> items = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            String line;
            while ((line=br.readLine())!=null) {
                //System.out.println(line);
                String A[]=line.split("\t");
                if (A.length!=2) {
                    LOGGER.error("Malformed line "+line);
                    LOGGER.error("Will skip line...");
                }
                GitHubPoster poster = new GitHubPoster(username, password, A[0].trim(),A[1].trim().replaceAll("\\\\n","\n"));
                poster.setLabel(githubLabel);
                if (forreal) {
                    try {
                        poster.postIssue();
                        successful_items++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println(poster.dryRun());
                }
            }
            if (forreal) {
                LOGGER.trace(String.format("Successfully posted %d issues to GitHub",successful_items ));
            } else {
                System.out.println();
                System.out.println("###  use -g flag to post these items to github");
                System.out.println();
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    @Override public String getName() { return "csv2git";}
}
