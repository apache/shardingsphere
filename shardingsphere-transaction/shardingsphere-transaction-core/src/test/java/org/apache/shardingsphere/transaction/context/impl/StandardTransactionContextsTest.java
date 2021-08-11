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

package org.apache.shardingsphere.transaction.context.impl;

import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class StandardTransactionContextsTest {
    
    @Test
    public void assertNewInstanceWithEmptyEngines() {
        TransactionContexts transactionContexts = new StandardTransactionContexts();
        Map<String, ShardingTransactionManagerEngine> engines = transactionContexts.getEngines();
        assertTrue(engines.isEmpty());
    }
    
    @Test
    public void assertGetDefaultTransactionManagerEngine() {
        Map<String, ShardingTransactionManagerEngine> actualEngines = Collections.singletonMap(DefaultSchema.LOGIC_NAME, new ShardingTransactionManagerEngine());
        TransactionContexts transactionContexts = new StandardTransactionContexts(actualEngines);
        Map<String, ShardingTransactionManagerEngine> engines = transactionContexts.getEngines();
        assertThat(engines.size(), is(1));
        assertThat(engines, is(actualEngines));
        ShardingTransactionManagerEngine defaultEngine = transactionContexts.getEngines().get(DefaultSchema.LOGIC_NAME);
        assertNotNull(defaultEngine);
    }
    
    @Test
    public void assertClose() throws Exception {
        ShardingTransactionManagerEngine shardingTransactionManagerEngine = mock(ShardingTransactionManagerEngine.class);
        TransactionContexts transactionContexts = new StandardTransactionContexts(Collections.singletonMap(DefaultSchema.LOGIC_NAME, shardingTransactionManagerEngine));
        transactionContexts.close();
        verify(shardingTransactionManagerEngine).close();
    }
    
    @Test(expected = Exception.class)
    public void assertCloseThrowsException() throws Exception {
        ShardingTransactionManagerEngine shardingTransactionManagerEngine = mock(ShardingTransactionManagerEngine.class);
        doThrow(new RuntimeException("")).when(shardingTransactionManagerEngine).close();
        TransactionContexts transactionContexts = new StandardTransactionContexts(Collections.singletonMap(DefaultSchema.LOGIC_NAME, shardingTransactionManagerEngine));
        transactionContexts.close();
    }
}
