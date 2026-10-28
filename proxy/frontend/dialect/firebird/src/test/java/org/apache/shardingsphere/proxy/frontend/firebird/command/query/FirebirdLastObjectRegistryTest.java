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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FirebirdLastObjectRegistryTest {
    
    private static final FirebirdLastObjectRegistry REGISTRY = FirebirdLastObjectRegistry.getInstance();
    
    private static final int CONNECTION_ID = 1;
    
    private static final int INVALID_OBJECT_HANDLE = 0xFFFF;
    
    @AfterEach
    void tearDown() {
        REGISTRY.unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertResolveStatementHandleWithLastStatement() {
        REGISTRY.registerLazySendConnection(CONNECTION_ID);
        REGISTRY.setLastStatement(CONNECTION_ID, 3);
        assertThat(REGISTRY.resolveStatementHandle(CONNECTION_ID, INVALID_OBJECT_HANDLE), is(3));
    }
    
    @Test
    void assertResolveStatementHandleWithSignExtendedLastStatement() {
        REGISTRY.registerLazySendConnection(CONNECTION_ID);
        REGISTRY.setLastStatement(CONNECTION_ID, 3);
        assertThat(REGISTRY.resolveStatementHandle(CONNECTION_ID, 0xFFFFFFFF), is(3));
    }
    
    @Test
    void assertResolveStatementHandleWithSignExtendedHandle() {
        assertThat(REGISTRY.resolveStatementHandle(CONNECTION_ID, 0xFFFF8001), is(0x8001));
    }
    
    @Test
    void assertResolveStatementHandleWithValidHandle() {
        REGISTRY.registerLazySendConnection(CONNECTION_ID);
        REGISTRY.setLastStatement(CONNECTION_ID, 3);
        assertThat(REGISTRY.resolveStatementHandle(CONNECTION_ID, 2), is(2));
    }
    
    @Test
    void assertResolveStatementHandleWithoutLazySend() {
        REGISTRY.setLastStatement(CONNECTION_ID, 3);
        assertThat(REGISTRY.resolveStatementHandle(CONNECTION_ID, INVALID_OBJECT_HANDLE), is(INVALID_OBJECT_HANDLE));
    }
    
    @Test
    void assertResolveStatementHandleWithLastNonStatementObject() {
        REGISTRY.registerLazySendConnection(CONNECTION_ID);
        REGISTRY.setLastStatement(CONNECTION_ID, 3);
        REGISTRY.setLastNonStatementObject(CONNECTION_ID);
        assertThat(REGISTRY.resolveStatementHandle(CONNECTION_ID, INVALID_OBJECT_HANDLE), is(INVALID_OBJECT_HANDLE));
    }
    
    @Test
    void assertUnregisterConnection() {
        REGISTRY.registerLazySendConnection(CONNECTION_ID);
        REGISTRY.setLastStatement(CONNECTION_ID, 3);
        REGISTRY.unregisterConnection(CONNECTION_ID);
        assertThat(REGISTRY.resolveStatementHandle(CONNECTION_ID, INVALID_OBJECT_HANDLE), is(INVALID_OBJECT_HANDLE));
    }
}
