package vn.edu.utc.office.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import vn.edu.utc.office.base.BasePage;

import java.util.List;

/**
 * Page Object Model (POM) representing the UTC Electronic Office Login Page.
 * Conforms strictly to Slide Buoi 8 (pages 51-52).
 */
public class LoginPage extends BasePage {

    public static final String URL = "https://vanphongdientu.utc.edu.vn/Login";

    // Exact locators matching Slide Buoi 8 p.51 and live portal
    private final By usernameField    = By.name("username");
    private final By passwordField    = By.name("userpwd");
    private final By loginButton      = By.cssSelector("input.submit_login");
    private final By rememberMeCheckbox = By.cssSelector("input#persistent, input[name='persistent']");
    private final By errorAlert       = By.cssSelector(".alert-danger, .error-message, .validation-summary-errors, span[id*='lblError' i], .toast-error, div.error");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Navigates directly to the login URL (Slide p.52).
     */
    public LoginPage open() {
        driver.get(URL);
        return this;
    }

    /**
     * Executes login action and returns target destination HomePage (Slide p.52).
     */
    public HomePage loginAs(String username, String password) {
        type(usernameField, username);
        type(passwordField, password);
        click(loginButton);
        return new HomePage(driver);
    }

    /**
     * Alias for loginAs to maintain compatibility across test suites.
     */
    public HomePage login(String username, String password) {
        return loginAs(username, password);
    }

    /**
     * Checks if current URL indicates user is still on the login screen (Slide p.52).
     */
    public boolean isOnLoginPage() {
        return driver.getCurrentUrl().contains("/Login");
    }

    /**
     * Toggles the Remember Me checkbox via JavaScript for maximum reliability.
     */
    public void clickRememberMe() {
        List<WebElement> checkboxes = driver.findElements(rememberMeCheckbox);
        if (!checkboxes.isEmpty()) {
            WebElement checkbox = checkboxes.get(0);
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
            } catch (Exception e) {
                checkbox.click();
            }
        }
    }

    /**
     * Extracts visible UI validation or authentication error message text.
     */
    public String getErrorMessage() {
        List<WebElement> alerts = driver.findElements(errorAlert);
        if (!alerts.isEmpty() && alerts.get(0).isDisplayed()) {
            return alerts.get(0).getText().trim();
        }
        return "";
    }
}
