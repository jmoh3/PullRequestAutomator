package com.example;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.nio.file.Paths;

public class RepoUtils {

    public static final String githubUrl = "https://github.com/";

    public static boolean cloneRepo(String slug, String directory) {
        String repoUrl = githubUrl + slug + ".git";
        String projectName = slug.split("/")[1];
        String cloneDirectoryPath = directory + "/" + projectName;

        File projectDirectory = new File(cloneDirectoryPath);

        if (projectDirectory.exists()) {
            System.out.println("Local repository already exists.");
            return true;
        }

        try {
            System.out.println("Cloning "+repoUrl+" into "+cloneDirectoryPath);
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(Paths.get(cloneDirectoryPath).toFile())
                    .call();
            System.out.println("Completed Cloning");
            return true;
        } catch (GitAPIException e) {
            System.out.println("Exception occurred while cloning repo");
            return false;
        }
    }
}
