/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.frontend.common.executor;

import io.netty.channel.ChannelId;
import io.netty.channel.EventLoopGroup;
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ExecutorGroupTest {
    
    private TransactionType originalTransactionType = TransactionType.LOCAL;
    
    @After
    public void tearDown() throws ReflectiveOperationException {
        setTransactionType(originalTransactionType);
    }
    
    @Test
    public void assertGetExecutorServiceWithLocal() throws ReflectiveOperationException {
        setTransactionType(TransactionType.LOCAL);
        EventLoopGroup eventLoopGroup = mock(EventLoopGroup.class);
        ChannelId channelId = mock(ChannelId.class);
        assertThat(new ExecutorGroup(eventLoopGroup, channelId).getExecutorService(), CoreMatchers.<ExecutorService>is(eventLoopGroup));
    }
    
    @Test
    public void assertGetExecutorServiceWithXA() throws ReflectiveOperationException {
        setTransactionType(TransactionType.XA);
        EventLoopGroup eventLoopGroup = mock(EventLoopGroup.class);
        ChannelId channelId = mock(ChannelId.class);
        ChannelThreadExecutorGroup.getInstance().register(channelId);
        assertThat(new ExecutorGroup(eventLoopGroup, channelId).getExecutorService(), Matchers.<ExecutorService>not(eventLoopGroup));
        assertNotNull(new ExecutorGroup(eventLoopGroup, channelId).getExecutorService());
        ChannelThreadExecutorGroup.getInstance().unregister(channelId);
    }
    
    private void setTransactionType(final TransactionType transactionType) throws ReflectiveOperationException {
        Field field = GlobalRegistry.getInstance().getClass().getDeclaredField("shardingProperties");
        field.setAccessible(true);
        field.set(GlobalRegistry.getInstance(), getShardingProperties(transactionType));
    }
    
    private ShardingProperties getShardingProperties(final TransactionType transactionType) {
        Properties props = new Properties();
        props.setProperty(ShardingPropertiesConstant.PROXY_TRANSACTION_ENABLED.getKey(), String.valueOf(transactionType == TransactionType.XA));
        return new ShardingProperties(props);
    }
}
