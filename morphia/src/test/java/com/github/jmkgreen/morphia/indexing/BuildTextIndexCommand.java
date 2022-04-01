package com.github.jmkgreen.morphia.indexing;

import com.mongodb.DBObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jamesgreen
 * Date: 23/03/2013
 * Time: 18:42
 * To change this template use File | Settings | File Templates.
 */
public class BuildTextIndexCommand {

    @Test(expected = IllegalArgumentException.class)
    public void testNoFieldsSpecified() {
        TextIndexCommand cmd = new TextIndexCommand();
        DBObject keys = cmd.getKeys();
        Assert.assertNotNull(keys);
    }

    @Test
    public void testWithFields() {
        TextIndexCommand cmd = new TextIndexCommand();
        String field = "myField";

        List<String> fields = new ArrayList();
        fields.add(field);
        cmd.setFields(fields);

        Assert.assertTrue(cmd.getKeys().toMap().containsKey(field));
    }

    @Test
    public void testAllFields() {
        TextIndexCommand cmd  = new TextIndexCommand();
        cmd.setIndexAllFields();

        Assert.assertTrue(cmd.getKeys().containsField("$**"));
    }

    @Test
    public void testDefaultLanguage() {
        TextIndexCommand cmd = new TextIndexCommand();
        cmd.setDefaultLanguage("spanish");

        Assert.assertTrue(cmd.getOptions().containsField("default_language"));
        Assert.assertTrue(cmd.getOptions().get("default_language").equals("spanish"));
    }

    @Test
    public void testOverrideLanguage() {
        TextIndexCommand cmd = new TextIndexCommand();
        cmd.setLanguageOverride("spanish");

        Assert.assertTrue(cmd.getOptions().containsField("language_override"));
        Assert.assertTrue(cmd.getOptions().get("language_override").equals("spanish"));
    }

    @Test
    public void testName() {
        TextIndexCommand cmd = new TextIndexCommand();
        cmd.setName("myIndex");

        Assert.assertTrue(cmd.getOptions().containsField("name"));
        Assert.assertTrue(cmd.getOptions().get("name").equals("myIndex"));
    }

    @Test
    public void testWeights() {

        TextIndexCommand cmd = new TextIndexCommand();
        Map<String, Integer> weights = new HashMap();
        weights.put("myImportantField", 10);
        weights.put("myUnimportantField", 5);
        weights.put("myDefaultWeightedField", 1);
        cmd.setWeights(weights);

        Assert.assertTrue(cmd.getOptions().containsField("weights"));
        HashMap<String, Integer> computedWeights = (HashMap) cmd.getOptions().get("weights");
        Assert.assertNotNull(computedWeights);
        Assert.assertTrue(computedWeights.containsKey("myImportantField"));
        Assert.assertTrue(computedWeights.containsKey("myUnimportantField"));
        Assert.assertTrue(computedWeights.containsKey("myDefaultWeightedField"));

        Assert.assertTrue(computedWeights.get("myImportantField").equals(10));
        Assert.assertTrue(computedWeights.get("myUnimportantField").equals(5));
        Assert.assertTrue(computedWeights.get("myDefaultWeightedField").equals(1));

    }
}
