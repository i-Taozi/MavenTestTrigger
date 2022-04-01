package com.github.webdriverextensions;

import static com.github.webdriverextensions.Bot.*;
import com.github.webdriverextensions.junitrunner.WebDriverRunner;
import com.github.webdriverextensions.junitrunner.annotations.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.BrowserType;

@RunWith(WebDriverRunner.class)
@RemoteAddress("http://andidev:80b7768e-dc06-4d5b-b793-5b3b83f0e24c@ondemand.saucelabs.com:80/wd/hub")
public class BotSauceLabsTest {

    @Test
    @Firefox
    public void browserIsTest() {
        assertThat(browser(), equalToIgnoringCase("firefox"));
        assertThat(browserIs("FIREFOX"), equalTo(true));
        assertThat(browserIsNot("chrome"), equalTo(true));
    }

    @Test
    @Android
    @Ignore // Does not run in sauce labs for unknown reason
    public void browserIsAndroidTest() {
        assertThat(browser(), equalToIgnoringCase(BrowserType.CHROME)); // Browser is chrome for Android devices since android 4.0
        assertThat(browserIsNotAndroid(), equalTo(true));
        assertThat(browserIsChrome(), equalTo(true));
        assertThat(browserIsNotEdge(), equalTo(true));
        assertThat(browserIsNotFirefox(), equalTo(true));
        assertThat(browserIsNotHtmlUnit(), equalTo(true));
        assertThat(browserIsNotIPad(), equalTo(true));
        assertThat(browserIsNotIPhone(), equalTo(true));
        assertThat(browserIsNotInternetExplorer(), equalTo(true));
        assertThat(browserIsNotOpera(), equalTo(true));
        assertThat(browserIsNotPhantomJS(), equalTo(true));
        assertThat(browserIsNotSafari(), equalTo(true));
    }

    @Test
    @Chrome
    public void browserIsChromeTest() {
        assertThat(browser(), equalToIgnoringCase(BrowserType.CHROME));
        assertThat(browserIsNotAndroid(), equalTo(true));
        assertThat(browserIsChrome(), equalTo(true));
        assertThat(browserIsNotFirefox(), equalTo(true));
        assertThat(browserIsNotHtmlUnit(), equalTo(true));
        assertThat(browserIsNotIPad(), equalTo(true));
        assertThat(browserIsNotIPhone(), equalTo(true));
        assertThat(browserIsNotInternetExplorer(), equalTo(true));
        assertThat(browserIsNotOpera(), equalTo(true));
        assertThat(browserIsNotPhantomJS(), equalTo(true));
        assertThat(browserIsNotSafari(), equalTo(true));
    }

    @Test
    @Firefox
    public void browserIsFirefoxTest() {
        assertThat(browser(), equalToIgnoringCase(BrowserType.FIREFOX));
        assertThat(browserIsNotAndroid(), equalTo(true));
        assertThat(browserIsNotChrome(), equalTo(true));
        assertThat(browserIsNotEdge(), equalTo(true));
        assertThat(browserIsFirefox(), equalTo(true));
        assertThat(browserIsNotHtmlUnit(), equalTo(true));
        assertThat(browserIsNotIPad(), equalTo(true));
        assertThat(browserIsNotIPhone(), equalTo(true));
        assertThat(browserIsNotInternetExplorer(), equalTo(true));
        assertThat(browserIsNotOpera(), equalTo(true));
        assertThat(browserIsNotPhantomJS(), equalTo(true));
        assertThat(browserIsNotSafari(), equalTo(true));
    }

    @Ignore // Not available at Sauce Labs
    @Test
    @HtmlUnit
    public void browserIsHtmlUnitTest() {
        assertThat(browser(), equalToIgnoringCase(BrowserType.HTMLUNIT));
        assertThat(browserIsNotAndroid(), equalTo(true));
        assertThat(browserIsNotChrome(), equalTo(true));
        assertThat(browserIsNotEdge(), equalTo(true));
        assertThat(browserIsNotFirefox(), equalTo(true));
        assertThat(browserIsHtmlUnit(), equalTo(true));
        assertThat(browserIsNotIPad(), equalTo(true));
        assertThat(browserIsNotIPhone(), equalTo(true));
        assertThat(browserIsNotInternetExplorer(), equalTo(true));
        assertThat(browserIsNotOpera(), equalTo(true));
        assertThat(browserIsNotPhantomJS(), equalTo(true));
        assertThat(browserIsNotSafari(), equalTo(true));
    }

