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

import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdBlobHandleGeneratorTest {
    
    private static final int CONNECTION_ID = 91;
    
    private static final int INVALID_OBJECT_HANDLE = 0xFFFF;
    
    private static final int MAX_OBJECT_HANDLE = INVALID_OBJECT_HANDLE - 1;
    
    @BeforeEach
    void setUp() {
        FirebirdBlobHandleGenerator.getInstance().registerConnection(CONNECTION_ID);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdBlobHandleGenerator.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertNextBlobHandleIncrements() {
        assertThat(FirebirdBlobHandleGenerator.getInstance().nextBlobHandle(CONNECTION_ID), is(1));
        assertThat(FirebirdBlobHandleGenerator.getInstance().nextBlobHandle(CONNECTION_ID), is(2));
    }
    
    @Test
    void assertNextBlobHandleWhenAllHandlesAreActive() {
        FirebirdBlobHandleGenerator generator = FirebirdBlobHandleGenerator.getInstance();
        int firstHandle = generator.nextBlobHandle(CONNECTION_ID);
        assertThat(firstHandle, is(1));
        for (int i = 1; i < MAX_OBJECT_HANDLE; i++) {
            generator.nextBlobHandle(CONNECTION_ID);
        }
        assertThrows(FirebirdProtocolException.class, () -> generator.nextBlobHandle(CONNECTION_ID));
    }
    
    @Test
    void assertNextBlobHandleWhenConnectionIsNotRegistered() {
        FirebirdBlobHandleGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        assertThrows(FirebirdProtocolException.class, () -> FirebirdBlobHandleGenerator.getInstance().nextBlobHandle(CONNECTION_ID));
    }
    
    @Test
    void assertNextBlobHandleReusesReleasedHandleAfterWrap() {
        FirebirdBlobHandleGenerator generator = FirebirdBlobHandleGenerator.getInstance();
        int releasedHandle = generator.nextBlobHandle(CONNECTION_ID);
        generator.releaseBlobHandle(CONNECTION_ID, releasedHandle);
        for (int i = 1; i < MAX_OBJECT_HANDLE; i++) {
            generator.nextBlobHandle(CONNECTION_ID);
        }
        assertThat(generator.nextBlobHandle(CONNECTION_ID), is(releasedHandle));
        assertThrows(FirebirdProtocolException.class, () -> generator.nextBlobHandle(CONNECTION_ID));
    }
    
    @Test
    void assertResolveBlobHandleWithRegularHandleExpectsSameHandle() {
        assertThat(FirebirdBlobHandleGenerator.getInstance().resolveBlobHandle(CONNECTION_ID, 5), is(5));
    }
    
    @Test
    void assertResolveBlobHandleWithPlaceholderExpectsLastGeneratedHandle() {
        FirebirdBlobHandleGenerator.getInstance().nextBlobHandle(CONNECTION_ID);
        int expected = FirebirdBlobHandleGenerator.getInstance().nextBlobHandle(CONNECTION_ID);
        assertThat(FirebirdBlobHandleGenerator.getInstance().resolveBlobHandle(CONNECTION_ID, INVALID_OBJECT_HANDLE), is(expected));
    }
    
    @Test
    void assertResolveBlobHandleWithPlaceholderAndNoGeneratedHandleExpectsPlaceholder() {
        assertThat(FirebirdBlobHandleGenerator.getInstance().resolveBlobHandle(CONNECTION_ID, INVALID_OBJECT_HANDLE), is(INVALID_OBJECT_HANDLE));
    }
    
    @Test
    void assertResolveBlobHandleWithPlaceholderAndUnknownConnectionExpectsPlaceholder() {
        assertThat(FirebirdBlobHandleGenerator.getInstance().resolveBlobHandle(92, INVALID_OBJECT_HANDLE), is(INVALID_OBJECT_HANDLE));
    }
    
    @Test
    void assertReleaseBlobHandleClearsLastBlobHandle() {
        FirebirdBlobHandleGenerator generator = FirebirdBlobHandleGenerator.getInstance();
        int blobHandle = generator.nextBlobHandle(CONNECTION_ID);
        generator.releaseBlobHandle(CONNECTION_ID, blobHandle);
        assertThat(generator.resolveBlobHandle(CONNECTION_ID, INVALID_OBJECT_HANDLE), is(INVALID_OBJECT_HANDLE));
        assertThat(generator.nextBlobHandle(CONNECTION_ID), is(2));
    }
    
    @Test
    void assertReleaseBlobHandleKeepsLastActiveBlobHandle() {
        FirebirdBlobHandleGenerator generator = FirebirdBlobHandleGenerator.getInstance();
        int blobHandle = generator.nextBlobHandle(CONNECTION_ID);
        int expected = generator.nextBlobHandle(CONNECTION_ID);
        generator.releaseBlobHandle(CONNECTION_ID, blobHandle);
        assertThat(generator.resolveBlobHandle(CONNECTION_ID, INVALID_OBJECT_HANDLE), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidBlobHandleProvider")
    void assertReleaseBlobHandleWhenHandleIsOutsideValidRange(final String name, final int blobHandle) {
        assertThrows(FirebirdProtocolException.class, () -> FirebirdBlobHandleGenerator.getInstance().releaseBlobHandle(CONNECTION_ID, blobHandle));
    }
    
    @Test
    void assertReleaseBlobHandleWhenHandleIsNotActive() {
        assertThrows(FirebirdProtocolException.class, () -> FirebirdBlobHandleGenerator.getInstance().releaseBlobHandle(CONNECTION_ID, 1));
    }
    
    @Test
    void assertReleaseBlobHandleWhenConnectionIsNotRegistered() {
        FirebirdBlobHandleGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        assertDoesNotThrow(() -> FirebirdBlobHandleGenerator.getInstance().releaseBlobHandle(CONNECTION_ID, 1));
    }
    
    private static Stream<Arguments> invalidBlobHandleProvider() {
        return Stream.of(
                Arguments.of("negative handle", -1),
                Arguments.of("zero handle", 0),
                Arguments.of("deferred handle", INVALID_OBJECT_HANDLE));
    }
}
