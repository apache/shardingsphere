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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction;

import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdObjectHandleGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobHandleGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementIdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FirebirdTransactionIdGeneratorTest {
    
    private static final int CONNECTION_ID = 1;
    
    @BeforeEach
    void setup() {
        FirebirdTransactionIdGenerator.getInstance().registerConnection(CONNECTION_ID);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertSignedTransactionHandle() {
        FirebirdObjectHandleGenerator objects = mock(FirebirdObjectHandleGenerator.class);
        try (MockedStatic<FirebirdObjectHandleGenerator> ignored = mockStatic(FirebirdObjectHandleGenerator.class)) {
            when(FirebirdObjectHandleGenerator.getInstance()).thenReturn(objects);
            when(objects.nextHandle(CONNECTION_ID)).thenReturn(32768);
            FirebirdTransactionIdGenerator actualTransactions = FirebirdTransactionIdGenerator.getInstance();
            assertThat(actualTransactions.nextTransactionId(CONNECTION_ID), is(32768));
            assertTrue(actualTransactions.isTransactionActive(CONNECTION_ID, -32768));
            actualTransactions.closeTransaction(CONNECTION_ID, -32768);
            assertFalse(actualTransactions.isTransactionActive(CONNECTION_ID, 32768));
            verify(objects).releaseHandle(CONNECTION_ID, 32768);
        }
    }
    
    @Test
    void assertNextTransactionId() {
        assertThat(FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID), is(1));
        assertThat(FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID), is(2));
    }
    
    @Test
    void assertNextTransactionIdWithOtherObjects() {
        FirebirdStatementIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBlobHandleGenerator.getInstance().registerConnection(CONNECTION_ID);
        try {
            int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
            assertThat(transactionId, is(1));
            assertThat(FirebirdStatementIdGenerator.getInstance().nextStatementId(CONNECTION_ID), is(2));
            assertThat(FirebirdBlobHandleGenerator.getInstance().nextBlobHandle(CONNECTION_ID), is(3));
            FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, transactionId);
            assertThat(FirebirdStatementIdGenerator.getInstance().nextStatementId(CONNECTION_ID), is(1));
        } finally {
            FirebirdStatementIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
            FirebirdBlobHandleGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        }
    }
    
    @Test
    void assertRegisterConnectionReleasesPreviousHandles() {
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        FirebirdTransactionIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        assertThat(FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID), is(transactionId));
    }
    
    @Test
    void assertGetTransactionId() {
        assertThat(FirebirdTransactionIdGenerator.getInstance().getTransactionId(CONNECTION_ID), is(0));
    }
    
    @Test
    void assertGetTransactionIdWithoutRegisteredConnection() {
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        assertThrows(IllegalStateException.class, () -> FirebirdTransactionIdGenerator.getInstance().getTransactionId(CONNECTION_ID));
    }
    
    @Test
    void assertIsTransactionActiveWithClosedTransaction() {
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, transactionId));
        FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, transactionId);
        assertFalse(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, transactionId));
    }
    
    @Test
    void assertIsTransactionActiveWithoutRegisteredConnection() {
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        assertThrows(IllegalStateException.class, () -> FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, 1));
    }
    
    @Test
    void assertHasActiveTransaction() {
        assertFalse(FirebirdTransactionIdGenerator.getInstance().hasActiveTransaction(CONNECTION_ID));
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        assertTrue(FirebirdTransactionIdGenerator.getInstance().hasActiveTransaction(CONNECTION_ID));
        FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, transactionId);
        assertFalse(FirebirdTransactionIdGenerator.getInstance().hasActiveTransaction(CONNECTION_ID));
    }
}
