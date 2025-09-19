// Selenium 4.x required (place all Selenium JARs in libs/ and compile with -cp "libs/*")
// Run: java -cp ".:libs/*" HertzScraper [optional_results_url]
// Note: This script demonstrates Tasks 1–3 with robust waits, interactions, multi-page, and CSV export.

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class HertzScraper {

    // Base domain; if Hertz changes domain redirects, pass a results URL as arg 0
    private static final String BASE = "https://www.hertz.ca";
    // Change to true for headless; leave false while developing
    private static final boolean HEADLESS = false;

    private WebDriver driver;
    private WebDriverWait wait;

    public static void main(String[] args) throws Exception {
        HertzScraper app = new HertzScraper();
        try {
            app.startDriver();
            String startUrl = args.length > 0  ? args[0] : BASE + "/rentacar/reservation/";
            
            app.open(startUrl);

            // Try to accept cookies if present (pop-up banner)
            app.acceptCookiesIfPresent();

            // If on the reservation page, attempt a basic search interaction (best effort)
            if (startUrl.contains("/reservation")) {
                app.trySimpleSearchFlow();
            }

            // Now we should be on results; wait for vehicle cards and interact
            List<Vehicle> all = app.scrapeAllPages();

            // Write CSV
            app.writeCsv("hertz_vehicles.csv", all);

            System.out.println("Scraped " + all.size() + " vehicle rows -> hertz_vehicles.csv");
        } finally {
            app.quit();
        }
    }

    private void startDriver() {
        ChromeOptions options = new ChromeOptions();
        if (HEADLESS) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--start-maximized");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1400,900");

        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        // Note: Do not mix implicit and explicit waits; we rely on explicit waits only here.
    }

    private void open(String url) {
        driver.get(url);
    }

    private void acceptCookiesIfPresent() {
        // Handle multiple cookie banner types
        List<By> cookieButtons = Arrays.asList(
                // Your specific cookie dialog
                By.cssSelector("a.cc-btn.cc-allow"),
                By.cssSelector("a[aria-label='allow cookies']"),
                // OneTrust cookie banner (original)
                By.id("onetrust-accept-btn-handler"),
                By.cssSelector("#onetrust-accept-btn-handler"),
                By.cssSelector("button#truste-consent-button"),
                By.cssSelector("button[aria-label='Accept All']"),
                By.xpath("//button[contains(.,'Accept')]"),
                // Generic accept patterns
                By.xpath("//a[contains(.,'Accept Cookies')]"),
                By.xpath("//a[contains(@aria-label,'allow cookies')]")
        );
        for (By b : cookieButtons) {
            try {
                WebElement el = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(b));
                el.click();
                // Once accepted, break
                break;
            } catch (TimeoutException | org.openqa.selenium.NoSuchElementException | ElementClickInterceptedException ignored) {
            }
        }
    }

    private void trySimpleSearchFlow() {
        // Fill out the Hertz reservation form with specific values
        try {
            // Fill pickup location - Toronto Pearson International Airport
            WebElement pickupInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("pickup-location")));
            pickupInput.clear();
            pickupInput.sendKeys("Toronto Pearson International Airport");
            sleep(1000); // Wait for autocomplete

            // Click on pickup date to open calendar
            WebElement pickupDateBox = wait.until(ExpectedConditions.elementToBeClickable(By.id("pickup-date-box")));
            pickupDateBox.click();
            sleep(1000);

            // Select October 10, 2025 from calendar
            try {
                WebElement calendar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[3]")));
                WebElement oct10 = calendar.findElement(By.xpath(".//td[text()='10' and not(contains(@class, 'empty'))]"));
                oct10.click();
                sleep(500);
            } catch (Exception e) {
                System.out.println("Calendar interaction failed, skipping date selection");
            }

            // Set pickup time to 1:00 PM (13:00)
            WebElement pickupTimeSelect = driver.findElement(By.cssSelector("select[name='pickupTime']"));
            Select pickupTime = new Select(pickupTimeSelect);
            pickupTime.selectByValue("13:00");

            // Click on dropoff date to open calendar
            WebElement dropoffDateBox = wait.until(ExpectedConditions.elementToBeClickable(By.id("dropoff-date-box")));
            dropoffDateBox.click();
            sleep(1000);

            // Select October 11, 2025 from calendar
            try {
                WebElement calendar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[3]")));
                WebElement oct11 = calendar.findElement(By.xpath(".//td[text()='11' and not(contains(@class, 'empty'))]"));
                oct11.click();
                sleep(500);
            } catch (Exception e) {
                System.out.println("Calendar interaction failed for dropoff date");
            }

            // Set dropoff time to 1:00 PM (13:00)
            WebElement dropoffTimeSelect = driver.findElement(By.cssSelector("select[name='dropoffTime']"));
            Select dropoffTime = new Select(dropoffTimeSelect);
            dropoffTime.selectByValue("13:00");

            // Click "View Vehicles" button
            WebElement viewVehiclesBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.res-submit")));
            viewVehiclesBtn.click();
            
            System.out.println("Form filled and submitted successfully");
            sleep(3000); // Wait for page navigation

        } catch (Exception e) {
            System.out.println("Form interaction failed: " + e.getMessage());
            // If navigation didn't happen, do nothing; we still proceed to scrape if the results are already present
        }
    }

    private void clickFirstVisible(List<By> locators) {
        for (By b : locators) {
            try {
                WebElement el = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(b));
                el.click();
                // minor pause
                sleep(300);
                return;
            } catch (TimeoutException | org.openqa.selenium.NoSuchElementException | ElementClickInterceptedException ignored) {
            }
        }
    }

    private List<Vehicle> scrapeAllPages() {
        List<Vehicle> results = new ArrayList<>();

        // Wait for at least one vehicle card to be present
        List<By> cardRoots = Arrays.asList(
                By.cssSelector("div.vehicle.vehCardRD"),
                By.cssSelector("div.gtm-vehicle"),
                By.cssSelector("article.dual")
        );

        boolean anyFound = waitForAny(cardRoots, 20);
        if (!anyFound) {
            System.out.println("No vehicle cards found on page.");
            return results;
        }

        // Pagination / load-more loop
        Set<String> seenKeys = new HashSet<>();
        while (true) {
            // Ensure any modal is closed before scraping to avoid obscuring elements
            closeVehicleModalIfOpen();

            // Collect card roots currently present
            List<WebElement> cards = collectElements(cardRoots);

            // Extract vehicles
            for (WebElement card : cards) {
                Vehicle v = extractVehicle(card);
                if (v != null) {
                    String key = v.sipp + "|" + v.title + "|" + v.model + "|" + v.payNowPrice + "|" + v.payLaterPrice;
                    if (seenKeys.add(key)) {
                        results.add(v);
                    }
                }
            }

            // Try to load more: look for “Show more”, “Load more”, “Next” buttons
            if (!clickLoadMoreIfAny()) {
                break;
            }

            // Wait for new content to appear
            sleep(1200);
            waitShortForMoreCards(cardRoots);
        }

        return results;
    }

    private boolean waitForAny(List<By> locators, int seconds) {
        long end = System.currentTimeMillis() + seconds * 1000L;
        while (System.currentTimeMillis() < end) {
            for (By b : locators) {
                if (!driver.findElements(b).isEmpty()) {
                    return true;
                }
            }
            sleep(250);
        }
        return false;
    }

    private void waitShortForMoreCards(List<By> cardRoots) {
        long end = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < end) {
            int count = 0;
            for (By b : cardRoots) {
                count += driver.findElements(b).size();
            }
            if (count > 0) return;
            sleep(200);
        }
    }

    private List<WebElement> collectElements(List<By> locators) {
        List<WebElement> out = new ArrayList<>();
        for (By b : locators) {
            out.addAll(driver.findElements(b));
        }
        return out;
    }

    private Vehicle extractVehicle(WebElement card) {
        try {
            // Attempt multiple selectors per field, using first non-empty text
            String sipp = attrOrEmpty(card, "data-sipp");
            String title = firstText(card,
                    By.cssSelector("h2.gtm-vehicle-title"),
                    By.cssSelector(".vehicle-type"),
                    By.cssSelector("h1"),
                    By.cssSelector(".gtm-vehicle-header h2")
            );

            String model = firstText(card,
                    By.cssSelector(".gtm-vehicle-type"),
                    By.xpath(".//h1")
            );

            String passengers = firstText(card,
                    By.cssSelector(".gtm-vehFeature-passengers .gtm-vehFeatureDesc")
            );
            if (passengers.isEmpty()) {
                passengers = firstMatchingLiText(card, "Passengers");
            }

            String luggage = firstText(card,
                    By.cssSelector(".gtm-vehFeature-suitcases .gtm-vehFeatureDesc")
            );
            if (luggage.isEmpty()) {
                luggage = firstMatchingLiText(card, "Suitcase");
            }

            String transmission = firstText(card,
                    By.cssSelector(".gtm-vehFeature-transmission .gtm-vehFeatureTooltip p"),
                    By.cssSelector(".gtm-vehFeature-transmission .gtm-vehFeatureDesc")
            );
            if (transmission.isEmpty()) {
                transmission = firstMatchingLiText(card, "Transmission");
            }

            String fuel = firstText(card,
                    By.cssSelector(".gtm-vehFeature-fuel .gtm-vehFeatureDesc"),
                    By.cssSelector(".gtm-vehFeature-fuel .gtm-vehFeatureTooltip p")
            );
            if (fuel.isEmpty()) {
                fuel = firstMatchingLiText(card, "l/100km");
            }

            String imgSrc = firstAttr(card,
                    By.cssSelector(".gtm-vehicle-img img"),
                    "src"
            );
            if (imgSrc.isEmpty()) {
                imgSrc = firstAttr(card, By.cssSelector("img.car-info"), "src");
            }

            String payNowPrice = firstText(card,
                    By.cssSelector(".gtm-price-cont.gtm-paynow .gtm-price")
            ).replaceAll("[^0-9.,]", "").trim();

            String payLaterPrice = firstText(card,
                    By.cssSelector(".gtm-price-cont.gtm-paylater .gtm-price")
            ).replaceAll("[^0-9.,]", "").trim();

            String currency = firstText(card,
                    By.cssSelector(".gtm-price-cont.gtm-paynow .gtm-price-currency-per"),
                    By.cssSelector(".gtm-price-cont.gtm-paylater .gtm-price-currency-per")
            );

            // Some pages use protocol-relative image URLs; prefix if needed
            if (imgSrc.startsWith("//")) {
                imgSrc = "https:" + imgSrc;
            }

            return new Vehicle(sipp, title, model, passengers, luggage, transmission, fuel,
                    payNowPrice, payLaterPrice, currency, imgSrc);
        } catch (StaleElementReferenceException e) {
            return null;
        }
    }

    private String firstText(WebElement root, By... locators) {
        for (By b : locators) {
            List<WebElement> els = root.findElements(b);
            for (WebElement el : els) {
                try {
                    String t = el.getText();
                    if (t != null && !t.trim().isEmpty()) return t.trim();
                } catch (StaleElementReferenceException ignored) {}
            }
        }
        return "";
    }

    private String firstAttr(WebElement root, By locator, String name) {
        List<WebElement> els = root.findElements(locator);
        for (WebElement el : els) {
            String v = el.getAttribute(name);
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return "";
    }

    private String attrOrEmpty(WebElement el, String name) {
        String v = el.getAttribute(name);
        return v == null ? "" : v.trim();
    }

    private String firstMatchingLiText(WebElement card, String contains) {
        List<WebElement> lis = card.findElements(By.cssSelector("li"));
        for (WebElement li : lis) {
            try {
                String t = li.getText();
                if (t != null && t.toLowerCase().contains(contains.toLowerCase())) {
                    return t.trim();
                }
            } catch (StaleElementReferenceException ignored) {}
        }
        return "";
    }

    private boolean clickLoadMoreIfAny() {
        // Try common “load more”/pagination buttons
        List<By> buttons = Arrays.asList(
                By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'show more')]"),
                By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'load more')]"),
                By.xpath("//a[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'next')]"),
                By.cssSelector("button[aria-label*='Show']")
        );
        for (By b : buttons) {
            List<WebElement> els = driver.findElements(b);
            for (WebElement el : els) {
                try {
                    scrollIntoView(el);
                    new WebDriverWait(driver, Duration.ofSeconds(5))
                            .until(ExpectedConditions.elementToBeClickable(el));
                    el.click();
                    return true;
                } catch (TimeoutException | ElementClickInterceptedException ignored) {
                }
            }
        }
        return false;
    }

    private void closeVehicleModalIfOpen() {
        // If modal foreground is present and not hidden, click close
        List<WebElement> modals = driver.findElements(By.cssSelector(".gtm-vehicleModal-foreground"));
        for (WebElement m : modals) {
            String cls = m.getAttribute("class");
            if (cls != null && !cls.contains("gtm-hide")) {
                List<WebElement> closes = driver.findElements(By.cssSelector(".gtm-modal-close img"));
                if (!closes.isEmpty()) {
                    try {
                        closes.get(0).click();
                        sleep(200);
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    private void scrollIntoView(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }

    private void writeCsv(String fileName, List<Vehicle> list) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(String.join(",",
                "sipp",
                "title",
                "model",
                "passengers",
                "luggage",
                "transmission",
                "fuel",
                "pay_now_price",
                "pay_later_price",
                "currency",
                "image_url"
        ));
        for (Vehicle v : list) {
            lines.add(csv(v.sipp) + "," +
                    csv(v.title) + "," +
                    csv(v.model) + "," +
                    csv(v.passengers) + "," +
                    csv(v.luggage) + "," +
                    csv(v.transmission) + "," +
                    csv(v.fuel) + "," +
                    csv(v.payNowPrice) + "," +
                    csv(v.payLaterPrice) + "," +
                    csv(v.currency) + "," +
                    csv(v.imageUrl));
        }
        Files.write(Paths.get(fileName), lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private String csv(String s) {
        if (s == null) return "";
        String t = s.replace("\"", "\"\"");
        if (t.contains(",") || t.contains("\"") || t.contains("\n")) {
            return "\"" + t + "\"";
        }
        return t;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { }
    }

    private void quit() {
        if (driver != null) {
            driver.quit();
        }
    }

    static class Vehicle {
        String sipp, title, model, passengers, luggage, transmission, fuel;
        String payNowPrice, payLaterPrice, currency, imageUrl;

        Vehicle(String sipp, String title, String model, String passengers, String luggage,
                String transmission, String fuel, String payNowPrice, String payLaterPrice,
                String currency, String imageUrl) {
            this.sipp = n(sipp);
            this.title = n(title);
            this.model = n(model);
            this.passengers = n(passengers);
            this.luggage = n(luggage);
            this.transmission = n(transmission);
            this.fuel = n(fuel);
            this.payNowPrice = n(payNowPrice);
            this.payLaterPrice = n(payLaterPrice);
            this.currency = n(currency);
            this.imageUrl = n(imageUrl);
        }
        private static String n(String s) { return s == null ? "" : s.trim(); }
    }
}
