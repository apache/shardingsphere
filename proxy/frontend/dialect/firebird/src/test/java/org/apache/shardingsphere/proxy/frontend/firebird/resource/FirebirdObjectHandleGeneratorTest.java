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

package org.apache.shardingsphere.proxy.frontend.firebird.resource;

import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdObjectHandleGeneratorTest {
    
    private static final int CONNECTION_ID = 999;
    
    private final FirebirdObjectHandleGenerator generator = FirebirdObjectHandleGenerator.getInstance();
    
    @BeforeEach
    void setup() {
        generator.registerConnection(CONNECTION_ID, FirebirdCommandPacketType.TRANSACTION);
        generator.registerConnection(CONNECTION_ID, FirebirdCommandPacketType.ALLOCATE_STATEMENT);
        generator.registerConnection(CONNECTION_ID, FirebirdCommandPacketType.CREATE_BLOB);
    }
    
    @AfterEach
    void tearDown() {
        generator.unregisterConnection(CONNECTION_ID, FirebirdCommandPacketType.TRANSACTION);
        generator.unregisterConnection(CONNECTION_ID, FirebirdCommandPacketType.ALLOCATE_STATEMENT);
        generator.unregisterConnection(CONNECTION_ID, FirebirdCommandPacketType.CREATE_BLOB);
    }
    
    @Test
    void assertNextHandleAcrossObjectKinds() {
        assertThat(generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.TRANSACTION, 1), is(1));
        assertThat(generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.ALLOCATE_STATEMENT, 1), is(2));
        assertThat(generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.CREATE_BLOB, 1), is(3));
    }
    
    @Test
    void assertNextHandleWithMinimumHandle() {
        int transactionId = generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.TRANSACTION, 1);
        generator.releaseHandle(CONNECTION_ID, FirebirdCommandPacketType.TRANSACTION, transactionId);
        assertThat(generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.TRANSACTION, transactionId + 1), is(2));
        assertThat(generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.CREATE_BLOB, 1), is(1));
    }
    
    @Test
    void assertNextHandleWrapsAcrossObjectKinds() {
        assertThat(generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.TRANSACTION, 65000), is(65000));
        assertThat(generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.ALLOCATE_STATEMENT, 65001), is(1));
        assertThat(generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.TRANSACTION, 65001), is(2));
    }
    
    @Test
    void assertNextHandleAtCapacity() {
        for (int i = 0; i < 0xFFFE; i++) {
            generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.CREATE_BLOB, 1);
        }
        assertThrows(DatabaseProtocolException.class, () -> generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.CREATE_BLOB, 1));
    }
    
    @Test
    void assertReleaseHandleWithDifferentObjectKind() {
        int statementId = generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.ALLOCATE_STATEMENT, 1);
        generator.releaseHandle(CONNECTION_ID, FirebirdCommandPacketType.CREATE_BLOB, statementId);
        assertThat(generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.TRANSACTION, 1), is(2));
    }
    
    @Test
    void assertUnregisterConnectionPreservesOtherKinds() {
        generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.TRANSACTION, 1);
        generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.ALLOCATE_STATEMENT, 1);
        generator.unregisterConnection(CONNECTION_ID, FirebirdCommandPacketType.ALLOCATE_STATEMENT);
        assertThat(generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.CREATE_BLOB, 1), is(2));
    }
    
    @Test
    void assertRegisterConnectionResetsOnlyItsObjectKind() {
        generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.TRANSACTION, 1);
        generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.ALLOCATE_STATEMENT, 1);
        generator.registerConnection(CONNECTION_ID, FirebirdCommandPacketType.ALLOCATE_STATEMENT);
        assertThat(generator.nextHandle(CONNECTION_ID, FirebirdCommandPacketType.CREATE_BLOB, 1), is(2));
    }
}
