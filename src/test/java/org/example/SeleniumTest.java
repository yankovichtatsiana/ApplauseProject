package org.example;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SeleniumTest {

    WebDriver driver;
    public static Logger log = LogManager.getLogger();

    @BeforeClass
    public void setUp() {
        String driverPath = "C:\\Users\\Tatsiana\\IdeaProjects\\ApplauseProject\\driver\\chromedriver.exe";
        System.setProperty("webdriver.chrome.driver", driverPath);
    }

    @BeforeMethod
    public void openTestPage() {
        driver = new ChromeDriver();
        String url = "https://www.douglas.de/de";
        driver.get(url);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        driver.manage().window().maximize();
        assert driver.getCurrentUrl().equals(url);
        log.info("Test page is opened as expected.");
    }


    @Test
    @Parameters({"highlightsValue", "markeValue", "productArtValue", "geschenkValue", "furWenValue"})
    private void testParfumFilters(String highlightsValue,
                                   String markeValue,
                                   String productArtValue,
                                   String geschenkValue,
                                   String furWenValue) {
        erlaubenCookies();
        navigateToParfum();
        setFilter("Highlights", highlightsValue);
        setFilter("Marke", markeValue);
        setFilter("Produktart", productArtValue);
        setFilter("Geschenk für", geschenkValue);
        setFilter("Für Wen", furWenValue);
    }

    private void erlaubenCookies() {
        WebElement alleErlaubenButton = driver.findElement(By.xpath("//*[contains(text(), 'Alle erlauben')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", alleErlaubenButton);
        wait5Seconds();
        log.info("Cookies are handled. Awaiting for navigation panel to interact with.");
    }

    private void navigateToParfum() {
        WebElement parfumNavButton = driver.findElement(By.xpath("//*[contains(text(), 'PARFUM')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", parfumNavButton);
        assert driver.getCurrentUrl().equals("https://www.douglas.de/de/c/parfum/01");
        log.info("Navigation to 'Parfum' page was successful");
    }

    private void setFilter(String filterName, String valueName) {
        if (!valueName.isEmpty()) {
            String filterLocator = "//*[@class='facet__title' and contains(text(),'TEXT')]".replace("TEXT", filterName);
            String valueLocator = "//*[@class='facet-option__checkbox--rating-stars']/div[contains(text(), 'TEXT')]".replace("TEXT", valueName);
            driver.findElement(By.xpath(filterLocator)).click();

            WebElement checkbox = driver.findElement(By.xpath(valueLocator + "/parent::div/preceding-sibling::div"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);

            log.info("Set Filter " + filterName + " with value " + valueName);

            waitForParfumFiltersPanelLoaded(filterName);

            closeFilter();

            verifyFilterAdded(valueName);
            log.info("Expected filter value is applied.");

        }
    }

    private void wait5Seconds() {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ie) {
            System.out.println(ie.getMessage());
        }
    }

    private void waitForParfumFiltersPanelLoaded(String filterName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'headline-wrapper')]")));
        log.info("Page is reloaded after applying the filter " + filterName + ". Continue performing the test");
    }

    private void closeFilter() {
        WebElement closeButton = driver.findElement(By.xpath("//button[contains(@class, 'facet__close-button')]"));

        try {
            closeButton.click();
        } catch (StaleElementReferenceException e) {
            log.info("Close button is not found.");
        }
    }

    private void verifyFilterAdded(String valueName) {
        String addedFilterLocator = "//button[contains(@class, 'selected-facets__value') and contains(text(), 'TEXT')]".replace("TEXT", valueName);
        WebElement addedFilter = driver.findElement(By.xpath(addedFilterLocator));
        Assert.assertTrue(addedFilter.isDisplayed(), "Filter is not added. It's not expected!");
    }

    @AfterMethod
    private void tearDown() {
        driver.quit();
    }
}
