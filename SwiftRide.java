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

public class SwiftRide {

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
                    
                    // Find the clickable vehicle image or "View Details" button
                    WebElement imageButton = null;
                    try {
                        // Try to find the "View Details" button first
                        imageButton = vehicle.findElement(By.xpath(".//button[contains(.//span, 'View Details')]"));
                    } catch (Exception e) {
                        try {
                            // Try to find the vehicle image
                            imageButton = vehicle.findElement(By.xpath(".//img"));
                        } catch (Exception e2) {
                            // Try to click on the entire card
                            imageButton = vehicle;
                        }
                    }
                    
                    // Get vehicle name for logging using SwiftRide specific selector
                    String vehicleName = "Unknown";
                    try {
                        vehicleName = vehicle.findElement(By.xpath(".//h3[contains(@class, 'text-[#57E667]')]")).getText();
                    } catch (Exception e) {
                        try {
                            vehicleName = vehicle.findElement(By.xpath(".//h3 | .//h2 | .//h1")).getText();
                        } catch (Exception e2) {
                            vehicleName = "Vehicle " + (i + 1);
                        }
                    }
                    
                    System.out.println("Clicking on element for: " + vehicleName);
                    
                    // Advanced Selenium: Scroll element into view before clicking
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", imageButton);
                    Thread.sleep(1000); // Small pause for smooth scrolling
                    
                    // Click the element to open pop-up or navigate
                    imageButton.click();
                    
