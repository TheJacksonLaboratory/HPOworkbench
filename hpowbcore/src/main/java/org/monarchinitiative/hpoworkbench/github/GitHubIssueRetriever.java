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

/**
 * Note: Apparently there is no way to retrieve more than 30 labels at a time, and so we cannot reliably
 * retrieve all of the open issues for some label. This may be useful for labels with less than 30 issues
 * to create a word doc, but this will not yet be reliable in general. Keep this class at the experimental/
 * command-line only stage for now.
 */
public class GitHubIssueRetriever {
    private static final Logger logger = LogManager.getLogger();

    private List<GitHubIssue> issues = new ArrayList<>();

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
        GitHubIssue.Builder builder = new GitHubIssue.Builder(title).body(body).label(label).number(number);
        issues.add(builder.build());
    }


    private void decodeJSON(String s) {
       // System.out.println(s);
        Object obj= JSONValue.parse(s);
        JSONArray jsonArray = (JSONArray) obj;
        Iterator<String> iterator = jsonArray.iterator();
        jsonArray.forEach(label -> parseLabelElement(label) );
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
            int responsecode=httpconnection.getResponseCode();
            return responsecode;
        } catch (MalformedURLException e) {
            e.printStackTrace();
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
