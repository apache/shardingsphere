package org.apache.shardingsphere.scaling.web;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HttpServerInitializerTest {
    
    @Mock
    private SocketChannel socketChannel;
    
    @Mock
    private ChannelPipeline channelPipeline;
    
    @Before
    public void setUp() {
        when(socketChannel.pipeline()).thenReturn(channelPipeline);
    }
    
    @Test
    public void assertInitChannel() {
        HttpServerInitializer httpServerInitializer = new HttpServerInitializer();
        httpServerInitializer.initChannel(socketChannel);
        verify(channelPipeline, times(3)).addLast(any(ChannelHandler.class));
    }
}
