/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.scaling.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.execute.engine.ShardingScalingExecuteEngine;
import org.apache.shardingsphere.scaling.utils.ReflectionUtil;
import org.apache.shardingsphere.scaling.utils.ScalingConfigurationUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class HttpServerHandlerTest {
    
    private static final Gson GSON = new Gson();
    
    private HttpServerHandler httpServerHandler;
    
    @Mock
    private ChannelHandlerContext channelHandlerContext;
    
    private FullHttpRequest fullHttpRequest;
    
    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setUp() {
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "serverConfig", new ServerConfiguration());
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "taskExecuteEngine", mock(ShardingScalingExecuteEngine.class));
        httpServerHandler = new HttpServerHandler();
    }
    
    @Test
    public void assertChannelReadStartSuccess() {
        startScalingJob("/config.json");
        ArgumentCaptor<FullHttpResponse> argumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(channelHandlerContext).writeAndFlush(argumentCaptor.capture());
        FullHttpResponse fullHttpResponse = argumentCaptor.getValue();
        assertTrue(fullHttpResponse.content().toString(CharsetUtil.UTF_8).contains("{\"success\":true"));
    }
    
    @Test
    public void assertShardingSphereJDBCTargetChannelReadStartSuccess() {
        startScalingJob("/config_sharding_sphere_jdbc_target.json");
        ArgumentCaptor<FullHttpResponse> argumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(channelHandlerContext).writeAndFlush(argumentCaptor.capture());
        FullHttpResponse fullHttpResponse = argumentCaptor.getValue();
        assertTrue(fullHttpResponse.content().toString(CharsetUtil.UTF_8).contains("{\"success\":true"));
    }
    
    @Test
    public void assertChannelReadProgressFail() {
        fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/scaling/job/progress/9");
        httpServerHandler.channelRead0(channelHandlerContext, fullHttpRequest);
        ArgumentCaptor<FullHttpResponse> argumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(channelHandlerContext).writeAndFlush(argumentCaptor.capture());
        FullHttpResponse fullHttpResponse = argumentCaptor.getValue();
        assertTrue(fullHttpResponse.content().toString(CharsetUtil.UTF_8).contains("Can't find scaling job id 9"));
    }
    
    @Test
    public void assertChannelReadProgressSuccess() {
        long jobId = startScalingJob("/config.json");
        fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/scaling/job/progress/" + jobId);
        httpServerHandler.channelRead0(channelHandlerContext, fullHttpRequest);
        ArgumentCaptor<FullHttpResponse> argumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(channelHandlerContext, times(2)).writeAndFlush(argumentCaptor.capture());
        FullHttpResponse fullHttpResponse = argumentCaptor.getValue();
        assertTrue(fullHttpResponse.content().toString(CharsetUtil.UTF_8).contains("{\"success\":true"));
    }
    
    @Test
    public void assertChannelReadStop() {
        long jobId = startScalingJob("/config.json");
        fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/scaling/job/stop/" + jobId);
        httpServerHandler.channelRead0(channelHandlerContext, fullHttpRequest);
        ArgumentCaptor<FullHttpResponse> argumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(channelHandlerContext, times(2)).writeAndFlush(argumentCaptor.capture());
        FullHttpResponse fullHttpResponse = argumentCaptor.getValue();
        assertTrue(fullHttpResponse.content().toString(CharsetUtil.UTF_8).contains("{\"success\":true"));
    }
    
    @Test
    public void assertCheckJob() {
        long jobId = startScalingJob("/config.json");
        fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/scaling/job/check/" + jobId);
        httpServerHandler.channelRead0(channelHandlerContext, fullHttpRequest);
        ArgumentCaptor<FullHttpResponse> argumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(channelHandlerContext, times(2)).writeAndFlush(argumentCaptor.capture());
        FullHttpResponse fullHttpResponse = argumentCaptor.getValue();
        assertTrue(fullHttpResponse.content().toString(CharsetUtil.UTF_8).contains("{\"success\":true"));
    }
    
    @Test
    public void assertCheckJobFail() {
        fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/scaling/job/check/9");
        httpServerHandler.channelRead0(channelHandlerContext, fullHttpRequest);
        ArgumentCaptor<FullHttpResponse> argumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(channelHandlerContext).writeAndFlush(argumentCaptor.capture());
        FullHttpResponse fullHttpResponse = argumentCaptor.getValue();
        assertTrue(fullHttpResponse.content().toString(CharsetUtil.UTF_8).contains("Can't find scaling job id 9"));
    }
    
    @Test
    public void assertChannelReadList() {
        fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/scaling/job/list");
        httpServerHandler.channelRead0(channelHandlerContext, fullHttpRequest);
        ArgumentCaptor<FullHttpResponse> argumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(channelHandlerContext).writeAndFlush(argumentCaptor.capture());
        FullHttpResponse fullHttpResponse = argumentCaptor.getValue();
        assertTrue(fullHttpResponse.content().toString(CharsetUtil.UTF_8).contains("{\"success\":true"));
    }
    
    @Test
    public void assertChannelReadUnsupportedUrl() {
        fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.DELETE, "/scaling/1");
        httpServerHandler.channelRead0(channelHandlerContext, fullHttpRequest);
        ArgumentCaptor<FullHttpResponse> argumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(channelHandlerContext).writeAndFlush(argumentCaptor.capture());
        FullHttpResponse fullHttpResponse = argumentCaptor.getValue();
        assertTrue(fullHttpResponse.content().toString(CharsetUtil.UTF_8).contains("Not support request!"));
    }
    
    @Test
    public void assertChannelReadUnsupportedMethod() {
        fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.DELETE, "/scaling/job/stop");
        httpServerHandler.channelRead0(channelHandlerContext, fullHttpRequest);
        ArgumentCaptor<FullHttpResponse> argumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(channelHandlerContext).writeAndFlush(argumentCaptor.capture());
        FullHttpResponse fullHttpResponse = argumentCaptor.getValue();
        assertTrue(fullHttpResponse.content().toString(CharsetUtil.UTF_8).contains("Not support request!"));
    }
    
    @Test
    public void assertExceptionCaught() {
        Throwable throwable = mock(Throwable.class);
        httpServerHandler.exceptionCaught(channelHandlerContext, throwable);
        verify(channelHandlerContext).close();
    }
    
    @SneakyThrows(IOException.class)
    private long startScalingJob(final String configFile) {
        ByteBuf byteBuf = Unpooled.copiedBuffer(GSON.toJson(ScalingConfigurationUtil.initConfig(configFile)), CharsetUtil.UTF_8);
        fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/scaling/job/start", byteBuf);
        httpServerHandler.channelRead0(channelHandlerContext, fullHttpRequest);
        ArgumentCaptor<FullHttpResponse> argumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(channelHandlerContext).writeAndFlush(argumentCaptor.capture());
        FullHttpResponse fullHttpResponse = argumentCaptor.getValue();
        JsonObject jsonObject = new Gson().fromJson(fullHttpResponse.content().toString(CharsetUtil.UTF_8), JsonObject.class);
        return jsonObject.get("model").getAsJsonObject().get("jobId").getAsLong();
    }
}
