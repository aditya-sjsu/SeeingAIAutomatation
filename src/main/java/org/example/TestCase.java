package org.example;

public class TestCase {
    private String test_number;
    private String result;
    private String scenario;
    private String actualResult;
    private String status;

    public TestCase() {
    }

    public TestCase(String test_number, String result, String scenario, String actualResult, String status) {
        this.test_number = test_number;
        this.result = result;
        this.scenario = scenario;
        this.actualResult = actualResult;
        this.status = status;
    }

    public String getTest_number() {
        return test_number;
    }

    public void setTest_number(String test_number) {
        this.test_number = test_number;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getActualResult() {
        return actualResult;
    }

    public void setActualResult(String actualResult) {
        this.actualResult = actualResult;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TestCase{" +
                "test_number='" + test_number + '\'' +
                ", result='" + result + '\'' +
                ", scenario='" + scenario + '\'' +
                '}';
    }
}