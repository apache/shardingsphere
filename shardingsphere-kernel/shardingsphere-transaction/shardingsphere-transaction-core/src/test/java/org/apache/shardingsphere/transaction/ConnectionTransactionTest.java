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

import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.transaction.ConnectionTransaction.DistributedTransactionOperationType;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThat;

public final class ConnectionTransactionTest {
    
    private ConnectionTransaction connectionTransaction;

    @Test
    public void assertDistributedTransactionOperationTypeCommit() throws Exception {
        Map<String, ShardingSphereTransactionManagerEngine> actualEngines = Collections.singletonMap(DefaultSchema.LOGIC_NAME, new ShardingSphereTransactionManagerEngine());
        TransactionContexts transactionContexts = new TransactionContexts(actualEngines);
        connectionTransaction = new ConnectionTransaction(DefaultSchema.LOGIC_NAME, new TransactionRule(new TransactionRuleConfiguration("XA", "Atomikos")), transactionContexts);
        DistributedTransactionOperationType operationType = connectionTransaction.getDistributedTransactionOperationType(true);
        assertThat(operationType, equalTo(DistributedTransactionOperationType.COMMIT));
    }
    
    @Test
    public void assertDistributedTransactionOperationTypeIgnore() {
        Map<String, ShardingSphereTransactionManagerEngine> actualEngines = Collections.singletonMap(DefaultSchema.LOGIC_NAME, new ShardingSphereTransactionManagerEngine());
        TransactionContexts transactionContexts = new TransactionContexts(actualEngines);
        connectionTransaction = new ConnectionTransaction(DefaultSchema.LOGIC_NAME, new TransactionRule(new TransactionRuleConfiguration("XA", "Atomikos")), transactionContexts);
        DistributedTransactionOperationType operationType = connectionTransaction.getDistributedTransactionOperationType(false);
        assertThat(operationType, equalTo(DistributedTransactionOperationType.IGNORE));
    }

    @Test
    public void assertIsLocalTransaction() {
        Map<String, ShardingSphereTransactionManagerEngine> actualEngines = Collections.singletonMap(DefaultSchema.LOGIC_NAME, new ShardingSphereTransactionManagerEngine());
        TransactionContexts transactionContexts = new TransactionContexts(actualEngines);
        connectionTransaction = new ConnectionTransaction(DefaultSchema.LOGIC_NAME,new TransactionRule(new TransactionRuleConfiguration("LOCAL","Atomikos")),transactionContexts);
        assertTrue(connectionTransaction.isLocalTransaction());
        connectionTransaction = new ConnectionTransaction(DefaultSchema.LOGIC_NAME,new TransactionRule(new TransactionRuleConfiguration("XA","Atomikos")),transactionContexts);
        assertFalse(connectionTransaction.isLocalTransaction());
    }

    @Test
    public void assertIsHoldTransaction() {
        Map<String, ShardingSphereTransactionManagerEngine> actualEngines = Collections.singletonMap(DefaultSchema.LOGIC_NAME, new ShardingSphereTransactionManagerEngine());
        TransactionContexts transactionContexts = new TransactionContexts(actualEngines);
        connectionTransaction = new ConnectionTransaction(DefaultSchema.LOGIC_NAME,new TransactionRule(new TransactionRuleConfiguration("LOCAL","Atomikos")),transactionContexts);
        assertTrue(connectionTransaction.isHoldTransaction(false));
        connectionTransaction = new ConnectionTransaction(DefaultSchema.LOGIC_NAME,new TransactionRule(new TransactionRuleConfiguration("XA","Atomikos")),transactionContexts);
        assertTrue(connectionTransaction.isInTransaction());
        assertTrue(connectionTransaction.isHoldTransaction(true));
        connectionTransaction = new ConnectionTransaction(DefaultSchema.LOGIC_NAME,new TransactionRule(new TransactionRuleConfiguration("LOCAL","Atomikos")),transactionContexts);
        assertFalse(connectionTransaction.isHoldTransaction(true));
    }
}
