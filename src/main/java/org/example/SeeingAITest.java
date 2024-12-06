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
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


class PassFail {
    private float pass;
    private float fail;

    public PassFail(float pass, float fail) {
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


public class SeeingAITest {
    public static AppiumDriver driver;
    public static int totalTests = 20;
    public static int passCount = 0;
    public static int failCount = 0;
    public static Set<String> objectSet = new HashSet<>();
    public static Map<String, PassFail> scenarioMap = new HashMap<>();

    public static void main(String[] args) {
        appiumTest();
        double passRate = (double) passCount / totalTests * 100;

        System.out.println("");
        System.out.println("XXXXXXXXXXXXXXXXXXX");
        System.out.println("");
        System.out.println("Test Statistics -----------------------------");
        System.out.println("Pass: " + passCount + " | Fail: " + failCount  + " | Total: " + totalTests );
        System.out.println("Pass %: " + passRate + "%");
        System.out.println("");
        System.out.println("XXXXXXXXXXXXXXXXXXX");
        System.out.println("");
        System.out.println("Test Coverage -----------------------------");
        for(String object : objectSet)
        {
            System.out.print(object + " | ");
        }
        System.out.println("\n");
        System.out.println("XXXXXXXXXXXXXXXXXXX");
        System.out.println("");
        System.out.println("Test Quality Assurance Criteria -----------------------------");
        System.out.println("A test is considered to be passed if the actual output given by the application has a"
                + " substring\n of the expected output that is given in the expected_outputs.json file.\n If this cindition"
                + " is not satisfied the test is considered to fail");
        System.out.println("");
        System.out.println("XXXXXXXXXXXXXXXXXXX");
        System.out.println("");
        System.out.println("Test Summary -----------------------------");
        System.out.println("The script ran tests for total "+totalTests+" test cases out of which "+ passRate+"% passed");
        System.out.println("The summary for various scenarios is listed below:");
        for (String key : scenarioMap.keySet()) {
            PassFail value = scenarioMap.get(key);
            float passPercent = (value.getPass()/( value.getPass() + value.getFail() )) * 100;
            float failPercent = (value.getFail()/( value.getPass() + value.getFail() )) * 100;
            // Do something with key and value
            System.out.println("Scenario: " + key + " | Pass%:  " + passPercent + " | Fail%:  " + failPercent + " | Total:  " + (value.getPass() + value.getFail()) );
            System.out.println("");
        }

    }

    public static void appiumTest() {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("platformName", "android");
        caps.setCapability("platformVersion", "13");
        caps.setCapability("deviceName", "pixel8");
        // caps.setCapability("udid", "emulator-5554");
        caps.setCapability("automationName", "UiAutomator2");
        caps.setCapability("appPackage", "com.microsoft.seeingai");
        caps.setCapability("appActivity", "crc64a8457ff90b487ee0.SplashActivity");

         List<TestCase> testCases = readTestCasesFromFile();

        try {
            URL url = new URL("http://127.0.0.1:4723/wd/hub/");
//            URL url = new URL("http://127.0.0.1:4723/");
            driver = new AndroidDriver(url, caps);
//            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            WebDriverWait wait = new WebDriverWait(driver, 30);

            skipButton(wait);
            acceptTerms(wait);
            getStarted(wait);
            handlePermissionDialog(wait);
            closeOverlay(wait);
            navigateToHome();
            selectPhoto(wait);

             for (int i = 0; i < totalTests; i++) {
                 sharePhoto(wait);
                 TestCase testcase = testCases.get(i);
                 getResults(wait, testcase);
                 testcase.setStatus(compareResults(testcase.getResult(), testcase.getScenario()));
                 goBack(wait);
                 swipeToNextImage();
                 objectSet.add(testcase.getResult());
             }

             navigateToHome();
             writeTestCasesToFile(testCases);

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void getResults(WebDriverWait wait, TestCase testcase) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.id("com.microsoft.seeingai:id/result_cell_text")));
        String actualResult = element.getText();
        testcase.setActualResult(actualResult);
        System.out.println("TestCase No.: " + testcase.getTest_number() );
        System.out.println("Scenario: " + testcase.getScenario());
        System.out.println("Expected Result: " + testcase.getResult());
        System.out.println("Actual Result: " + actualResult);
    }

    private static String compareResults(String expectedResult, String scenario) {
        WebElement element = driver.findElement(By.id("com.microsoft.seeingai:id/result_cell_text"));
        String actualResult = element.getText().toLowerCase();
        boolean pass = true;
        if (!actualResult.contains(expectedResult)) {
            pass = false;
        }
        PassFail obj = scenarioMap.get(scenario);
        if (pass) {
            passCount++;
            System.out.println("Pass");
            obj.setPass(obj.getPass()+1);
            return "Pass";
        } else {
            failCount++;
            System.out.println("Fail");
            obj.setFail(obj.getFail()+1);
            return "Fail";
        }
    }

    private static List<TestCase> readTestCasesFromFile() {
        ObjectMapper objectMapper = new ObjectMapper();
        List<TestCase> testCases = new ArrayList<>();

        try {
            // Read JSON file
            JsonNode jsonNode = objectMapper.readTree(new File("expected_outputs.json"));

            JsonNode testCasesNode = jsonNode.get("test_cases");

            for (JsonNode testCaseNode : testCasesNode) {
                TestCase testCase = objectMapper.treeToValue(testCaseNode, TestCase.class);
                testCases.add(testCase);
                if(!scenarioMap.containsKey(testCase.getScenario()))
                {
                    scenarioMap.put(testCase.getScenario(), new PassFail(0,0));
                }
            }

            // Print the test cases
            for (TestCase testCase : testCases) {
                System.out.println(testCase);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testCases;
    }

    private static void writeTestCasesToFile(List<TestCase> testCases) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Enable pretty printing
        try {
            objectMapper.writeValue(new File("actual_outputs.json"), testCases);
            System.out.println("Test cases have been written to actual_outputs.json successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void skipButton(WebDriverWait wait) {
        WebElement skipButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("com.microsoft.seeingai:id/pagedSkipButton")));
        skipButton.click();
    }

    private static void acceptTerms(WebDriverWait wait) {
        WebElement termsCheckBox = wait.until(ExpectedConditions.elementToBeClickable(By.id("com.microsoft.seeingai:id/terms_check_box")));
        termsCheckBox.click();
    }

    private static void getStarted(WebDriverWait wait) {
        WebElement getStartedButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("com.microsoft.seeingai:id/terms_getstarted_button")));
        getStartedButton.click();
    }

