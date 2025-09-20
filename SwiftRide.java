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
                Thread.sleep(2000); // Wait for search results to load
                System.out.println("Search completed for 'honda'");
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
                WebDriverWait vehicleWait = new WebDriverWait(driver, Duration.ofSeconds(20));
                
                List<WebElement> vehicleElements = new ArrayList<>();
                
                // Define multiple XPath strategies to locate vehicle containers reliably
                String[] vehicleSelectors = {
                    "//div[contains(@class, 'rounded-lg border text-card-foreground shadow-sm flex flex-col w-full cursor-pointer')]", // Primary SwiftRide layout
                    "//div[@role='button' and @tabindex='0']", // Interactive elements
                    "//div[contains(@class, 'car-item')] | //div[contains(@class, 'vehicle-item')]", // Generic car containers
                    "//div[contains(@class, 'car-card')] | //div[contains(@class, 'vehicle-card')]", // Card-based layouts
                    "//div[contains(@class, 'car')] | //div[contains(@class, 'vehicle')]" // Broad car-related divs
                };
                
                // Try each selector strategy until vehicle elements are found
                for (String selector : vehicleSelectors) {
                    try {
                        vehicleWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector)));
                        vehicleElements = driver.findElements(By.xpath(selector));
                        if (!vehicleElements.isEmpty()) {
                            System.out.println("Found " + vehicleElements.size() + " vehicles using selector: " + selector);
                            break; // Stop trying once successful
                        }
                    } catch (Exception e) {
                        continue; // Try next selector if current one fails
                    }
                }
                
                // Fallback to generic content if specific vehicle selectors fail
                if (vehicleElements.isEmpty()) {
                    vehicleElements = driver.findElements(By.xpath("//div[contains(@class, 'content')] | //section | //article"));
                    System.out.println("Using generic content extraction. Found " + vehicleElements.size() + " content sections.");
                }
                
                if (!vehicleElements.isEmpty()) {
                    // Task 3: Demonstrate advanced Selenium - handle vehicle interactions
                    System.out.println("\n=== TASK 3: Demonstrating Advanced Selenium Commands ===");
                    handleVehicleImagePopups(driver, wait, vehicleElements, allScrapedData, mainPageTitle);
                    
                    // Process each discovered vehicle element to extract detailed information
                    int maxVehicles = Math.min(10, vehicleElements.size()); // Limit processing to avoid overwhelming data
                    for (int i = 0; i < maxVehicles; i++) {
                        WebElement element = vehicleElements.get(i);
                        try {
                            // Initialize data collection variables with default values
                            String vehicleName = "N/A";
                            String vehicleDescription = "N/A";
                            String priceInfo = "N/A";
                            String additionalInfo = "";
                            
                            // Priority 1: Extract vehicle brand and model name
                            try {
                                WebElement nameElement = element.findElement(By.xpath(".//h3[contains(@class, 'text-[#57E667]')]"));
                                vehicleName = nameElement.getText().trim();
                            } catch (Exception e) {
                                try {
                                    // Fallback: Use any heading element for vehicle name
                                    WebElement altNameElement = element.findElement(By.xpath(".//h3 | .//h2 | .//h1"));
                                    vehicleName = altNameElement.getText().trim();
                                } catch (Exception e2) {
                                    vehicleName = "Vehicle " + (i + 1); // Generic name if nothing found
                                }
                            }
                            
                            // Priority 2: Capture vehicle year and basic specs
                            String yearInfo = "N/A";
                            try {
                                WebElement yearElement = element.findElement(By.xpath(".//p[contains(@class, 'text-gray-400')]"));
                                yearInfo = yearElement.getText().trim();
                            } catch (Exception e) {
                                yearInfo = "Year not specified";
                            }
                            
                            // Priority 3: Extract rental pricing information
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