                    // Advanced Selenium: Wait for modal/pop-up to appear or page change
                    try {
                        // Check if a modal appeared
                        WebElement modal = wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//div[contains(@class, 'modal') or contains(@class, 'dialog') or contains(@class, 'overlay') or contains(@class, 'popup') or contains(@class, 'lightbox')]")
                        ));
                        System.out.println("✓ Pop-up opened successfully for " + vehicleName);
                        
                        // Extract any additional information from the modal
                        try {
                            String modalContent = modal.getText();
                            if (!modalContent.trim().isEmpty()) {
                                allScrapedData.add(new String[]{pageTitle, "Vehicle Modal " + (i+1), vehicleName + " - Modal Content", modalContent.replace(",", "").substring(0, Math.min(100, modalContent.length())) + "..."});
                            }
                        } catch (Exception e) {
                            System.out.println("Could not extract modal content: " + e.getMessage());
                        }
                        
                        // Advanced Selenium: Close the modal using various methods
                        try {
                            // Method 1: Look for close button
                            WebElement closeButton = driver.findElement(By.xpath("//button[contains(@class, 'close') or contains(@aria-label, 'close') or contains(@aria-label, 'Close') or contains(text(), '×') or contains(text(), 'Close')]"));
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
                        // Check if we navigated to a new page instead
                        String currentUrl = driver.getCurrentUrl();
                        if (!currentUrl.contains("/cars")) {
                            System.out.println("Navigated to new page: " + currentUrl);
                            allScrapedData.add(new String[]{pageTitle, "Vehicle Details " + (i+1), vehicleName + " - Details Page", currentUrl});
                            
                            // Navigate back to the main cars page
                            driver.navigate().back();
                            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                            System.out.println("✓ Navigated back to main cars page");
                        } else {
                            System.out.println("No modal appeared or different interaction for " + vehicleName + ": " + modalException.getMessage());
                        }
                    }
                    
                    Thread.sleep(2000); // Pause between clicks
                    
                } catch (Exception vehicleException) {
                    System.out.println("Could not interact with vehicle " + (i+1) + ": " + vehicleException.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error in handleVehicleImagePopups: " + e.getMessage());
        }
    }

    // Task 2: Crawl multiple pages from the same website
    private static void crawlMultiplePages(WebDriver driver, WebDriverWait wait, List<String[]> multiPageData) {
        String[] pagesToCrawl = {
            "https://swiftride.net/",
            "https://swiftride.net/how-it-works",
            "https://swiftride.net/contact-us"
        };
        
        String[] pageDescriptions = {
            "SwiftRide Home Page",
            "How It Works Page",
            "Contact Us Page"
        };
        
        for (int i = 0; i < pagesToCrawl.length; i++) {
            try {
                System.out.println("Crawling page " + (i+1) + ": " + pagesToCrawl[i]);
                driver.get(pagesToCrawl[i]);
                
                // Wait for page to load completely
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                
                String pageTitle = driver.getTitle();
                System.out.println("Page title: " + pageTitle);
                
                // Extract main heading
                try {
                    WebElement mainHeading = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//h1 | //h2[1] | //div[contains(@class, 'hero')] | //div[contains(@class, 'title')]")));
                    String headingText = mainHeading.getText();
                    multiPageData.add(new String[]{pageTitle.replace(",", ""), pageDescriptions[i] + " - Main Heading", headingText.replace(",", ""), pagesToCrawl[i]});
                    System.out.println("Main heading: " + headingText);
                } catch (Exception e) {
                    System.out.println("Could not find main heading on " + pageDescriptions[i]);
                    multiPageData.add(new String[]{pageTitle.replace(",", ""), pageDescriptions[i] + " - Main Heading", "No heading found", pagesToCrawl[i]});
                }
                
                // Page-specific data extraction
                if (i == 0) { // Home Page
                    extractHomePageData(driver, wait, multiPageData, pageTitle);
                } else if (i == 1) { // About Page
                    extractAboutPageData(driver, wait, multiPageData, pageTitle);
                } else if (i == 2) { // Contact Page
                    extractContactPageData(driver, wait, multiPageData, pageTitle);
                }
                
                Thread.sleep(3000); // Pause between pages
                
            } catch (Exception e) {
                System.out.println("Error crawling page " + (i+1) + ": " + e.getMessage());
                multiPageData.add(new String[]{"Error", pageDescriptions[i], "Failed to load page", e.getMessage()});
            }
        }
    }
    
    private static void extractHomePageData(WebDriver driver, WebDriverWait wait, List<String[]> multiPageData, String pageTitle) {
        try {
            // Look for featured cars or promotional content
            List<WebElement> featuredItems = driver.findElements(By.xpath("//div[contains(@class, 'featured')] | //div[contains(@class, 'promo')] | //div[contains(@class, 'hero')] | //section"));
            int itemCount = Math.min(3, featuredItems.size());
            
            for (int i = 0; i < itemCount; i++) {
                try {
                    WebElement item = featuredItems.get(i);
                    String itemText = item.getText().trim();
                    
                    if (!itemText.isEmpty() && itemText.length() > 10) {
                        String shortText = itemText.length() > 100 ? itemText.substring(0, 100) + "..." : itemText;
                        multiPageData.add(new String[]{pageTitle.replace(",", ""), "Home Featured " + (i+1), shortText.replace(",", ""), ""});
                        System.out.println("Found home feature: " + shortText);
                    }
                } catch (Exception e) {
                    System.out.println("Error extracting home item " + (i+1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Error extracting home page data: " + e.getMessage());
        }
    }
    
    private static void extractAboutPageData(WebDriver driver, WebDriverWait wait, List<String[]> multiPageData, String pageTitle) {
        try {
            // Look for about sections or company information
            List<WebElement> aboutSections = driver.findElements(By.xpath("//p | //div[contains(@class, 'about')] | //div[contains(@class, 'content')] | //section"));
            int sectionCount = 0;
            
            for (WebElement section : aboutSections) {
                try {
                    String text = section.getText().trim();
                    if (!text.isEmpty() && text.length() > 30 && text.length() < 300 && sectionCount < 3) {
                        multiPageData.add(new String[]{pageTitle.replace(",", ""), "About Section " + (sectionCount+1), text.replace(",", ""), ""});
                        System.out.println("Found about info: " + text.substring(0, Math.min(50, text.length())) + "...");
                        sectionCount++;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e) {
            System.out.println("Error extracting about page data: " + e.getMessage());
        }
    }
    
    private static void extractContactPageData(WebDriver driver, WebDriverWait wait, List<String[]> multiPageData, String pageTitle) {
        try {
            // Look for contact information
            List<WebElement> contactInfo = driver.findElements(By.xpath("//div[contains(@class, 'contact')] | //address | //p[contains(text(), 'Phone') or contains(text(), 'Email') or contains(text(), 'Address')]"));
            int contactCount = Math.min(5, contactInfo.size());
            
            for (int i = 0; i < contactCount; i++) {
                try {
                    WebElement contact = contactInfo.get(i);
                    String contactText = contact.getText().trim();
                    
                    if (!contactText.isEmpty()) {
                        multiPageData.add(new String[]{pageTitle.replace(",", ""), "Contact Info " + (i+1), contactText.replace(",", ""), ""});
                        System.out.println("Found contact info: " + contactText);
                    }
                } catch (Exception e) {
                    System.out.println("Error extracting contact info " + (i+1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Error extracting contact page data: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        List<String[]> allScrapedData = new ArrayList<>();
        allScrapedData.add(new String[]{"Page Title", "Section", "Vehicle/Content Info", "Details"});

        try {
            // --- Main SwiftRide Cars Page ---
            System.out.println("=== TASK 1: SwiftRide Car Rental Scraping ===");
            driver.get("https://swiftride.net/cars");
            driver.manage().window().maximize();


            // // Handle any potential alerts after initial page load
            // handleAlert(driver);

            // Wait for page to fully load
            Thread.sleep(2000);

            // Task 1: Use the search functionality to search for "honda"
            try {
                System.out.println("Searching for 'honda' vehicles...");
                WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//input[@placeholder='Search by make, model, or year...']")));
                searchInput.clear();
                searchInput.sendKeys("honda");
                Thread.sleep(2000); // Wait for search results to load
                System.out.println("Search completed for 'honda'");
            } catch (Exception e) {
                System.out.println("Could not perform search: " + e.getMessage());
            }

            // Get page title and heading
            String mainPageTitle = driver.getTitle();
            System.out.println("Page title: " + mainPageTitle);
            
            try {
                WebElement mainHeading = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h1 | //h2[1] | //div[contains(@class, 'hero')] | //div[contains(@class, 'title')]")));
                String headingText = mainHeading.getText();
                allScrapedData.add(new String[]{mainPageTitle.replace(",", ""), "Main Page Heading", headingText.replace(",", ""), ""});
                System.out.println("Main heading: " + headingText);
            } catch (Exception e) {
                System.out.println("Could not find main heading: " + e.getMessage());
                allScrapedData.add(new String[]{mainPageTitle.replace(",", ""), "SwiftRide Cars Page", "Car Rental Service", ""});
            }

            // Extract vehicle information
            try {
                // Wait for vehicle results to load - try multiple selectors
                WebDriverWait vehicleWait = new WebDriverWait(driver, Duration.ofSeconds(20));
                
                List<WebElement> vehicleElements = new ArrayList<>();
                
                // Use the exact selector from the SwiftRide HTML structure
                String[] vehicleSelectors = {
                    "//div[contains(@class, 'rounded-lg border text-card-foreground shadow-sm flex flex-col w-full cursor-pointer')]",
                    "//div[@role='button' and @tabindex='0']",
                    "//div[contains(@class, 'car-item')] | //div[contains(@class, 'vehicle-item')]",
                    "//div[contains(@class, 'car-card')] | //div[contains(@class, 'vehicle-card')]", 
                    "//div[contains(@class, 'car')] | //div[contains(@class, 'vehicle')]"
                };
                
                for (String selector : vehicleSelectors) {
                    try {
                        vehicleWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector)));
                        vehicleElements = driver.findElements(By.xpath(selector));
                        if (!vehicleElements.isEmpty()) {
                            System.out.println("Found " + vehicleElements.size() + " vehicles using selector: " + selector);
                            break;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
                
                if (vehicleElements.isEmpty()) {
                    // Try generic content extraction if no specific vehicle elements found
                    vehicleElements = driver.findElements(By.xpath("//div[contains(@class, 'content')] | //section | //article"));
                    System.out.println("Using generic content extraction. Found " + vehicleElements.size() + " content sections.");
                }
                
                if (!vehicleElements.isEmpty()) {
                    // Task 3: Demonstrate advanced Selenium - handle vehicle interactions
                    System.out.println("\n=== TASK 3: Demonstrating Advanced Selenium Commands ===");
                    handleVehicleImagePopups(driver, wait, vehicleElements, allScrapedData, mainPageTitle);
                    
                    // Extract vehicle/content information
                    int maxVehicles = Math.min(10, vehicleElements.size()); // Limit to first 10
                    for (int i = 0; i < maxVehicles; i++) {
                        WebElement element = vehicleElements.get(i);
                        try {
                            String vehicleName = "N/A";
                            String vehicleDescription = "N/A";
                            String priceInfo = "N/A";
                            String additionalInfo = "";
                            
                            // Extract vehicle name using SwiftRide specific selector
                            try {
                                WebElement nameElement = element.findElement(By.xpath(".//h3[contains(@class, 'text-[#57E667]')]"));
                                vehicleName = nameElement.getText().trim();
                            } catch (Exception e) {
                                try {
                                    // Alternative selector for vehicle name
                                    WebElement altNameElement = element.findElement(By.xpath(".//h3 | .//h2 | .//h1"));
                                    vehicleName = altNameElement.getText().trim();
                                } catch (Exception e2) {
                                    vehicleName = "Vehicle " + (i + 1);
                                }
                            }
                            
                            // Extract year information
                            String yearInfo = "N/A";
                            try {
                                WebElement yearElement = element.findElement(By.xpath(".//p[contains(@class, 'text-gray-400')]"));
                                yearInfo = yearElement.getText().trim();
                            } catch (Exception e) {
                                yearInfo = "Year not specified";
                            }
                            
                            // Extract price information using SwiftRide specific selector
                            try {
                                WebElement priceElement = element.findElement(By.xpath(".//span[contains(@class, 'text-3xl font-bold text-white')]"));
                                String weeklyRate = element.findElement(By.xpath(".//span[contains(@class, 'text-sm text-gray-400 font-medium')]")).getText();
                                priceInfo = priceElement.getText().trim() + weeklyRate;
                            } catch (Exception e) {
                                priceInfo = "Price not displayed";
                            }
                            
                            // Extract vehicle basics (fuel type, transmission, etc.)
                            try {
                                List<WebElement> basics = element.findElements(By.xpath(".//div[contains(@class, 'inline-flex items-center text-xs bg-gray-800')]"));
                                StringBuilder basicsBuilder = new StringBuilder();
                                for (WebElement basic : basics) {
                                    String basicText = basic.getText().trim();
                                    if (!basicText.isEmpty()) {
                                        if (basicsBuilder.length() > 0) basicsBuilder.append(" | ");
                                        basicsBuilder.append(basicText);
                                    }
                                }
                                additionalInfo = basicsBuilder.toString();
                            } catch (Exception e) {
                                additionalInfo = "No additional info";
                            }
                            
                            // Extract features
                            try {
                                WebElement featuresElement = element.findElement(By.xpath(".//p[contains(@class, 'text-xs text-gray-400 leading-relaxed')]"));
                                String features = featuresElement.getText().trim();
                                if (!features.isEmpty()) {
                                    vehicleDescription = features;
                                } else {
                                    vehicleDescription = yearInfo;
                                }
                            } catch (Exception e) {
                                vehicleDescription = yearInfo;
                            }
                            
                            // Only add to CSV if we have meaningful data
                            if (!vehicleName.equals("N/A") && !vehicleName.isEmpty() && vehicleName.length() > 1) {
                                String contentInfo = vehicleName + (vehicleDescription.length() > 10 ? " - " + vehicleDescription : "");
                                String contentDetails = "Price: " + priceInfo + (additionalInfo.length() > 0 ? " | " + additionalInfo : "");
                                
                                allScrapedData.add(new String[]{
                                    mainPageTitle.replace(",", ""), 
                                    "Car/Content " + (i+1), 
                                    contentInfo.replace(",", "").substring(0, Math.min(200, contentInfo.length())), 
                                    contentDetails.replace(",", "").substring(0, Math.min(200, contentDetails.length()))
                                });
                                
                                System.out.println("Extracted item " + (i+1) + ": " + vehicleName + " - " + priceInfo);
                            }
                            
                        } catch (Exception e) {
                            System.out.println("Could not extract details for item " + (i+1) + ": " + e.getMessage());
                        }
                    }
                } else {
                    System.out.println("No vehicle or content elements found on page");
                    // Add a fallback entry
                    allScrapedData.add(new String[]{mainPageTitle.replace(",", ""), "Page Content", "SwiftRide cars page loaded", "No specific vehicle data found"});
                }
                
            } catch (Exception e) {
                System.out.println("Error extracting vehicle information: " + e.getMessage());
                allScrapedData.add(new String[]{mainPageTitle.replace(",", ""), "Error", "Failed to extract vehicles", e.getMessage()});
            }

            // Save all scraped data to a CSV file
            try (FileWriter csvWriter = new FileWriter("swiftride_data.csv")) {
                for (String[] rowData : allScrapedData) {
                    csvWriter.append(String.join(",", rowData));
                    csvWriter.append("\n");
                }
                System.out.println("All data from SwiftRide saved to swiftride_data.csv");
            } catch (IOException e) {
                System.err.println("Error writing to CSV file: " + e.getMessage());
            }

            // Task 2: Crawl multiple pages from the same website
            System.out.println("\n=== TASK 2: Crawling Multiple Pages ===");
            List<String[]> multiPageData = new ArrayList<>();
            multiPageData.add(new String[]{"Page Title", "Section", "Content Info", "Details"});
            
            crawlMultiplePages(driver, wait, multiPageData);
            
            // Save multi-page data to a separate CSV file
            try (FileWriter csvWriter = new FileWriter("swiftride_multipage_data.csv")) {
                for (String[] rowData : multiPageData) {
                    csvWriter.append(String.join(",", rowData));
                    csvWriter.append("\n");
                }
                System.out.println("Multi-page crawling data saved to swiftride_multipage_data.csv");
            } catch (IOException e) {
                System.err.println("Error writing multi-page CSV file: " + e.getMessage());
            }

        } catch (Exception mainException) {
            System.err.println("Main execution error: " + mainException.getMessage());
            mainException.printStackTrace();
        }
        
        finally {
            // driver.quit(); // Commented out to keep Chrome open for debugging
            System.out.println("Chrome browser left open for debugging. Close manually when done.");
        }
    }

}