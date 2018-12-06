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
import io.shardingsphere.shardingproxy.frontend.mysql.CommandExecuteEngine;
import io.shardingsphere.shardingproxy.frontend.mysql.CommandExecutor;
import io.shardingsphere.shardingproxy.frontend.mysql.CommandExecutorContext;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.junit.After;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public final class ExecutorGroupTest {
    
    private TransactionType originalTransactionType = TransactionType.LOCAL;
    
    private final Map<String, Boolean> assertMap = new HashMap<>();
    
    @After
    public void tearDown() throws ReflectiveOperationException {
        setTransactionType(originalTransactionType);
    }
    
    @Test
    public void assertGetExecutorServiceWithLocal() throws ReflectiveOperationException {
        setTransactionType(TransactionType.LOCAL);
        CommandExecutor commandExecutor = mock(CommandExecutor.class);
        ChannelId channelId = mock(ChannelId.class);
        CommandExecuteEngine commandExecuteEngine = mock(CommandExecuteEngine.class);
        assertMap.put("assert", false);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) {
                assertMap.put("assert", true);
                return null;
            }
        }).when(commandExecuteEngine).execute(commandExecutor);
        setCommandExecuteEngine(commandExecuteEngine);
        new ExecutorGroup(channelId).execute(commandExecutor);
        assertTrue("Use command execute engine.", assertMap.get("assert"));
        assertMap.clear();
    }
    
    @Test
    public void assertGetExecutorServiceWithXA() throws ReflectiveOperationException {
        setTransactionType(TransactionType.XA);
        CommandExecutor commandExecutor = mock(CommandExecutor.class);
        final ExecutorService executorService = mock(ExecutorService.class);
        ChannelId channelId = mock(ChannelId.class);
        assertMap.put("assert", false);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) {
                assertMap.put("assert", true);
                return null;
            }
        }).when(executorService).execute(commandExecutor);
        setExecuteService(channelId, executorService);
        new ExecutorGroup(channelId).execute(commandExecutor);
        ChannelThreadExecutorGroup.getInstance().unregister(channelId);
        assertTrue("Use single executor to execute.", assertMap.get("assert"));
        assertMap.clear();
    }
    
    private void setCommandExecuteEngine(CommandExecuteEngine commandExecuteEngine) throws ReflectiveOperationException {
        Field field = CommandExecutorContext.getInstance().getClass().getDeclaredField("commandExecuteEngine");
        field.setAccessible(true);
        Field modifiers = field.getClass().getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(CommandExecutorContext.getInstance(), commandExecuteEngine);
    }
    
    private void setExecuteService(final ChannelId channelId, final ExecutorService executorService) throws ReflectiveOperationException {
        Field field = ChannelThreadExecutorGroup.getInstance().getClass().getDeclaredField("executorServices");
        field.setAccessible(true);
        Map<ChannelId, ExecutorService> executorServices = (Map<ChannelId, ExecutorService>) field.get(ChannelThreadExecutorGroup.getInstance());
        executorServices.put(channelId, executorService);
        field.set(ChannelThreadExecutorGroup.getInstance(), executorServices);
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
