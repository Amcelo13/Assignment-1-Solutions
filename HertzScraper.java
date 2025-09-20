import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.Alert;
import java.time.Duration;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class HertzScraper {

    private static void handleAlert(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            System.out.println("Alert text: " + alert.getText());
            alert.accept(); // Accept the alert
            System.out.println("Alert accepted.");
        } catch (NoAlertPresentException e) {
            System.out.println("No alert present.");
        } catch (Exception e) {
            System.out.println("Error handling alert: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");

        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); // Commented out to show browser window
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        List<String[]> allScrapedData = new ArrayList<>();
        allScrapedData.add(new String[]{"Page Title", "Main Heading", "Link Text", "Link URL"});

        try {
            // --- Page 1: Main Car Rental Page ---
            driver.get("https://www.enterprise.ca/en/car-rental.html");
            driver.manage().window().maximize();

            // Task 1: Interact with cookie banner
            try {
                WebElement closeCookieButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), \'CLOSE\')]")));
                closeCookieButton.click();
                System.out.println("Cookie banner closed.");
            } catch (Exception e) {
                System.out.println("Cookie banner not found or not clickable on main page.");
            }

            // Handle any potential alerts after initial page load or cookie interaction
            handleAlert(driver);

            // Task 1: Fill out the booking form
            try {
                // Wait for the location input field and enter "Pearson International"
                WebElement locationInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("pickupLocationTextBox")));
                locationInput.clear();
                locationInput.sendKeys("Pearson International");
                Thread.sleep(2000); // Wait for autocomplete suggestions
                
                // Click on the Toronto Pearson International Airport option from dropdown
                try {
                    // Wait for the dropdown to appear and find the specific Pearson airport option
                    WebElement pearsonOption = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//li[@id='location-1019226']//button[@data-location-type='airports'] | " +
                                "//li[contains(@class, 'location-group__item')]//span[contains(text(), 'Toronto Pearson International Airport')]/..")
                    ));
                    pearsonOption.click();
                    System.out.println("Selected Toronto Pearson International Airport (YYZ)");
                } catch (Exception e) {
                    // Try alternative selector for the dropdown option
                    try {
                        WebElement pearsonAlt = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[contains(.//span, 'Toronto Pearson International Airport')] | " +
                                    "//li[contains(., 'Toronto Pearson International Airport')]")
                        ));
                        pearsonAlt.click();
                        System.out.println("Selected Toronto Pearson International Airport (alternative selector)");
                    } catch (Exception e2) {
                        System.out.println("Autocomplete suggestion not found, continuing with manual entry: " + e2.getMessage());
                    }
                }

                // Set pickup time to 10:00 AM
                try {
                    WebElement pickupTimeSelect = driver.findElement(By.xpath("//select[contains(@aria-label, 'Pick-Up Time Selector')]"));
                    pickupTimeSelect.click();
                    WebElement pickupTime = driver.findElement(By.xpath("//option[@value='10:00 AM']"));
                    pickupTime.click();
                    System.out.println("Set pickup time to 10:00 AM");
                } catch (Exception e) {
                    System.out.println("Could not set pickup time: " + e.getMessage());
                }

                // Set return time to 10:00 AM
                try {
                    WebElement returnTimeSelect = driver.findElement(By.xpath("//select[contains(@aria-label, 'Return Time Selector')]"));
                    returnTimeSelect.click();
                    WebElement returnTime = driver.findElement(By.xpath("//option[@value='10:00 AM']"));
                    returnTime.click();
                    System.out.println("Set return time to 10:00 AM");
                } catch (Exception e) {
                    System.out.println("Could not set return time: " + e.getMessage());
                }

                // Set renter age to 25+
                try {
                    WebElement ageSelect = driver.findElement(By.id("age"));
                    ageSelect.click();
                    WebElement age25Plus = driver.findElement(By.xpath("//option[@value='25']"));
                    age25Plus.click();
                    System.out.println("Set renter age to 25+");
                } catch (Exception e) {
                    System.out.println("Could not set renter age: " + e.getMessage());
                }

                // Click Browse Vehicles button
                try {
                    WebElement browseVehiclesBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("continueButton")));
                    browseVehiclesBtn.click();
                    System.out.println("Clicked Browse Vehicles button");
                    Thread.sleep(3000); // Wait for page to load
                } catch (Exception e) {
                    System.out.println("Could not click Browse Vehicles button: " + e.getMessage());
                }

            } catch (Exception e) {
                System.out.println("Error filling out booking form: " + e.getMessage());
            }

            // String page1Title = driver.getTitle();
            // WebElement page1MainHeading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
            // String page1HeadingText = page1MainHeading.getText();
            // allScrapedData.add(new String[]{page1Title.replace(",", ""), page1HeadingText.replace(",", ""), "", ""});

            // // Extract vehicle information if browse vehicles was successful
            // try {
            //     // Wait for vehicle results to load
            //     Thread.sleep(5000);
                
            //     // Look for vehicle cards or results
            //     List<WebElement> vehicleElements = driver.findElements(By.xpath("//div[contains(@class, 'vehicle-card') or contains(@class, 'car-item') or contains(@class, 'vehicle-item')]"));
            //     if (!vehicleElements.isEmpty()) {
            //         System.out.println("Found " + vehicleElements.size() + " vehicle options");
            //         for (int i = 0; i < Math.min(5, vehicleElements.size()); i++) { // Limit to first 5 vehicles
            //             WebElement vehicle = vehicleElements.get(i);
            //             try {
            //                 String vehicleName = vehicle.findElement(By.xpath(".//h3 | .//h4 | .//*[contains(@class, 'vehicle-name')]")).getText();
            //                 allScrapedData.add(new String[]{page1Title.replace(",", ""), "Vehicle Option", vehicleName.replace(",", ""), ""});
            //             } catch (Exception e) {
            //                 System.out.println("Could not extract vehicle " + (i+1) + " details");
            //             }
            //         }
            //     } else {
            //         System.out.println("No vehicle results found on page");
            //     }
            // } catch (Exception e) {
            //     System.out.println("Error extracting vehicle information: " + e.getMessage());
            // }

            // // Extract links from main page (e.g., Car Rental Deals)
            // List<WebElement> mainPageLinks = driver.findElements(By.xpath("//a[contains(@href, \'car-rental-deals\')]"));
            // for (WebElement link : mainPageLinks) {
            //     allScrapedData.add(new String[]{page1Title.replace(",", ""), page1HeadingText.replace(",", ""), link.getText().replace(",", ""), link.getAttribute("href").replace(",", "")});
            // }

            // // --- Page 2: Canada Car Rental Locations ---
            // System.out.println("Navigating to Canada Car Rental Locations...");
            // driver.get("https://www.enterprise.ca/en/car-rental/locations/canada.html");
            // handleAlert(driver);

            // try {
            //     WebElement closeCookieButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), \'CLOSE\')]")));
            //     closeCookieButton.click();
            //     System.out.println("Cookie banner closed on Canada page.");
            // } catch (Exception e) {
            //     System.out.println("Cookie banner not found or not clickable on Canada page.");
            // }

            // String page2Title = driver.getTitle();
            // WebElement page2MainHeading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
            // String page2HeadingText = page2MainHeading.getText();
            // allScrapedData.add(new String[]{page2Title.replace(",", ""), page2HeadingText.replace(",", ""), "", ""});

            // // Extract city links from Canada page
            // // Updated XPath to be more specific to the location list
            // List<WebElement> canadaCityLinks = driver.findElements(By.xpath("//div[contains(@class, \'cmp-location-finder__results-list\')]//a"));
            // if (canadaCityLinks.isEmpty()) {
            //     // Fallback to a broader search if the specific class is not found or links are empty
            //     canadaCityLinks = driver.findElements(By.xpath("//a[contains(@href, \'/en/car-rental/locations/canada/\') and not(contains(@href, \'.html\'))]"));
            // }
            // for (WebElement link : canadaCityLinks) {
            //     if (!link.getText().isEmpty() && !link.getText().contains("View Details")) { // Filter out generic links
            //         allScrapedData.add(new String[]{page2Title.replace(",", ""), page2HeadingText.replace(",", ""), link.getText().replace(",", ""), link.getAttribute("href").replace(",", "")});
            //     }
            // }

            // // --- Page 3: International Car Rental Locations (Handle 404) ---
            // System.out.println("Attempting to navigate to International Car Rental Locations...");
            // String internationalUrl = "https://www.enterprise.ca/en/car-rental/locations/international.html";
            // driver.get(internationalUrl);
            // handleAlert(driver);

            // if (driver.getTitle().contains("404 Page Not Found")) {
            //     System.out.println("International Car Rental Locations page resulted in 404. Skipping data extraction for this page.");
            //     allScrapedData.add(new String[]{"404 Page Not Found", "Error 404: Page Not Found", "", internationalUrl});
            // } else {
            //     try {
            //         WebElement closeCookieButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), \'CLOSE\')]")));
            //         closeCookieButton.click();
            //         System.out.println("Cookie banner closed on International page.");
            //     } catch (Exception e) {
            //         System.out.println("Cookie banner not found or not clickable on International page.");
            //     }

            //     String page3Title = driver.getTitle();
            //     WebElement page3MainHeading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
            //     String page3HeadingText = page3MainHeading.getText();
            //     allScrapedData.add(new String[]{page3Title.replace(",", ""), page3HeadingText.replace(",", ""), "", ""});

            //     // Extract country links from International page
            //     List<WebElement> internationalCountryLinks = driver.findElements(By.xpath("//a[contains(@href, \'/en/car-rental/locations/international/\') and not(contains(@href, \".html\"))]"));
            //     for (WebElement link : internationalCountryLinks) {
            //         if (!link.getText().isEmpty()) {
            //             allScrapedData.add(new String[]{page3Title.replace(",", ""), page3HeadingText.replace(",", ""), link.getText().replace(",", ""), link.getAttribute("href").replace(",", "")});
            //         }
            //     }
            // }

            // // Save all scraped data to a CSV file
            // try (FileWriter csvWriter = new FileWriter("scraped_data_multi_page.csv")) {
            //     for (String[] rowData : allScrapedData) {
            //         csvWriter.append(String.join(",", rowData));
            //         csvWriter.append("\n");
            //     }
            //     System.out.println("All data saved to scraped_data_multi_page.csv");
            // } catch (IOException e) {
            //     System.err.println("Error writing to CSV file: " + e.getMessage());
            // }

        }
        
         finally {
            // driver.quit(); // Commented out to keep Chrome open for debugging
            System.out.println("Chrome browser left open for debugging. Close manually when done.");
        }
    }
}

