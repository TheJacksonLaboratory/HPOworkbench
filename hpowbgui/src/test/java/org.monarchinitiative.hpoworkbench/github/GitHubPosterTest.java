package org.monarchinitiative.hpoworkbench.github;

import org.junit.Test;


public class GitHubPosterTest {



    @Test
    public void testPost() throws  Exception{
        String uname="x";
        String passw="y";
        String title="Test";
        String messagebody="Test";

        GitHubPoster poster = new GitHubPoster(uname,  passw,  title,  messagebody);
        //poster.postHpoIssue();
    }


}
