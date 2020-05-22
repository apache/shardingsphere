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

package org.apache.shardingsphere.transaction;

import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.fixture.ShardingTransactionManagerFixture;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class ShardingTransactionManagerEngineTest {
    
    private final ShardingTransactionManagerEngine shardingTransactionManagerEngine = new ShardingTransactionManagerEngine();
    
    @Test
    public void assertGetEngine() {
        assertThat(shardingTransactionManagerEngine.getTransactionManager(TransactionType.XA), instanceOf(ShardingTransactionManagerFixture.class));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertRegisterTransactionResource() {
        Runnable caller = mock(Runnable.class);
        ShardingTransactionManagerFixture shardingTransactionManager = (ShardingTransactionManagerFixture) shardingTransactionManagerEngine.getTransactionManager(TransactionType.XA);
        shardingTransactionManager.setCaller(caller);
        shardingTransactionManagerEngine.init(DatabaseTypes.getActualDatabaseType("H2"), mock(Map.class));
        verify(caller).run();
    }
}
