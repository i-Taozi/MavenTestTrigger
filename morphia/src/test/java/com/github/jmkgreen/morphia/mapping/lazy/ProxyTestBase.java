/**
 *
 */
package com.github.jmkgreen.morphia.mapping.lazy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;

import org.junit.Ignore;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.mapping.lazy.proxy.ProxiedReference;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 *
 */
public class ProxyTestBase extends TestBase {

	protected void assertFetched(final Object e) {
        Assert.assertTrue(e instanceof ProxiedReference);
		Assert.assertTrue(isFetched(e));
	}

	protected void assertNotFetched(final Object e) {
        Assert.assertTrue("e is not a ProxiedReference, it is however a: " + e.getClass().getName(), e instanceof ProxiedReference);
		Assert.assertFalse(isFetched(e));
	}

	protected boolean isFetched(final Object e) {
		return asProxiedReference(e).__isFetched();
	}

	protected ProxiedReference asProxiedReference(final Object e) {
		return (ProxiedReference) e;
	}

	protected void assertIsProxy(Object p) {
		Assert.assertTrue(p instanceof ProxiedReference);
	}

	protected <T> T deserialize(final Object t) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(baos);
			os.writeObject(t);
			os.close();
			byte[] ba = baos.toByteArray();

			return (T) new ObjectInputStream(new ByteArrayInputStream(ba))
			.readObject();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}


}