    private static void handlePermissionDialog(WebDriverWait wait) {
        WebElement allowButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("com.android.permissioncontroller:id/permission_allow_foreground_only_button")));
        allowButton.click();
    }

    private static void closeOverlay(WebDriverWait wait) {
        WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("com.microsoft.seeingai:id/close_icon_bottom_sheet")));
        closeButton.click();
    }

    private static void navigateToHome() {
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.HOME));
        System.out.println("At Home");
    }

    private static void selectPhoto(WebDriverWait wait) {
        WebElement fileApp = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//android.widget.TextView[@content-desc=\"Photos\"]")));
        fileApp.click();
        System.out.println("In Photos App");

        WebElement photo1 = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//android.widget.ImageView[@content-desc=\"Photo taken on Dec 5, 2024 1:39 AM\"]")));
        photo1.click();
    }

    private static void sharePhoto(WebDriverWait wait) {
        WebElement shareButton2 = wait.until(ExpectedConditions.elementToBeClickable(By.id("com.google.android.apps.photos:id/share")));
        shareButton2.click();
        WebElement appToShare = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//android.widget.RelativeLayout[@resource-id=\"com.google.android.apps.photos:id/peoplekit_new_app_item\"])[1]")));
        appToShare.click();
        System.out.println("Sharing Image to SeeingAI");
    }

    private static void goBack(WebDriverWait wait) {
        WebElement backButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//android.widget.ImageButton[@content-desc=\"Navigate up\"]")));
        backButton.click();
    }


    private static void swipeToNextImage() throws InterruptedException {
        Thread.sleep(1000);
        TouchAction ta = new TouchAction((PerformsTouchActions) driver);
        ta.press(PointOption.point(900, 1200)).waitAction(WaitOptions.waitOptions(Duration.ofMillis(1000))).moveTo(PointOption.point(100, 1200)).release().perform();
        Thread.sleep(1000);
        System.out.println("swiped to next Image------------------");
    }

}