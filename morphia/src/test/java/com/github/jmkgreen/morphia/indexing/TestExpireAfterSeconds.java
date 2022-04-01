package com.github.jmkgreen.morphia.indexing;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Indexed;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jamesgreen
 * Date: 23/03/2013
 * Time: 22:13
 * To change this template use File | Settings | File Templates.
 */
public class TestExpireAfterSeconds extends TestBase {

    @Entity
    public static class HasExpiryField {

        @Id
        private ObjectId id;
        private String offerIs;
        @Indexed(expireAfterSeconds = 60)
        private Date offerExpiresAt;

        public String getOfferIs() {
            return offerIs;
        }

        public void setOfferIs(String offerIs) {
            this.offerIs = offerIs;
        }

        public Date getOfferExpiresAt() {
            return offerExpiresAt;
        }

        public void setOfferExpiresAt(Date offerExpiresAt) {
            this.offerExpiresAt = offerExpiresAt;
        }
    }

    @Test
    public void testStore() {
        HasExpiryField hasExpiryField = new HasExpiryField();
        hasExpiryField.setOfferIs("Good");
        Calendar c = Calendar.getInstance();
        hasExpiryField.setOfferExpiresAt(c.getTime());
        ds.getMapper().addMappedClass(HasExpiryField.class);
        ds.ensureIndexes();
        ds.save(hasExpiryField);

        DB db = ds.getDB();
        DBCollection dbCollection = db.getCollection("HasExpiryField");
        List<DBObject> indexes = dbCollection.getIndexInfo();

        Assert.assertNotNull(indexes);
        Assert.assertEquals(2, indexes.size());
        DBObject index = null;
        for (DBObject candidateIndex : indexes) {
            if (candidateIndex.containsField("expireAfterSeconds")) {
                index = candidateIndex;
            }
        }
        Assert.assertNotNull(index);
        Assert.assertTrue(index.containsField("expireAfterSeconds"));
        Assert.assertEquals(60, index.get("expireAfterSeconds"));
    }
}
