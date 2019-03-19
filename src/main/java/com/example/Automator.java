package com.example;

import com.jcabi.github.*;
import org.apache.commons.codec.binary.Base64;

import javax.json.Json;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Automator {

    private String absolutePathToFile;
    private String repoPathToFile;
    private String baseBranch;
    private String newBranch;
    private String repoCoordinates;

    private String pullComment;

    private Github github;
    private Repo repo;

    private String name;
    private String username;
    private String email;

    Automator(String absolutePathToFile, String repoPathToFile, String baseBranch, String newBranch,
              String repoCoordinates, String pullComment,
              String username, String pathToPasswordFile, String name, String email) {

        this.absolutePathToFile = absolutePathToFile;
        this.repoPathToFile = repoPathToFile;

        this.baseBranch = baseBranch;
        this.newBranch = newBranch;
        this.repoCoordinates = repoCoordinates;

        this.pullComment = pullComment;

        this.username = username;
        this.name = name;
        this.email = email;

        try {
            String password = readFileAsString(pathToPasswordFile);
            this.github = new RtGithub(username, password);
            this.repo = github.repos().get(
                    new Coordinates.Simple(repoCoordinates)
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Automator(Patch patch, String base, String repoCoordinates, String username, String pathToPasswordFile,
              String name, String email) {

        this.absolutePathToFile = patch.getPathToFile();
        this.pullComment = patch.getPullRequestDescription();
        this.baseBranch = base;
        this.repoCoordinates = repoCoordinates;

        this.name = name;
        this.email = email;

        try {
            String password = readFileAsString(pathToPasswordFile);
            this.github = new RtGithub(username, password);
            this.repo = github.repos().get(
                    new Coordinates.Simple(repoCoordinates)
            );

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public boolean makePullRequest() {
        String sha = commit();

        if (sha != null) {
            boolean success = pull(sha);

            return success;
        } else {
            return false;
        }
    }

    /**
     * Generates commit and pull request for file that already exists in github repository.
     * @return
     */
    public String commit() {
        try {
            File file = new File(this.absolutePathToFile);

            if (!file.canRead()) {
                System.out.println("Cannot read file.");
                return null;
            }

            Content pathContent = repo.contents().get(this.repoPathToFile, this.newBranch);

            final Content.Smart content = new Content.Smart(pathContent);
            String sha = content.sha();

            byte[] fileContent = convertToByteArray(file);

            final String enc = Base64.encodeBase64String(content.decoded());

            RepoCommit newCommit = repo.contents().update(
                    this.repoPathToFile,
                    Json.createObjectBuilder()
                            .add("message", "Committed new file!")
                            .add("content", enc)
                            .add("sha", sha)
                            .add("branch", this.newBranch)
                            .add(
                                    "committer",
                                    Json.createObjectBuilder()
                                            .add("name", this.name)
                                            .add("email", this.email)
                            )
                            .build()
            );

            return newCommit.sha();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Makes a pull request.
     *
     * @return true if succeeds, false otherwise.
     */
    public boolean pull(String commitSha) {
        try {
            Pulls pulls = repo.pulls();
            Pull pullRequest = pulls.create("Pull Request Name", "testGithubPR", "master");
            PullComments comments = pullRequest.comments();

            comments.post(this.pullComment, commitSha, this.repoPathToFile, 1);

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Helper that reads a file as string.
     *
     * @param fileName name of file to be read.
     * @return String of contents of file.
     * @throws Exception
     */
    private String readFileAsString(String fileName) throws Exception {
        String data = "";
        try {
            data = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (Exception e) {
            System.out.println(e);
        }
        return data;
    }

    /**
     * Converts a given file to a byte array.
     *
     * @param file file to use.
     * @return byte array.
     */
    private byte[] convertToByteArray(File file)
    {
        byte[] fileBytes = new byte[(int) file.length()];

        try {
            FileInputStream inputStream = new FileInputStream(file);
            inputStream.read(fileBytes);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return fileBytes;
    }

    // TODO - Pull request method
}
