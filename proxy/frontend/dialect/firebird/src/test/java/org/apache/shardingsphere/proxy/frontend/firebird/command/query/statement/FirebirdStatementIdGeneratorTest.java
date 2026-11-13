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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
    
    @SuppressWarnings("unchecked")
    @Test
    void assertNextStatementIdAfterInvalidObjectHandle() throws ReflectiveOperationException {
        Map<Integer, AtomicInteger> registry = (Map<Integer, AtomicInteger>) Plugins.getMemberAccessor().get(FirebirdStatementIdGenerator.class.getDeclaredField("connectionRegistry"), GENERATOR);
        registry.get(1).set(0xFFFE);
        assertThat(GENERATOR.nextStatementId(1), is(0x10000));
        assertFalse(GENERATOR.isStatementAllocated(1, 0xFFFF));
    }
    
    @Test
    void assertIsStatementAllocated() {
        assertTrue(GENERATOR.isStatementAllocated(1, GENERATOR.nextStatementId(1)));
    }
    
    @Test
    void assertIsStatementAllocatedWithNotAllocatedStatement() {
        assertFalse(GENERATOR.isStatementAllocated(1, 1));
    }
    
    @Test
    void assertReleaseStatementId() {
        int statementId = GENERATOR.nextStatementId(1);
        GENERATOR.releaseStatementId(1, statementId);
        assertFalse(GENERATOR.isStatementAllocated(1, statementId));
    }
    
    @Test
    void assertGetStatementId() {
        assertThat(GENERATOR.getStatementId(1), is(0));
    }
    
    @Test
    void assertUnregisterConnection() {
        GENERATOR.unregisterConnection(1);
        assertThrows(IllegalStateException.class, () -> GENERATOR.getStatementId(1));
    }
}
