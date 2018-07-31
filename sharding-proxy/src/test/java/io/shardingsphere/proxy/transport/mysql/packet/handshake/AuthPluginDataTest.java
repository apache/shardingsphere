package io.shardingsphere.proxy.transport.mysql.packet.handshake;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AuthPluginDataTest {
    
    AuthPluginData authPluginData = new AuthPluginData();
    
    @Test
    public void testGetAuthPluginData() {
        assertEquals(authPluginData.getAuthPluginData().length, 20);
    }
    
    @Test
    public void testGetAuthPluginDataPart1() {
        assertEquals(authPluginData.getAuthPluginDataPart1().length, 8);
    }
    
    @Test
    public void testGetAuthPluginDataPart2() {
        assertEquals(authPluginData.getAuthPluginDataPart2().length, 12);
    }
}