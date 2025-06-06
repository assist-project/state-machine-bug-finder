package se.uu.it.smbugfinder.encoding;

import org.junit.Assert;
import org.junit.Test;

import se.uu.it.smbugfinder.DtlsResources;
import se.uu.it.smbugfinder.ResourceManager;

/**
 * Tests loading of a pattern language.
 */
public class OcamlValuesTest {

	@Test
	public void testOnDtlsClientParametricBugPatterns() {
		String path = ResourceManager.getResourceAsAbsolutePathString(DtlsResources.DTLS_CLIENT_PATTERN_LANGUAGE);
		OcamlValues values = new OcamlValues(path);
		checkElementCount(values, 3, 1, 3);
	}
	
	@Test
	public void testOnDtlsServerParametricBugPatterns() {
		String path = ResourceManager.getResourceAsAbsolutePathString(DtlsResources.DTLS_SERVER_PATTERN_LANGUAGE);
		OcamlValues values = new OcamlValues(path);
		checkElementCount(values, 1, 0, 1);
	}
	
	private void checkElementCount(OcamlValues values, int expectedFieldCount, int expectedFunctionCount, int expectedMessageCount) {
		Assert.assertEquals("Invalid field count", expectedFieldCount, values.getFieldsMap().size());
		Assert.assertEquals("Invalid function count", expectedFunctionCount, values.getFunctionsList().size());
		Assert.assertEquals("Invalid message count", expectedMessageCount, values.getMessageMap().size());
	}
}
