package com.example;

import org.apache.commons.io.FileUtils;

import java.io.*;

public class Patch {

    /**
     * All possible Patch statuses.
     */
    enum Status {  INLINE_FAIL, INLINE_SUCCESS, NO_CLEANERS };

    /** Status of patch. */
    private Status status;

    /** Path to file where patch is being applied. */
    private String pathToFile;

    /** Actual patch (diff output to apply). */
    private String diff;
    /** Path to patch file. */
    private String pathToPatch;

    /** Flaky test that patch is solving. */
    private String flaky;
    /** Modified test. */
    private String modified;
    /** Test that causes flaky to fail. */
    private String polluter;
    /** Test that causes flaky to succeed when inbetween polluter and flaky. */
    private String cleaner;

    /**
     * Creates a patch object for a patch file.
     *
     * @param contentFileName name of patch file being parsed.
     */
    Patch(String contentFileName) {

        String[] splitFileName = contentFileName.split("/");

        this.flaky = splitFileName[splitFileName.length - 1].replaceAll(".patch", "");

        this.diff = "";

        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(contentFileName));

            boolean readingDiff = false;

            String line = reader.readLine();
            while (line != null) {

                if (readingDiff)  {
                    this.diff += line + "\n";
                    line = reader.readLine();
                    continue;
                }

                if (line.equals("=========================="))  {
                    readingDiff = true;
                }

                String[] splitString = line.split(": ");

                if (splitString.length > 1) {

                    if (splitString[0].equals("STATUS") && splitString[1].equals("INLINE FAIL")) {
                        this.status = Status.INLINE_FAIL;
                    } else if (splitString[0].equals("STATUS") && splitString[1].equals("INLINE SUCCESSFUL")) {
                        this.status = Status.INLINE_SUCCESS;
                    } else if (splitString[0].equals("STATUS") && splitString[1].equals("NO CLEANERS")) {
                        this.status = Status.NO_CLEANERS;
                    }

                    if (splitString[0].equals("MODIFIED")) {
                        this.modified = splitString[1];
                    }

                    if (splitString[0].equals("CLEANER")) {
                        this.cleaner = splitString[1];
                    }

                    if (splitString[0].equals("POLLUTER")) {
                        this.polluter = splitString[1];
                    }

                }
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets path to file where patch is to be applied.
     * @return path to file.
     */
    public String getPathToFile() {
        return this.pathToFile;
    }

    /**
     * Gets the flaky test.
     *
     * @return String describing flaky test.
     */
    public String getFlaky() {
        return this.flaky;
    }

    /**
     * Gets the polluter for the flaky test.
     *
     * @return String describing polluter test.
     */
    public String getPolluter() {
        return this.polluter;
    }

    /**
     * Gets the cleaner test for the flaky test.
     *
     * @return cleaner for flaky test.
     */
    public String getCleaner() {
        return this.cleaner;
    }

    /**
     * Gets the modified test.
     *
     * @return modified test.
     */
    public String getModified() {
        return this.modified;
    }

    /**
     * Gets the diff (actual patch portion of patch file).
     *
     * @return Diff output.
     */
    public String getDiff() {
        return this.diff;
    }

    /**
     * Gets Patch status.
     *
     * @return status of patch.
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * Applies patch.
     *
     * @return true if succeeds, false otherwise.
     */
    public boolean applyPatch() {
        if (this.diff.length() != 0) {
            try {
                FileUtils.writeStringToFile(new File(this.flaky + "modifiedPatch.patch"), this.diff);
                Runtime.getRuntime().exec("patch <  " + this.flaky + "modifiedPatch.patch");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * Gets the pull request description for patch.
     *
     * @return String describing patch for PR description.
     */
    public String getPullRequestDescription() {

        String output;

        output = this.flaky + " identified as order dependent flaky test.\n";
        output += this.polluter + " causes test to fail when run before it.\n";
        output += "Code from " + this.cleaner + " used to create patch at lines ";

        return output;
    }

}
