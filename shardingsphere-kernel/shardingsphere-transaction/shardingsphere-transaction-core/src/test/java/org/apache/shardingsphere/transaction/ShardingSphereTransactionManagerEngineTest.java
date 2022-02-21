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

import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.fixture.ShardingSphereTransactionManagerFixture;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class ShardingSphereTransactionManagerEngineTest {
    
    private final ShardingSphereTransactionManagerEngine transactionManagerEngine = new ShardingSphereTransactionManagerEngine();
    
    @Test
    public void assertGetEngine() {
        assertThat(transactionManagerEngine.getTransactionManager(TransactionType.XA), instanceOf(ShardingSphereTransactionManagerFixture.class));
    }
    
    @Test
    public void assertRegisterTransactionResource() {
        Runnable caller = mock(Runnable.class);
        ShardingSphereTransactionManagerFixture transactionManager = (ShardingSphereTransactionManagerFixture) transactionManagerEngine.getTransactionManager(TransactionType.XA);
        transactionManager.setCaller(caller);
        TransactionRule transactionRule = new TransactionRule(new TransactionRuleConfiguration("XA", "Atomikos", new Properties()));
        transactionManagerEngine.init(DatabaseTypeRegistry.getActualDatabaseType("H2"), Collections.emptyMap(), transactionRule);
        verify(caller).run();
    }
}
