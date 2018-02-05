package org.monarchinitiative.hpoworkbench.github;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class encapsulates information about an issue and will be used to create a summary of the issue for
 * output on the command line or in a dialog.
 */
public class GitHubIssue {
    private final String title;
    private final String body;
    private final String label;
    private final List<String> comments;

    GitHubIssue(String title,String body, String label, List<String> comments){
        this.title=title;
        this.body=body;
        this.label=label;
        this.comments=comments;
    }


    public String toString() {
        return String.format("%s: %s [%s] %s",title,body,label,comments.stream().collect(Collectors.joining(";")));
    }




    public static class Builder {
        private final String title;
        private String body="";
        private String label="none";
        List<String> comments = new ArrayList<>();

        public Builder(String title){
            this.title=title;
        }

        public Builder body(String b) {
            body=b;
            return this;
        }

        public Builder label(String l) {
            label=l;
            return this;
        }

        public Builder comment(String c) {
            comments.add(c);
            return this;
        }

        public Builder comments(List<String> c) {
            comments.addAll(c);
            return this;
        }

        public GitHubIssue build() {
            return new GitHubIssue(title,body,label,comments);
        }

    }



}
