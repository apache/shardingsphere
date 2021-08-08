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

import org.apache.shardingsphere.governance.core.registry.config.event.datasource.DataSourceChangeCompletedEvent;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.core.XATransactionManagerType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class GovernanceTransactionContextsTest {
    
    @Mock
    private TransactionContexts transactionContexts;
    
    @Mock
    private ShardingTransactionManagerEngine engine;
    
    @Mock
    private Map<String, ShardingTransactionManagerEngine> engines;
    
    @Test
    public void assertRenew() throws Exception {
        DataSourceChangeCompletedEvent event = new DataSourceChangeCompletedEvent("name", mock(DatabaseType.class), Collections.emptyMap());
        when(transactionContexts.getEngines()).thenReturn(engines);
        when(engines.remove("name")).thenReturn(engine);
        new GovernanceTransactionContexts(transactionContexts, XATransactionManagerType.ATOMIKOS.getType()).renew(event);
        verify(engine).close();
        verify(engines).put(eq("name"), any(ShardingTransactionManagerEngine.class));
    }
}
