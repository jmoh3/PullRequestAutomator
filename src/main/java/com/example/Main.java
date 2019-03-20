package com.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
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
