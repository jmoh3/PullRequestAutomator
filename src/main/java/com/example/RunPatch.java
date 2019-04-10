package com.example;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class RunPatch {

    public static final String REPO_DIRECTORY = "/Users/jackieoh/Desktop/PURE/openSourceRepositories";

    public static final String PATCHES_DIRECTORY = "/Users/jackieoh/Desktop/PURE/all-patches/";

    public static final String PATCH_LOCATIONS = "/Users/jackieoh/Desktop/PURE/patch-locations";

    public static final String COMMIT_ID_CSV = "/Users/jackieoh/Desktop/PURE/comprehensive_subject_loc.csv";

    public static void main(String[] args) {

        File patchesDirectory = new File(PATCHES_DIRECTORY);

        if (!patchesDirectory.exists() || !patchesDirectory.isDirectory()) {
            System.out.println("Invalid patch directory.");
        }

        Set<String> alreadyCheckedOut = new HashSet<String>();

        // iterate through patches in all-patches directory
        for (String patchFilename : patchesDirectory.list()) {

            // get patch location from patch-locations
            String patchLocation = findPatchLocation(patchFilename, PATCH_LOCATIONS);

            // get important information from patch location
            HashMap<String, String> map = Patch.parsePackageFilePath(patchLocation);

            String slug = map.get("slug");
            String projectName = map.get("projectName");

            // find commit id from commit id csv
            String commitId = getCommitId(slug, COMMIT_ID_CSV);

            // create patch & check that it's inline success (otherwise just continue)
            System.out.println(PATCHES_DIRECTORY + patchFilename);
            Patch patch = new Patch(PATCHES_DIRECTORY + patchFilename);
            if (patch.getStatus() != Patch.Status.INLINE_SUCCESS) {
                System.out.println("Not inline success. Continuing.");
                continue;
            }

            // if haven't already clone repo w/ slug from patch and commit id
            if (!alreadyCheckedOut.contains(projectName)) {
                RepoUtils.cloneRepo(slug, REPO_DIRECTORY);

                try {
                    Repository repo = new FileRepositoryBuilder()
                            .setGitDir(new File(REPO_DIRECTORY + "/" + projectName + "/.git"))
                            .build();

                    Git git = new Git(repo);
                    git.checkout().setName(commitId).call();
                    alreadyCheckedOut.add(projectName);
                } catch (Exception e) {
                    System.out.println("could not checkout correct commit id");
                }
            }

            // initialize patch now that we have repo started
            patch.init(patchLocation, REPO_DIRECTORY);

            // modify POM.xml so open source project works with iDFlakies
//            ProcessBuilder processBuilder = new ProcessBuilder("/Users/jackieoh/IdeaProjects/PullRequestAutomator/pom-modify/modify-project.sh");

            // get polluter and flaky test from patch
            String polluter = patch.getPolluter();
            System.out.println(polluter);
            String flaky = patch.getFlaky();
            System.out.println(flaky);

            // run polluter then victim in isolation and check to make sure it fails

            // apply patch
            System.out.println(patch.applyPatch());

            // measure time it takes to run new test method (run it 5 times)

            // record result

            // undo patch
            System.out.println(patch.undoPatch());
            break;
        }
    }

    private static String findPatchLocation(String patchName, String patchLocationFilename) {

        try {
            BufferedReader reader = new BufferedReader(new FileReader(patchLocationFilename));

            String line = reader.readLine();

            while (line != null) {
                if (line.contains(patchName)) {
                    return line;
                }
                line = reader.readLine();
            }

        } catch (Exception e) {
            System.out.println("An error has occurred. Could not find patch location.");
            return null;
        }

        System.out.println("No matching patch location.");

        return null;
    }

    private static String getCommitId(String slug, String commitIdCsv) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(commitIdCsv));

            String line = reader.readLine();

            while (line != null) {
                if (line.contains(slug)) {
                    return line.split(",")[1];
                }
                line = reader.readLine();
            }

        } catch (Exception e) {
            System.out.println("An error has occurred. Could not find patch location.");
            return null;
        }

        System.out.println("No matching patch location.");

        return null;
    }

}
