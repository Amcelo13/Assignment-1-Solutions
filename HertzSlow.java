// Alternative approach with longer delays and different entry points
// This tries to mimic very slow human behavior

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.Arrays;

public class HertzSlow {
    public static void main(String[] args) throws Exception {
        System.out.println("=== ALTERNATIVE APPROACHES ===");
        System.out.println("1. Try manual HTML file approach for your assignment");
        System.out.println("2. Try a different car rental website (Budget, Enterprise, etc.)");
        System.out.println("3. Use longer delays between requests (current approach)");
        System.out.println("4. Consider using official APIs if available");
        
        System.out.println("\nFor your assignment, I recommend:");
        System.out.println("- Save some example Hertz pages manually");
        System.out.println("- Write code to parse the saved HTML files");
        System.out.println("- This demonstrates web scraping skills without legal/technical issues");
        
        // Uncomment below to try the slow approach
        /*
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");
        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
        WebDriver driver = new ChromeDriver(options);
        
        try {
            System.out.println("Trying very slow approach...");
            driver.get("https://www.google.com");
            Thread.sleep(10000); // Wait 10 seconds
            driver.get("https://www.hertz.ca");
            Thread.sleep(15000); // Wait 15 seconds
            System.out.println("Page title: " + driver.getTitle());
        } finally {
            driver.quit();
        }
        */
    }
}
