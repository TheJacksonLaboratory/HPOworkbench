package org.monarchinitiative.hpoworkbench.github;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class GitHubIssueRetriever {
    private static final Logger logger = LogManager.getLogger();

    private List<GitHubIssue> issues = new ArrayList<>();

    private HttpURLConnection httpconnection=null;


    public GitHubIssueRetriever() {
        int page=1;
        logger.trace("GitHubIssueRetriever CTOR");
        for (int i=1;i<20;i++) {
        //while (retrieveIssues(page) ) {
            boolean response = retrieveIssues(i);
            System.out.println("got page "+i);
           if (! response) break;
        }
    }


    public List<GitHubIssue> getIssues(){ return issues; }


    private void parseLabelElement(Object obj) {
        JSONObject jsonObject = (JSONObject) obj;
        String title = jsonObject.get("title").toString();
        String body = jsonObject.get("body").toString();
        String label = jsonObject.get("label")==null?"none":jsonObject.get("label").toString();
        GitHubIssue.Builder builder = new GitHubIssue.Builder(title).body(body).label(label);
//        JSONObject comments = (JSONObject) jsonObject.get("comments");
//        if (comments != null) {
//
//        }

        //System.out.println(jsonObject.toString() +"\n");
        issues.add(builder.build());
    }


    private void decodeJSON(String s) {
        Object obj= JSONValue.parse(s);
        JSONArray jsonArray = (JSONArray) obj;
        Iterator<String> iterator = jsonArray.iterator();
        jsonArray.forEach(label -> parseLabelElement(label) );
    }



    private boolean retrieveIssues(int page)  {
        try {
            URL url = new URL(String.format("https://api.github.com/repos/obophenotype/human-phenotype-ontology/issues?state=all",page));
            if (httpconnection==null) {
                httpconnection = (HttpURLConnection) url.openConnection();
                httpconnection.setRequestMethod("GET");
                httpconnection.connect();
                httpconnection.setDoOutput(true);
            }
            Scanner scanner = new Scanner(url.openStream());String response = scanner.useDelimiter("\\Z").next();
            scanner.close();
            decodeJSON(response);
            int responsecode=httpconnection.getResponseCode();
            //httpconnection.disconnect();
            return responsecode==400;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; // we should never get here with a good response.
    }


    private void retrieveIssuesOLD(int page)  {
        try {
            URL url = new URL("https://api.github.com/repos/obophenotype/human-phenotype-ontology/issues?state=open");
            URLConnection con = url.openConnection();
            con.setDoOutput(true);
            Scanner scanner = new Scanner(url.openStream());
            String response = scanner.useDelimiter("\\Z").next();
            scanner.close();
            decodeJSON(response);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
