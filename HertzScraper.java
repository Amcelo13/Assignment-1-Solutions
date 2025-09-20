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

    // Task 3: Advanced Selenium - Handle vehicle image pop-ups/modals (MOVED TO END)
    private static void handleVehicleImagePopups(WebDriver driver, WebDriverWait wait, List<String[]> allScrapedData, String pageTitle) {
        try {
            System.out.println("\n=== TASK 3: Demonstrating Advanced Selenium Commands ===");
            System.out.println("Navigating back to vehicle selection page for advanced Selenium demonstrations...");
            
            // Navigate to the vehicle page for Task 3
            driver.get("https://www.enterprise.ca/en/car-rental.html");
            
            // Handle cookie banner
            try {
                WebElement closeCookieButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'CLOSE')]")));
                closeCookieButton.click();
                System.out.println("Cookie banner closed for Task 3.");
            } catch (Exception e) {
                System.out.println("No cookie banner found for Task 3.");
            }

            // Fill out the form again to get to vehicle page
            try {
                WebElement locationInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("pickupLocationTextBox")));
                locationInput.clear();
                locationInput.sendKeys("Pearson International");
                Thread.sleep(2000);
                
                try {
                    WebElement pearsonOption = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//li[@id='location-1019226']//button[@data-location-type='airports'] | " +
                                "//li[contains(@class, 'location-group__item')]//span[contains(text(), 'Toronto Pearson International Airport')]/..")
                    ));
                    pearsonOption.click();
                    System.out.println("Selected airport for Task 3");
                } catch (Exception e) {
                    System.out.println("Using manual entry for Task 3");
                }

                WebElement browseVehiclesBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("continueButton")));
                browseVehiclesBtn.click();
                Thread.sleep(3000);
                
            } catch (Exception e) {
                System.out.println("Error setting up form for Task 3: " + e.getMessage());
                return;
            }

            // Advanced Selenium Task 3: Demonstrate advanced interactions
            try {
                WebDriverWait vehicleWait = new WebDriverWait(driver, Duration.ofSeconds(15));
                vehicleWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//li[contains(@class, 'vehicle-list__item')]")));
                
                List<WebElement> vehicleElements = driver.findElements(By.xpath("//li[contains(@class, 'vehicle-list__item')]"));
                
                if (!vehicleElements.isEmpty()) {
                    System.out.println("=== Advanced Selenium Demonstrations ===");
                    
                    // Advanced Task 3.1: JavaScript Execution
                    System.out.println("\n--- Advanced Task 3.1: JavaScript Execution ---");
                    try {
                        JavascriptExecutor js = (JavascriptExecutor) driver;
                        
                        // Get page info using JavaScript
                        String pageInfo = (String) js.executeScript("return 'Page Title: ' + document.title + ', URL: ' + window.location.href + ', Vehicle Count: ' + document.querySelectorAll('li[class*=\"vehicle-list__item\"]').length;");
                        System.out.println("JavaScript execution result: " + pageInfo);
                        allScrapedData.add(new String[]{pageTitle, "Advanced JS Execution", "Page Information", pageInfo.replace(",", "")});
                        
                        // Scroll to bottom using JavaScript
                        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                        Thread.sleep(1000);
                        System.out.println("✓ Scrolled to bottom using JavaScript");
                        
                        // Scroll back to top
                        js.executeScript("window.scrollTo(0, 0);");
                        Thread.sleep(1000);
                        System.out.println("✓ Scrolled to top using JavaScript");
                        
                    } catch (Exception e) {
                        System.out.println("Error in JavaScript execution: " + e.getMessage());
                    }
                    
                    // Advanced Task 3.2: Advanced Element Interactions
                    System.out.println("\n--- Advanced Task 3.2: Advanced Element Interactions ---");
                    int vehiclesToTest = Math.min(3, vehicleElements.size());
                    
                    for (int i = 0; i < vehiclesToTest; i++) {
                        try {
                            WebElement vehicle = vehicleElements.get(i);
                            String vehicleName = "Vehicle " + (i + 1);
                            
                            try {
                                vehicleName = vehicle.findElement(By.xpath(".//h2[@class='mb-0']")).getText();
                            } catch (Exception e) {
                                vehicleName = "Vehicle " + (i + 1);
                            }
                            
                            System.out.println("Testing advanced interactions for: " + vehicleName);
                            
                            // Advanced Selenium: Scroll element into view
                            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", vehicle);
                            Thread.sleep(1000);
                            System.out.println("✓ Smooth scrolled to vehicle using JavaScript");
                            
                            // Advanced Selenium: Highlight element
                            ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid red';", vehicle);
                            Thread.sleep(1000);
                            System.out.println("✓ Highlighted vehicle element with red border");
                            
                            // Advanced Selenium: Get element properties
                            String elementInfo = (String) ((JavascriptExecutor) driver).executeScript(
                                "var rect = arguments[0].getBoundingClientRect(); " +
                                "return 'Position: (' + Math.round(rect.left) + ',' + Math.round(rect.top) + '), Size: ' + Math.round(rect.width) + 'x' + Math.round(rect.height);", 
                                vehicle);
                            System.out.println("✓ Element properties: " + elementInfo);
                            allScrapedData.add(new String[]{pageTitle, "Advanced Element Info " + (i+1), vehicleName + " Properties", elementInfo});
                            
                            // Remove highlight
                            ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='';", vehicle);
                            
                            // Advanced Selenium: Try to find and interact with clickable image
                            try {
                                WebElement imageButton = vehicle.findElement(By.xpath(".//button[contains(@class, 'car-item__vehicle-image--clickable')] | .//img | .//div[contains(@class, 'image')]"));
                                
                                // Highlight the clickable element
                                ((JavascriptExecutor) driver).executeScript("arguments[0].style.outline='3px solid blue';", imageButton);
                                System.out.println("✓ Found and highlighted clickable image element");
                                
                                // Advanced click using JavaScript
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", imageButton);
                                System.out.println("✓ Clicked using JavaScript execution");
                                
                                // Wait for potential modal/popup
                                try {
                                    WebElement modal = wait.until(ExpectedConditions.presenceOfElementLocated(
                                        By.xpath("//div[contains(@class, 'modal') or contains(@class, 'dialog') or contains(@class, 'overlay') or contains(@class, 'popup') or contains(@role, 'dialog')]")
                                    ));
                                    System.out.println("✓ Modal/popup detected for " + vehicleName);
                                    
                                    // Advanced modal handling
                                    String modalText = modal.getText();
                                    if (!modalText.trim().isEmpty()) {
                                        allScrapedData.add(new String[]{pageTitle, "Advanced Modal Content " + (i+1), vehicleName + " Modal", modalText.replace(",", "").substring(0, Math.min(100, modalText.length())) + "..."});
                                    }
                                    
                                    // Advanced modal closing techniques
                                    try {
                                        // Method 1: Find close button
                                        WebElement closeBtn = modal.findElement(By.xpath(".//button[contains(@class, 'close') or contains(@aria-label, 'close')] | .//*[contains(text(), '×')] | .//*[contains(text(), 'Close')]"));
                                        closeBtn.click();
                                        System.out.println("✓ Closed modal using close button");
                                    } catch (Exception e1) {
                                        try {
                                            // Method 2: ESC key
                                            modal.sendKeys(Keys.ESCAPE);
                                            System.out.println("✓ Closed modal using ESC key");
                                        } catch (Exception e2) {
                                            try {
                                                // Method 3: Click outside modal
                                                ((JavascriptExecutor) driver).executeScript("document.body.click();");
                                                System.out.println("✓ Closed modal by clicking outside");
                                            } catch (Exception e3) {
                                                System.out.println("Modal close methods attempted");
                                            }
                                        }
                                    }
                                    
                                    // Wait for modal to disappear
                                    wait.until(ExpectedConditions.invisibilityOf(modal));
                                    
                                } catch (Exception modalException) {
                                    System.out.println("No modal appeared or different interaction mechanism");
                                }
                                
                                // Remove highlight
                                ((JavascriptExecutor) driver).executeScript("arguments[0].style.outline='';", imageButton);
                                
                            } catch (Exception imageException) {
                                System.out.println("No clickable image found for " + vehicleName + ": " + imageException.getMessage());
                            }
                            
                            Thread.sleep(2000); // Pause between vehicles
                            
                        } catch (Exception vehicleException) {
                            System.out.println("Error with advanced interactions for vehicle " + (i+1) + ": " + vehicleException.getMessage());
                        }
                    }
                    
                    // Advanced Task 3.3: Advanced Navigation and Window Handling
                    System.out.println("\n--- Advanced Task 3.3: Advanced Navigation Techniques ---");
                    try {
                        // Get current URL
                        String currentUrl = driver.getCurrentUrl();
                        System.out.println("Current URL: " + currentUrl);
                        
                        // Advanced navigation: Refresh page
                        driver.navigate().refresh();
                        Thread.sleep(2000);
                        System.out.println("✓ Page refreshed using navigate().refresh()");
                        
                        // Advanced navigation: Back and forward
                        driver.navigate().back();
                        Thread.sleep(1000);
                        System.out.println("✓ Navigated back");
                        
                        driver.navigate().forward();
                        Thread.sleep(1000);
                        System.out.println("✓ Navigated forward");
                        
                        // Advanced: Get page source length
                        int pageSourceLength = driver.getPageSource().length();
                        System.out.println("✓ Page source length: " + pageSourceLength + " characters");
                        allScrapedData.add(new String[]{pageTitle, "Advanced Navigation", "Page Source Analysis", "Length: " + pageSourceLength + " chars"});
                        
                        // Advanced: Window size manipulation
                        org.openqa.selenium.Dimension originalSize = driver.manage().window().getSize();
                        System.out.println("✓ Original window size: " + originalSize.width + "x" + originalSize.height);
                        
                        // Resize window
                        driver.manage().window().setSize(new org.openqa.selenium.Dimension(1200, 800));
                        Thread.sleep(1000);
                        System.out.println("✓ Resized window to 1200x800");
                        
                        // Restore original size
                        driver.manage().window().setSize(originalSize);
                        Thread.sleep(1000);
                        System.out.println("✓ Restored original window size");
                        
                    } catch (Exception navException) {
                        System.out.println("Error in advanced navigation: " + navException.getMessage());
                    }
                    
                    System.out.println("\n=== Task 3 Advanced Selenium Demonstrations Completed ===");
                    
                } else {
                    System.out.println("No vehicles found for advanced Selenium demonstrations");
                }
                
            } catch (Exception e) {
                System.out.println("Error in advanced Selenium demonstrations: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("Error in handleVehicleImagePopups (Task 3): " + e.getMessage());
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
            // =============================================
            // TASK 1: Form Filling and Vehicle Extraction
            // =============================================
            System.out.println("=== TASK 1: Form Filling and Vehicle Extraction ===");
            
            driver.get("https://www.enterprise.ca/en/car-rental.html");
            driver.manage().window().maximize();

            // Handle cookie banner
            try {
                WebElement closeCookieButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'CLOSE')]")));
                closeCookieButton.click();
                System.out.println("Cookie banner closed.");
            } catch (Exception e) {
                System.out.println("Cookie banner not found or not clickable on main page.");
            }

            // Handle any potential alerts
            handleAlert(driver);

            // Fill out the booking form
            try {
                WebElement locationInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("pickupLocationTextBox")));
                locationInput.clear();
                locationInput.sendKeys("Pearson International");
                Thread.sleep(2000);
                
                try {
                    WebElement pearsonOption = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//li[@id='location-1019226']//button[@data-location-type='airports'] | " +
                                "//li[contains(@class, 'location-group__item')]//span[contains(text(), 'Toronto Pearson International Airport')]/..")
                    ));
                    pearsonOption.click();
                    System.out.println("Selected Toronto Pearson International Airport (YYZ)");
                } catch (Exception e) {
                    System.out.println("Autocomplete suggestion not found, continuing with manual entry");
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
                    Thread.sleep(3000);
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

            // Extract vehicle information
            try {
                WebDriverWait vehicleWait = new WebDriverWait(driver, Duration.ofSeconds(15));
                vehicleWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//li[contains(@class, 'vehicle-list__item')]")));
                
                List<WebElement> vehicleElements = driver.findElements(By.xpath("//li[contains(@class, 'vehicle-list__item')]"));
                if (!vehicleElements.isEmpty()) {
                    System.out.println("Found " + vehicleElements.size() + " vehicle options");
                    
                    for (int i = 0; i < vehicleElements.size(); i++) {
                        WebElement vehicle = vehicleElements.get(i);
                        try {
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
                                String symbol = vehicle.findElement(By.xpath(".//span[@class='rs-price-tag__symbol']")).getText();
                                String unit = vehicle.findElement(By.xpath(".//span[@class='rs-price-tag__unit']")).getText();
                                String fraction = vehicle.findElement(By.xpath(".//span[@class='rs-price-tag__fraction']")).getText();
                                priceAmount = symbol + " " + unit + fraction;
                            } catch (Exception e) {
                                try {
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

            // =============================================
            // TASK 2: Crawling Multiple Pages
            // =============================================
            System.out.println("\n=== TASK 2: Crawling Multiple Pages ===");
            crawlMultiplePages(driver, wait, allScrapedData);

            // Save data after Task 1 and 2
            try (FileWriter csvWriter = new FileWriter("hertz_vehicles_tasks_1_and_2.csv")) {
                for (String[] rowData : allScrapedData) {
                    csvWriter.append(String.join(",", rowData));
                    csvWriter.append("\n");
                }
                System.out.println("Data from Tasks 1 and 2 saved to hertz_vehicles_tasks_1_and_2.csv");
            } catch (IOException e) {
                System.err.println("Error writing to CSV file: " + e.getMessage());
            }

            // =============================================
            // TASK 3: Advanced Selenium (AT THE END)
            // =============================================
            handleVehicleImagePopups(driver, wait, allScrapedData, page1Title);

            // Save final data including Task 3
            try (FileWriter csvWriter = new FileWriter("hertz_vehicles_complete.csv")) {
                for (String[] rowData : allScrapedData) {
                    csvWriter.append(String.join(",", rowData));
                    csvWriter.append("\n");
                }
                System.out.println("Complete data from all 3 tasks saved to hertz_vehicles_complete.csv");
            } catch (IOException e) {
                System.err.println("Error writing to final CSV file: " + e.getMessage());
            }

        } finally {
            // driver.quit(); // Commented out to keep Chrome open for debugging
            System.out.println("Chrome browser left open for debugging. Close manually when done.");
        }
    }
}