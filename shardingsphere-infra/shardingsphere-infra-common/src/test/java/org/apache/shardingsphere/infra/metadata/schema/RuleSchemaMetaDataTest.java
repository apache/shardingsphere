package org.apache.shardingsphere.infra.metadata.schema;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class RuleSchemaMetaDataTest {
    @Test
    public void assertNewInstance() {
        RuleSchemaMetaData obj = new RuleSchemaMetaData(null, null);
        assertNotNull("RuleSchemaMetaData is null", obj);
    }
}
