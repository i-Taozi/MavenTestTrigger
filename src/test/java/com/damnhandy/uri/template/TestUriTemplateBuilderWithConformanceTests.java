package com.damnhandy.uri.template;

import com.damnhandy.uri.template.*;
import com.damnhandy.uri.template.conformance.AbstractUriTemplateConformanceTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author <a href="ryan@damnhandy.com">Ryan J. McDonough</a>
 * Created by ryan on 11/14/15.
 */
public class TestUriTemplateBuilderWithConformanceTests extends AbstractUriTemplateBuilderTests
{

    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object[]> testData() throws Exception
    {
        File file = new File("./uritemplate-test/spec-examples.json");
        return loadTestData(file);
    }

    /**
     * @param vars
     * @param template
     * @param expected
     * @param testsuite
     */
    public TestUriTemplateBuilderWithConformanceTests(Map<String, Object> vars, String template, Object expected, String testsuite)
    {
        super(vars, template, expected, testsuite);
    }


}
