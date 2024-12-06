package org.example;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.PerformsTouchActions;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.*;

class PassFailMetrics {
    private float pass;
    private float fail;

    public PassFailMetrics(float pass, float fail) {
        this.pass = pass;
        this.fail = fail;
    }

    public float getPass() {
        return pass;
    }

    public void setPass(float pass) {
        this.pass = pass;
    }

    public float getFail() {
        return fail;
    }

    public void setFail(float fail) {
        this.fail = fail;
    }

    @Override
    public String toString() {
        return "(Pass: " + pass + ", Fail: " + fail + ")";
    }
}

class TestCase {
    @JsonProperty("test_number")
    private String testNumber;

    @JsonProperty("result")
    private String expectedResult;

    @JsonProperty("scenario")
    private String scenario;

    @JsonProperty("actualResult")
    private String actualResult;

    @JsonProperty("status")
    private String status;

    public TestCase() {}

    public TestCase(String testNumber, String expectedResult, String scenario, String actualResult, String status) {
        this.testNumber = testNumber;
        this.expectedResult = expectedResult;
        this.scenario = scenario;
        this.actualResult = actualResult;
        this.status = status;
    }

    public String getTestNumber() {
        return testNumber;
    }

    public void setTestNumber(String testNumber) {
        this.testNumber = testNumber;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
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
                "testNumber='" + testNumber + '\'' +
                ", expectedResult='" + expectedResult + '\'' +
                ", scenario='" + scenario + '\'' +
                ", actualResult='" + actualResult + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

public class SeeingAITest {
    private static AppiumDriver driver;
    private static final int totalTests = 20;
    private static int passCount = 0;
    private static int failCount = 0;
    private static final Set<String> objectSet = new HashSet<>();
    private static final Map<String, PassFailMetrics> scenarioMetrics = new HashMap<>();

    public static void main(String[] args) {
        executeAppiumTests();
        generateTestSummary();
    }

    private static void executeAppiumTests() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", "android");
        capabilities.setCapability("platformVersion", "13");
        capabilities.setCapability("deviceName", "pixel8");
        capabilities.setCapability("automationName", "UiAutomator2");
        capabilities.setCapability("appPackage", "com.microsoft.seeingai");
        capabilities.setCapability("appActivity", "crc64a8457ff90b487ee0.SplashActivity");

        List<TestCase> testCases = readTestCasesFromJson();

        try {
            driver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
            WebDriverWait wait = new WebDriverWait(driver, 30);

            setupInitialAppFlow(wait);
            runTests(wait, testCases);
            saveTestResults(testCases);

        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL", e);
        }
    }

    private static void setupInitialAppFlow(WebDriverWait wait) {
        clickButton(wait, "com.microsoft.seeingai:id/pagedSkipButton");
        clickButton(wait, "com.microsoft.seeingai:id/terms_check_box");
        clickButton(wait, "com.microsoft.seeingai:id/terms_getstarted_button");
        clickButton(wait, "com.android.permissioncontroller:id/permission_allow_foreground_only_button");
        clickButton(wait, "com.microsoft.seeingai:id/close_icon_bottom_sheet");
    }

    private static void runTests(WebDriverWait wait, List<TestCase> testCases) {
        navigateToHomeScreen();
        selectPhoto(wait);

        for (int i = 0; i < totalTests; i++) {
            sharePhoto(wait);
            TestCase testCase = testCases.get(i);
            processTestCase(wait, testCase);
            objectSet.add(testCase.getActualResult());
            navigateToPreviousScreen(wait);
            swipeToNextImage();
        }
    }

    private static void selectPhoto(WebDriverWait wait) {
        try {
            WebElement fileApp = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//android.widget.TextView[@content-desc=\"Photos\"]")));
            fileApp.click();

            WebElement photo1 = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//android.widget.ImageView[@content-desc=\"Photo taken on Dec 5, 2024 1:39 AM\"]")));
            photo1.click();
        } catch (Exception e) {
            System.out.println("Error selecting photo.");
            throw e;
        }
    }

    private static void sharePhoto(WebDriverWait wait) {
        try {
            WebElement shareButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("com.google.android.apps.photos:id/share")));
            shareButton.click();

            WebElement appToShare = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//android.widget.RelativeLayout[@resource-id=\"com.google.android.apps.photos:id/peoplekit_new_app_item\"])[1]")));
            appToShare.click();
        } catch (Exception e) {
            System.out.println("Error during sharing.");
            throw e;
        }
    }

    private static void processTestCase(WebDriverWait wait, TestCase testCase) {
        WebElement resultElement = wait.until(ExpectedConditions.elementToBeClickable(By.id("com.microsoft.seeingai:id/result_cell_text")));
        String actualResult = resultElement.getText();
        testCase.setActualResult(actualResult);

        boolean isPass = actualResult.contains(testCase.getExpectedResult());
        if (isPass) {
            passCount++;
            scenarioMetrics.computeIfAbsent(testCase.getScenario(), k -> new PassFailMetrics(0, 0)).setPass(scenarioMetrics.get(testCase.getScenario()).getPass() + 1);
            testCase.setStatus("Pass");
        } else {
            failCount++;
            scenarioMetrics.computeIfAbsent(testCase.getScenario(), k -> new PassFailMetrics(0, 0)).setFail(scenarioMetrics.get(testCase.getScenario()).getFail() + 1);
            testCase.setStatus("Fail");
        }
    }

    private static List<TestCase> readTestCasesFromJson() {
        ObjectMapper mapper = new ObjectMapper();
        List<TestCase> testCases = new ArrayList<>();
        try {
            JsonNode rootNode = mapper.readTree(new File("expected_outputs.json")).get("test_cases");
            for (JsonNode node : rootNode) {
                TestCase testCase = mapper.treeToValue(node, TestCase.class);
                testCases.add(testCase);
                scenarioMetrics.putIfAbsent(testCase.getScenario(), new PassFailMetrics(0, 0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testCases;
    }

    private static void saveTestResults(List<TestCase> testCases) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            mapper.writeValue(new File("actual_outputs.json"), testCases);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void clickButton(WebDriverWait wait, String elementId) {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.id(elementId)));
        button.click();
    }

    private static void navigateToHomeScreen() {
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.HOME));
    }

    private static void navigateToPreviousScreen(WebDriverWait wait) {
        WebElement backButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//android.widget.ImageButton[@content-desc=\"Navigate up\"]")));
        backButton.click();
    }

    private static void swipeToNextImage() {
        new TouchAction<>((PerformsTouchActions) driver)
                .press(PointOption.point(900, 1200))
                .waitAction(WaitOptions.waitOptions(Duration.ofSeconds(1)))
                .moveTo(PointOption.point(100, 1200))
                .release()
                .perform();
    }

    private static void generateTestSummary() {
        double passRate = (double) passCount / totalTests * 100;

        System.out.println("Pass: " + passCount + " | Fail: " + failCount + " | Total: " + totalTests);
        System.out.println("Pass Rate: " + passRate + "%");

        scenarioMetrics.forEach((scenario, metrics) -> {
            float total = metrics.getPass() + metrics.getFail();
            float passPercentage = (metrics.getPass() / total) * 100;
            float failPercentage = (metrics.getFail() / total) * 100;
            System.out.println("Scenario: " + scenario + " | Pass: " + passPercentage + "% | Fail: " + failPercentage + "%");
        });
    }
}


