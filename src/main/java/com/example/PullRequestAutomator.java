package com.example;

import com.jcabi.github.*;
import org.apache.commons.codec.binary.Base64;

import javax.json.Json;
import javax.print.attribute.HashAttributeSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class PullRequestAutomator {

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
    PullRequestAutomator(String absolutePathToFile, String repoPathToFile, String baseBranch, String newBranch,
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
     * @param repoPathToFile path to file that was modified within github repo.
     * @param base base branch (to merge into, typically master).
     * @param newBranch new branch.
     * @param repoCoordinates repo coordinates.
     * @param credentialsPath path to credentials file.
     */
    PullRequestAutomator(Patch patch, String repoPathToFile, String base, String newBranch,
                         String repoCoordinates, String credentialsPath) {

        this.patch = patch;
        this.repoPathToFile = repoPathToFile;

        this.absolutePathToFile = patch.getPathToFile();
        System.out.println(this.absolutePathToFile);
        this.pullComment = patch.getPullRequestDescription();
        System.out.println(this.pullComment);

        this.baseBranch = base;
        this.newBranch = newBranch;
        this.repoCoordinates = repoCoordinates;

        HashMap<String, String> credentialsMap = readCredentials(credentialsPath);

        this.name = credentialsMap.get("NAME");
        this.username = credentialsMap.get("USERNAME");
        this.email = credentialsMap.get("EMAIL");

        try {
            String password = credentialsMap.get("PASSWORD");
            this.github = new RtGithub(this.username, password);
            this.repo = this.github.repos().get(
                    new Coordinates.Simple(repoCoordinates)
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a user's github credentials from a file and returns a HashMap containing those credentials.
     *
     * @param credentialsPath path to the file containing credentials.
     * @return HashMap mapping credential name (username, password, etc) to value of credential.
     */
    HashMap<String, String> readCredentials(String credentialsPath) {
        BufferedReader reader;
        HashMap<String, String> credentials = new HashMap<String, String>();

        try {
            reader = new BufferedReader(new FileReader(credentialsPath));

            String line = reader.readLine();
            while (line != null) {
                if (line.contains("NAME: ")) {
                    String name = line.replace("NAME: ", "");
                    credentials.put("NAME", name);
                }
                if (line.contains("USER: ")) {
                    String username = line.replace("USER: ", "");
                    credentials.put("USERNAME", username);
                }
                if (line.contains("PASSWORD: ")) {
                    String password = line.replace("PASSWORD: ", "");
                    credentials.put("PASSWORD", password);
                }
                if (line.contains("EMAIL: ")) {
                    String email = line.replace("EMAIL: ", "");
                    credentials.put("EMAIL", email);
                }
                line = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return credentials;
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
            return pull(sha);
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
                            .add("message", "Patching flaky test " + this.patch.getFlaky())
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
            Pull pullRequest = pulls.create("Pull Request Name", this.newBranch, baseBranch);
            PullComments comments = pullRequest.comments();
            if (patch != null) {
                comments.post(this.pullComment, commitSha, this.repoPathToFile, this.patch.getLineNumber());
            }

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
    public static String readFileAsString(String fileName) throws Exception {
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
    private byte[] convertToByteArray(File file) {
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
