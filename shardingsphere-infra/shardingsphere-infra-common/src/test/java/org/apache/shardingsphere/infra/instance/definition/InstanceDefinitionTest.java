package org.apache.shardingsphere.infra.instance.definition;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InstanceDefinitionTest {

    private static final String INSTANCE = "instance1";
    private static final String PORT = "8080";
    private static final String IP = "192.168.1.88";
    private static final String ATTRIBUTES = "192.168.1.88@8080";
    private InstanceDefinition instanceDefinition;

    @Test
    public void testWithTypeAndId() {
        instanceDefinition = new InstanceDefinition(InstanceType.JDBC, INSTANCE);
        assertEquals(INSTANCE, instanceDefinition.getInstanceId());
        assertEquals(InstanceType.JDBC, instanceDefinition.getInstanceType());
        assertEquals(IP, instanceDefinition.getIp());
        assertNotNull(instanceDefinition.getUniqueSign());
        assertTrue(instanceDefinition.getAttributes().startsWith(IP + "@"));
    }

    @Test
    public void testWithPort() {
        instanceDefinition = new InstanceDefinition(InstanceType.PROXY, Integer.valueOf(PORT), INSTANCE);
        assertEquals(INSTANCE, instanceDefinition.getInstanceId());
        assertEquals(InstanceType.PROXY, instanceDefinition.getInstanceType());
        assertEquals(IP, instanceDefinition.getIp());
        assertEquals(PORT, instanceDefinition.getUniqueSign());
        assertEquals(ATTRIBUTES, instanceDefinition.getAttributes());
    }

    @Test
    public void testWithAttributes() {
        instanceDefinition = new InstanceDefinition(InstanceType.PROXY, INSTANCE, ATTRIBUTES);
        assertEquals(INSTANCE, instanceDefinition.getInstanceId());
        assertEquals(InstanceType.PROXY, instanceDefinition.getInstanceType());
        assertEquals(IP, instanceDefinition.getIp());
        assertEquals(PORT, instanceDefinition.getUniqueSign());
        assertEquals(ATTRIBUTES, instanceDefinition.getAttributes());
    }
}