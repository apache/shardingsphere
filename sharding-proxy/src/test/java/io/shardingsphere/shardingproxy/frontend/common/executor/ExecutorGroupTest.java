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
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.shardingproxy.frontend.ShardingProxy;
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
        ExecutorService commandExecutorService = mock(ExecutorService.class);
        setShardingProxyCommandExecutorService(commandExecutorService);
        ChannelId channelId = mock(ChannelId.class);
        assertThat(new ExecutorGroup(channelId).getExecutorService(), CoreMatchers.is(commandExecutorService));
    }
    
    @Test
    public void assertGetExecutorServiceWithXA() throws ReflectiveOperationException {
        setTransactionType(TransactionType.XA);
        ExecutorService commandExecutorService = mock(ExecutorService.class);
        setShardingProxyCommandExecutorService(commandExecutorService);
        ChannelId channelId = mock(ChannelId.class);
        ChannelThreadExecutorGroup.getInstance().register(channelId);
        assertThat(new ExecutorGroup(channelId).getExecutorService(), Matchers.not(commandExecutorService));
        assertNotNull(new ExecutorGroup(channelId).getExecutorService());
        ChannelThreadExecutorGroup.getInstance().unregister(channelId);
    }
    
    private void setShardingProxyCommandExecutorService(final ExecutorService commandExecutorService) throws ReflectiveOperationException {
        Field field = ShardingProxy.getInstance().getClass().getDeclaredField("commandExecutorService");
        field.setAccessible(true);
        field.set(ShardingProxy.getInstance(), commandExecutorService);
    }
    
    private void setTransactionType(final TransactionType transactionType) throws ReflectiveOperationException {
        Field field = GlobalRegistry.getInstance().getClass().getDeclaredField("shardingProperties");
        field.setAccessible(true);
        field.set(GlobalRegistry.getInstance(), getShardingProperties(transactionType));
    }
    
    private ShardingProperties getShardingProperties(final TransactionType transactionType) {
        Properties props = new Properties();
        props.setProperty(ShardingPropertiesConstant.PROXY_TRANSACTION_TYPE.getKey(), transactionType.name());
        return new ShardingProperties(props);
    }
}
