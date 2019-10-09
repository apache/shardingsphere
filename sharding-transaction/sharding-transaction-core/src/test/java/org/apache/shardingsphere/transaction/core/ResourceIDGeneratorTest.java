package org.apache.shardingsphere.transaction.core;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ResourceIDGeneratorTest {
    
    @Test
    public void assertNextIdProperly() {
        assertTrue(ResourceIDGenerator.getInstance().nextId().contains("resource"));
    }
}
