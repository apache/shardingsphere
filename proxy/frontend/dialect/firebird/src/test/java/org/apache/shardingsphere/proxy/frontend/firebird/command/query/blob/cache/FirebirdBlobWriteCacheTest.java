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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache;

import lombok.SneakyThrows;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobHandleGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdBlobWriteCacheTest {
    
    private static final FirebirdBlobWriteCache CACHE = FirebirdBlobWriteCache.getInstance();
    
    @AfterEach
    void tearDown() {
        getHandleCache().clear();
        getIdCache().clear();
    }
    
    @Test
    void assertSignedTransactionHandle() {
        int connectionId = 10925;
        CACHE.registerConnection(connectionId);
        CACHE.registerBlob(connectionId, 32768, 11, 1L, false);
        CACHE.appendSegment(connectionId, 11, new byte[]{4, 5});
        CACHE.closeWrite(connectionId, 11);
        assertTrue(CACHE.isClosed(connectionId, -32768, 1L));
        assertThat(CACHE.getBlobData(connectionId, -32768, 1L).get(), is(new byte[]{4, 5}));
        CACHE.unregisterTransaction(connectionId, -32768);
        assertFalse(CACHE.isClosed(connectionId, 32768, 1L));
    }
    
    @Test
    void assertRegisterAndUnregisterConnection() {
        int connectionId = 1;
        CACHE.registerConnection(connectionId);
        Map<Integer, Map<Integer, FirebirdBlobWrite>> handleMap = getHandleCache();
        Map<Integer, Map<Integer, Map<Long, FirebirdBlobWrite>>> actualIdMap = getIdCache();
        assertTrue(handleMap.containsKey(connectionId));
        assertTrue(actualIdMap.containsKey(connectionId));
        CACHE.unregisterConnection(connectionId);
        assertFalse(handleMap.containsKey(connectionId));
        assertFalse(actualIdMap.containsKey(connectionId));
    }
    
    @Test
    void assertRegisterBlobCreatesMappingsWhenConnectionMissing() {
        CACHE.registerBlob(2, 0, 3, 4L, false);
        OptionalLong actualBlobId = CACHE.getBlobId(2, 3);
        assertTrue(actualBlobId.isPresent());
        assertThat(actualBlobId.getAsLong(), is(4L));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("missingWriteProvider")
    void assertMissingWriteReturnsEmpty(final String caseName, final BooleanSupplier invocation) {
        assertFalse(invocation.getAsBoolean(), caseName);
    }
    
    @Test
    void assertIsClosedStatusTransitions() {
        int connectionId = 6;
        int blobHandle = 8;
        long blobId = 9L;
        CACHE.registerConnection(connectionId);
        assertFalse(CACHE.isClosed(connectionId, 0, blobId));
        CACHE.registerBlob(connectionId, 0, blobHandle, blobId, false);
        OptionalInt actualSizeAfterAppend = CACHE.appendSegment(connectionId, blobHandle, new byte[]{1, 2, 3});
        assertTrue(actualSizeAfterAppend.isPresent());
        assertThat(actualSizeAfterAppend.getAsInt(), is(3));
        assertFalse(CACHE.isClosed(connectionId, 0, blobId));
        OptionalInt actualSizeAfterClose = CACHE.closeWrite(connectionId, blobHandle);
        assertTrue(actualSizeAfterClose.isPresent());
        assertThat(actualSizeAfterClose.getAsInt(), is(3));
        assertFalse(CACHE.getBlobId(connectionId, blobHandle).isPresent());
        assertTrue(CACHE.isClosed(connectionId, 0, blobId));
        Optional<byte[]> actualBlobData = CACHE.getBlobData(connectionId, 0, blobId);
        assertTrue(actualBlobData.isPresent());
        assertThat(actualBlobData.get(), is(new byte[]{1, 2, 3}));
        CACHE.removeWrite(connectionId, 0, blobId);
        assertFalse(getHandleCache().get(connectionId).containsKey(blobHandle));
        assertFalse(getIdCache().get(connectionId).get(0).containsKey(blobId));
    }
    
    @Test
    void assertRemoveWriteDoesNotRemoveReusedHandle() {
        int connectionId = 12;
        int blobHandle = 8;
        long oldBlobId = 9L;
        long expectedBlobId = 10L;
        CACHE.registerConnection(connectionId);
        CACHE.registerBlob(connectionId, 0, blobHandle, oldBlobId, false);
        CACHE.closeWrite(connectionId, blobHandle);
        CACHE.registerBlob(connectionId, 0, blobHandle, expectedBlobId, false);
        CACHE.removeWrite(connectionId, 0, oldBlobId);
        OptionalLong actualBlobId = CACHE.getBlobId(connectionId, blobHandle);
        assertTrue(actualBlobId.isPresent());
        assertThat(actualBlobId.getAsLong(), is(expectedBlobId));
    }
    
    @Test
    void assertRemoveWriteWhenWriteMissing() {
        int connectionId = 7;
        CACHE.registerConnection(connectionId);
        CACHE.removeWrite(connectionId, 0, 11L);
        assertTrue(getHandleCache().get(connectionId).isEmpty());
        assertTrue(getIdCache().get(connectionId).get(0).isEmpty());
    }
    
    @Test
    void assertTransactionsWithSameBlobIdAreIsolated() {
        int connectionId = 14;
        CACHE.registerConnection(connectionId);
        CACHE.registerBlob(connectionId, 1, 11, 1L, false);
        CACHE.registerBlob(connectionId, 2, 12, 1L, true);
        CACHE.appendSegment(connectionId, 11, new byte[]{1});
        CACHE.appendSegment(connectionId, 12, new byte[]{2});
        CACHE.closeWrite(connectionId, 11);
        CACHE.closeWrite(connectionId, 12);
        assertThat(CACHE.getBlobData(connectionId, 1, 1L).get(), is(new byte[]{1}));
        assertThat(CACHE.getBlobData(connectionId, 2, 1L).get(), is(new byte[]{2}));
        assertFalse(CACHE.isStreamBlob(connectionId, 1, 1L));
        assertTrue(CACHE.isStreamBlob(connectionId, 2, 1L));
        CACHE.unregisterTransaction(connectionId, 1);
        assertFalse(CACHE.isClosed(connectionId, 1, 1L));
        assertTrue(CACHE.isClosed(connectionId, 2, 1L));
        assertThat(CACHE.getBlobData(connectionId, 2, 1L).get(), is(new byte[]{2}));
    }
    
    @Test
    void assertTransactionCleanupReleasesOnlyItsOpenHandles() {
        int connectionId = 15;
        FirebirdBlobHandleGenerator actualHandles = FirebirdBlobHandleGenerator.getInstance();
        actualHandles.registerConnection(connectionId);
        CACHE.registerConnection(connectionId);
        try {
            int firstHandle = actualHandles.nextBlobHandle(connectionId);
            int secondHandle = actualHandles.nextBlobHandle(connectionId);
            CACHE.registerBlob(connectionId, 1, firstHandle, 1L, false);
            CACHE.registerBlob(connectionId, 2, secondHandle, 1L, false);
            CACHE.unregisterTransaction(connectionId, 1);
            assertFalse(actualHandles.isAllocated(connectionId, firstHandle));
            assertTrue(actualHandles.isAllocated(connectionId, secondHandle));
            assertFalse(CACHE.getBlobId(connectionId, firstHandle).isPresent());
            assertTrue(CACHE.getBlobId(connectionId, secondHandle).isPresent());
            CACHE.removeWriteByHandle(connectionId, secondHandle);
            assertFalse(CACHE.getBlobData(connectionId, 2, 1L).isPresent());
        } finally {
            actualHandles.unregisterConnection(connectionId);
            CACHE.unregisterConnection(connectionId);
        }
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Map<Integer, Map<Integer, FirebirdBlobWrite>> getHandleCache() {
        return (Map<Integer, Map<Integer, FirebirdBlobWrite>>) Plugins.getMemberAccessor().get(FirebirdBlobWriteCache.class.getDeclaredField("writesByHandle"), CACHE);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Map<Integer, Map<Integer, Map<Long, FirebirdBlobWrite>>> getIdCache() {
        return (Map<Integer, Map<Integer, Map<Long, FirebirdBlobWrite>>>) Plugins.getMemberAccessor().get(FirebirdBlobWriteCache.class.getDeclaredField("writesById"), CACHE);
    }
    
    private static Stream<Arguments> missingWriteProvider() {
        return Stream.of(
                Arguments.of("append missing write returns empty", (BooleanSupplier) () -> CACHE.appendSegment(3, 5, new byte[]{1}).isPresent()),
                Arguments.of("close missing write returns empty", (BooleanSupplier) () -> CACHE.closeWrite(4, 6).isPresent()),
                Arguments.of("get blob data missing write returns empty", (BooleanSupplier) () -> CACHE.getBlobData(5, 0, 7L).isPresent()),
                Arguments.of("get blob id missing write returns empty", (BooleanSupplier) () -> CACHE.getBlobId(8, 10).isPresent()));
    }
}
