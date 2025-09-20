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
        allScrapedData.add(new String[]{"Page Title", "Section", "Vehicle Info", "Details"});

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

            // Get page title and heading after form submission
            String page1Title = driver.getTitle();
            try {
                WebElement page1MainHeading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
                String page1HeadingText = page1MainHeading.getText();
                allScrapedData.add(new String[]{page1Title.replace(",", ""), page1HeadingText.replace(",", ""), "", ""});
            } catch (Exception e) {
                System.out.println("Could not find main heading: " + e.getMessage());
                allScrapedData.add(new String[]{page1Title.replace(",", ""), "Vehicle Selection Page", "", ""});
            }

            // Extract vehicle information if browse vehicles was successful
            try {
                // Wait for vehicle results to load
                Thread.sleep(5000);
                
                // Look for vehicle cards using the actual class structure
                List<WebElement> vehicleElements = driver.findElements(By.xpath("//li[contains(@class, 'vehicle-list__item')]"));
                if (!vehicleElements.isEmpty()) {
                    System.out.println("Found " + vehicleElements.size() + " vehicle options");
                    for (int i = 0; i < vehicleElements.size(); i++) { // Extract all vehicles
                        WebElement vehicle = vehicleElements.get(i);
                        try {
                            // Extract vehicle details based on the actual HTML structure
                            String vehicleCode = "";
                            String vehicleName = "";
                            String vehicleDescription = "";
                            String priceAmount = "";
                            String transmission = "";
                            String passengers = "";
                            String bags = "";
                            
                            try {
                                vehicleCode = vehicle.findElement(By.xpath(".//p[@class='vehicle-item__tour-info mb-0']")).getText();
                            } catch (Exception e) {
                                vehicleCode = "N/A";
                            }
                            
                            try {
                                vehicleName = vehicle.findElement(By.xpath(".//h2[@class='mb-0']")).getText();
                            } catch (Exception e) {
                                vehicleName = "N/A";
                            }
                            
                            try {
                                vehicleDescription = vehicle.findElement(By.xpath(".//p[@class='descriptor mb-0']")).getText();
                            } catch (Exception e) {
                                vehicleDescription = "N/A";
                            }
                            
                            try {
                                // Extract price (symbol + unit + fraction)
                                String symbol = vehicle.findElement(By.xpath(".//span[@class='rs-price-tag__symbol']")).getText();
                                String unit = vehicle.findElement(By.xpath(".//span[@class='rs-price-tag__unit']")).getText();
                                String fraction = vehicle.findElement(By.xpath(".//span[@class='rs-price-tag__fraction']")).getText();
                                priceAmount = symbol + " " + unit + fraction;
                            } catch (Exception e) {
                                try {
                                    // Alternative: look for "Call For Availability"
                                    priceAmount = vehicle.findElement(By.xpath(".//p[contains(@class, 'car-item__price-details-message')]")).getText();
                                } catch (Exception e2) {
                                    priceAmount = "N/A";
                                }
                            }
                            
                            try {
                                List<WebElement> attributes = vehicle.findElements(By.xpath(".//section[@class='car-item__vehicle-attributes-item']//span[@class='descriptor mb-0']"));
                                if (attributes.size() >= 3) {
                                    transmission = attributes.get(0).getText();
                                    passengers = attributes.get(1).getText();
                                    bags = attributes.get(2).getText();
                                }
                            } catch (Exception e) {
                                transmission = "N/A";
                                passengers = "N/A";
                                bags = "N/A";
                            }
                            
                            // Add vehicle data to CSV
                            String vehicleInfo = vehicleCode + " - " + vehicleName + " (" + vehicleDescription + ")";
                            String vehicleDetails = "Price: " + priceAmount + " | " + transmission + " | " + passengers + " | " + bags;
                            allScrapedData.add(new String[]{page1Title.replace(",", ""), "Vehicle Option " + (i+1), vehicleInfo.replace(",", ""), vehicleDetails.replace(",", "")});
                            
                            System.out.println("Extracted vehicle " + (i+1) + ": " + vehicleCode + " - " + vehicleName + " - " + priceAmount);
                            
                        } catch (Exception e) {
                            System.out.println("Could not extract vehicle " + (i+1) + " details: " + e.getMessage());
                        }
                    }
                } else {
                    System.out.println("No vehicle results found on page");
                }
            } catch (Exception e) {
                System.out.println("Error extracting vehicle information: " + e.getMessage());
            }

            // Save all scraped data to a CSV file
            try (FileWriter csvWriter = new FileWriter("hertz_vehicles.csv")) {
                for (String[] rowData : allScrapedData) {
                    csvWriter.append(String.join(",", rowData));
                    csvWriter.append("\n");
                }
                System.out.println("All vehicle data saved to hertz_vehicles.csv");
            } catch (IOException e) {
                System.err.println("Error writing to CSV file: " + e.getMessage());
            }

        }
        
         finally {
            // driver.quit(); // Commented out to keep Chrome open for debugging
            System.out.println("Chrome browser left open for debugging. Close manually when done.");
        }
    }
}

