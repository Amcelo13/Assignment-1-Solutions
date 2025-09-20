// Simple test to check if we can access Hertz without being blocked
// Run: java -cp ".:/Users/chetansmac/Desktop/UWindsor/COMP Advanced Computing Concepts/Assignment 1/selenium-java-4.35.0/*" HertzTest

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.time.Duration;
import java.util.Arrays;

public class HertzTest {
    private WebDriver driver;
    
    public static void main(String[] args) throws Exception {
        HertzTest test = new HertzTest();
        try {
            test.startDriver();
            test.testAccess();
        } finally {
            test.quit();
        }
    }
    
    private void startDriver() {
        ChromeOptions options = new ChromeOptions();
        
        // Anti-detection options
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        
        // Realistic browser setup
        options.addArguments("--start-maximized");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");
        
        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
        driver = new ChromeDriver(options);
        
        // Remove automation indicators
        ((JavascriptExecutor) driver).executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
        
        System.out.println("Driver started successfully");
    }
    
    private void testAccess() {
        try {
            System.out.println("Attempting to access Hertz website...");
            driver.get("https://www.hertz.ca");
            
            // Wait a bit
            Thread.sleep(3000);
            
            String title = driver.getTitle();
            String url = driver.getCurrentUrl();
            String pageSource = driver.getPageSource();
            
            System.out.println("Page Title: " + title);
            System.out.println("Current URL: " + url);
            
            if (pageSource.toLowerCase().contains("access denied") || 
                pageSource.toLowerCase().contains("blocked") ||
                title.toLowerCase().contains("access denied")) {
                System.out.println("❌ BLOCKED: Access denied detected");
                System.out.println("Page source excerpt: " + pageSource.substring(0, Math.min(500, pageSource.length())));
            } else {
                System.out.println("✅ SUCCESS: Page loaded successfully");
                System.out.println("First 200 chars of page: " + pageSource.substring(0, Math.min(200, pageSource.length())));
            }
            
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
        }
    }
    
    private void quit() {
        if (driver != null) {
            driver.quit();
        }
    }
}
