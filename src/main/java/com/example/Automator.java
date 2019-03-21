package com.example;

import com.jcabi.github.*;
import org.apache.commons.codec.binary.Base64;

import javax.json.Json;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Automator {

    /** Absolute path to file that was modified. */
    private String absolutePathToFile;
    /** Path to file that was modified within github repo. */
    private String repoPathToFile;
    /** Base branch (to merge into, typically master). */
    private String baseBranch;
    /** New branch. */
    private String newBranch;
    /** Repo coordinates. */
    private String repoCoordinates;

    /** Comment for pull request (should be filled in template). */
    private String pullComment;

    /** Patch used for pull request. */
    private Patch patch;

    /** Github object. */
    private Github github;
    /** Repo we are modifying. */
    private Repo repo;

    /** Name of user making pull request. */
    private String name;
    /** Username of user making pull request. */
    private String username;
    /** Email of user making pull request. */
    private String email;

    /**
     * Constructor that does not use a patch (primarily for testing purposes).
     *
     * @param absolutePathToFile absolute path to file that was modified.
     * @param repoPathToFile path to file that was modified within github repo.
     * @param baseBranch base branch (to merge into, typically master).
     * @param newBranch new branch.
     * @param repoCoordinates repo coordinates.
     * @param pullComment comment for pull request.
     * @param username username of user making pull request.
     * @param pathToPasswordFile path to the password file of user making pull request.
     * @param name name of user making pull request.
     * @param email email of user making pull request.
     */
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

    /**
     * Constructor that takes in un-applied patch object to be used for pull request.
     *
     * @param patch patch to be applied.
     * @param base base branch (to merge into, typically master).
     * @param newBranch new branch.
     * @param repoCoordinates repo coordinates.
     * @param username username of user making pull request.
     * @param pathToPasswordFile path to the password file of user making pull request.
     * @param name name of user making pull request.
     * @param email email of user making pull request.
     */
    Automator(Patch patch, String base, String newBranch, String repoCoordinates, String username, String pathToPasswordFile,
              String name, String email) {

        this.patch = patch;

        this.absolutePathToFile = patch.getPathToFile();
        this.pullComment = patch.getPullRequestDescription();

        this.baseBranch = base;
        this.newBranch = newBranch;
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
            e.printStackTrace();
        }
    }

    /**
     * Completes the pull request process from start to finish.
     *
     * @return true if succeeded, false otherwise.
     */
    public boolean makePullRequest() {

        if (this.patch != null) {
            this.patch.applyPatch();
        }

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
     *
     * @return commit sha, or null if commit fails.
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
            e.printStackTrace();
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
}
