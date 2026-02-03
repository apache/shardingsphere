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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.upload;

import lombok.SneakyThrows;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdBlobUploadCacheTest {
    
    private static final FirebirdBlobUploadCache CACHE = FirebirdBlobUploadCache.getInstance();
    
    @AfterEach
    void tearDown() {
        getHandleCache().clear();
        getIdCache().clear();
    }
    
    @Test
    void assertRegisterAndUnregisterConnection() {
        int connectionId = 1;
        CACHE.registerConnection(connectionId);
        Map<Integer, Map<Integer, FirebirdBlobUpload>> handleMap = getHandleCache();
        Map<Integer, Map<Long, FirebirdBlobUpload>> idMap = getIdCache();
        assertTrue(handleMap.containsKey(connectionId));
        assertTrue(idMap.containsKey(connectionId));
        CACHE.unregisterConnection(connectionId);
        assertFalse(handleMap.containsKey(connectionId));
        assertFalse(idMap.containsKey(connectionId));
    }
    
    @Test
    void assertRegisterBlobCreatesMappingsWhenConnectionMissing() {
        CACHE.registerBlob(2, 3, 4L);
        OptionalLong actualBlobId = CACHE.getBlobId(2, 3);
        assertTrue(actualBlobId.isPresent());
        assertThat(actualBlobId.getAsLong(), is(4L));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("missingUploadProvider")
    void assertMissingUploadReturnsEmpty(final String caseName, final BooleanSupplier invocation) {
        assertFalse(invocation.getAsBoolean(), caseName);
    }
    
    @Test
    void assertIsClosedStatusTransitions() {
        int connectionId = 6;
        int blobHandle = 8;
        long blobId = 9L;
        CACHE.registerConnection(connectionId);
        boolean actualClosedWhenMissing = CACHE.isClosed(connectionId, blobId);
        assertFalse(actualClosedWhenMissing);
        CACHE.registerBlob(connectionId, blobHandle, blobId);
        OptionalInt actualSizeAfterAppend = CACHE.appendSegment(connectionId, blobHandle, new byte[]{1, 2, 3});
        assertTrue(actualSizeAfterAppend.isPresent());
        assertThat(actualSizeAfterAppend.getAsInt(), is(3));
        assertFalse(CACHE.isClosed(connectionId, blobId));
        OptionalInt actualSizeAfterClose = CACHE.closeUpload(connectionId, blobHandle);
        assertTrue(actualSizeAfterClose.isPresent());
        assertThat(actualSizeAfterClose.getAsInt(), is(3));
        assertTrue(CACHE.isClosed(connectionId, blobId));
        Optional<byte[]> actualBlobData = CACHE.getBlobData(connectionId, blobId);
        assertTrue(actualBlobData.isPresent());
        assertThat(actualBlobData.get(), is(new byte[]{1, 2, 3}));
        CACHE.removeUpload(connectionId, blobId);
        assertFalse(getHandleCache().get(connectionId).containsKey(blobHandle));
        assertFalse(getIdCache().get(connectionId).containsKey(blobId));
    }
    
    @Test
    void assertRemoveUploadWhenUploadMissing() {
        int connectionId = 7;
        CACHE.registerConnection(connectionId);
        CACHE.removeUpload(connectionId, 11L);
        assertTrue(getHandleCache().get(connectionId).isEmpty());
        assertTrue(getIdCache().get(connectionId).isEmpty());
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Map<Integer, Map<Integer, FirebirdBlobUpload>> getHandleCache() {
        return (Map<Integer, Map<Integer, FirebirdBlobUpload>>) Plugins.getMemberAccessor().get(FirebirdBlobUploadCache.class.getDeclaredField("uploadsByHandle"), CACHE);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Map<Integer, Map<Long, FirebirdBlobUpload>> getIdCache() {
        return (Map<Integer, Map<Long, FirebirdBlobUpload>>) Plugins.getMemberAccessor().get(FirebirdBlobUploadCache.class.getDeclaredField("uploadsById"), CACHE);
    }
    
    private static Stream<Arguments> missingUploadProvider() {
        return Stream.of(
                Arguments.of("append missing upload returns empty", (BooleanSupplier) () -> CACHE.appendSegment(3, 5, new byte[]{1}).isPresent()),
                Arguments.of("close missing upload returns empty", (BooleanSupplier) () -> CACHE.closeUpload(4, 6).isPresent()),
                Arguments.of("get blob data missing upload returns empty", (BooleanSupplier) () -> CACHE.getBlobData(5, 7L).isPresent()),
                Arguments.of("get blob id missing upload returns empty", (BooleanSupplier) () -> CACHE.getBlobId(8, 10).isPresent())
        );
    }
}
