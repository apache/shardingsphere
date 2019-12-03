package org.apache.shardingsphere.shardingscaling.core.web;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author ssxlulu
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpServerHandlerTest {

    @Mock
    private ChannelHandlerContext channelHandlerContext;

    private FullHttpRequest fullHttpRequest;

    private HttpServerHandler httpServerHandler;

    @Before
    public void setUp() {
        httpServerHandler = new HttpServerHandler();
    }

    @Test
    public void channelReadStart() {
        fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/shardingscaling/start");
        httpServerHandler.channelRead0(channelHandlerContext, fullHttpRequest);
        ArgumentCaptor argumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(channelHandlerContext).writeAndFlush(argumentCaptor.capture());
        FullHttpResponse fullHttpResponse = (FullHttpResponse) argumentCaptor.getValue();
        assertEquals("start", fullHttpResponse.content().toString(CharsetUtil.UTF_8));
    }

    @Test
    public void channelReadProgress() {
        fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/shardingscaling/progress/1");
        httpServerHandler.channelRead0(channelHandlerContext, fullHttpRequest);
        ArgumentCaptor argumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(channelHandlerContext).writeAndFlush(argumentCaptor.capture());
        FullHttpResponse fullHttpResponse = (FullHttpResponse) argumentCaptor.getValue();
        assertEquals("progress", fullHttpResponse.content().toString(CharsetUtil.UTF_8));
    }

    @Test
    public void channelReadStop() {
        fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.DELETE, "/shardingscaling/stop/1");
        httpServerHandler.channelRead0(channelHandlerContext, fullHttpRequest);
        ArgumentCaptor argumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(channelHandlerContext).writeAndFlush(argumentCaptor.capture());
        FullHttpResponse fullHttpResponse = (FullHttpResponse) argumentCaptor.getValue();
        assertEquals("stop", fullHttpResponse.content().toString(CharsetUtil.UTF_8));
    }

    @Test
    public void channelReadUnsupport() {
        fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.DELETE, "/shardingscaling/1");
        httpServerHandler.channelRead0(channelHandlerContext, fullHttpRequest);
        ArgumentCaptor argumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(channelHandlerContext).writeAndFlush(argumentCaptor.capture());
        FullHttpResponse fullHttpResponse = (FullHttpResponse) argumentCaptor.getValue();
        assertEquals("not support request", fullHttpResponse.content().toString(CharsetUtil.UTF_8));
    }

    @Test
    public void exceptionCaught() {
        Throwable throwable = mock(Throwable.class);
        httpServerHandler.exceptionCaught(channelHandlerContext, throwable);
        verify(channelHandlerContext).close();
    }
}