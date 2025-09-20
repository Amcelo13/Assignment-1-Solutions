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

    // Task 3: Advanced Selenium - Only popup handling and specific element waiting
    private static void handlePopUps(WebDriver driver, WebDriverWait wait, List<WebElement> vehicleElements, 
                                                List<String[]> allScrapedData, String pageTitle) {
        try {
            // Advanced Selenium Demonstration 1: Click specific popup button to open
            try {
                System.out.println("1. Opening popup using specific XPath selector...");
                WebElement openPopupButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@id=\"cw-bubble-holder\"]/button[1]")
                ));
                
                // Advanced Selenium: Scroll to element before clicking
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", openPopupButton);
                Thread.sleep(1000);
                
                openPopupButton.click();
                System.out.println("✓ Successfully clicked popup open button");
                
                // Add data about popup interaction
                allScrapedData.add(new String[]{pageTitle, "Task 3 - Popup Demo", "Popup Open Button Clicked", "XPath: //*[@id=\"cw-bubble-holder\"]/button[1]"});
                
                Thread.sleep(2000); // Wait for popup to fully appear
                
            } catch (Exception e) {
                System.out.println("Could not find or click popup open button: " + e.getMessage());
            }
            
            // Advanced Selenium Demonstration 2: Wait for specific element (where Honda search results will appear)
            try {
                System.out.println("2. Waiting for target element (where Honda search results will appear)...");
                WebElement targetElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("/html/body/div[2]/div/main/div/section/div[2]")
                ));
                
                System.out.println("✓ Successfully found and waited for target element");
                
                // Extract some information from the waited element
                String elementText = "";
                try {
                    elementText = targetElement.getText().trim();
                    if (elementText.length() > 100) {
                        elementText = elementText.substring(0, 100) + "...";
                    }
                } catch (Exception e) {
                    elementText = "Element found but no text content";
                }
                
                allScrapedData.add(new String[]{pageTitle, "Task 3 - Element Wait", "Target Element Found", "XPath: /html/body/div[2]/div/main/div/section/div[2] | Content: " + elementText.replace(",", "")});
                
            } catch (Exception e) {
                System.out.println("Could not find target element for waiting demonstration: " + e.getMessage());
                allScrapedData.add(new String[]{pageTitle, "Task 3 - Element Wait", "Target Element Not Found", "XPath: /html/body/div[2]/div/main/div/section/div[2] | Error: " + e.getMessage()});
            }
            
            // Advanced Selenium Demonstration 3: Close popup using specific button
            try {
                System.out.println("3. Closing popup using specific XPath selector...");
                WebElement closePopupButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@id=\"cw-bubble-holder\"]/button[2]")
                ));
                
                closePopupButton.click();
                System.out.println("✓ Successfully clicked popup close button");
                
                allScrapedData.add(new String[]{pageTitle, "Task 3 - Popup Demo", "Popup Close Button Clicked", "XPath: //*[@id=\"cw-bubble-holder\"]/button[2]"});
                
                Thread.sleep(1000); // Wait for popup to close
                
            } catch (Exception e) {
                System.out.println("Could not find or click popup close button: " + e.getMessage());
            }
            
            System.out.println("=== Task 3 Advanced Selenium Demonstrations Completed ===");
            
        } catch (Exception e) {
            System.out.println("Error in handlePopUps: " + e.getMessage());
            allScrapedData.add(new String[]{pageTitle, "Task 3 - Error", "Advanced Selenium Demo Failed", e.getMessage()});
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
                } else if (i == 1) { // How It Works Page
                    extractHowItWorksPageData(driver, wait, multiPageData, pageTitle);
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
            // Look for the main hero heading "Drive Your Dreams Into Reality"
            try {
                WebElement heroText = driver.findElement(By.xpath("//h1[contains(text(), 'Drive Your Dreams')]"));
                String heroContent = heroText.getText().trim();
                multiPageData.add(new String[]{pageTitle.replace(",", ""), "Hero Text", heroContent.replace(",", ""), ""});
                System.out.println("Found hero text: " + heroContent);
            } catch (Exception e) {
                System.out.println("Could not find hero text: " + e.getMessage());
            }
            
            // Look for the subtitle text about car subscription platform
            try {
                WebElement subtitle = driver.findElement(By.xpath("//p[contains(text(), 'car subscription platform')]"));
                String subtitleText = subtitle.getText().trim();
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
    
    private static void extractHowItWorksPageData(WebDriver driver, WebDriverWait wait, List<String[]> multiPageData, String pageTitle) {
        try {
            // Look for the main heading
            try {
                WebElement mainHero = driver.findElement(By.xpath("//h1[contains(text(), 'couple')]"));
                String heroText = mainHero.getText().trim();
                multiPageData.add(new String[]{pageTitle.replace(",", ""), "Hero Section", heroText.replace(",", ""), ""});
                System.out.println("Found hero text: " + heroText);
            } catch (Exception e) {
                System.out.println("Could not find hero section: " + e.getMessage());
            }
            
            // Extract step information (limit to 2 steps for demonstration)
            List<WebElement> steps = driver.findElements(By.xpath("//h3[contains(text(), 'Apply') or contains(text(), 'Sign Agreement')]"));
            int stepCount = Math.min(2, steps.size());
            
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
    
    private static void extractContactPageData(WebDriver driver, WebDriverWait wait, List<String[]> multiPageData, String pageTitle) {
        try {
            // Look for the main heading "How Can We Help?"
            try {
                WebElement mainHeading = driver.findElement(By.xpath("//h1[contains(text(), 'How Can We')]"));
                String headingText = mainHeading.getText().trim();
                multiPageData.add(new String[]{pageTitle.replace(",", ""), "Main Heading", headingText.replace(",", ""), ""});
                System.out.println("Found main heading: " + headingText);
            } catch (Exception e) {
                System.out.println("Could not find main heading: " + e.getMessage());
            }
            
            // Look for email information
            try {
                WebElement emailLink = driver.findElement(By.xpath("//a[contains(@href, 'mailto:hello@swiftride.net')]"));
                String emailText = emailLink.getText().trim();
                multiPageData.add(new String[]{pageTitle.replace(",", ""), "Contact Email", emailText.replace(",", ""), ""});
                System.out.println("Found email: " + emailText);
            } catch (Exception e) {
                System.out.println("Could not find email: " + e.getMessage());
            }
            
            // Look for support section titles (limit to 2 for demonstration)
            try {
                List<WebElement> supportTitles = driver.findElements(By.xpath("//h3[contains(text(), 'Email Support') or contains(text(), 'Live Chat')]"));
                int titleCount = Math.min(2, supportTitles.size());
                
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

            // Task 3: Advanced Selenium FIRST - popup handling and element waiting (before Honda search)
            System.out.println("\n=== TASK 3: Demonstrating Advanced Selenium - Popup & Element Wait ===");
            List<WebElement> dummyVehicleList = new ArrayList<>(); // Empty list for the method signature
            List<String[]> dummyDataList = new ArrayList<>(); // Empty list - we don't want to save Task 3 data
            handlePopUps(driver, wait, dummyVehicleList, dummyDataList, driver.getTitle());

            // Task 1: Use the search functionality to search for "honda"
            try {
                System.out.println("Searching for 'honda' vehicles...");
                WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//input[@placeholder='Search by make, model, or year...']")));
                searchInput.clear();
                searchInput.sendKeys("honda");
                searchInput.sendKeys(Keys.ENTER);
                Thread.sleep(1000); // Wait for search results to load
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
                    // Extract vehicle/content information (Task 3 already completed above)
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

            // Save all scraped data to a CSV file (clean vehicle data only)
            try (FileWriter csvWriter = new FileWriter("swiftride_data.csv")) {
                for (String[] rowData : allScrapedData) {
                    csvWriter.append(String.join(",", rowData));
                    csvWriter.append("\n");
                }
                System.out.println("All vehicle data from SwiftRide saved to swiftride_data.csv");
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