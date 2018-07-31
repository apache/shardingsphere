package io.shardingsphere.proxy.transport.mysql.packet.handshake;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConnectionIdGeneratorTest {
    
    private final ConnectionIdGenerator generator = ConnectionIdGenerator.getInstance();
    
    @Test
    public void assertNextId() {
        assertEquals(generator.nextId(), 1);
    }
    
    @Test
    public void assertMaxNextId() throws NoSuchFieldException, IllegalAccessException {
        Field currentId = generator.getClass().getDeclaredField("currentId");
        currentId.setAccessible(true);
        currentId.setInt(generator, 2147483647);
        assertEquals(1, generator.nextId());
    }
    
    @Test
    public void assertGetInstance() {
        assertTrue(null != ConnectionIdGenerator.getInstance());
    }
}