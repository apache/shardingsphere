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

package org.apache.shardingsphere.proxy.backend.connector.jdbc.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConnectionResourceLockTest {
    
    @Mock
    private ChannelHandlerContext channelHandlerContext;
    
    @Mock
    private Channel channel;
    
    @Mock
    private ConnectionResourceLock connectionResourceLock;
    
    @Test
    void assertDoAwait() throws NoSuchFieldException, IllegalAccessException {
        when(channel.isWritable()).thenReturn(false);
        when(channel.isActive()).thenReturn(true);
        when(channelHandlerContext.channel()).thenReturn(channel);
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(() -> connectionResourceLock.doAwait(channelHandlerContext));
        Awaitility.await().pollDelay(200L, TimeUnit.MILLISECONDS).until(() -> true);
        Plugins.getMemberAccessor().set(ConnectionResourceLock.class.getDeclaredField("condition"), connectionResourceLock, new ReentrantLock().newCondition());
        verify(connectionResourceLock, times(1)).doAwait(channelHandlerContext);
    }
    
    @Test
    void assertDoNotify() {
        when(channel.isWritable()).thenReturn(true);
        when(channel.isActive()).thenReturn(true);
        when(channelHandlerContext.channel()).thenReturn(channel);
        long startTime = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(() -> {
            Awaitility.await().pollDelay(50L, TimeUnit.MILLISECONDS).until(() -> true);
            connectionResourceLock.doNotify();
        });
        connectionResourceLock.doAwait(channelHandlerContext);
        assertTrue(System.currentTimeMillis() >= startTime);
    }
}
