package com.example;

public class TestResult {
    private String name;
    private String result;
    private double time;

    public TestResult(String setName, String setResult, double setTime) {
        this.name = setName;
        this.result = setResult;
        this.time = setTime;
    }

    public String getName() {
        return name;
    }

    public String getResult() {
        return result;
    }

    public double getTime() {
        return time;
    }
}
