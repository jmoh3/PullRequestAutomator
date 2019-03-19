package com.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Patch {

    enum Status {  INLINE_FAIL, INLINE_SUCCESS, NO_CLEANERS };

    private Status status;

    private String pathToFile;
    private String modified;
    private String polluter;
    private String cleaner;

    /**
     * Creates a patch object for a patch file.
     *
     * @param contentFileName name of patch file being parsed.
     */
    Patch(String contentFileName) {

        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(contentFileName));
            String line = reader.readLine();
            while (line != null) {

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

    public String getPolluter() {
        return this.polluter;
    }

    public String getCleaner() {
        return this.cleaner;
    }

    public String getModified() {
        return this.modified;
    }

    public Status getStatus() {
        return this.status;
    }

    public String getPullRequestDescription() {
        // TODO - fill in pull request template to get this description

        return "";
    }

}
