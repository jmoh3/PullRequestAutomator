package com.example;

public class Main {

    /**
     * Main method to apply a patch and make a pull request in an open source repository.
     *
     * @param args pass in path to patch file.
     */
    public static void main(String[] args) {

        if (args.length == 7)  {
            Patch patch = new Patch(args[0], args[1]);

            PullRequestAutomator automator = new PullRequestAutomator(patch, args[2], args[3], args[4], args[5], args[6]);

            boolean success = automator.makePullRequest();

            if (success) {
                System.out.println("Pull request has succeeded.");
                patch.undoPatch();
            } else {
                System.out.println("Pull request has failed.");
                patch.undoPatch();
            }
        } else {
            System.out.println("Incorrect arguments");
        }

    }
}
