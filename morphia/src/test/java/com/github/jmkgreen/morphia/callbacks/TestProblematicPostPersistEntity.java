package com.github.jmkgreen.morphia.callbacks;

import org.junit.Assert;

import org.junit.Test;

import com.github.jmkgreen.morphia.TestBase;


public class TestProblematicPostPersistEntity extends TestBase{

	@Test
	public void testCallback() throws Exception {
		ProblematicPostPersistEntity p = new ProblematicPostPersistEntity();
		ds.save(p);
		Assert.assertTrue(p.called);
		Assert.assertTrue(p.i.called);
	}
}
