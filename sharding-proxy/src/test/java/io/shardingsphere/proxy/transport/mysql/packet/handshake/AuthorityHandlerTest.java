package io.shardingsphere.proxy.transport.mysql.packet.handshake;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AuthorityHandlerTest {
    
    private final AuthorityHandler authorityHandler = new AuthorityHandler();
    
    @Test
    public void assertGetAuthPluginData() {
        assertEquals(authorityHandler.getAuthPluginData().getAuthPluginData().length, 20);
    }
}