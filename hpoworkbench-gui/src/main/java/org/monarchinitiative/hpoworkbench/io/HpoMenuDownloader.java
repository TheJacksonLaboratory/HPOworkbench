package org.monarchinitiative.hpoworkbench.io;

import org.monarchinitiative.hpoworkbench.exception.HpoWorkbenchRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

public class HpoMenuDownloader {
    private static final Logger logger = LoggerFactory.getLogger(HpoMenuDownloader.class);
    private final static String HP_JSON = "hp.json";
    /** URL of the hp.obo file. */
    private final static String HP_JSON_URL ="https://raw.githubusercontent.com/obophenotype/human-phenotype-ontology/master/hp.json";


    private final String downloadDirectory;

    public HpoMenuDownloader(String downloadDir) {
        downloadDirectory = downloadDir;
    }


    public String downloadHpo() throws HpoWorkbenchRuntimeException {
        File dest = new File(downloadDirectory + File.separator + HP_JSON);
        try {
            URL url = new URL(HP_JSON_URL);
            logger.debug("Created url from "+HP_JSON_URL+": "+url);
            copyURLToFileThroughURL(url, dest);
        } catch (MalformedURLException e) {
            logger.error(String.format("Malformed URL for %s [%s]",dest.getAbsolutePath(), HP_JSON_URL));
            logger.error(e.getMessage());
            throw new HpoWorkbenchRuntimeException(e.getMessage());
        }
        return dest.getAbsolutePath();
    }



    /**
     * Copy contents of a URL to a file using the {@link URL} class.
     *
     * This works for the HTTP and the HTTPS protocol and for FTP through a proxy. For plain FTP, we need to use the
     * passive mode.
     */
    private boolean copyURLToFileThroughURL(URL src, File dest)  throws HpoWorkbenchRuntimeException {
        // actually copy the file
        BufferedInputStream in;
        FileOutputStream out;
        try {
            int connectionTimeout = 5000; // 5 seconds should be more than enough to connect to a server
            final String TEXTPLAIN_REQUEST_TYPE = ", text/plain; q=0.1";
            String actualAcceptHeaders = TEXTPLAIN_REQUEST_TYPE;
            URLConnection connection =  connect(src.openConnection(),connectionTimeout,actualAcceptHeaders,new HashSet<>());
            final int fileSize = connection.getContentLength();
            in = new BufferedInputStream(connection.getInputStream());
            out = new FileOutputStream(dest);

            // Download file.
            byte [] buffer = new byte[128 * 1024];
            int readCount;
            long pos = 0;

            while ((readCount = in.read(buffer)) > 0) {
                out.write(buffer, 0, readCount);
                pos += readCount;
            }
            in.close();
            out.close();

        } catch (IOException | IllegalStateException e) {
            logger.error(String.format("Failed to downloaded file from %s",src.getHost()),e);
            throw new HpoWorkbenchRuntimeException("ERROR: Problem downloading file: " + e.getMessage());
        }
        return true;
    }


    protected static URLConnection connect(URLConnection conn, int connectionTimeout, String acceptHeaders, Set<String> visited)
            throws IOException {
        if (conn instanceof HttpURLConnection con) {
            // follow redirects to HTTPS
            con.connect();
            int responseCode = con.getResponseCode();
            // redirect
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_SEE_OTHER
                    // no constants for temporary and permanent redirect in HttpURLConnection
                    || responseCode == 307 || responseCode == 308) {
                String location = con.getHeaderField("Location");
                if (visited.add(location)) {
                    URL newURL = new URL(location);
                    return connect(rebuildConnection(connectionTimeout, newURL, acceptHeaders),
                            connectionTimeout, acceptHeaders, visited);
                } else {
                    throw new IllegalStateException(
                            "Infinite loop: redirect cycle detected. " + visited);
                }
            }
        }
        return conn;
    }

    protected static URLConnection rebuildConnection(int connectionTimeout, URL newURL, String acceptHeaders) throws IOException {
        URLConnection conn;
        conn = newURL.openConnection();
        final String ACCEPTABLE_CONTENT_ENCODING = "xz,gzip,deflate";
        conn.addRequestProperty("Accept", acceptHeaders);
        conn.setRequestProperty("Accept-Encoding", ACCEPTABLE_CONTENT_ENCODING);
        conn.setConnectTimeout(connectionTimeout);
        return conn;
    }


}