    @Test
    @IPad
    @Ignore // TODO: Maybe remove since sauce labs seems to use SAFARI as browser type for IPad
    public void browserIsIPadTest() {
        assertThat(browser(), equalToIgnoringCase(BrowserType.IPAD));
        assertThat(browserIsNotAndroid(), equalTo(true));
        assertThat(browserIsNotChrome(), equalTo(true));
        assertThat(browserIsNotEdge(), equalTo(true));
        assertThat(browserIsNotFirefox(), equalTo(true));
        assertThat(browserIsNotHtmlUnit(), equalTo(true));
        assertThat(browserIsIPad(), equalTo(true));
        assertThat(browserIsNotIPhone(), equalTo(true));
        assertThat(browserIsNotInternetExplorer(), equalTo(true));
        assertThat(browserIsNotOpera(), equalTo(true));
        assertThat(browserIsNotPhantomJS(), equalTo(true));
        assertThat(browserIsNotSafari(), equalTo(true));
    }

    @Test
    @IPhone
    @Ignore // TODO: Maybe remove since sauce labs seems to use SAFARI as browser type for IPhone
    public void browserIsIPhoneTest() {
        assertThat(browser(), equalToIgnoringCase(BrowserType.IPHONE));
        assertThat(browserIsNotAndroid(), equalTo(true));
        assertThat(browserIsNotChrome(), equalTo(true));
        assertThat(browserIsNotEdge(), equalTo(true));
        assertThat(browserIsNotFirefox(), equalTo(true));
        assertThat(browserIsNotHtmlUnit(), equalTo(true));
        assertThat(browserIsNotIPad(), equalTo(true));
        assertThat(browserIsIPhone(), equalTo(true));
        assertThat(browserIsNotInternetExplorer(), equalTo(true));
        assertThat(browserIsNotOpera(), equalTo(true));
        assertThat(browserIsNotPhantomJS(), equalTo(true));
        assertThat(browserIsNotSafari(), equalTo(true));
    }

    @Test
    @InternetExplorer
    public void browserIsInternetExplorerTest() {
        assertThat(browserIsNotAndroid(), equalTo(true));
        assertThat(browserIsNotChrome(), equalTo(true));
        assertThat(browserIsNotEdge(), equalTo(true));
        assertThat(browserIsNotFirefox(), equalTo(true));
        assertThat(browserIsNotHtmlUnit(), equalTo(true));
        assertThat(browserIsNotIPad(), equalTo(true));
        assertThat(browserIsNotIPhone(), equalTo(true));
        assertThat(browserIsInternetExplorer(), equalTo(true));
        assertThat(browserIsNotOpera(), equalTo(true));
        assertThat(browserIsNotPhantomJS(), equalTo(true));
        assertThat(browserIsNotSafari(), equalTo(true));
    }

    @Test
    @Edge
    public void browserIsEdgeTest() {
        assertThat(browserIsNotAndroid(), equalTo(true));
        assertThat(browserIsNotChrome(), equalTo(true));
        assertThat(browserIsEdge(), equalTo(true));
        assertThat(browserIsNotFirefox(), equalTo(true));
        assertThat(browserIsNotHtmlUnit(), equalTo(true));
        assertThat(browserIsNotIPad(), equalTo(true));
        assertThat(browserIsNotIPhone(), equalTo(true));
        assertThat(browserIsNotInternetExplorer(), equalTo(true));
        assertThat(browserIsNotOpera(), equalTo(true));
        assertThat(browserIsNotPhantomJS(), equalTo(true));
        assertThat(browserIsNotSafari(), equalTo(true));
    }

    @Ignore // Not available at Sauce Labs
    @Test
    @Opera
    public void browserIsOperaTest() {
        assertThat(browser(), equalToIgnoringCase(BrowserType.OPERA));
        assertThat(browserIsNotAndroid(), equalTo(true));
        assertThat(browserIsNotChrome(), equalTo(true));
        assertThat(browserIsNotEdge(), equalTo(true));
        assertThat(browserIsNotFirefox(), equalTo(true));
        assertThat(browserIsNotHtmlUnit(), equalTo(true));
        assertThat(browserIsNotIPad(), equalTo(true));
        assertThat(browserIsNotIPhone(), equalTo(true));
        assertThat(browserIsNotInternetExplorer(), equalTo(true));
        assertThat(browserIsOpera(), equalTo(true));
        assertThat(browserIsNotPhantomJS(), equalTo(true));
        assertThat(browserIsNotSafari(), equalTo(true));
    }

