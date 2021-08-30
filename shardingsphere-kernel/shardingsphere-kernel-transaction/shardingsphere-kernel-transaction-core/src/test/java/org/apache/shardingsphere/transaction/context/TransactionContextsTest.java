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

package org.apache.shardingsphere.transaction.context;

import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
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

public final class TransactionContextsTest {
    
    @Test
    public void assertNewInstanceWithEmptyEngines() {
        TransactionContexts transactionContexts = new TransactionContexts();
        Map<String, ShardingSphereTransactionManagerEngine> engines = transactionContexts.getEngines();
        assertTrue(engines.isEmpty());
    }
    
    @Test
    public void assertGetDefaultTransactionManagerEngine() {
        Map<String, ShardingSphereTransactionManagerEngine> actualEngines = Collections.singletonMap(DefaultSchema.LOGIC_NAME, new ShardingSphereTransactionManagerEngine());
        TransactionContexts transactionContexts = new TransactionContexts(actualEngines);
        Map<String, ShardingSphereTransactionManagerEngine> engines = transactionContexts.getEngines();
        assertThat(engines.size(), is(1));
        assertThat(engines, is(actualEngines));
        ShardingSphereTransactionManagerEngine defaultEngine = transactionContexts.getEngines().get(DefaultSchema.LOGIC_NAME);
        assertNotNull(defaultEngine);
    }
    
    @Test
    public void assertClose() throws Exception {
        ShardingSphereTransactionManagerEngine shardingSphereTransactionManagerEngine = mock(ShardingSphereTransactionManagerEngine.class);
        TransactionContexts transactionContexts = new TransactionContexts(Collections.singletonMap(DefaultSchema.LOGIC_NAME, shardingSphereTransactionManagerEngine));
        transactionContexts.close();
        verify(shardingSphereTransactionManagerEngine).close();
    }
    
    @Test(expected = Exception.class)
    public void assertCloseThrowsException() throws Exception {
        ShardingSphereTransactionManagerEngine shardingSphereTransactionManagerEngine = mock(ShardingSphereTransactionManagerEngine.class);
        doThrow(new RuntimeException("")).when(shardingSphereTransactionManagerEngine).close();
        TransactionContexts transactionContexts = new TransactionContexts(Collections.singletonMap(DefaultSchema.LOGIC_NAME, shardingSphereTransactionManagerEngine));
        transactionContexts.close();
    }
}
