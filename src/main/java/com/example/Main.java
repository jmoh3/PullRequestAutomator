package com.example;

public class Main {

    /**
     * Main method to apply a patch and make a pull request in an open source repository.
     *
     * @param args pass in path to patch file.
     */
    public static void main(String[] args) {

        if (args.length == 8)  {
            PullRequestAutomator automator = new PullRequestAutomator(new Patch(args[0]), args[1], args[2], args[3],
                    args[4], args[5], args[6], args[7]);

            boolean success = automator.makePullRequest();

            if (success) {
                System.out.println("Pull request has succeeded.");
            } else {
                System.out.println("Pull request has failed.");
            }
        }

    }
}
