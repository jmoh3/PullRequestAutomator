package com.example;

import org.apache.commons.io.FileUtils;

import java.io.*;

public class Patch {

    enum Status {  INLINE_FAIL, INLINE_SUCCESS, NO_CLEANERS };

    private Status status;

    private String pathToFile;

    private String diff;
    private String pathToPatch;

    private String flaky;
    private String modified;
    private String polluter;
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

    public String getPathToFile() {
        return this.pathToFile;
    }

    public String getFlaky() {
        return this.flaky;
    }

    public String getPolluter() {
        return this.polluter;
    }

    public String getCleaner() {
        return this.cleaner;
    }

    public String getModified() {
        return this.modified;
    }

    public String getDiff() {
        return this.diff;
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

    public Status getStatus() {
        return this.status;
    }

    public String getPullRequestDescription() {

        String output;

        output = this.flaky + " identified as order dependent flaky test.\n";
        output += this.polluter + " causes test to fail when run before it.\n";
        output += "Code from " + this.cleaner + " used to create patch at lines ";

        return output;
    }

}
