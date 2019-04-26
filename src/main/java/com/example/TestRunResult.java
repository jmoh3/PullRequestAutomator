package com.example;

import java.util.HashMap;

public class TestRunResult {
    private String id;
    private String[] testOrder;
    private HashMap<String, TestResult> results;

    public TestRunResult(String setId, String[] setTestOrder, HashMap<String, TestResult> setTestResultHashMap) {
        this.id = setId;
        this.testOrder = setTestOrder;
        this.results = setTestResultHashMap;
    }

    public String getId() {
        return id;
    }

    public HashMap<String, TestResult> getResults() {
        return results;
    }

    public String[] getTestOrder() {
        return testOrder;
    }
}
