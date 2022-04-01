package com.github.webdriverextensions.junitrunner;

import static com.github.webdriverextensions.Bot.*;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.github.webdriverextensions.junitrunner.annotations.*;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(WebDriverRunner.class)
@Firefox
@Chrome
@InternetExplorer
@HtmlUnit
@Edge
@PhantomJS
//@Safari Disabled until bug is solved, see https://code.google.com/p/selenium/issues/detail?id=7933
@Browsers(chrome = {
    @Chrome(desiredCapabilities = "{chromeOptions: {args: ['start-maximized']}}"),
    @Chrome(desiredCapabilitiesClass = StartChromeMaximized.class)
})
public class WebDriverRunnerTest {

    @Test
    public void successfulTest() {
        open("http://htmlunit.sourceforge.net/");
        if (browserIsHtmlUnit()) {
            waitFor(5, SECONDS);
        }
        assertCurrentUrlContains("htmlunit");
    }
}
