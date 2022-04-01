package com.github.jmkgreen.morphia.indexing;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.SimpleTextIndex;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jamesgreen
 * Date: 23/03/2013
 * Time: 20:30
 * To change this template use File | Settings | File Templates.
 */
@Ignore
public class TestTextIndexedClass extends TestBase {

    @Entity
    public static class TextSpanish {
        @Id
        private ObjectId id;
        @SimpleTextIndex(name = "myTextIndex", defaultLanguage = "spanish")
        private String myIndexedTextField;
        private String myStdTextField;

        public String getMyIndexedTextField() {
            return myIndexedTextField;
        }

        public void setMyIndexedTextField(String myIndexedTextField) {
            this.myIndexedTextField = myIndexedTextField;
        }

        public String getMyStdTextField() {
            return myStdTextField;
        }

        public void setMyStdTextField(String myStdTextField) {
            this.myStdTextField = myStdTextField;
        }
    }

    @Test
    public void ensureSpanishIndexed() {
        TextSpanish indexed = new TextSpanish();
        indexed.setMyIndexedTextField("words for indexing");
        indexed.setMyStdTextField("words not for indexing");
        morphia.map(TextSpanish.class);
        ds.ensureIndexes();
        ds.save(indexed);

        DB db = ds.getMongo().getDB(DB_NAME);
        DBCollection testIndexedCollection = db.getCollection("TextSpanish");
        List<DBObject> indexes = testIndexedCollection.getIndexInfo();
        Assert.assertNotNull(indexes);
        Assert.assertEquals(2, indexes.size());
        DBObject index = null;
        for (DBObject candidateIndex : indexes) {
            if (candidateIndex.containsField("name") && candidateIndex.get("name").equals("myTextIndex")) {
                index = candidateIndex;
            }
        }
        Assert.assertEquals(2, indexes.size());
        Assert.assertNotNull(index);
        Assert.assertTrue(index.containsField("default_language"));
        Assert.assertEquals("spanish", index.get("default_language"));

        Assert.assertTrue(index.containsField("name"));
        Assert.assertEquals("myTextIndex", index.get("name"));
    }

    @Entity
    public static class TextEnglishIndexed {
        @Id
        private ObjectId id;
        @SimpleTextIndex(name = "myTextIndex", defaultLanguage = "english")
        private String myIndexedTextField;
        private String myStdTextField;

        public String getMyIndexedTextField() {
            return myIndexedTextField;
        }

        public void setMyIndexedTextField(String myIndexedTextField) {
            this.myIndexedTextField = myIndexedTextField;
        }

        public String getMyStdTextField() {
            return myStdTextField;
        }

        public void setMyStdTextField(String myStdTextField) {
            this.myStdTextField = myStdTextField;
        }
    }
    @Test
    public void ensureEnglishIndexed() {
        TextSpanish indexed = new TextSpanish();
        indexed.setMyIndexedTextField("words for indexing");
        indexed.setMyStdTextField("words not for indexing");
        morphia.map(TextEnglishIndexed.class);
        ds.ensureIndexes();
        ds.save(indexed);

        DB db = ds.getMongo().getDB(DB_NAME);
        DBCollection testIndexedCollection = db.getCollection("TextEnglishIndexed");
        List<DBObject> indexes = testIndexedCollection.getIndexInfo();
        Assert.assertNotNull(indexes);
        Assert.assertEquals(2, indexes.size());
        DBObject index = null;
        for (DBObject candidateIndex : indexes) {
            if (candidateIndex.containsField("name") && candidateIndex.get("name").equals("myTextIndex")) {
                index = candidateIndex;
            }
        }
        Assert.assertEquals(2, indexes.size());
        Assert.assertNotNull(index);
        Assert.assertTrue(index.containsField("default_language"));
        Assert.assertEquals("english", index.get("default_language"));

        Assert.assertTrue(index.containsField("name"));
        Assert.assertEquals("myTextIndex", index.get("name"));
    }
}
