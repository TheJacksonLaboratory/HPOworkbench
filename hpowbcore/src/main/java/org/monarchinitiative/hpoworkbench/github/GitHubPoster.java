package org.monarchinitiative.hpoworkbench.github;

import org.json.simple.JSONValue;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;


/**
 * The purpose of this class is to post an issue to the HPO GitHub issue tracker.
 * The user of the software must have a valid GitHub user name and password.
 * TODO we only use the JSON library to format the string. Write our own function to reduce
 * dependency on the external library and make the app smaller.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.2.13
 */
public class GitHubPoster {
    /** GitHub username. */
    private String username;
    /** GitHub password. */
    private String password;
    /** The contents of the GitHub issue we want to create.. */
    private String payload;
    /** The HTML response code of the GitHub server. */
    private int responsecode;
    /** THe response message of the GitHub server. */
    private String response=null;
    /**  @return the response of the GitHub server following our attempt to create a new issue*/
    public String getHttpResponse() { return String.format("%s [code: %d]",response,responsecode);}


    public GitHubPoster(String uname, String passw, String title, String messagebody) {
        this.password = passw;
        this.username = uname;
        this.payload=formatPayload(title,messagebody);
    }

    /** TODO create our won escape formated (new line, quotation mark etc. */
    private String jsonFormat(String s) {
        return JSONValue.escape(s);
    }



    private String formatPayload(String title, String messagebody) {
        return String.format("{\n" +
                        "\"title\": \"%s\",\n" +
                        "\"body\": \"%s\"}",
                JSONValue.escape(title),JSONValue.escape(messagebody));
    }



    public void postIssue() throws Exception {
        URL url = new URL("https://api.github.com/repos/obophenotype/human-phenotype-ontology/issues");
        URLConnection con = url.openConnection();
        String userpass = String.format("%s:%s",username,password);
        String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST"); // PUT is another valid option
        http.setDoOutput(true);
        byte[] out = payload.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;
        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        http.setRequestProperty("Authorization",basicAuth);
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(out);
            os.close();
        }
        if (http.getResponseCode()==400) {
            String erro=String.format("URL:%s\nPayload=%s\nServer response: %s [%d]",
                    http.toString(),
                    payload,
                    http.getResponseMessage(),
                    http.getResponseCode());
            throw new Exception(erro);
        } else {
            this.response=http.getResponseMessage();
            this.responsecode=http.getResponseCode();
        }
    }

}
