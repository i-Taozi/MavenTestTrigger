/**
 *
 */
package com.github.jmkgreen.morphia.mapping;

import java.util.ArrayList;
import java.util.List;


import org.bson.types.ObjectId;
import org.junit.Test;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Id;
import org.junit.Assert;

/**
 * @author scotthernandez
 *
 */
public class ListOfDoubleArrayMappingTest extends TestBase {
	private static class ContainsListDoubleArray {
		@Id ObjectId id;
		List<Double[]> points = new ArrayList<Double[]>();
	}


	@Test
	public void testMapping() throws Exception {
		morphia.map(ContainsListDoubleArray.class);
		ContainsListDoubleArray ent = new ContainsListDoubleArray();
		ent.points.add(new Double[] { 1.1, 2.2});
		ds.save(ent);
		ContainsListDoubleArray loaded = ds.get(ent);
		Assert.assertNotNull(loaded.id);
                Assert.assertNotNull(loaded.points);
		Assert.assertEquals(1.1D, loaded.points.get(0)[0], 0);
		Assert.assertEquals(2.2D, loaded.points.get(0)[1], 0);

	}


}
