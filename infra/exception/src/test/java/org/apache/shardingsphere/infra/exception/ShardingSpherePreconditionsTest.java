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

package org.apache.shardingsphere.infra.exception;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShardingSpherePreconditionsTest {
    
    @Test
    void assertCheckStateToThrowsException() {
        assertThrows(SQLException.class, () -> ShardingSpherePreconditions.checkState(false, SQLException::new));
    }
    
    @Test
    void assertCheckStateToNotThrowException() {
        assertDoesNotThrow(() -> ShardingSpherePreconditions.checkState(true, SQLException::new));
    }
    
    @Test
    void assertCheckNotNullToThrowsException() {
        assertThrows(SQLException.class, () -> ShardingSpherePreconditions.checkNotNull(null, SQLException::new));
    }
    
    @Test
    void assertCheckNotNullToNotThrowException() {
        assertDoesNotThrow(() -> ShardingSpherePreconditions.checkNotNull(new Object(), SQLException::new));
    }
    
    @Test
    void assertCheckNotEmptyWithStringToThrowsException() {
        assertThrows(SQLException.class, () -> ShardingSpherePreconditions.checkNotEmpty((String) null, SQLException::new));
        assertThrows(SQLException.class, () -> ShardingSpherePreconditions.checkNotEmpty("", SQLException::new));
    }
    
    @Test
    void assertCheckNotEmptyWithStringToNotThrowException() {
        assertDoesNotThrow(() -> ShardingSpherePreconditions.checkNotEmpty("foo", SQLException::new));
    }
    
    @Test
    void assertCheckNotEmptyWithCollectionToThrowsException() {
        assertThrows(SQLException.class, () -> ShardingSpherePreconditions.checkNotEmpty(Collections.emptyList(), SQLException::new));
    }
    
    @Test
    void assertCheckNotEmptyWithCollectionToNotThrowException() {
        assertDoesNotThrow(() -> ShardingSpherePreconditions.checkNotEmpty(Collections.singleton("foo"), SQLException::new));
    }
    
    @Test
    void assertCheckNotEmptyWithMapToThrowsException() {
        assertThrows(SQLException.class, () -> ShardingSpherePreconditions.checkNotEmpty(Collections.emptyMap(), SQLException::new));
    }
    
    @Test
    void assertCheckNotEmptyWithMapToNotThrowException() {
        assertDoesNotThrow(() -> ShardingSpherePreconditions.checkNotEmpty(Collections.singletonMap("key", "value"), SQLException::new));
    }
    
    @Test
    void assertCheckMustEmptyWithCollectionToThrowsException() {
        assertThrows(SQLException.class, () -> ShardingSpherePreconditions.checkMustEmpty(Collections.singleton("foo"), SQLException::new));
    }
    
    @Test
    void assertCheckMustEmptyWithCollectionToNotThrowException() {
        assertDoesNotThrow(() -> ShardingSpherePreconditions.checkMustEmpty(Collections.emptyList(), SQLException::new));
    }
    
    @Test
    void assertCheckMustEmptyWithMapToThrowsException() {
        assertThrows(SQLException.class, () -> ShardingSpherePreconditions.checkMustEmpty(Collections.singletonMap("key", "value"), SQLException::new));
    }
    
    @Test
    void assertCheckMustEmptyWithMapToNotThrowException() {
        assertDoesNotThrow(() -> ShardingSpherePreconditions.checkMustEmpty(Collections.emptyMap(), SQLException::new));
    }
    
    @Test
    void assertCheckContainsToNotThrowException() {
        assertDoesNotThrow(() -> ShardingSpherePreconditions.checkContains(Collections.singleton("foo"), "foo", SQLException::new));
    }
    
    @Test
    void assertCheckContainsToThrowsException() {
        assertThrows(SQLException.class, () -> ShardingSpherePreconditions.checkContains(Collections.singleton("foo"), "bar", SQLException::new));
    }
    
    @Test
    void assertCheckNotContainsToNotThrowException() {
        assertDoesNotThrow(() -> ShardingSpherePreconditions.checkNotContains(Collections.singleton("foo"), "bar", SQLException::new));
    }
    
    @Test
    void assertCheckNotContainsToThrowsException() {
        assertThrows(SQLException.class, () -> ShardingSpherePreconditions.checkNotContains(Collections.singleton("foo"), "foo", SQLException::new));
    }
    
    @Test
    void assertCheckContainsKeyToNotThrowException() {
        assertDoesNotThrow(() -> ShardingSpherePreconditions.checkContainsKey(Collections.singletonMap("foo", "value"), "foo", SQLException::new));
    }
    
    @Test
    void assertCheckContainsKeyToThrowsException() {
        assertThrows(SQLException.class, () -> ShardingSpherePreconditions.checkContainsKey(Collections.singletonMap("foo", "value"), "bar", SQLException::new));
    }
}
