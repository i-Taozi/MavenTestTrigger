package com.github.jmkgreen.morphia.mapping;

import org.junit.Assert;

import org.junit.Test;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.github.jmkgreen.morphia.testutil.TestEntity;

/**
 * @author josephpachod
 */
@SuppressWarnings("unused")
public class ReferencesInEmbeddedTest extends TestBase
{
    @Entity
    private static class Container extends TestEntity {
		private static final long serialVersionUID = 1L;
		String name ;
        @Embedded
        private EmbedContainingReference embed;
    }
    
    private static class EmbedContainingReference {
        String name ;
        @Reference
        protected ReferencedEntity ref;
        
        @Reference(lazy=true)
        protected ReferencedEntity lazyRef;
    }
    
    @Entity
    public static class ReferencedEntity extends TestEntity{
		private static final long serialVersionUID = 1L;
		String foo;
    }
    @Test
    public void testMapping() throws Exception {
        morphia.map(Container.class);
        morphia.map(ReferencedEntity.class);
    }
    
    @Test
    public void testNonLazyReferencesInEmbebbed() throws Exception {
        Container container = new Container();
        container.name= "nonLazy";
        ds.save(container);
        ReferencedEntity referencedEntity = new ReferencedEntity();
        ds.save(referencedEntity);
        
        container.embed = new EmbedContainingReference();
        container.embed.ref = referencedEntity;
        ds.save(container);
        
        Container reloadedContainer = ds.get(container);
        Assert.assertNotNull(reloadedContainer);
    }
    @Test
    public void testLazyReferencesInEmbebbed() throws Exception {
        Container container = new Container();
        container.name="lazy";
        ds.save(container);
        ReferencedEntity referencedEntity = new ReferencedEntity();
        ds.save(referencedEntity);
        
        container.embed = new EmbedContainingReference();
        container.embed.lazyRef = referencedEntity;
        ds.save(container);
        
        Container reloadedContainer = ds.get(container);
        Assert.assertNotNull(reloadedContainer);
    }
}
