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

import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.core.fixture.ShardingSphereTransactionManagerFixture;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ShardingSphereTransactionManagerEngineTest {
    
    private final ShardingSphereTransactionManagerEngine transactionManagerEngine = new ShardingSphereTransactionManagerEngine(TransactionType.XA);
    
    @Test
    void assertGetEngine() {
        assertThat(transactionManagerEngine.getTransactionManager(TransactionType.XA), instanceOf(ShardingSphereTransactionManagerFixture.class));
    }
    
    @Test
    void assertRegisterTransactionResource() {
        Runnable caller = mock(Runnable.class);
        ShardingSphereTransactionManagerFixture transactionManager = (ShardingSphereTransactionManagerFixture) transactionManagerEngine.getTransactionManager(TransactionType.XA);
        transactionManager.setCaller(caller);
        transactionManagerEngine.init(Collections.singletonMap("sharding_db.ds_0", TypedSPILoader.getService(DatabaseType.class, "H2")), Collections.emptyMap(), "Atomikos");
        verify(caller).run();
    }
}
