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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator;

import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdBlobHandleGeneratorTest {
    
    private static final FirebirdBlobHandleGenerator GENERATOR = FirebirdBlobHandleGenerator.getInstance();
    
    private static final int CONNECTION_ID = 91;
    
    private static final int INVALID_OBJECT_HANDLE = 0xFFFF;
    
    private static final int MAX_OBJECT_HANDLE = INVALID_OBJECT_HANDLE - 1;
    
    @BeforeEach
    void setUp() {
        GENERATOR.registerConnection(CONNECTION_ID);
    }
    
    @AfterEach
    void tearDown() {
        GENERATOR.unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertNextBlobHandleIncrements() {
        assertThat(GENERATOR.nextBlobHandle(CONNECTION_ID), is(1));
        assertThat(GENERATOR.nextBlobHandle(CONNECTION_ID), is(2));
    }
    
    @Test
    void assertNextBlobHandleWhenAllHandlesAreActive() {
        assertThat(GENERATOR.nextBlobHandle(CONNECTION_ID), is(1));
        for (int i = 1; i < MAX_OBJECT_HANDLE; i++) {
            GENERATOR.nextBlobHandle(CONNECTION_ID);
        }
        assertThrows(DatabaseProtocolException.class, () -> GENERATOR.nextBlobHandle(CONNECTION_ID));
    }
    
    @Test
    void assertNextBlobHandleWhenConnectionIsNotRegistered() {
        GENERATOR.unregisterConnection(CONNECTION_ID);
        assertThrows(DatabaseProtocolException.class, () -> GENERATOR.nextBlobHandle(CONNECTION_ID));
    }
    
    @Test
    void assertNextBlobHandleReusesReleasedHandle() {
        GENERATOR.nextBlobHandle(CONNECTION_ID);
        int releasedHandle = GENERATOR.nextBlobHandle(CONNECTION_ID);
        int lastHandle = GENERATOR.nextBlobHandle(CONNECTION_ID);
        GENERATOR.releaseBlobHandle(CONNECTION_ID, releasedHandle);
        assertThat(GENERATOR.nextBlobHandle(CONNECTION_ID), is(releasedHandle));
        assertThat(GENERATOR.nextBlobHandle(CONNECTION_ID), is(lastHandle + 1));
    }
    
    @Test
    void assertNextBlobHandleReusesLowestReleasedHandle() {
        int lowestHandle = GENERATOR.nextBlobHandle(CONNECTION_ID);
        GENERATOR.nextBlobHandle(CONNECTION_ID);
        int highestHandle = GENERATOR.nextBlobHandle(CONNECTION_ID);
        GENERATOR.releaseBlobHandle(CONNECTION_ID, lowestHandle);
        GENERATOR.releaseBlobHandle(CONNECTION_ID, highestHandle);
        assertThat(GENERATOR.nextBlobHandle(CONNECTION_ID), is(lowestHandle));
        assertThat(GENERATOR.nextBlobHandle(CONNECTION_ID), is(highestHandle));
    }
    
    @Test
    void assertResolveBlobHandleWithRegularHandleExpectsSameHandle() {
        assertThat(GENERATOR.resolveBlobHandle(CONNECTION_ID, 5), is(5));
    }
    
    @Test
    void assertResolveBlobHandleWithPlaceholderExpectsLastGeneratedHandle() {
        GENERATOR.nextBlobHandle(CONNECTION_ID);
        int expected = GENERATOR.nextBlobHandle(CONNECTION_ID);
        assertThat(GENERATOR.resolveBlobHandle(CONNECTION_ID, INVALID_OBJECT_HANDLE), is(expected));
    }
    
    @Test
    void assertResolveBlobHandleWithPlaceholderAndNoGeneratedHandleExpectsPlaceholder() {
        assertThat(GENERATOR.resolveBlobHandle(CONNECTION_ID, INVALID_OBJECT_HANDLE), is(INVALID_OBJECT_HANDLE));
    }
    
    @Test
    void assertResolveBlobHandleWithPlaceholderAndUnknownConnectionExpectsPlaceholder() {
        assertThat(GENERATOR.resolveBlobHandle(92, INVALID_OBJECT_HANDLE), is(INVALID_OBJECT_HANDLE));
    }
    
    @Test
    void assertIsAllocatedWithActiveHandle() {
        assertTrue(GENERATOR.isAllocated(CONNECTION_ID, GENERATOR.nextBlobHandle(CONNECTION_ID)));
    }
    
    @Test
    void assertIsAllocatedAfterRelease() {
        int blobHandle = GENERATOR.nextBlobHandle(CONNECTION_ID);
        GENERATOR.releaseBlobHandle(CONNECTION_ID, blobHandle);
        assertFalse(GENERATOR.isAllocated(CONNECTION_ID, blobHandle));
    }
    
    @Test
    void assertIsAllocatedWhenConnectionIsNotRegistered() {
        assertFalse(GENERATOR.isAllocated(92, 1));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidBlobHandleProvider")
    void assertIsAllocatedWhenHandleIsOutsideValidRange(final String name, final int blobHandle) {
        assertFalse(GENERATOR.isAllocated(CONNECTION_ID, blobHandle));
    }
    
    @Test
    void assertReleaseBlobHandleClearsLastBlobHandle() {
        int blobHandle = GENERATOR.nextBlobHandle(CONNECTION_ID);
        GENERATOR.releaseBlobHandle(CONNECTION_ID, blobHandle);
        assertThat(GENERATOR.resolveBlobHandle(CONNECTION_ID, INVALID_OBJECT_HANDLE), is(INVALID_OBJECT_HANDLE));
        assertThat(GENERATOR.nextBlobHandle(CONNECTION_ID), is(blobHandle));
    }
    
    @Test
    void assertReleaseBlobHandleKeepsLastActiveBlobHandle() {
        int blobHandle = GENERATOR.nextBlobHandle(CONNECTION_ID);
        int expected = GENERATOR.nextBlobHandle(CONNECTION_ID);
        GENERATOR.releaseBlobHandle(CONNECTION_ID, blobHandle);
        assertThat(GENERATOR.resolveBlobHandle(CONNECTION_ID, INVALID_OBJECT_HANDLE), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidBlobHandleProvider")
    void assertReleaseBlobHandleWhenHandleIsOutsideValidRange(final String name, final int blobHandle) {
        assertThrows(DatabaseProtocolException.class, () -> GENERATOR.releaseBlobHandle(CONNECTION_ID, blobHandle));
    }
    
    @Test
    void assertReleaseBlobHandleWhenHandleIsNotActive() {
        assertThrows(DatabaseProtocolException.class, () -> GENERATOR.releaseBlobHandle(CONNECTION_ID, 1));
    }
    
    @Test
    void assertReleaseBlobHandleWhenConnectionIsNotRegistered() {
        GENERATOR.unregisterConnection(CONNECTION_ID);
        assertDoesNotThrow(() -> GENERATOR.releaseBlobHandle(CONNECTION_ID, 1));
    }
    
    private static Stream<Arguments> invalidBlobHandleProvider() {
        return Stream.of(Arguments.of("negative handle", -1), Arguments.of("zero handle", 0), Arguments.of("deferred handle", INVALID_OBJECT_HANDLE));
    }
}
