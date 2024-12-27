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
import org.apache.shardingsphere.transaction.spi.ShardingSphereDistributedTransactionManager;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

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
    void assertIsNotInDistributedTransactionWhenTransactionIsNotBegin() {
        TransactionConnectionContext context = new TransactionConnectionContext();
        assertFalse(new ConnectionTransaction(mock(TransactionRule.class, RETURNS_DEEP_STUBS), context).isInDistributedTransaction(context));
    }
    
    @Test
    void assertIsNotInDistributedTransactionWhenIsNotDistributedTransaction() {
        TransactionConnectionContext context = new TransactionConnectionContext();
        context.beginTransaction("LOCAL", mock(ShardingSphereDistributedTransactionManager.class));
        assertFalse(new ConnectionTransaction(mock(TransactionRule.class), context).isInDistributedTransaction(context));
    }
    
    @Test
    void assertIsNotInDistributedTransactionWhenDistributedTransactionIsNotBegin() {
        TransactionConnectionContext context = new TransactionConnectionContext();
        context.beginTransaction("XA", mock(ShardingSphereDistributedTransactionManager.class));
        assertFalse(new ConnectionTransaction(mock(TransactionRule.class, RETURNS_DEEP_STUBS), context).isInDistributedTransaction(context));
    }
    
    @Test
    void assertIsInDistributedTransaction() {
        TransactionConnectionContext context = new TransactionConnectionContext();
        ShardingSphereDistributedTransactionManager distributedTransactionManager = mock(ShardingSphereDistributedTransactionManager.class);
        when(distributedTransactionManager.isInTransaction()).thenReturn(true);
        context.beginTransaction("XA", distributedTransactionManager);
        TransactionRule rule = mock(TransactionRule.class, RETURNS_DEEP_STUBS);
        when(rule.getResource().getTransactionManager(rule.getDefaultType()).isInTransaction()).thenReturn(true);
        assertTrue(new ConnectionTransaction(rule, context).isInDistributedTransaction(context));
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
        ShardingSphereDistributedTransactionManager distributedTransactionManager = mock(ShardingSphereDistributedTransactionManager.class);
        when(distributedTransactionManager.isInTransaction()).thenReturn(true);
        context.beginTransaction("XA", distributedTransactionManager);
        assertTrue(new ConnectionTransaction(rule, context).isHoldTransaction(true));
    }
    
    @Test
    void assertBegin() {
        ShardingSphereDistributedTransactionManager distributedTransactionManager = mock(ShardingSphereDistributedTransactionManager.class);
        TransactionRule rule = mock(TransactionRule.class, RETURNS_DEEP_STUBS);
        when(rule.getResource().getTransactionManager(rule.getDefaultType())).thenReturn(distributedTransactionManager);
        new ConnectionTransaction(rule, new TransactionConnectionContext()).begin();
        verify(distributedTransactionManager).begin();
    }
    
    @Test
    void assertCommit() {
        ShardingSphereDistributedTransactionManager distributedTransactionManager = mock(ShardingSphereDistributedTransactionManager.class);
        TransactionRule rule = mock(TransactionRule.class, RETURNS_DEEP_STUBS);
        when(rule.getResource().getTransactionManager(rule.getDefaultType())).thenReturn(distributedTransactionManager);
        new ConnectionTransaction(rule, new TransactionConnectionContext()).commit();
        verify(distributedTransactionManager).commit(false);
    }
    
    @Test
    void assertRollback() {
        ShardingSphereDistributedTransactionManager distributedTransactionManager = mock(ShardingSphereDistributedTransactionManager.class);
        TransactionRule rule = mock(TransactionRule.class, RETURNS_DEEP_STUBS);
        when(rule.getResource().getTransactionManager(rule.getDefaultType())).thenReturn(distributedTransactionManager);
        new ConnectionTransaction(rule, new TransactionConnectionContext()).rollback();
        verify(distributedTransactionManager).rollback();
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
        ShardingSphereDistributedTransactionManager distributedTransactionManager = mock(ShardingSphereDistributedTransactionManager.class);
        when(distributedTransactionManager.isInTransaction()).thenReturn(true);
        when(distributedTransactionManager.getConnection("foo_db", "foo_ds")).thenReturn(mock(Connection.class));
        context.beginTransaction("XA", distributedTransactionManager);
        TransactionRule rule = mock(TransactionRule.class, RETURNS_DEEP_STUBS);
        when(rule.getResource().getTransactionManager(rule.getDefaultType()).isInTransaction()).thenReturn(true);
        when(rule.getResource().getTransactionManager(rule.getDefaultType()).getConnection("foo_db", "foo_ds")).thenReturn(mock(Connection.class));
        assertTrue(new ConnectionTransaction(rule, context).getConnection("foo_db", "foo_ds", context).isPresent());
    }
    
    @Test
    void assertGetDistributedTransactionBeginOperationType() {
        assertThat(new ConnectionTransaction(mock(TransactionRule.class, RETURNS_DEEP_STUBS), new TransactionConnectionContext()).getDistributedTransactionOperationType(false),
                is(Optional.of(DistributedTransactionOperationType.BEGIN)));
    }
    
    @Test
    void assertGetDistributedTransactionCommitOperationType() {
        TransactionRule rule = mock(TransactionRule.class, RETURNS_DEEP_STUBS);
        when(rule.getResource().getTransactionManager(rule.getDefaultType()).isInTransaction()).thenReturn(true);
        assertThat(new ConnectionTransaction(rule, new TransactionConnectionContext()).getDistributedTransactionOperationType(true), is(Optional.of(DistributedTransactionOperationType.COMMIT)));
    }
    
    @Test
    void assertDistributedTransactionOperationTypeFailedWhenIsAutoCommit() {
        TransactionRule rule = mock(TransactionRule.class, RETURNS_DEEP_STUBS);
        when(rule.getResource().getTransactionManager(rule.getDefaultType()).isInTransaction()).thenReturn(true);
        assertFalse(new ConnectionTransaction(rule, new TransactionConnectionContext()).getDistributedTransactionOperationType(false).isPresent());
    }
    
    @Test
    void assertDistributedTransactionOperationTypeFailedWhenIsNotInDistributedTransaction() {
        ConnectionTransaction connectionTransaction = new ConnectionTransaction(mock(TransactionRule.class, RETURNS_DEEP_STUBS), new TransactionConnectionContext());
        assertFalse(connectionTransaction.getDistributedTransactionOperationType(true).isPresent());
    }
}
