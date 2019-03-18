package com.example;

public class Patch {

    enum Status {  INLINE_FAIL, INLINE_SUCCESS };

    private String contents;

    private Status status;

    private String pathToFile;
    private String polluter;
    private String cleaner;

    Patch(String contents) {
        this.contents = contents;

        // TODO - parse contents here
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

    public String getContents() {
        return this.contents;
    }

    public Status getStatus() {
        return this.status;
    }

    public String getPullRequestDescription() {
        // TODO - fill in pull request template to get this description

        return "";
    }

}
