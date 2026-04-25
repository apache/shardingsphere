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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdBatchRegistryTest {
    
    @Test
    void assertRegisterAndGetBatchStatement() {
        FirebirdBatchRegistry registry = FirebirdBatchRegistry.getInstance();
        registry.registerConnection(1);
        FirebirdBatchStatement expectedBatchStatement = new FirebirdBatchStatement(2);
        registry.registerBatchStatement(1, 2, expectedBatchStatement);
        assertThat(registry.getBatchStatement(1, 2), is(expectedBatchStatement));
        registry.unregisterConnection(1);
    }
    
    @Test
    void assertRegisterBatchStatementWithoutConnection() {
        FirebirdBatchRegistry registry = FirebirdBatchRegistry.getInstance();
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registry.registerBatchStatement(99, 1, new FirebirdBatchStatement(1)));
        assertThat(ex.getMessage(), is("Connection [99] is not registered."));
    }
    
    @Test
    void assertUnregisterBatchStatement() {
        FirebirdBatchRegistry registry = FirebirdBatchRegistry.getInstance();
        registry.registerConnection(3);
        registry.registerBatchStatement(3, 7, new FirebirdBatchStatement(7));
        registry.unregisterBatchStatement(3, 7);
        assertNull(registry.getBatchStatement(3, 7));
        registry.unregisterConnection(3);
    }
    
    @Test
    void assertUnregisterConnection() {
        FirebirdBatchRegistry registry = FirebirdBatchRegistry.getInstance();
        registry.registerConnection(5);
        registry.registerBatchStatement(5, 8, new FirebirdBatchStatement(8));
        registry.unregisterConnection(5);
        assertNull(registry.getBatchStatement(5, 8));
    }
}
