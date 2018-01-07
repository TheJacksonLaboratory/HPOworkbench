package org.monarchinitiative.hpoworkbench.github;


import org.json.simple.JSONValue;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;



public class GitHubPoster {


    /** username and password need to be passed on the command line to this program
     * java -jar ErnAlyzer.jar username password
     * They are the github pass/name.
     */
    private String username;
    private String password;
    private String payload;

    private int responsecode;
    private String response=null;


    public String getHttpResponse() { return String.format("%s [code: %d]",response,responsecode);}


    public GitHubPoster(String uname, String passw, String title, String messagebody) {
        this.password = passw;
        this.username = uname;
        this.payload=formatPayload(title,messagebody);
    }


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
        System.out.println("PAYLOAD="+new String(out));
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
