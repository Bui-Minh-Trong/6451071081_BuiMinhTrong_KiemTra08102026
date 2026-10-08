package vn.edu.utc.office.pages;

import org.openqa.selenium.WebDriver;
import vn.edu.utc.office.base.BasePage;

/**
 * HomePage represents the destination page after successful authentication.
 * Conforms to Slide Buoi 8 (page 52).
 */
public class HomePage extends BasePage {

    public HomePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Verifies that the user has navigated away from the login page to the authenticated area.
     */
    public boolean isLoaded() {
        return !getCurrentUrl().contains("/Login");
    }
}
