// Alternative approach: Use direct URL with parameters to bypass form filling
// Run: java -cp ".:/Users/chetansmac/Desktop/UWindsor/COMP Advanced Computing Concepts/Assignment 1/selenium-java-4.35.0/*" HertzDirect

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class HertzDirect {
    private WebDriver driver;
    private WebDriverWait wait;
    
    public static void main(String[] args) throws Exception {
        HertzDirect scraper = new HertzDirect();
        try {
            scraper.startDriver();
            scraper.accessDirectResults();
        } finally {
            scraper.quit();
        }
    }
    
    private void startDriver() {
        ChromeOptions options = new ChromeOptions();
        
        // Maximum stealth
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        
        options.addArguments("--start-maximized");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");
        
        // Additional stealth options
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-plugins-discovery");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--ignore-certificate-errors");
        
        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        
        // Remove automation indicators
        ((JavascriptExecutor) driver).executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
        ((JavascriptExecutor) driver).executeScript("Object.defineProperty(navigator, 'plugins', {get: () => [1, 2, 3, 4, 5]})");
        
        System.out.println("Driver started with maximum stealth");
    }
    
    private void accessDirectResults() {
        try {
            // Try to construct a direct URL to the results page
            // This bypasses the form submission entirely
            LocalDate pickupDate = LocalDate.now().plusDays(30); // 30 days from now
            LocalDate dropoffDate = pickupDate.plusDays(1); // Next day
            
            String directUrl = "https://www.hertz.ca/rentacar/reservation/vehicles" +
                "?pickUpLocation=YYZ" + // Toronto Pearson Airport code
                "&returnLocation=YYZ" +
                "&pickUpDate=" + pickupDate.toString() +
                "&returnDate=" + dropoffDate.toString() +
                "&pickUpTime=13:00" +
                "&returnTime=13:00";
            
            System.out.println("Attempting direct access to results: " + directUrl);
            
            driver.get(directUrl);
            Thread.sleep(5000); // Wait for page load
            
            String title = driver.getTitle();
            String url = driver.getCurrentUrl();
            System.out.println("Page Title: " + title);
            System.out.println("Current URL: " + url);
            
            // Look for vehicle cards
            List<By> vehicleSelectors = Arrays.asList(
                By.cssSelector("div.vehicle"),
                By.cssSelector("div.gtm-vehicle"),
                By.cssSelector("[class*='vehicle-card']"),
                By.cssSelector("[class*='car-card']")
            );
            
            boolean foundVehicles = false;
            for (By selector : vehicleSelectors) {
                List<WebElement> elements = driver.findElements(selector);
                if (!elements.isEmpty()) {
                    System.out.println("✅ Found " + elements.size() + " vehicles using selector: " + selector);
                    foundVehicles = true;
                    break;
                }
            }
            
            if (!foundVehicles) {
                System.out.println("❌ No vehicles found");
                String pageSource = driver.getPageSource();
                if (pageSource.toLowerCase().contains("access denied") || 
                    pageSource.toLowerCase().contains("blocked")) {
                    System.out.println("❌ Still blocked");
                } else {
                    System.out.println("Page might be loading or no results available");
                    System.out.println("Page source length: " + pageSource.length());
                    System.out.println("First 500 chars: " + pageSource.substring(0, Math.min(500, pageSource.length())));
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void quit() {
        if (driver != null) {
            driver.quit();
        }
    }
}
