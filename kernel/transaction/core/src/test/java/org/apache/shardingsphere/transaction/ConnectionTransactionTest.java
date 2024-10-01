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

import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.transaction.ConnectionTransaction.DistributedTransactionOperationType;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.spi.ShardingSphereDistributionTransactionManager;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConnectionTransactionTest {
    
    @Test
    void assertIsNotInTransactionWhenTransactionIsNotBegin() {
        TransactionConnectionContext context = new TransactionConnectionContext();
        assertFalse(new ConnectionTransaction(mock(TransactionRule.class, RETURNS_DEEP_STUBS), context).isInTransaction(context));
    }
    
    @Test
    void assertIsNotInTransactionWhenIsNotDistributionTransaction() {
        TransactionConnectionContext context = new TransactionConnectionContext();
        context.beginTransaction("LOCAL");
        assertFalse(new ConnectionTransaction(mock(TransactionRule.class), context).isInTransaction(context));
    }
    
    @Test
    void assertIsNotInTransactionWhenDistributionTransactionIsNotBegin() {
        TransactionConnectionContext context = new TransactionConnectionContext();
        context.beginTransaction("XA");
        assertFalse(new ConnectionTransaction(mock(TransactionRule.class, RETURNS_DEEP_STUBS), context).isInTransaction(context));
    }
    
    @Test
    void assertIsInTransaction() {
        TransactionConnectionContext context = new TransactionConnectionContext();
        context.beginTransaction("XA");
        TransactionRule rule = mock(TransactionRule.class, RETURNS_DEEP_STUBS);
        when(rule.getResource().getTransactionManager(rule.getDefaultType()).isInTransaction()).thenReturn(true);
        assertTrue(new ConnectionTransaction(rule, context).isInTransaction(context));
    }
    
    @Test
    void assertIsLocalTransaction() {
        TransactionRule rule = mock(TransactionRule.class);
        when(rule.getDefaultType()).thenReturn(TransactionType.LOCAL);
        assertTrue(new ConnectionTransaction(rule, new TransactionConnectionContext()).isLocalTransaction());
    }
    
    @Test
    void assertIsNotLocalTransaction() {
        TransactionRule rule = mock(TransactionRule.class, RETURNS_DEEP_STUBS);
        when(rule.getDefaultType()).thenReturn(TransactionType.XA);
        assertFalse(new ConnectionTransaction(rule, new TransactionConnectionContext()).isLocalTransaction());
    }
    
    @Test
    void assertIsHoldTransactionWithLocalAndNotAutoCommit() {
        TransactionRule rule = mock(TransactionRule.class);
        when(rule.getDefaultType()).thenReturn(TransactionType.LOCAL);
        assertTrue(new ConnectionTransaction(rule, new TransactionConnectionContext()).isHoldTransaction(false));
    }
    
    @Test
    void assertIsHoldTransactionWithXAAndAutoCommit() {
        TransactionRule rule = mock(TransactionRule.class, RETURNS_DEEP_STUBS);
        when(rule.getDefaultType()).thenReturn(TransactionType.XA);
        when(rule.getResource().getTransactionManager(TransactionType.XA).isInTransaction()).thenReturn(true);
        TransactionConnectionContext context = new TransactionConnectionContext();
        context.beginTransaction("XA");
        assertTrue(new ConnectionTransaction(rule, context).isHoldTransaction(true));
    }
    
    @Test
    void assertBegin() {
        ShardingSphereDistributionTransactionManager distributionTransactionManager = mock(ShardingSphereDistributionTransactionManager.class);
        TransactionRule rule = mock(TransactionRule.class, RETURNS_DEEP_STUBS);
        when(rule.getResource().getTransactionManager(rule.getDefaultType())).thenReturn(distributionTransactionManager);
        new ConnectionTransaction(rule, new TransactionConnectionContext()).begin();
        verify(distributionTransactionManager).begin();
    }
    
    @Test
    void assertCommit() {
        ShardingSphereDistributionTransactionManager distributionTransactionManager = mock(ShardingSphereDistributionTransactionManager.class);
        TransactionRule rule = mock(TransactionRule.class, RETURNS_DEEP_STUBS);
        when(rule.getResource().getTransactionManager(rule.getDefaultType())).thenReturn(distributionTransactionManager);
        new ConnectionTransaction(rule, new TransactionConnectionContext()).commit();
        verify(distributionTransactionManager).commit(false);
    }
    
    @Test
    void assertRollback() {
        ShardingSphereDistributionTransactionManager distributionTransactionManager = mock(ShardingSphereDistributionTransactionManager.class);
        TransactionRule rule = mock(TransactionRule.class, RETURNS_DEEP_STUBS);
        when(rule.getResource().getTransactionManager(rule.getDefaultType())).thenReturn(distributionTransactionManager);
        new ConnectionTransaction(rule, new TransactionConnectionContext()).rollback();
        verify(distributionTransactionManager).rollback();
    }
    
    @Test
    void assertIsHoldTransactionWithLocalAndAutoCommit() {
        TransactionRule rule = mock(TransactionRule.class);
        when(rule.getDefaultType()).thenReturn(TransactionType.LOCAL);
        assertFalse(new ConnectionTransaction(rule, new TransactionConnectionContext()).isHoldTransaction(true));
    }
    
    @Test
    void assertGetConnectionWithoutInDistributeTransaction() throws SQLException {
        TransactionRule rule = mock(TransactionRule.class);
        when(rule.getDefaultType()).thenReturn(TransactionType.LOCAL);
        TransactionConnectionContext context = new TransactionConnectionContext();
        assertFalse(new ConnectionTransaction(rule, context).getConnection("foo_db", "foo_ds", context).isPresent());
    }
    
    @Test
    void assertGetConnectionWithInDistributeTransaction() throws SQLException {
        TransactionConnectionContext context = new TransactionConnectionContext();
        context.beginTransaction("XA");
        TransactionRule rule = mock(TransactionRule.class, RETURNS_DEEP_STUBS);
        when(rule.getResource().getTransactionManager(rule.getDefaultType()).isInTransaction()).thenReturn(true);
        when(rule.getResource().getTransactionManager(rule.getDefaultType()).getConnection("foo_db", "foo_ds")).thenReturn(mock(Connection.class));
        assertTrue(new ConnectionTransaction(rule, context).getConnection("foo_db", "foo_ds", context).isPresent());
    }
    
    @Test
    void assertGetDistributedTransactionBeginOperationType() {
        assertThat(new ConnectionTransaction(mock(TransactionRule.class, RETURNS_DEEP_STUBS), new TransactionConnectionContext()).getDistributedTransactionOperationType(false),
                is(DistributedTransactionOperationType.BEGIN));
    }
    
    @Test
    void assertGetDistributedTransactionCommitOperationType() {
        TransactionRule rule = mock(TransactionRule.class, RETURNS_DEEP_STUBS);
        when(rule.getResource().getTransactionManager(rule.getDefaultType()).isInTransaction()).thenReturn(true);
        assertThat(new ConnectionTransaction(rule, new TransactionConnectionContext()).getDistributedTransactionOperationType(true), is(DistributedTransactionOperationType.COMMIT));
    }
    
    @Test
    void assertDistributedTransactionIgnoreOperationTypeWhenIsAutoCommit() {
        TransactionRule rule = mock(TransactionRule.class, RETURNS_DEEP_STUBS);
        when(rule.getResource().getTransactionManager(rule.getDefaultType()).isInTransaction()).thenReturn(true);
        assertThat(new ConnectionTransaction(rule, new TransactionConnectionContext()).getDistributedTransactionOperationType(false), is(DistributedTransactionOperationType.IGNORE));
    }
    
    @Test
    void assertDistributedTransactionIgnoreOperationTypeWhenIsNotInDistributedTransaction() {
        ConnectionTransaction connectionTransaction = new ConnectionTransaction(mock(TransactionRule.class, RETURNS_DEEP_STUBS), new TransactionConnectionContext());
        assertThat(connectionTransaction.getDistributedTransactionOperationType(true), is(DistributedTransactionOperationType.IGNORE));
    }
}
