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

    /**
     * Method to automatically handle browser alert dialogs
     * Waits for an alert to appear and accepts it if found
     */
    private static void manageWebPageAlerts(WebDriver driver) {
        try {
            // Create a wait instance to pause execution until alert appears
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            
            // Display the alert message for debugging purposes
            System.out.println("Alert text: " + alert.getText());
            alert.accept(); // Click OK button on the alert
            System.out.println("Alert accepted.");
        } catch (NoAlertPresentException e) {
            // No problem if no alert exists
            System.out.println("No alert present.");
        } catch (Exception e) {
            // Catch any other issues with alert handling
            System.out.println("Error handling alert: " + e.getMessage());
        }
    }

    /**
     * Task 3: Advanced Selenium - Handle vehicle image pop-ups/modals
     * Shows how to interact with modal dialogs and wait for specific elements
     */
 private static void handleVehicleImagePopups(WebDriver driver, WebDriverWait wait, String pageTitle) {
    try {
                // --- OPEN POPUP ---
                WebElement popupOpener = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='cw-bubble-holder']/button[1]")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", popupOpener);
                Thread.sleep(1000);
                popupOpener.click();

                // --- WAIT FOR POPUP CONTENT ---
                Thread.sleep(1000);

                // --- CLOSE POPUP ---
                try {
                    WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='cw-bubble-holder']/button[2]")));
                    closeButton.click();
                    // Ensure popup disappears
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("cw-bubble-holder")));
                } catch (Exception closeEx) {
                }
          

    } catch (Exception e) {
        System.out.println("❌ Error in handleVehicleImagePopups: " + e.getMessage());
    }
}

    /**
     * Multi-page website navigation and data extraction
     * Systematically visits different sections of the same domain
     */
    private static void performMultiPageDataCollection(WebDriver driver, WebDriverWait wait, List<String[]> multiPageData) {
        // Define the sequence of pages to visit within the SwiftRide website
        String[] pagesToCrawl = {
            "https://swiftride.net/",
            "https://swiftride.net/how-it-works",
            "https://swiftride.net/contact-us"
        };
        
        // Human-readable labels for each page being processed
        String[] pageDescriptions = {
            "SwiftRide Home Page",
            "How It Works Page",
            "Contact Us Page"
        };
        
        // Visit each page in sequence and extract relevant information
        for (int i = 0; i < pagesToCrawl.length; i++) {
            try {
                System.out.println("Crawling page " + (i+1) + ": " + pagesToCrawl[i]);
                driver.get(pagesToCrawl[i]); // Navigate to the specific URL
                
                // Ensure page content has fully loaded before proceeding
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                
                String pageTitle = driver.getTitle(); // Get the browser tab title
                System.out.println("Page title: " + pageTitle);
                
                // Locate and extract the primary heading element from the page
                try {
                    // Use multiple XPath selectors to find the main heading reliably
                    WebElement mainHeading = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//h1 | //h2[1] | //div[contains(@class, 'hero')] | //div[contains(@class, 'title')]")));
                    String headingText = mainHeading.getText();
                    // Store the heading info, removing commas to avoid CSV formatting issues
                    multiPageData.add(new String[]{pageTitle.replace(",", ""), pageDescriptions[i] + " - Main Heading", headingText.replace(",", ""), pagesToCrawl[i]});
                    System.out.println("Main heading: " + headingText);
                } catch (Exception e) {
                    System.out.println("Could not find main heading on " + pageDescriptions[i]);
                    multiPageData.add(new String[]{pageTitle.replace(",", ""), pageDescriptions[i] + " - Main Heading", "No heading found", pagesToCrawl[i]});
                }
                
                // Route to specialized extraction methods based on page type
                if (i == 0) { // Process home page content
                    gatherHomepageInformation(driver, wait, multiPageData, pageTitle);
                } else if (i == 1) { // Process how-it-works page content
                    gatherProcessInformation(driver, wait, multiPageData, pageTitle);
                } else if (i == 2) { // Process contact page content
                    gatherContactInformation(driver, wait, multiPageData, pageTitle);
                }
                
                Thread.sleep(3000); // Allow brief pause between page visits to avoid overwhelming server
                
            } catch (Exception e) {
                System.out.println("Error crawling page " + (i+1) + ": " + e.getMessage());
                multiPageData.add(new String[]{"Error", pageDescriptions[i], "Failed to load page", e.getMessage()});
            }
        }
    }
    
    /**
     * Specialized data extraction for the website's main landing page
     * Focuses on hero content and key messaging elements
     */
    private static void gatherHomepageInformation(WebDriver driver, WebDriverWait wait, List<String[]> multiPageData, String pageTitle) {
        try {
            // Search for the prominent hero banner text that captures attention
            try {
                WebElement heroText = driver.findElement(By.xpath("//h1[contains(text(), 'Drive Your Dreams')]"));
                String heroContent = heroText.getText().trim();
                multiPageData.add(new String[]{pageTitle.replace(",", ""), "Hero Text", heroContent.replace(",", ""), ""});
                System.out.println("Found hero text: " + heroContent);
            } catch (Exception e) {
                System.out.println("Could not find hero text: " + e.getMessage());
            }
            
            // Extract the descriptive subtitle that explains the service offering
            try {
                WebElement subtitle = driver.findElement(By.xpath("//p[contains(text(), 'car subscription platform')]"));
                String subtitleText = subtitle.getText().trim();
                // Truncate lengthy descriptions to keep data manageable
                if (subtitleText.length() > 50) {
                    subtitleText = subtitleText.substring(0, 50) + "...";
                }
                multiPageData.add(new String[]{pageTitle.replace(",", ""), "Subtitle", subtitleText.replace(",", ""), ""});
                System.out.println("Found subtitle: " + subtitleText);
            } catch (Exception e) {
                System.out.println("Could not find subtitle: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("Error extracting home page data: " + e.getMessage());
        }
    }
    
    /**
     * Targeted content extraction for the process explanation page
     * Captures step-by-step instructions and workflow information
     */
    private static void gatherProcessInformation(WebDriver driver, WebDriverWait wait, List<String[]> multiPageData, String pageTitle) {
        try {
            // Locate the primary heading that introduces the process explanation
            try {
                WebElement mainHero = driver.findElement(By.xpath("//h1[contains(text(), 'couple')]"));
                String heroText = mainHero.getText().trim();
                multiPageData.add(new String[]{pageTitle.replace(",", ""), "Hero Section", heroText.replace(",", ""), ""});
                System.out.println("Found hero text: " + heroText);
            } catch (Exception e) {
                System.out.println("Could not find hero section: " + e.getMessage());
            }
            
            // Identify and collect the workflow step information (limiting to 2 for efficiency)
            List<WebElement> steps = driver.findElements(By.xpath("//h3[contains(text(), 'Apply') or contains(text(), 'Sign Agreement')]"));
            int stepCount = Math.min(2, steps.size()); // Restrict to first 2 steps to avoid overloading
            
            // Process each workflow step found on the page
            for (int i = 0; i < stepCount; i++) {
                try {
                    WebElement step = steps.get(i);
                    String stepTitle = step.getText().trim();
                    multiPageData.add(new String[]{pageTitle.replace(",", ""), "Process Step " + (i+1), stepTitle.replace(",", ""), ""});
                    System.out.println("Found step: " + stepTitle);
                } catch (Exception e) {
                    System.out.println("Error extracting step " + (i+1) + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error extracting how it works page data: " + e.getMessage());
        }
    }
    
    /**
     * Contact page specific data mining
     * Collects support channels and communication methods
     */
    private static void gatherContactInformation(WebDriver driver, WebDriverWait wait, List<String[]> multiPageData, String pageTitle) {
        try {
            // Search for the welcoming customer service heading
            try {
                WebElement mainHeading = driver.findElement(By.xpath("//h1[contains(text(), 'How Can We')]"));
                String headingText = mainHeading.getText().trim();
                multiPageData.add(new String[]{pageTitle.replace(",", ""), "Main Heading", headingText.replace(",", ""), ""});
                System.out.println("Found main heading: " + headingText);
            } catch (Exception e) {
                System.out.println("Could not find main heading: " + e.getMessage());
            }
            
            // Extract direct email contact information for customer inquiries
            try {
                WebElement emailLink = driver.findElement(By.xpath("//a[contains(@href, 'mailto:hello@swiftride.net')]"));
                String emailText = emailLink.getText().trim();
                multiPageData.add(new String[]{pageTitle.replace(",", ""), "Contact Email", emailText.replace(",", ""), ""});
                System.out.println("Found email: " + emailText);
            } catch (Exception e) {
                System.out.println("Could not find email: " + e.getMessage());
            }
            
            // Collect available customer support channels (limiting to 2 to keep data focused)
            try {
                List<WebElement> supportTitles = driver.findElements(By.xpath("//h3[contains(text(), 'Email Support') or contains(text(), 'Live Chat')]"));
                int titleCount = Math.min(2, supportTitles.size()); // Only process first 2 support options
                
                // Record each support method available to customers
                for (int i = 0; i < titleCount; i++) {
                    String titleText = supportTitles.get(i).getText().trim();
                    multiPageData.add(new String[]{pageTitle.replace(",", ""), "Support Option " + (i+1), titleText.replace(",", ""), ""});
                    System.out.println("Found support option: " + titleText);
                }
            } catch (Exception e) {
                System.out.println("Could not find support options: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("Error extracting contact page data: " + e.getMessage());
        }
    }

    /**
     * Main execution method - orchestrates all web scraping tasks
     * Coordinates browser setup, page navigation, and data collection
     */
    public static void main(String[] args) {
        // Configure Chrome WebDriver location for Selenium automation
        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");

        // Set up Chrome browser options to avoid detection and improve stability
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox"); // Bypass OS security restrictions
        options.addArguments("--disable-dev-shm-usage"); // Overcome limited resource problems
        options.addArguments("--disable-blink-features=AutomationControlled"); // Hide automation indicators
        options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"); // Mimic real browser

        // Initialize browser driver and wait handler for dynamic content
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Prepare data storage structure with CSV headers
        List<String[]> allScrapedData = new ArrayList<>();
        allScrapedData.add(new String[]{"Page Title", "Section", "Vehicle/Content Info", "Details"});

        try {
            // === PRIMARY TASK: Vehicle rental information extraction ===
            System.out.println("=== TASK 1: SwiftRide Car Rental Scraping ===");
            driver.get("https://swiftride.net/cars"); // Navigate to the cars catalog page
            driver.manage().window().maximize(); // Ensure full page visibility

            // Allow page scripts and dynamic content to fully initialize
            Thread.sleep(2000);



            // === SEARCH FUNCTIONALITY: Filter results by vehicle brand ===
            try {
                System.out.println("Searching for 'honda' vehicles...");
                // Locate the search input field and ensure it's ready for interaction
                WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//input[@placeholder='Search by make, model, or year...']")));
                searchInput.clear(); // Remove any existing text
                searchInput.sendKeys("honda"); // Type the search term
                Thread.sleep(1000);
                searchInput.sendKeys(Keys.ENTER); // Submit the search
                Thread.sleep(2000); // Wait for search results to load
            } catch (Exception e) {
                System.out.println("Could not perform search: " + e.getMessage());
            }

            // Extract basic page identification information
            String mainPageTitle = driver.getTitle();
            System.out.println("Page title: " + mainPageTitle);
            
            // Attempt to capture the main page heading for context
            try {
                WebElement mainHeading = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h1 | //h2[1] | //div[contains(@class, 'hero')] | //div[contains(@class, 'title')]")));
                String headingText = mainHeading.getText();
                allScrapedData.add(new String[]{mainPageTitle.replace(",", ""), "Main Page Heading", headingText.replace(",", ""), ""});
                System.out.println("Main heading: " + headingText);
            } catch (Exception e) {
                System.out.println("Could not find main heading: " + e.getMessage());
                // Provide fallback data if heading extraction fails
                allScrapedData.add(new String[]{mainPageTitle.replace(",", ""), "SwiftRide Cars Page", "Car Rental Service", ""});
            }



            // === VEHICLE DATA EXTRACTION: Systematic content mining ===
            try {
                // Create extended wait period for dynamic vehicle loading
                WebDriverWait vehicleWait = new WebDriverWait(driver, Duration.ofSeconds(10));
                
                // Wait for the search results grid to appear
                WebElement resultsGrid = vehicleWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//div[contains(@class, 'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 xl:grid-cols-3')]")));
                
                // Find all vehicle cards within the results grid
                List<WebElement> vehicleElements = resultsGrid.findElements(
                    By.xpath(".//div[contains(@class, 'rounded-lg border text-card-foreground shadow-sm flex flex-col w-full cursor-pointer')]"));
                
                System.out.println("Found " + vehicleElements.size() + " Honda vehicles from search results");
                
                if (!vehicleElements.isEmpty()) {
                    // Task 3: Demonstrate advanced Selenium - handle vehicle interactions
                    System.out.println("\n=== TASK 3: Demonstrating Advanced Selenium Commands ===");
                    handleVehicleImagePopups(driver, wait, mainPageTitle);
                    
                    // Process each discovered vehicle element to extract detailed information
                    int maxVehicles = Math.min(10, vehicleElements.size()); // Limit processing to avoid overwhelming data
                    for (int i = 0; i < maxVehicles; i++) {
                        WebElement vehicleCard = vehicleElements.get(i);
                        try {
                            // Initialize data collection variables with default values
                            String vehicleName = "N/A";
                            String vehicleYear = "N/A";
                            String priceInfo = "N/A";
                            String features = "N/A";
                            String basics = "N/A";
                            String location = "N/A";
                            String availability = "N/A";
                            
                            // Extract vehicle name (Honda model)
                            try {
                                WebElement nameElement = vehicleCard.findElement(By.xpath(".//h3[contains(@class, 'text-[#57E667]')]"));
                                vehicleName = nameElement.getText().trim();
                            } catch (Exception e) {
                                vehicleName = "Honda Vehicle " + (i + 1);
                            }
                            
                            // Extract vehicle year
                            try {
                                WebElement yearElement = vehicleCard.findElement(By.xpath(".//p[contains(@class, 'text-gray-400') and contains(@class, 'body-sm')]"));
                                vehicleYear = yearElement.getText().trim();
                            } catch (Exception e) {
                                vehicleYear = "Year not specified";
                            }
                            
                            // Extract price information
                            try {
                                WebElement priceElement = vehicleCard.findElement(By.xpath(".//span[contains(@class, 'text-3xl font-bold text-white')]"));
                                WebElement weeklyElement = vehicleCard.findElement(By.xpath(".//span[contains(@class, 'text-sm text-gray-400 font-medium')]"));
                                priceInfo = priceElement.getText().trim() + " " + weeklyElement.getText().trim();
                            } catch (Exception e) {
                                priceInfo = "Price not available";
                            }
                            
                            // Extract availability status
                            try {
                                WebElement availabilityElement = vehicleCard.findElement(By.xpath(".//span[contains(@class, 'bg-[#57E667]/20 text-[#57E667]')]"));
                                availability = availabilityElement.getText().trim();
                            } catch (Exception e) {
                                availability = "Status unknown";
                            }
                            
                            // Extract vehicle basics (fuel type, transmission, etc.)
                            try {
                                List<WebElement> basicElements = vehicleCard.findElements(By.xpath(".//div[contains(@class, 'inline-flex items-center text-xs bg-gray-800')]"));
                                StringBuilder basicsBuilder = new StringBuilder();
                                for (WebElement basic : basicElements) {
                                    String basicText = basic.getText().trim();
                                    if (!basicText.isEmpty()) {
                                        if (basicsBuilder.length() > 0) basicsBuilder.append(" | ");
                                        basicsBuilder.append(basicText);
                                    }
                                }
                                basics = basicsBuilder.toString().isEmpty() ? "Basic info not available" : basicsBuilder.toString();
                            } catch (Exception e) {
                                basics = "Basic info not available";
                            }
                            
                            // Extract features
                            try {
                                WebElement featuresElement = vehicleCard.findElement(By.xpath(".//p[contains(@class, 'text-xs text-gray-400 leading-relaxed')]"));
                                features = featuresElement.getText().trim();
                                if (features.isEmpty()) features = "Features not listed";
                            } catch (Exception e) {
                                features = "Features not listed";
                            }
                            
                            // Extract location information
                            try {
                                WebElement locationElement = vehicleCard.findElement(By.xpath(".//span[contains(@class, 'inline-flex items-center') and contains(text(), 'mi •')]"));
                                location = locationElement.getText().trim();
                            } catch (Exception e) {
                                location = "Location not specified";
                            }
                            
                            // Create comprehensive vehicle description (safely handle length)
                            String vehicleDescription = vehicleYear + " " + vehicleName + " - " + features + " - " + basics;
                            String vehicleDetails = "Price: " + priceInfo + " | Location: " + location + " | Status: " + availability;
                            
                            // Clean strings by removing commas and limiting length safely
                            String cleanDescription = vehicleDescription.replace(",", " ");
                            String cleanDetails = vehicleDetails.replace(",", " ");
                            
                            // Safely truncate strings if they're too long
                            if (cleanDescription.length() > 200) {
                                cleanDescription = cleanDescription.substring(0, 200) + "...";
                            }
                            if (cleanDetails.length() > 200) {
                                cleanDetails = cleanDetails.substring(0, 200) + "...";
                            }
                            
                            // Add to CSV data
                            allScrapedData.add(new String[]{
                                mainPageTitle.replace(",", ""), 
                                "Honda Vehicle " + (i+1), 
                                cleanDescription, 
                                cleanDetails
                            });
                            
                            System.out.println("Extracted Honda " + (i+1) + ": " + vehicleName + " (" + vehicleYear + ") - " + priceInfo);
                            
                        } catch (Exception e) {
                            System.out.println("Could not extract details for Honda vehicle " + (i+1) + ": " + e.getMessage());
                            // Add error entry with safe string handling
                            allScrapedData.add(new String[]{
                                mainPageTitle.replace(",", ""), 
                                "Honda Vehicle " + (i+1), 
                                "Data extraction failed", 
                                "Error: " + e.getMessage().replace(",", " ")
                            });
                        }
                    }
                } else {
                    System.out.println("No Honda vehicles found in search results");
                    allScrapedData.add(new String[]{mainPageTitle.replace(",", ""), "Search Results", "No Honda vehicles found", "Search may have returned no results"});
                }
                
            } catch (Exception e) {
                System.out.println("Error extracting Honda vehicle information: " + e.getMessage());
                allScrapedData.add(new String[]{mainPageTitle.replace(",", ""), "Error", "Failed to extract Honda vehicles", e.getMessage().replace(",", " ")});
            }

            // === DATA PERSISTENCE: Export collected vehicle information ===
            try (FileWriter csvWriter = new FileWriter("swiftride_data.csv")) {
                // Write each data row to the CSV file with proper formatting
                for (String[] rowData : allScrapedData) {
                    csvWriter.append(String.join(",", rowData)); // Convert array to comma-separated values
                    csvWriter.append("\n"); // Add newline for next record
                }
                System.out.println("All vehicle data from SwiftRide saved to swiftride_data.csv");
            } catch (IOException e) {
                System.err.println("Error writing to CSV file: " + e.getMessage());
            }

            // === SECONDARY TASK: Comprehensive website navigation ===
            System.out.println("\n=== TASK 2: Crawling Multiple Pages ===");
            List<String[]> multiPageData = new ArrayList<>();
            multiPageData.add(new String[]{"Page Title", "Section", "Content Info", "Details"});
            
            // Execute systematic exploration of different website sections
            performMultiPageDataCollection(driver, wait, multiPageData);
            
            // Store multi-page exploration results in dedicated CSV file
            try (FileWriter csvWriter = new FileWriter("swiftride_multipage_data.csv")) {
                for (String[] rowData : multiPageData) {
                    csvWriter.append(String.join(",", rowData)); // Convert array to CSV row
                    csvWriter.append("\n"); // Add line break for next row
                }
                System.out.println("Multi-page crawling data saved to swiftride_multipage_data.csv");
            } catch (IOException e) {
                System.err.println("Error writing multi-page CSV file: " + e.getMessage());
            }

        } catch (Exception mainException) {
            // Handle any unexpected errors during the scraping process
            System.err.println("Main execution error: " + mainException.getMessage());
            mainException.printStackTrace(); // Print full error trace for debugging
        }
        
        finally {
            // driver.quit(); // Commented out to keep Chrome open for debugging
            System.out.println("Chrome browser left open for debugging. Close manually when done.");
        }
    }

}