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

package org.apache.shardingsphere.proxy.frontend.executor;

import io.netty.channel.ChannelId;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class CommandExecutorSelectorTest {
    
    @Test
    public void assertGetExecutorServiceWithLocal() {
        ChannelId channelId = mock(ChannelId.class);
        assertThat(CommandExecutorSelector.getExecutor(false, false, TransactionType.LOCAL, channelId), instanceOf(ExecutorService.class));
    }
    
    @Test
    public void assertGetExecutorServiceWithOccupyThreadForPerConnection() {
        ChannelId channelId = mock(ChannelId.class);
        ChannelThreadExecutorGroup.getInstance().register(channelId);
        assertThat(CommandExecutorSelector.getExecutor(true, false, TransactionType.LOCAL, channelId), instanceOf(ExecutorService.class));
    }
    
    @Test
    public void assertGetExecutorServiceWithXA() {
        ChannelId channelId = mock(ChannelId.class);
        ChannelThreadExecutorGroup.getInstance().register(channelId);
        assertThat(CommandExecutorSelector.getExecutor(false, false, TransactionType.XA, channelId), instanceOf(ExecutorService.class));
    }
    
    @Test
    public void assertGetExecutorServiceWithBASE() {
        ChannelId channelId = mock(ChannelId.class);
        ChannelThreadExecutorGroup.getInstance().register(channelId);
        assertThat(CommandExecutorSelector.getExecutor(false, false, TransactionType.BASE, channelId), instanceOf(ExecutorService.class));
    }
}
