/**
 *
 */
package com.github.jmkgreen.morphia;

import com.mongodb.MongoClient;
import org.junit.After;
import org.junit.Before;

import com.github.jmkgreen.morphia.mapping.MappedClass;
import com.mongodb.DB;
import com.mongodb.Mongo;

public abstract class TestBase
{
    public static final String DB_NAME = "morphia_test";
    protected Mongo mongo;
    protected DB db;
    protected Datastore ds;
    protected AdvancedDatastore ads;
    protected Morphia morphia = new Morphia();

    protected TestBase() {
        try {
			this.mongo = new MongoClient();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

    @Before
    public void setUp()
    {
        this.db = this.mongo.getDB(DB_NAME);
        this.ds = this.morphia.createDatastore(this.mongo, this.db.getName());
        this.ads = (AdvancedDatastore) ds;
        //ads.setDecoderFact(LazyDBDecoder.FACTORY);
    }

    protected void cleanup() {
//        this.mongo.dropDatabase("morphia_test");
		for(MappedClass mc : morphia.getMapper().getMappedClasses())
//			if( mc.getEntityAnnotation() != null )
				db.getCollection(mc.getCollectionName()).drop();

    }

	@After
	public void tearDown() {
    	cleanup();
		mongo.close();
	}
}
