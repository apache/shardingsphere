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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectionResourceLockTest {
    
    @Mock
    private ChannelHandlerContext context;
    
    @Mock
    private Channel channel;
    
    @BeforeEach
    void setUp() {
        when(context.channel()).thenReturn(channel);
    }
    
    @Test
    void assertDoAwaitWhenChannelIsWritable() {
        when(channel.isWritable()).thenReturn(true);
        new ConnectionResourceLock().doAwait(context);
        verify(channel).isWritable();
        verify(context, never()).flush();
    }
    
    @Test
    void assertDoAwaitWhenChannelBecomesWritableAfterAwait() throws InterruptedException {
        AtomicBoolean writable = new AtomicBoolean(false);
        when(channel.isWritable()).thenAnswer(invocation -> writable.get());
        when(channel.isActive()).thenReturn(true);
        CountDownLatch flushLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            flushLatch.countDown();
            return null;
        }).when(context).flush();
        ConnectionResourceLock connectionResourceLock = new ConnectionResourceLock();
        Thread awaitThread = new Thread(() -> connectionResourceLock.doAwait(context));
        awaitThread.start();
        assertTrue(flushLatch.await(1, TimeUnit.SECONDS));
        writable.set(true);
        connectionResourceLock.doNotify();
        awaitThread.join(1000L);
        assertFalse(awaitThread.isAlive());
        verify(context, atLeastOnce()).flush();
        verify(channel, atLeast(2)).isWritable();
        assertTrue(channel.isActive());
    }
}
