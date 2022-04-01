package com.github.webdriverextensions.junitrunner;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Test;

import com.github.webdriverextensions.WebDriverProperties;

public class DisabledBrowserTest {

    @Test
    public void testParseDisabledBrowserSring() {
	assertThat(WebDriverRunner.parseDisabledBrowserString("").size(), is(0));

	assertThat(WebDriverRunner.parseDisabledBrowserString("firefox").size(), is(1));
	assertThat(WebDriverRunner.parseDisabledBrowserString("firefox"), hasItem("firefox"));
	
	assertThat(WebDriverRunner.parseDisabledBrowserString("firefox,chrome").size(), is(2));
	assertThat(WebDriverRunner.parseDisabledBrowserString("firefox,chrome"), hasItem("firefox"));
	assertThat(WebDriverRunner.parseDisabledBrowserString("firefox,chrome"), hasItem("chrome"));
    
	assertThat(WebDriverRunner.parseDisabledBrowserString("firefox , chrome").size(), is(2));
	assertThat(WebDriverRunner.parseDisabledBrowserString("firefox , chrome"), hasItem("firefox"));
	assertThat(WebDriverRunner.parseDisabledBrowserString("firefox , chrome"), hasItem("chrome"));
    }
    
    @Test
    public void testGetDisabledBrowsers() {
	System.setProperty(WebDriverProperties.DISABLED_BROWSERS_PROPERTY_NAME, "firefox");
	assertThat(WebDriverRunner.getDisabledBrowsers().size(), is(1));
    }
}
