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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement;

import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdObjectHandleGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdStatementIdGeneratorTest {
    
    private static final FirebirdStatementIdGenerator GENERATOR = FirebirdStatementIdGenerator.getInstance();
    
    @BeforeEach
    void setUp() {
        GENERATOR.registerConnection(1);
    }
    
    @AfterEach
    void tearDown() {
        GENERATOR.unregisterConnection(1);
    }
    
    @Test
    void assertNextStatementId() {
        assertThat(GENERATOR.nextStatementId(1), is(1));
    }
    
    @Test
    void assertGetStatementId() {
        assertThat(GENERATOR.getStatementId(1), is(0));
    }
    
    @Test
    void assertIsStatementAllocated() {
        assertFalse(GENERATOR.isStatementAllocated(1, 1));
        int statementId = GENERATOR.nextStatementId(1);
        assertTrue(GENERATOR.isStatementAllocated(1, statementId));
        GENERATOR.releaseStatementId(1, statementId);
        assertFalse(GENERATOR.isStatementAllocated(1, statementId));
        assertFalse(GENERATOR.isStatementAllocated(2, statementId));
    }
    
    @Test
    void assertResolveLazyStatementId() {
        int statementId = GENERATOR.nextStatementId(1);
        int lastHandle = FirebirdObjectHandleGenerator.getInstance().nextHandle(1);
        try {
            assertThat(GENERATOR.resolveStatementId(1, -1, true), is(lastHandle));
            assertThat(GENERATOR.resolveStatementId(1, 65535, true), is(lastHandle));
            assertThat(GENERATOR.resolveStatementId(1, -1, false), is(65535));
            assertThat(GENERATOR.resolveStatementId(1, -32768, false), is(32768));
            assertThat(GENERATOR.resolveStatementId(1, statementId, true), is(statementId));
        } finally {
            FirebirdObjectHandleGenerator.getInstance().releaseHandle(1, lastHandle);
        }
    }
    
    @Test
    void assertReleaseStatementId() {
        FirebirdTransactionIdGenerator.getInstance().registerConnection(1);
        try {
            int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(1);
            int statementId = GENERATOR.nextStatementId(1);
            GENERATOR.releaseStatementId(1, transactionId);
            assertThat(GENERATOR.nextStatementId(1), is(3));
            GENERATOR.releaseStatementId(1, statementId);
            assertThat(GENERATOR.nextStatementId(1), is(statementId));
        } finally {
            FirebirdTransactionIdGenerator.getInstance().unregisterConnection(1);
        }
    }
    
    @Test
    void assertRegisterConnectionReleasesPreviousHandles() {
        int statementId = GENERATOR.nextStatementId(1);
        GENERATOR.registerConnection(1);
        assertThat(GENERATOR.nextStatementId(1), is(statementId));
    }
    
    @Test
    void assertUnregisterConnection() {
        GENERATOR.unregisterConnection(1);
        assertThrows(IllegalStateException.class, () -> GENERATOR.getStatementId(1));
    }
}
