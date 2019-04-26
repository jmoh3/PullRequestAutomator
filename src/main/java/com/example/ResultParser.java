package com.example;

import com.google.gson.Gson;

import java.util.HashMap;

public class ResultParser {

    public enum Result {
        PASS, FAIL
    }

    private HashMap<String, Result> results;
    private HashMap<String, Double> times;

    public ResultParser(String jsonFilename) {

        String jsonString = null;

        try {
            jsonString = PullRequestAutomator.readFileAsString(jsonFilename);
        } catch (Exception e) {
            return;
        }

        Gson gson = new Gson();
        TestRunResult result = gson.fromJson(jsonString, TestRunResult.class);

        times = new HashMap<String, Double>();
        results = new HashMap<String, Result>();

        HashMap<String, TestResult> resultHashMap = result.getResults();

        for (String key : resultHashMap.keySet()) {
            Double time = resultHashMap.get(key).getTime();
            times.put(key, time);

            if (resultHashMap.get(key).getResult().equals("PASS")) {
                results.put(key, Result.PASS);
            } else {
                results.put(key, Result.FAIL);
            }
        }
    }

    public double getTimeForTest(String testName) {
        return times.get(testName);
    }

    public Result getResultForTest(String testName) {
        return results.get(testName);
    }
}
