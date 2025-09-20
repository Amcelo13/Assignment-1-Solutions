import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Keys;
import org.openqa.selenium.JavascriptExecutor;
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
            alert.accept(); 
            System.out.println("Alert accepted.");
        } catch (NoAlertPresentException e) {
            System.out.println("No alert present.");
        } catch (Exception e) {
            System.out.println("Error handling alert: " + e.getMessage());
        }
    }

    // Task 3: Advanced Selenium - Handle vehicle image pop-ups/modals
    private static void handleVehicleImagePopups(WebDriver driver, WebDriverWait wait, List<WebElement> vehicleElements, 
                                                List<String[]> allScrapedData, String pageTitle) {
        try {
            // Demonstrate clicking on vehicle images to open pop-ups (first 3 vehicles to avoid too much time)
            int vehiclesToTest = Math.min(3, vehicleElements.size());
            System.out.println("Testing image pop-ups for first " + vehiclesToTest + " vehicles...");
            
            for (int i = 0; i < vehiclesToTest; i++) {
                try {
                    WebElement vehicle = vehicleElements.get(i);
                    
                    // Find the clickable vehicle image button
                    WebElement imageButton = vehicle.findElement(By.xpath(".//button[contains(@class, 'car-item__vehicle-image--clickable')]"));
                    
                    // Get vehicle name for logging
                    String vehicleName = "Unknown";
                    try {
                        vehicleName = vehicle.findElement(By.xpath(".//h2[@class='mb-0']")).getText();
                    } catch (Exception e) {
                        vehicleName = "Vehicle " + (i + 1);
                    }
                    
                    System.out.println("Clicking on image for: " + vehicleName);
                    
                    // Advanced Selenium: Scroll element into view before clicking
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", imageButton);
                    Thread.sleep(1000); // Small pause for smooth scrolling
                    
                    // Click the image to open pop-up
                    imageButton.click();
                    
                    // Advanced Selenium: Wait for modal/pop-up to appear
                    try {
                        // Wait for modal dialog or overlay to appear
                        WebElement modal = wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//div[contains(@class, 'modal') or contains(@class, 'dialog') or contains(@class, 'overlay') or contains(@class, 'popup')]")
                        ));
                        System.out.println("✓ Pop-up opened successfully for " + vehicleName);
                        
                        // Extract any additional information from the modal
                        try {
                            String modalContent = modal.getText();
                            if (!modalContent.trim().isEmpty()) {
                                allScrapedData.add(new String[]{pageTitle, "Vehicle Image Modal " + (i+1), vehicleName + " - Modal Content", modalContent.replace(",", "").substring(0, Math.min(100, modalContent.length())) + "..."});
                            }
                        } catch (Exception e) {
                            System.out.println("Could not extract modal content: " + e.getMessage());
                        }
                        
                        // Advanced Selenium: Close the modal using various methods
                        try {
                            // Method 1: Look for close button
                            WebElement closeButton = driver.findElement(By.xpath("//button[contains(@class, 'close') or contains(@aria-label, 'close') or contains(@aria-label, 'Close')]"));
                            closeButton.click();
                            System.out.println("✓ Closed modal using close button");
                        } catch (Exception e1) {
                            try {
                                // Method 2: Press ESC key
                                modal.sendKeys(Keys.ESCAPE);
                                System.out.println("✓ Closed modal using ESC key");
                            } catch (Exception e2) {
                                try {
                                    // Method 3: Click outside modal (on overlay)
                                    ((JavascriptExecutor) driver).executeScript("document.body.click();");
                                    System.out.println("✓ Closed modal by clicking outside");
                                } catch (Exception e3) {
                                    System.out.println("Could not close modal, continuing...");
                                }
                            }
                        }
                        
                        // Wait for modal to disappear
                        wait.until(ExpectedConditions.invisibilityOf(modal));
                        
                    } catch (Exception modalException) {
                        System.out.println("No modal appeared or different pop-up mechanism for " + vehicleName + ": " + modalException.getMessage());
                    }
                    
                    Thread.sleep(2000); // Pause between clicks
                    
                } catch (Exception vehicleException) {
                    System.out.println("Could not interact with vehicle " + (i+1) + " image: " + vehicleException.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error in handleVehicleImagePopups: " + e.getMessage());
        }
    }

    // Task 2: Crawl multiple pages from the same website
    private static void crawlMultiplePages(WebDriver driver, WebDriverWait wait, List<String[]> allScrapedData) {
        String[] pagesToCrawl = {
            "https://www.enterprise.ca/en/car-rental/locations/canada.html",
            "https://www.enterprise.ca/en/rental-cars/ca/cars.html",
            "https://www.enterprise.ca/en/reserve/receipts.html"
        };
        
        String[] pageDescriptions = {
            "Canada Locations Page",
            "Debit Cards Information Page",
            "Help & Support Page"
        };
        
        for (int i = 0; i < pagesToCrawl.length; i++) {
            try {
                System.out.println("Crawling page " + (i+1) + ": " + pagesToCrawl[i]);
                driver.get(pagesToCrawl[i]);
                
                // Handle cookie banner on each page
                try {
                    WebElement closeCookieButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'CLOSE')]")));
                    closeCookieButton.click();
                    System.out.println("Cookie banner closed on " + pageDescriptions[i]);
                } catch (Exception e) {
                    System.out.println("No cookie banner found on " + pageDescriptions[i]);
                }
                
                // Handle any alerts
                handleAlert(driver);
                
                // Wait for page to load completely
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                
                String pageTitle = driver.getTitle();
                System.out.println("Page title: " + pageTitle);
                
                // Extract main heading
                try {
                    WebElement mainHeading = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
                    String headingText = mainHeading.getText();
                    allScrapedData.add(new String[]{pageTitle.replace(",", ""), pageDescriptions[i] + " - Main Heading", headingText.replace(",", ""), pagesToCrawl[i]});
                    System.out.println("Main heading: " + headingText);
                } catch (Exception e) {
                    System.out.println("Could not find main heading on " + pageDescriptions[i]);
                    allScrapedData.add(new String[]{pageTitle.replace(",", ""), pageDescriptions[i] + " - Main Heading", "No heading found", pagesToCrawl[i]});
                }
                
                // Page-specific data extraction
                if (i == 0) { // Canada Locations Page
                    extractCanadaLocationData(driver, wait, allScrapedData, pageTitle);
                } else if (i == 1) { // Debit Cards Page
                    extractDebitCardInfo(driver, wait, allScrapedData, pageTitle);
                } else if (i == 2) { // Help Page
                    extractHelpPageInfo(driver, wait, allScrapedData, pageTitle);
                }
                
                Thread.sleep(3000); // Pause between pages
                
            } catch (Exception e) {
                System.out.println("Error crawling page " + (i+1) + ": " + e.getMessage());
                allScrapedData.add(new String[]{"Error", pageDescriptions[i], "Failed to load page", e.getMessage()});
            }
        }
    }
    
    private static void extractCanadaLocationData(WebDriver driver, WebDriverWait wait, List<String[]> allScrapedData, String pageTitle) {
        try {
            // Look for location links or city names
            List<WebElement> locationLinks = driver.findElements(By.xpath("//a[contains(@href, '/locations/canada/')]"));
            int locationCount = Math.min(5, locationLinks.size()); // Limit to first 5
            
            for (int i = 0; i < locationCount; i++) {
                try {
                    WebElement link = locationLinks.get(i);
                    String locationName = link.getText().trim();
                    String locationUrl = link.getAttribute("href");
                    
                    if (!locationName.isEmpty() && !locationName.contains("View Details")) {
                        allScrapedData.add(new String[]{pageTitle.replace(",", ""), "Canada Location " + (i+1), locationName.replace(",", ""), locationUrl});
                        System.out.println("Found location: " + locationName);
                    }
                } catch (Exception e) {
                    System.out.println("Error extracting location " + (i+1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Error extracting Canada location data: " + e.getMessage());
        }
    }
    
    private static void extractDebitCardInfo(WebDriver driver, WebDriverWait wait, List<String[]> allScrapedData, String pageTitle) {
        try {
            // Look for information sections or bullet points
            List<WebElement> infoSections = driver.findElements(By.xpath("//p | //li"));
            int infoCount = 0;
            
            for (WebElement section : infoSections) {
                try {
                    String text = section.getText().trim();
                    if (!text.isEmpty() && text.length() > 20 && text.length() < 200 && infoCount < 3) {
                        allScrapedData.add(new String[]{pageTitle.replace(",", ""), "Debit Card Info " + (infoCount+1), text.replace(",", ""), ""});
                        System.out.println("Found info: " + text.substring(0, Math.min(50, text.length())) + "...");
                        infoCount++;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e) {
            System.out.println("Error extracting debit card info: " + e.getMessage());
        }
    }
    
    private static void extractHelpPageInfo(WebDriver driver, WebDriverWait wait, List<String[]> allScrapedData, String pageTitle) {
        try {
            // Look for FAQ sections or help topics
            List<WebElement> helpTopics = driver.findElements(By.xpath("//h2 | //h3"));
            int topicCount = Math.min(5, helpTopics.size());
            
            for (int i = 0; i < topicCount; i++) {
                try {
                    WebElement topic = helpTopics.get(i);
                    String topicText = topic.getText().trim();
                    
                    if (!topicText.isEmpty()) {
                        allScrapedData.add(new String[]{pageTitle.replace(",", ""), "Help Topic " + (i+1), topicText.replace(",", ""), ""});
                        System.out.println("Found help topic: " + topicText);
                    }
                } catch (Exception e) {
                    System.out.println("Error extracting help topic " + (i+1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Error extracting help page info: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");

        ChromeOptions options = new ChromeOptions();
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
                // Wait for vehicle results to load using explicit wait
                WebDriverWait vehicleWait = new WebDriverWait(driver, Duration.ofSeconds(15));
                vehicleWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//li[contains(@class, 'vehicle-list__item')]")));
                
                // Look for vehicle cards using the actual class structure
                List<WebElement> vehicleElements = driver.findElements(By.xpath("//li[contains(@class, 'vehicle-list__item')]"));
                if (!vehicleElements.isEmpty()) {
                    System.out.println("Found " + vehicleElements.size() + " vehicle options");
                    
                    // Task 3: Demonstrate advanced Selenium - handle vehicle image pop-ups
                    System.out.println("\n=== TASK 3: Demonstrating Advanced Selenium Commands ===");
                    handleVehicleImagePopups(driver, wait, vehicleElements, allScrapedData, page1Title);
                    
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
                            
                            // Only add vehicle data to CSV if we have valid information
                            if (!vehicleName.equals("N/A") && !vehicleName.isEmpty() && 
                                !priceAmount.equals("N/A") && !priceAmount.isEmpty() && 
                                !priceAmount.trim().equals("")) {
                                
                                String vehicleInfo = vehicleCode + " - " + vehicleName + " (" + vehicleDescription + ")";
                                String vehicleDetails = "Price: " + priceAmount + " | " + transmission + " | " + passengers + " | " + bags;
                                allScrapedData.add(new String[]{page1Title.replace(",", ""), "Vehicle Option " + (i+1), vehicleInfo.replace(",", ""), vehicleDetails.replace(",", "")});
                                
                                System.out.println("Extracted vehicle " + (i+1) + ": " + vehicleCode + " - " + vehicleName + " - " + priceAmount);
                            } else {
                                System.out.println("Skipping vehicle " + (i+1) + " due to missing data: " + vehicleName + " | " + priceAmount);
                            }
                            
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

            // Task 2: Crawl multiple pages from the same website
            System.out.println("\n=== TASK 2: Crawling Multiple Pages ===");
            crawlMultiplePages(driver, wait, allScrapedData);

            // Save all scraped data to a CSV file
            try (FileWriter csvWriter = new FileWriter("hertz_vehicles.csv")) {
                for (String[] rowData : allScrapedData) {
                    csvWriter.append(String.join(",", rowData));
                    csvWriter.append("\n");
                }
                System.out.println("All data from multiple pages saved to hertz_vehicles.csv");
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

