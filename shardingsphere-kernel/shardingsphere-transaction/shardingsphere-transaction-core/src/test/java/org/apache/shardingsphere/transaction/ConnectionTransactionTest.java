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

import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.transaction.ConnectionTransaction.DistributedTransactionOperationType;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class ConnectionTransactionTest {
    
    private ConnectionTransaction connectionTransaction;
    
    @Test
    public void assertDistributedTransactionOperationTypeCommit() {
        connectionTransaction = new ConnectionTransaction(DefaultDatabase.LOGIC_NAME, getXATransactionRule());
        DistributedTransactionOperationType operationType = connectionTransaction.getDistributedTransactionOperationType(true);
        assertThat(operationType, is(DistributedTransactionOperationType.COMMIT));
    }
    
    @Test
    public void assertDistributedTransactionOperationTypeIgnore() {
        connectionTransaction = new ConnectionTransaction(DefaultDatabase.LOGIC_NAME, getXATransactionRule());
        DistributedTransactionOperationType operationType = connectionTransaction.getDistributedTransactionOperationType(false);
        assertThat(operationType, is(DistributedTransactionOperationType.IGNORE));
    }
    
    @Test
    public void assertIsLocalTransaction() {
        connectionTransaction = new ConnectionTransaction(DefaultDatabase.LOGIC_NAME, getLocalTransactionRule());
        assertTrue(connectionTransaction.isLocalTransaction());
        connectionTransaction = new ConnectionTransaction(DefaultDatabase.LOGIC_NAME, getXATransactionRule());
        assertFalse(connectionTransaction.isLocalTransaction());
    }
    
    @Test
    public void assertIsHoldTransaction() {
        connectionTransaction = new ConnectionTransaction(DefaultDatabase.LOGIC_NAME, getLocalTransactionRule());
        assertTrue(connectionTransaction.isHoldTransaction(false));
        connectionTransaction = new ConnectionTransaction(DefaultDatabase.LOGIC_NAME, getXATransactionRule());
        assertTrue(connectionTransaction.isInTransaction());
        assertTrue(connectionTransaction.isHoldTransaction(true));
        connectionTransaction = new ConnectionTransaction(DefaultDatabase.LOGIC_NAME, getLocalTransactionRule());
        assertFalse(connectionTransaction.isHoldTransaction(true));
    }
    
    private TransactionRule getLocalTransactionRule() {
        return new TransactionRule(new TransactionRuleConfiguration("LOCAL", null, new Properties()), Collections.emptyMap(), mock(InstanceContext.class));
    }
    
    private TransactionRule getXATransactionRule() {
        return new TransactionRule(new TransactionRuleConfiguration("XA", "Atomikos", new Properties()), Collections.emptyMap(), mock(InstanceContext.class));
    }
}
