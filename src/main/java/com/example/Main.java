package com.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    /**
     * Main method to apply a patch and make a pull request in an open source repository.
     *
     * @param args pass in path to patch file.
     */
    public static void main(String[] args) {
        String pathToPatchFile;

        if (args.length > 0)  {
            pathToPatchFile = args[0];
        }

        Automator automator = new Automator("/Users/jackieoh/IdeaProjects/adventure/src/com/example/GithubPullRequestTest.java",
                "/src/com/example/GithubPullRequestTest.java",
                "master",
                "testGithubPR",
                "jmoh3/adventure",
                "comment",
                "jmoh3",
                "/Users/jackieoh/IdeaProjects/PullRequestAutomator/Password.txt",
                "Jackie Oh",
                "jmoh3@illinois.edu");

        automator.makePullRequest();

    }
}
