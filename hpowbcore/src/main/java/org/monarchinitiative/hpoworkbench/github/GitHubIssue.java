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
    private final String issueNumber;

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getLabel() {
        return label;
    }

    public List<String> getComments() {
        return comments;
    }

    public String getIssueNumber() {
        return issueNumber;
    }

    public boolean hasValidIssueNumber(){
        return this.issueNumber!=null && !this.issueNumber.startsWith("?");
    }

    GitHubIssue(String title, String body, String label, List<String> comments, String number){
        this.title=title;
        this.body=body;
        this.label=label;
        this.comments=comments;
        issueNumber=number;

    }


    public String toString() {
        return String.format("%s: %s [%s] %s",title,body,label, String.join(";", comments));
    }




    public static class Builder {
        private final String title;
        private String body="";
        private String label="none";
        private String issueNumber="";
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

        public Builder number(String number) {
            issueNumber=number;
            return this;
        }

        public GitHubIssue build() {
            return new GitHubIssue(title,body,label,comments,issueNumber);
        }

    }



}
