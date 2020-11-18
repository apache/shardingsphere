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

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.execute.engine.ShardingScalingExecuteEngine;
import org.apache.shardingsphere.scaling.utils.ReflectionUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class HttpServerInitializerTest {
    
    @Mock
    private SocketChannel socketChannel;
    
    @Mock
    private ChannelPipeline channelPipeline;
    
    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setUp() {
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "serverConfig", new ServerConfiguration());
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "taskExecuteEngine", mock(ShardingScalingExecuteEngine.class));
        when(socketChannel.pipeline()).thenReturn(channelPipeline);
    }
    
    @Test
    public void assertInitChannel() {
        HttpServerInitializer httpServerInitializer = new HttpServerInitializer();
        httpServerInitializer.initChannel(socketChannel);
        verify(channelPipeline, times(3)).addLast(any(ChannelHandler.class));
    }
}
