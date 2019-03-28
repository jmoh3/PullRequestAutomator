package com.example;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

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
    /** Starting line number where insertion takes place. */
    private int lineNumber;

    /** Flaky test that patch is solving. */
    private String flaky;
    /** Modified test. */
    private String modified;
    /** Test that causes flaky to fail. */
    private String polluter;
    /** Test that causes flaky to succeed when inbetween polluter and flaky. */
    private String cleaner;

    /** True if patch applied. */
    private boolean applied = false;

    /**
     * Creates a patch object for a patch file.
     *
     * @param pathToPatch name of patch file being parsed.
     * @param pathToFile name of file to apply patch to.
     */
    Patch(String pathToPatch, String pathToFile) {

        this.pathToPatch = pathToPatch;
        this.pathToFile = pathToFile;

        String[] splitFileName = pathToPatch.split("/");

        this.flaky = splitFileName[splitFileName.length - 1].replaceAll(".patch", "");

        this.diff = "";

        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(pathToPatch));

            boolean readingDiff = false;

            String line = reader.readLine();
            while (line != null) {

                if (readingDiff)  {
                    this.diff += line + "\n";

                    // Parses line number.
                    if (line.length() > 2 && line.substring(0, 2).equals("@@")) {
                        String[] splitLine = line.split(" ");

                        if (splitLine.length > 2) {

                            String lineNumber = splitLine[1];
                            lineNumber = lineNumber.replaceAll("-", "");
                            String[] splitLineNumber = lineNumber.split(",");

                            if (splitLineNumber.length > 0) {
                                this.lineNumber = Integer.parseInt(splitLineNumber[0]);
                            }
                        }
                    }

                    line = reader.readLine();
                    continue;
                }

                // Indicates start of diff.
                if (line.equals("=========================="))  {
                    readingDiff = true;

                    line = reader.readLine();
                    continue;
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
     * Gets start modified line number.
     *
     * @return line number.
     */
    public int getLineNumber() {
        return this.lineNumber;
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

        if (this.status == Status.NO_CLEANERS) {
            System.out.println("No cleaners, no patch produced.");
            return false;
        }

        if (this.status == Status.INLINE_FAIL) {
            System.out.println("Inline fail case cannot be handled at the moment.");
            return false;
        }

        File cwd = new File("").getAbsoluteFile();

        ProcessBuilder processBuilder = new ProcessBuilder("patch", "-p0", this.pathToFile, this.pathToPatch);
        processBuilder.redirectErrorStream(true);

        processBuilder.directory(cwd);

        Process process = null;

        try {
            process = processBuilder.start();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(byteArrayOutputStream);
            IOUtils.copy(process.getInputStream(), printStream);
            process.waitFor();

            String message = byteArrayOutputStream.toString();

            if (message != null && message.equals("patching file " + this.pathToFile + "\n")) {
                this.applied = true;
                return true;
            } else {
                System.out.println(message);
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return false;
        }
    }

    public boolean undoPatch() {
        if (!this.applied) {
            return false;
        }

        File cwd = new File("").getAbsoluteFile();

        ProcessBuilder processBuilder = new ProcessBuilder("patch", "-R", "-p0", this.pathToFile, this.pathToPatch);
        processBuilder.redirectErrorStream(true);

        processBuilder.directory(cwd);

        Process process = null;

        try {
            process = processBuilder.start();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(byteArrayOutputStream);
            IOUtils.copy(process.getInputStream(), printStream);
            process.waitFor();

            String message = byteArrayOutputStream.toString();

            if (message != null && message.equals("patching file " + this.pathToFile + "\n")) {
                this.applied = true;
                return true;
            } else {
                System.out.println(message);
                return false;
            }

        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets the pull request description for patch.
     *
     * @return String describing patch for PR description.
     */
    public String getPullRequestDescription() {

        String output;

        output = this.flaky + " identified as order dependent flaky test.\n";
        if (this.polluter != null) {
            output += this.polluter + " causes test to fail when run before it.\n";
        }
        if (this.cleaner != null) {
            output += "Code from " + this.cleaner + " used to create patch at line " + this.lineNumber;
        }
        return output;
    }

}
