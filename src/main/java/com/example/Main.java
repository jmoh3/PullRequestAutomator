package com.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    /**
     * Main method to apply a patch and make a pull request in an open source repository.
     *
     * @param args pass in path to patch file.
     */
    public static void main(String[] args) {

        if (args.length == 8)  {
            Automator automator = new Automator(new Patch(args[0]), args[1], args[2], args[3],
                    args[4], args[5], args[6], args[7]);

            automator.makePullRequest();
        }

    }
}
