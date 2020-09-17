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
import org.junit.Test;

import java.util.HashMap;
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
        StandardTransactionContexts standardTransactionContexts = new StandardTransactionContexts();
        Map<String, ShardingTransactionManagerEngine> engines = standardTransactionContexts.getEngines();
        assertTrue(engines.isEmpty());
    }
    
    @Test
    public void assertGetDefaultTransactionManagerEngine() {
        Map<String, ShardingTransactionManagerEngine> actualEngine = new HashMap<>();
        actualEngine.put(DefaultSchema.LOGIC_NAME, new ShardingTransactionManagerEngine());
        StandardTransactionContexts standardTransactionContexts = new StandardTransactionContexts(actualEngine);
        Map<String, ShardingTransactionManagerEngine> engines = standardTransactionContexts.getEngines();
        assertThat(engines.size(), is(1));
        assertThat(engines, is(actualEngine));
        ShardingTransactionManagerEngine defaultEngine = standardTransactionContexts.getDefaultTransactionManagerEngine();
        assertNotNull(defaultEngine);
    }
    
    @Test
    public void assertClose() throws Exception {
        ShardingTransactionManagerEngine shardingTransactionManagerEngine = mock(ShardingTransactionManagerEngine.class);
        Map<String, ShardingTransactionManagerEngine> actualEngine = new HashMap<>();
        actualEngine.put(DefaultSchema.LOGIC_NAME, shardingTransactionManagerEngine);
        StandardTransactionContexts standardTransactionContexts = new StandardTransactionContexts(actualEngine);
        standardTransactionContexts.close();
        verify(shardingTransactionManagerEngine).close();
    }
    
    @Test(expected = Exception.class)
    public void assertCloseThrowsException() throws Exception {
        ShardingTransactionManagerEngine shardingTransactionManagerEngine = mock(ShardingTransactionManagerEngine.class);
        doThrow(new RuntimeException("")).when(shardingTransactionManagerEngine).close();
        Map<String, ShardingTransactionManagerEngine> actualEngine = new HashMap<>();
        actualEngine.put(DefaultSchema.LOGIC_NAME, shardingTransactionManagerEngine);
        StandardTransactionContexts standardTransactionContexts = new StandardTransactionContexts(actualEngine);
        standardTransactionContexts.close();
    }
}
