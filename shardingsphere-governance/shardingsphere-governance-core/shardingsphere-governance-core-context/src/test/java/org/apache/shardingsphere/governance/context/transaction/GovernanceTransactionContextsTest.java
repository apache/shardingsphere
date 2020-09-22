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

package org.apache.shardingsphere.governance.context.transaction;

import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.core.event.model.datasource.DataSourceChangeCompletedEvent;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class GovernanceTransactionContextsTest {
    
    @Mock
    private TransactionContexts transactionContexts;
    
    @Mock
    private ShardingTransactionManagerEngine engine;
    
    @Mock
    private Map<String, ShardingTransactionManagerEngine> engineMap;
    
    @Mock
    private DataSourceChangeCompletedEvent event;
    
    @Test
    public void assertNewInstance() {
        when(transactionContexts.getDefaultTransactionManagerEngine()).thenReturn(engine);
        when(transactionContexts.getEngines()).thenReturn(Collections.singletonMap("name", engine));
        GovernanceTransactionContexts actual = new GovernanceTransactionContexts(transactionContexts);
        assertThat(actual.getEngines(), is(Collections.singletonMap("name", engine)));
        assertThat(actual.getDefaultTransactionManagerEngine(), is(engine));
    }
    
    @Test
    @SneakyThrows
    public void assertClose() {
        GovernanceTransactionContexts actual = new GovernanceTransactionContexts(transactionContexts);
        actual.close();
        verify(transactionContexts).close();
    }
    
    @Test
    @SneakyThrows
    public void assertRenew() {
        when(event.getSchemaName()).thenReturn("name");
        when(transactionContexts.getEngines()).thenReturn(engineMap);
        when(engineMap.remove(eq("name"))).thenReturn(engine);
        GovernanceTransactionContexts actual = new GovernanceTransactionContexts(transactionContexts);
        actual.renew(event);
        verify(engine).close();
        verify(engineMap).put(eq("name"), any(ShardingTransactionManagerEngine.class));
    }
}