    @Test
    @PhantomJS
    @Ignore // Not available at Sauce Labs
    public void browserIsPhantomJSTest() {
        assertThat(browser(), equalToIgnoringCase(BrowserType.PHANTOMJS));
        assertThat(browserIsNotAndroid(), equalTo(true));
        assertThat(browserIsNotChrome(), equalTo(true));
        assertThat(browserIsNotEdge(), equalTo(true));
        assertThat(browserIsNotFirefox(), equalTo(true));
        assertThat(browserIsNotHtmlUnit(), equalTo(true));
        assertThat(browserIsNotIPad(), equalTo(true));
        assertThat(browserIsNotIPhone(), equalTo(true));
        assertThat(browserIsNotInternetExplorer(), equalTo(true));
        assertThat(browserIsNotOpera(), equalTo(true));
        assertThat(browserIsPhantomJS(), equalTo(true));
        assertThat(browserIsNotSafari(), equalTo(true));
    }

    @Test
    @Safari
    public void browserIsSafariTest() {
        assertThat(browser(), equalToIgnoringCase(BrowserType.SAFARI));
        assertThat(browserIsNotAndroid(), equalTo(true));
        assertThat(browserIsNotChrome(), equalTo(true));
        assertThat(browserIsNotEdge(), equalTo(true));
        assertThat(browserIsNotFirefox(), equalTo(true));
        assertThat(browserIsNotHtmlUnit(), equalTo(true));
        assertThat(browserIsNotIPad(), equalTo(true));
        assertThat(browserIsNotIPhone(), equalTo(true));
        assertThat(browserIsNotInternetExplorer(), equalTo(true));
        assertThat(browserIsNotOpera(), equalTo(true));
        assertThat(browserIsNotPhantomJS(), equalTo(true));
        assertThat(browserIsSafari(), equalTo(true));
    }

    @Test
    @Firefox(version = "66.0")
    public void versionIsTest() {
        assertThat(version(), equalTo("66.0"));
        assertThat(versionIs("66.0"), equalTo(true));
        assertThat(versionIsNot("65.0"), equalTo(true));
    }

    @Test
    @Firefox(platform = Platform.LINUX)
    public void platformIsTest() {
        assertThat(platform(), equalTo(Platform.LINUX));
        assertThat(platformIs(Platform.LINUX), equalTo(true));
        assertThat(platformIsNot(Platform.WINDOWS), equalTo(true));
    }

    // TODO: investigatem currently fails for some reason
    @Test
    @Ignore
    @Android(platform = Platform.ANDROID)
    public void platformIsAndroidTest() {
        assertThat(platformIsAndroid(), equalTo(true));
        assertThat(platformIsLinux(), equalTo(true));
        assertThat(platformIsNotMac(), equalTo(true));
        assertThat(platformIsUnix(), equalTo(true));
        assertThat(platformIsNotWindows(), equalTo(true));
        assertThat(platformIsNotWin8(), equalTo(true));
        assertThat(platformIsNotWin8_1(), equalTo(true));
        assertThat(platformIsNotVista(), equalTo(true));
        assertThat(platformIsNotXP(), equalTo(true));
    }

    @Test
    @Firefox(platform = Platform.LINUX)
    public void platformIsLinuxTest() {
        assertThat(platformIsNotAndroid(), equalTo(true));
        assertThat(platformIsLinux(), equalTo(true));
        assertThat(platformIsNotMac(), equalTo(true));
        assertThat(platformIsUnix(), equalTo(true));
        assertThat(platformIsNotWindows(), equalTo(true));
        assertThat(platformIsNotWin8(), equalTo(true));
        assertThat(platformIsNotWin8_1(), equalTo(true));
        assertThat(platformIsNotVista(), equalTo(true));
        assertThat(platformIsNotXP(), equalTo(true));
    }

