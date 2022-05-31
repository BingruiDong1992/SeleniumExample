import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v99.network.Network;
import org.openqa.selenium.devtools.v99.network.model.RequestId;
import org.openqa.selenium.devtools.v99.network.model.Response;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;

import java.util.Optional;
import java.util.logging.Level;

public class TestSelenium {
    private static final String TEST_URL = "https://www.google.com";

    public static void main(String[] args) throws InterruptedException {
        System.setProperty("webdriver.chrome.driver", "place\\to\\webdriver\\chromedriver.exe");
        ChromeOptions option = new ChromeOptions();
        option.setBinary("path\\to\\chrome\\chrome.exe");

        // The browser logging config
        LoggingPreferences preferences = new LoggingPreferences();
        preferences.enable(LogType.PERFORMANCE, Level.ALL);
        preferences.enable(LogType.BROWSER, Level.ALL);
        option.setCapability(CapabilityType.LOGGING_PREFS, preferences);
        option.setCapability("goog:loggingPrefs", preferences);
        option.addArguments();
        // Easy to debug
//        option.addArguments("headless");

        // Use the CDP to catch the browser actions
        ChromeDriver driver = new ChromeDriver(option);
        DevTools devTools = driver.getDevTools();
        devTools.createSession();
        devTools.send(Network.clearBrowserCache());
        devTools.send(Network.setCacheDisabled(true));
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.of(100000000)));

        // Listen for target network actions
        devTools.addListener(Network.responseReceived(), responseReceived -> {
            Response res = responseReceived.getResponse();

            if (res.getUrl().contains("play.google.com")) {
                System.out.println(res.getUrl());
                RequestId requestIds = responseReceived.getRequestId();
                String  responseBody = devTools.send(Network.getResponseBody(requestIds)).getBody();
                System.out.println(responseBody);
            }
        });

        driver.get(TEST_URL);
        Thread.sleep(7000);
        // Find the "Gmail" button, and click. Use find element by text
        driver.findElement (By.xpath ("//*[contains(text(),'Gmail')]")).click();
        Thread.sleep(2000);

        while (true) {
            LogEntries logs = driver.manage().logs().get(LogType.PERFORMANCE);

            for (LogEntry entry : logs) {
                if(entry.toString().contains("Network.responseReceived")) {
                    System.out.println(entry.getMessage());
                }
            }
            System.out.println("=====================");
            Thread.sleep(3000);
        }
    }
}
