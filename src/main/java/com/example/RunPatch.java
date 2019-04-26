package com.example;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.*;
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

                    // modify POM.xml so open source project works with iDFlakies
                    ProcessBuilder processBuilder = new ProcessBuilder("/Users/jackieoh/IdeaProjects/PullRequestAutomator/pom-modify/modify-project.sh", REPO_DIRECTORY + "/" + projectName);

                    processBuilder.redirectErrorStream(true);

                    Process process = null;

                    process = processBuilder.start();
                    process.waitFor();
                    System.out.println("Added iDFlakies to pom.xml");

                    alreadyCheckedOut.add(projectName);
                } catch (Exception e) {
                    System.out.println("could not checkout correct commit id");
                    continue;
                }
            }

            // initialize patch now that we have repo started
            patch.init(patchLocation, REPO_DIRECTORY);

            // get polluter and flaky test from patch
            String polluter = patch.getPolluter();
            System.out.println(polluter);
            String flaky = patch.getFlaky();
            System.out.println(flaky);

            // run polluter then victim in isolation and check to make sure it fails
            ProcessBuilder runTestsProcessBuilder = new ProcessBuilder("sh", "/Users/jackieoh/IdeaProjects/PullRequestAutomator/scripts/runTests.sh", REPO_DIRECTORY + "/" + projectName, polluter, flaky, patchFilename + ".beforepatch");
            runTestsProcessBuilder.redirectErrorStream(true);

            Process process = null;

            try {
                process = runTestsProcessBuilder.start();

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PrintStream printStream = new PrintStream(byteArrayOutputStream);
                IOUtils.copy(process.getInputStream(), printStream);
                process.waitFor();

                String message = byteArrayOutputStream.toString();

                System.out.println(message);
            } catch (Exception e) {
                System.out.println(e.toString());
                System.out.println("HERE");
            }

            // apply patch
            System.out.println(patch.applyPatch());

            // measure time it takes to run new test method (run it 5 times)
            runTestsProcessBuilder = new ProcessBuilder("sh", "/Users/jackieoh/IdeaProjects/PullRequestAutomator/scripts/runTests.sh", REPO_DIRECTORY + "/" + projectName, polluter, flaky, patchFilename + ".afterpatch");

            process = null;

            try {
                process = runTestsProcessBuilder.start();
                process.waitFor();
            } catch (Exception e) {
                System.out.println(e.toString());
                System.out.println("HERE");
            }

            // record result
            ResultParser beforeParser = new ResultParser("/Users/jackieoh/Desktop/PURE/output/" + patchFilename + ".beforepatch" +".json");
            ResultParser afterParser = new ResultParser("/Users/jackieoh/Desktop/PURE/output/\" + patchFilename + \".afterpatch\" +\".json");

            // undo patch
            System.out.println(patch.undoPatch());
            break;
        }
    }

    /**
     * Finds the patch location given its name and the location of the patch-locations file.
     *
     * @param patchName name of patch.
     * @param patchLocationFilename path to patch-locations.
     * @return patch location
     */
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

    /**
     * Gets the correct commit ID of a repo from a csv of commit ids.
     *
     * @param slug repo coordinates.
     * @param commitIdCsv filepath to csv of commit ids.
     * @return commit id.
     */
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