    @Test
    @Safari(platform = Platform.MAC)
    public void platformIsMacTest() {
        assertThat(platformIsNotAndroid(), equalTo(true));
        assertThat(platformIsNotLinux(), equalTo(true));
        assertThat(platformIsMac(), equalTo(true));
        assertThat(platformIsNotUnix(), equalTo(true));
        assertThat(platformIsNotWindows(), equalTo(true));
        assertThat(platformIsNotWin8(), equalTo(true));
        assertThat(platformIsNotWin8_1(), equalTo(true));
        assertThat(platformIsNotVista(), equalTo(true));
        assertThat(platformIsNotXP(), equalTo(true));
    }

    @Test
    @Firefox(platform = Platform.UNIX)
    public void platformIsUnixTest() {
        assertThat(platformIsNotAndroid(), equalTo(true));
        assertThat(platformIsNotMac(), equalTo(true));
        assertThat(platformIsUnix(), equalTo(true));
        assertThat(platformIsNotWindows(), equalTo(true));
        assertThat(platformIsNotWin8(), equalTo(true));
        assertThat(platformIsNotWin8_1(), equalTo(true));
        assertThat(platformIsNotVista(), equalTo(true));
        assertThat(platformIsNotXP(), equalTo(true));
    }

    @Ignore // since platform is not correctly set by sauce labs
    @Test
    @Firefox(platform = Platform.WINDOWS)
    public void platformIsWindowsTest() {
        assertThat(platformIsNotAndroid(), equalTo(true));
        assertThat(platformIsNotLinux(), equalTo(true));
        assertThat(platformIsNotMac(), equalTo(true));
        assertThat(platformIsNotUnix(), equalTo(true));
        assertThat(platformIsWindows(), equalTo(true));
    }

    @Ignore // since platform is not correctly set by sauce labs
    @Test
    @InternetExplorer(platform = Platform.WIN8)
    public void platformIsWin8Test() {
        assertThat(platformIsNotAndroid(), equalTo(true));
        assertThat(platformIsNotLinux(), equalTo(true));
        assertThat(platformIsNotMac(), equalTo(true));
        assertThat(platformIsNotUnix(), equalTo(true));
        assertThat(platformIsWindows(), equalTo(true));
        assertThat(platformIsWin8(), equalTo(true));
        assertThat(platformIsNotWin8_1(), equalTo(true));
        assertThat(platformIsNotVista(), equalTo(true));
        assertThat(platformIsNotXP(), equalTo(true));
    }

    @Ignore // since platform is not correctly set by sauce labs
    @Test
    @InternetExplorer(platform = Platform.WIN8)
    public void platformIsWin8_1Test() {
        assertThat(platformIsNotAndroid(), equalTo(true));
        assertThat(platformIsNotLinux(), equalTo(true));
        assertThat(platformIsNotMac(), equalTo(true));
        assertThat(platformIsNotUnix(), equalTo(true));
        assertThat(platformIsWindows(), equalTo(true));
        assertThat(platformIsNotWin8(), equalTo(true));
        assertThat(platformIsWin8_1(), equalTo(true));
        assertThat(platformIsNotVista(), equalTo(true));
        assertThat(platformIsNotXP(), equalTo(true));
    }

    @Ignore // since platform is not correctly set by sauce labs
    @Test
    @InternetExplorer(platform = Platform.VISTA)
    public void platformIsVistaTest() {
        assertThat(platformIsNotAndroid(), equalTo(true));
        assertThat(platformIsNotLinux(), equalTo(true));
        assertThat(platformIsNotMac(), equalTo(true));
        assertThat(platformIsNotUnix(), equalTo(true));
        assertThat(platformIsWindows(), equalTo(true));
        assertThat(platformIsNotWin8(), equalTo(true));
        assertThat(platformIsNotWin8_1(), equalTo(true));
        assertThat(platformIsVista(), equalTo(true));
        assertThat(platformIsNotXP(), equalTo(true));
    }

    @Ignore // since platform is not correctly set by sauce labs
    @Test
    @InternetExplorer(platform = Platform.XP)
    public void platformIsXPTest() {
        assertThat(platformIsNotAndroid(), equalTo(true));
        assertThat(platformIsNotLinux(), equalTo(true));
        assertThat(platformIsNotMac(), equalTo(true));
        assertThat(platformIsNotUnix(), equalTo(true));
        assertThat(platformIsWindows(), equalTo(true));
        assertThat(platformIsNotWin8(), equalTo(true));
        assertThat(platformIsNotWin8_1(), equalTo(true));
        assertThat(platformIsNotVista(), equalTo(true));
        assertThat(platformIsXP(), equalTo(true));
    }

}
