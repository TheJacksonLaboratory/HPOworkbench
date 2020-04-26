package org.monarchinitiative.hpoworkbench.github;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Note: Apparently there is no way to retrieve more than 30 labels at a time, and so we cannot reliably
 * retrieve all of the open issues for some label. This may be useful for labels with less than 30 issues
 * to create a word doc, but this will not yet be reliable in general. Keep this class at the experimental/
 * command-line only stage for now.
 */
public class GitHubIssueRetriever {
    private static final Logger logger = LoggerFactory.getLogger(GitHubIssueRetriever.class);

    private final List<GitHubIssue> issues = new ArrayList<>();

    private HttpURLConnection httpconnection=null;

    private final String issue;


    public GitHubIssueRetriever(String myIssue) {
        issue=myIssue;
        int responsecode = retrieveIssues();
        logger.error(String.format("We retrieved %d issues for %s with response code %d", issues.size(),issue,responsecode));
    }


    public List<GitHubIssue> getIssues(){ return issues; }


    private void parseLabelElement(Object obj) {
        JSONObject jsonObject = (JSONObject) obj;
        String title = jsonObject.get("title").toString();
        String body = jsonObject.get("body").toString();
        String label = jsonObject.get("label")==null?"none":jsonObject.get("label").toString();
        String number = jsonObject.get("number")==null?"?":jsonObject.get("number").toString();
        String comments_url = (String) jsonObject.get("comments_url");
        List<String> comments = new ArrayList<>();
        if (comments_url != null) {
            comments = getComments(comments_url);
        }
        GitHubIssue.Builder builder = new GitHubIssue.Builder(title).body(body).label(label).number(number).comments(comments);
        issues.add(builder.build());
    }


    private List<String> getComments(String urlstring) {
        List<String> comments = new ArrayList<>();
        if (urlstring == null || urlstring.isEmpty()) {
           return comments;
        }
        try {
            URL url = new URL(urlstring);
            if (httpconnection==null) {
                httpconnection = (HttpURLConnection) url.openConnection();
                httpconnection.setRequestMethod("GET");
                httpconnection.connect();
            }
            Scanner scanner = new Scanner(url.openStream());
            String response = scanner.useDelimiter("\\Z").next();
            scanner.close();
            Object obj = JSONValue.parse(response);
            JSONArray jarray = (JSONArray)obj;
            for (Object ob : jarray) {
                String c = parseCommentElement(ob);
                comments.add(c);
            }
            int code = httpconnection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpconnection!=null) {
                httpconnection.disconnect();
            }
        }
        return comments;
    }



    private String parseCommentElement(Object obj) {
        JSONObject jo = (JSONObject) obj;
        if (jo != null) {
            String body = (String) jo.get("body");
            if (body != null) {
                return body;
            }
        }
        return "";// the comment had no body of text
    }




    private void decodeJSON(String s) {
        Object obj= JSONValue.parse(s);
        JSONArray jsonArray = (JSONArray) obj;
        jsonArray.forEach(this::parseLabelElement);
    }



    private int retrieveIssues()  {
        try {
            URL url = new URL(String.format("https://api.github.com/repos/obophenotype/human-phenotype-ontology/issues?labels=%s",issue));
            if (httpconnection==null) {
                httpconnection = (HttpURLConnection) url.openConnection();
                httpconnection.setRequestMethod("GET");
                httpconnection.connect();
            }
            Scanner scanner = new Scanner(url.openStream());String response = scanner.useDelimiter("\\Z").next();
            scanner.close();
            decodeJSON(response);
            return httpconnection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpconnection!=null) {
                httpconnection.disconnect();
            }
        }
        return -1; // we should never get here with a good response.
    }



}
