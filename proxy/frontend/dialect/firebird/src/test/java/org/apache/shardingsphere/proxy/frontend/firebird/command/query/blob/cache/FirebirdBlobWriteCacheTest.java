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
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

class FirebirdBlobWriteCacheTest {
    
    private static final FirebirdBlobWriteCache CACHE = FirebirdBlobWriteCache.getInstance();
    
    @TempDir
    private Path temporaryDirectory;
    
    @AfterEach
    void tearDown() {
        for (Integer each : getHandleCache().keySet()) {
            CACHE.unregisterConnection(each);
        }
        for (Integer each : getIdCache().keySet()) {
            CACHE.unregisterConnection(each);
        }
    }
    
    @Test
    void assertRegisterAndUnregisterConnection() {
        int connectionId = 1;
        CACHE.registerConnection(connectionId);
        Map<Integer, Map<Integer, FirebirdBlobWrite>> handleMap = getHandleCache();
        Map<Integer, Map<Long, FirebirdBlobWrite>> idMap = getIdCache();
        assertTrue(handleMap.containsKey(connectionId));
        assertTrue(idMap.containsKey(connectionId));
        CACHE.unregisterConnection(connectionId);
        assertFalse(handleMap.containsKey(connectionId));
        assertFalse(idMap.containsKey(connectionId));
    }
    
    @Test
    void assertUnregisterConnectionWithUsedBlob() {
        CACHE.registerBlob(1, 11, 21L, 31, false);
        CACHE.appendSegment(1, 11, new byte[]{1, 2, 3});
        CACHE.closeWrite(1, 11);
        CACHE.useBlobData(1, 21L, 31);
        Path actualBackingFile = getBackingFile(1, 21L);
        assertTrue(Files.exists(actualBackingFile));
        CACHE.unregisterConnection(1);
        assertFalse(Files.exists(actualBackingFile));
        assertFalse(getIdCache().containsKey(1));
        assertFalse(getHandleCache().containsKey(1));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Path getBackingFile(final int connectionId, final long blobId) {
        return (Path) Plugins.getMemberAccessor().get(FirebirdBlobWrite.class.getDeclaredField("backingFile"), getIdCache().get(connectionId).get(blobId));
    }
    
    @Test
    void assertUnregisterConnectionWhenReleaseFails() {
        CACHE.registerBlob(1, 11, 21L, 31, false);
        CACHE.closeWrite(1, 11);
        CACHE.useBlobData(1, 21L, 31);
        Path actualBackingFile = getBackingFile(1, 21L);
        IOException expected = new IOException("foo_delete_failure");
        try (MockedStatic<Files> mocked = mockStatic(Files.class)) {
            mocked.when(() -> Files.delete(actualBackingFile)).thenThrow(expected);
            assertThat(assertThrows(IOException.class, () -> CACHE.unregisterConnection(1)), sameInstance(expected));
            assertTrue(getIdCache().get(1).containsKey(21L));
            assertTrue(getHandleCache().containsKey(1));
        }
        CACHE.unregisterConnection(1);
        assertFalse(Files.exists(actualBackingFile));
    }
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"getTransactionId", "createTempFile", "write", "readAllBytes"})
    void assertUnregisterConnectionDuringBlobAccess(final String methodName) throws Exception {
        byte[] expectedData = new byte[]{1, 2, 3};
        registerClosedBlob(1, 11, 21L, 31, expectedData);
        AtomicReference<Path> actualBackingFile = new AtomicReference<>();
        if ("readAllBytes".equals(methodName)) {
            CACHE.useBlobData(1, 21L, 31);
            actualBackingFile.set(getBackingFile(1, 21L));
            releaseSoftlyReachableData(getIdCache().get(1).get(21L));
        }
        CountDownLatch accessStarted = new CountDownLatch(1);
        CountDownLatch continueAccess = new CountDownLatch(1);
        if ("getTransactionId".equals(methodName)) {
            FirebirdBlobWrite write = spy(getIdCache().get(1).get(21L));
            getIdCache().get(1).put(21L, write);
            doAnswer(invocation -> {
                pauseBlobAccess(accessStarted, continueAccess);
                return invocation.callRealMethod();
            }).when(write).getTransactionId();
        }
        registerClosedBlob(2, 12, 22L, 32, expectedData);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            Future<Optional<byte[]>> actualData = executor.submit(() -> {
                try (MockedStatic<Files> ignored = mockConcurrentFileAccess(methodName, accessStarted, continueAccess, actualBackingFile)) {
                    return CACHE.useBlobData(1, 21L, 31);
                }
            });
            assertTrue(accessStarted.await(5, TimeUnit.SECONDS));
            AtomicReference<Thread> releasingThread = new AtomicReference<>();
            Future<?> unregister = executor.submit(() -> {
                releasingThread.set(Thread.currentThread());
                CACHE.unregisterConnection(1);
            });
            awaitCallCompletionOrWait(unregister, releasingThread);
            Future<Optional<byte[]>> actualOtherData = executor.submit(() -> CACHE.useBlobData(2, 22L, 32));
            assertThat(actualOtherData.get(5, TimeUnit.SECONDS).get(), is(expectedData));
            continueAccess.countDown();
            actualData.get(5, TimeUnit.SECONDS).ifPresent(actual -> assertThat(actual, is(expectedData)));
            unregister.get(5, TimeUnit.SECONDS);
            assertFalse(getIdCache().containsKey(1));
            assertFalse(getHandleCache().containsKey(1));
            if (null != actualBackingFile.get()) {
                assertFalse(Files.exists(actualBackingFile.get()));
            }
        } finally {
            continueAccess.countDown();
            executor.shutdownNow();
            try {
                assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
            } finally {
                CACHE.unregisterConnection(1);
                CACHE.unregisterConnection(2);
                if (null != actualBackingFile.get()) {
                    Files.deleteIfExists(actualBackingFile.get());
                }
            }
        }
    }
    
    private void registerClosedBlob(final int connectionId, final int blobHandle, final long blobId, final int transactionId, final byte[] data) {
        CACHE.registerBlob(connectionId, blobHandle, blobId, transactionId, false);
        CACHE.appendSegment(connectionId, blobHandle, data);
        CACHE.closeWrite(connectionId, blobHandle);
    }
    
    @SneakyThrows(IllegalAccessException.class)
    private void releaseSoftlyReachableData(final FirebirdBlobWrite write) {
        for (Field each : FirebirdBlobWrite.class.getDeclaredFields()) {
            Object value = Plugins.getMemberAccessor().get(each, write);
            if (value instanceof Reference) {
                ((Reference<?>) value).clear();
            }
        }
    }
    
    private void pauseBlobAccess(final CountDownLatch accessStarted, final CountDownLatch continueAccess) throws InterruptedException {
        accessStarted.countDown();
        assertTrue(continueAccess.await(5, TimeUnit.SECONDS));
    }
    
    private MockedStatic<Files> mockConcurrentFileAccess(final String methodName, final CountDownLatch accessStarted, final CountDownLatch continueAccess,
                                                         final AtomicReference<Path> actualBackingFile) {
        return mockStatic(Files.class, invocation -> {
            if (methodName.equals(invocation.getMethod().getName())) {
                pauseBlobAccess(accessStarted, continueAccess);
            }
            Object result = invocation.callRealMethod();
            if ("createTempFile".equals(invocation.getMethod().getName())) {
                actualBackingFile.set((Path) result);
            }
            return result;
        });
    }
    
    private void awaitCallCompletionOrWait(final Future<?> call, final AtomicReference<Thread> threadReference) {
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> {
            if (call.isDone()) {
                return true;
            }
            Thread thread = threadReference.get();
            return null != thread && (Thread.State.BLOCKED == thread.getState() || Thread.State.WAITING == thread.getState() || Thread.State.TIMED_WAITING == thread.getState());
        });
    }
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"createTempFile", "write", "readAllBytes"})
    void assertUseBlobDataConcurrently(final String methodName) throws Exception {
        byte[] expectedData = new byte[]{1, 2, 3};
        registerClosedBlob(1, 11, 21L, 31, expectedData);
        AtomicReference<Path> actualBackingFile = new AtomicReference<>();
        if ("readAllBytes".equals(methodName)) {
            CACHE.useBlobData(1, 21L, 31);
            actualBackingFile.set(getBackingFile(1, 21L));
            releaseSoftlyReachableData(getIdCache().get(1).get(21L));
        }
        CountDownLatch accessStarted = new CountDownLatch(1);
        CountDownLatch continueAccess = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Optional<byte[]>> actualFirstUse = executor.submit(() -> {
                try (MockedStatic<Files> ignored = mockConcurrentFileAccess(methodName, accessStarted, continueAccess, actualBackingFile)) {
                    return CACHE.useBlobData(1, 21L, 31);
                }
            });
            assertTrue(accessStarted.await(5, TimeUnit.SECONDS));
            AtomicReference<Thread> usingThread = new AtomicReference<>();
            Future<Optional<byte[]>> actualSecondUse = executor.submit(() -> {
                usingThread.set(Thread.currentThread());
                return CACHE.useBlobData(1, 21L, 31);
            });
            awaitCallCompletionOrWait(actualSecondUse, usingThread);
            continueAccess.countDown();
            byte[] actual = actualFirstUse.get(5, TimeUnit.SECONDS).get();
            assertThat(actual, is(expectedData));
            assertThat(actualSecondUse.get(5, TimeUnit.SECONDS).get(), sameInstance(actual));
            assertThat(Files.readAllBytes(actualBackingFile.get()), is(expectedData));
        } finally {
            continueAccess.countDown();
            executor.shutdownNow();
            try {
                assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
            } finally {
                CACHE.unregisterConnection(1);
                if (null != actualBackingFile.get()) {
                    Files.deleteIfExists(actualBackingFile.get());
                }
            }
        }
    }
    
    @Test
    void assertRegisterBlobCreatesMappingsWhenConnectionMissing() {
        CACHE.registerBlob(2, 3, 4L, 1, false);
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
        assertFalse(CACHE.isClosed(connectionId, blobId));
        CACHE.registerBlob(connectionId, blobHandle, blobId, 1, false);
        OptionalInt actualSizeAfterAppend = CACHE.appendSegment(connectionId, blobHandle, new byte[]{1, 2, 3});
        assertTrue(actualSizeAfterAppend.isPresent());
        assertThat(actualSizeAfterAppend.getAsInt(), is(3));
        assertFalse(CACHE.isClosed(connectionId, blobId));
        OptionalInt actualSizeAfterClose = CACHE.closeWrite(connectionId, blobHandle);
        assertTrue(actualSizeAfterClose.isPresent());
        assertThat(actualSizeAfterClose.getAsInt(), is(3));
        assertFalse(CACHE.getBlobId(connectionId, blobHandle).isPresent());
        assertTrue(CACHE.isClosed(connectionId, blobId));
        Optional<byte[]> actualBlobData = CACHE.getBlobData(connectionId, blobId);
        assertTrue(actualBlobData.isPresent());
        assertThat(actualBlobData.get(), is(new byte[]{1, 2, 3}));
        CACHE.removeWrite(connectionId, blobId);
        assertFalse(getHandleCache().get(connectionId).containsKey(blobHandle));
        assertFalse(getIdCache().get(connectionId).containsKey(blobId));
    }
    
    @Test
    void assertGetBlobDataAfterClose() {
        CACHE.registerBlob(1, 11, 21L, 31, false);
        CACHE.appendSegment(1, 11, new byte[]{1, 2});
        CACHE.appendSegment(1, 11, new byte[]{3});
        CACHE.closeWrite(1, 11);
        byte[] actual = CACHE.getBlobData(1, 21L).get();
        assertThat(actual, is(new byte[]{1, 2, 3}));
        assertThat(CACHE.getBlobData(1, 21L).get(), sameInstance(actual));
    }
    
    @Test
    void assertReuseBlobData() {
        CACHE.registerBlob(1, 11, 21L, 31, false);
        CACHE.appendSegment(1, 11, new byte[]{1, 2, 3});
        CACHE.closeWrite(1, 11);
        byte[] actual = CACHE.useBlobData(1, 21L, 31).get();
        assertThat(actual, is(new byte[]{1, 2, 3}));
        assertThat(CACHE.useBlobData(1, 21L, 31).get(), sameInstance(actual));
    }
    
    @Test
    void assertUseBlobDataOfOtherTransaction() {
        CACHE.registerBlob(1, 11, 21L, 31, false);
        CACHE.closeWrite(1, 11);
        assertFalse(CACHE.useBlobData(1, 21L, 32).isPresent());
    }
    
    @Test
    void assertUseBlobDataBeforeClose() {
        CACHE.registerBlob(1, 11, 21L, 31, false);
        assertFalse(CACHE.useBlobData(1, 21L, 31).isPresent());
    }
    
    @Test
    void assertUseBlobDataAfterGarbageCollection() {
        CACHE.registerBlob(1, 11, 21L, 31, false);
        CACHE.appendSegment(1, 11, new byte[]{1, 2, 3});
        CACHE.closeWrite(1, 11);
        CACHE.useBlobData(1, 21L, 31);
        releaseSoftlyReachableData(getIdCache().get(1).get(21L));
        assertThat(CACHE.useBlobData(1, 21L, 31).get(), is(new byte[]{1, 2, 3}));
        releaseSoftlyReachableData(getIdCache().get(1).get(21L));
        assertThat(CACHE.getBlobData(1, 21L).get(), is(new byte[]{1, 2, 3}));
    }
    
    @Test
    void assertUseBlobDataWhenBackingCreationFails() {
        CACHE.registerBlob(1, 11, 21L, 31, false);
        CACHE.appendSegment(1, 11, new byte[]{1, 2, 3});
        CACHE.closeWrite(1, 11);
        IOException expected = new IOException("foo_create_failure");
        try (MockedStatic<Files> mocked = mockStatic(Files.class)) {
            mocked.when(() -> Files.createTempFile("shardingsphere-firebird-blob-", ".tmp")).thenThrow(expected);
            assertThat(assertThrows(IOException.class, () -> CACHE.useBlobData(1, 21L, 31)), sameInstance(expected));
            assertThat(CACHE.getBlobData(1, 21L).get(), is(new byte[]{1, 2, 3}));
        }
        assertThat(CACHE.useBlobData(1, 21L, 31).get(), is(new byte[]{1, 2, 3}));
    }
    
    @Test
    void assertUseBlobDataWhenBackingWriteFails() throws IOException {
        CACHE.registerBlob(1, 11, 21L, 31, false);
        byte[] expectedData = new byte[]{1, 2, 3};
        CACHE.appendSegment(1, 11, expectedData);
        CACHE.closeWrite(1, 11);
        Path actualBackingFile = Files.createTempFile(temporaryDirectory, "foo_blob", ".tmp");
        IOException expected = new IOException("foo_write_failure");
        try (MockedStatic<Files> mocked = mockStatic(Files.class)) {
            mocked.when(() -> Files.createTempFile("shardingsphere-firebird-blob-", ".tmp")).thenReturn(actualBackingFile);
            mocked.when(() -> Files.write(actualBackingFile, expectedData)).thenThrow(expected);
            assertThat(assertThrows(IOException.class, () -> CACHE.useBlobData(1, 21L, 31)), sameInstance(expected));
            assertThat(CACHE.getBlobData(1, 21L).get(), is(expectedData));
            assertThat(getBackingFile(1, 21L), is(actualBackingFile));
        }
        assertThat(CACHE.useBlobData(1, 21L, 31).get(), is(expectedData));
        assertThat(Files.readAllBytes(actualBackingFile), is(expectedData));
    }
    
    @Test
    void assertUseBlobDataWhenBackingReadFails() {
        CACHE.registerBlob(1, 11, 21L, 31, false);
        CACHE.appendSegment(1, 11, new byte[]{1, 2, 3});
        CACHE.closeWrite(1, 11);
        CACHE.useBlobData(1, 21L, 31);
        releaseSoftlyReachableData(getIdCache().get(1).get(21L));
        Path actualBackingFile = getBackingFile(1, 21L);
        IOException expected = new IOException("foo_read_failure");
        try (MockedStatic<Files> mocked = mockStatic(Files.class)) {
            mocked.when(() -> Files.readAllBytes(actualBackingFile)).thenThrow(expected);
            assertThat(assertThrows(IOException.class, () -> CACHE.useBlobData(1, 21L, 31)), sameInstance(expected));
            assertThat(getBackingFile(1, 21L), is(actualBackingFile));
        }
        assertThat(CACHE.useBlobData(1, 21L, 31).get(), is(new byte[]{1, 2, 3}));
    }
    
    @Test
    void assertRemoveWriteDoesNotRemoveReusedHandle() {
        int connectionId = 12;
        int blobHandle = 8;
        long oldBlobId = 9L;
        long expectedBlobId = 10L;
        CACHE.registerConnection(connectionId);
        CACHE.registerBlob(connectionId, blobHandle, oldBlobId, 1, false);
        CACHE.appendSegment(connectionId, blobHandle, new byte[]{1, 2, 3});
        CACHE.closeWrite(connectionId, blobHandle);
        CACHE.useBlobData(connectionId, oldBlobId, 1);
        Path actualBackingFile = getBackingFile(connectionId, oldBlobId);
        CACHE.registerBlob(connectionId, blobHandle, expectedBlobId, 1, false);
        CACHE.removeWrite(connectionId, oldBlobId);
        assertFalse(Files.exists(actualBackingFile));
        OptionalLong actualBlobId = CACHE.getBlobId(connectionId, blobHandle);
        assertTrue(actualBlobId.isPresent());
        assertThat(actualBlobId.getAsLong(), is(expectedBlobId));
    }
    
    @Test
    void assertRemoveWriteWhenReleaseFails() {
        CACHE.registerBlob(1, 11, 21L, 31, false);
        CACHE.appendSegment(1, 11, new byte[]{1, 2, 3});
        CACHE.closeWrite(1, 11);
        CACHE.useBlobData(1, 21L, 31);
        Path actualBackingFile = getBackingFile(1, 21L);
        IOException expected = new IOException("foo_delete_failure");
        try (MockedStatic<Files> mocked = mockStatic(Files.class)) {
            mocked.when(() -> Files.delete(actualBackingFile)).thenThrow(expected);
            assertThat(assertThrows(IOException.class, () -> CACHE.removeWrite(1, 21L)), sameInstance(expected));
            assertThat(CACHE.useBlobData(1, 21L, 31).get(), is(new byte[]{1, 2, 3}));
            assertThat(getBackingFile(1, 21L), is(actualBackingFile));
        }
        CACHE.removeWrite(1, 21L);
        assertFalse(Files.exists(actualBackingFile));
        assertFalse(CACHE.getBlobData(1, 21L).isPresent());
    }
    
    @Test
    void assertRemoveWriteWhenWriteMissing() {
        int connectionId = 7;
        CACHE.registerConnection(connectionId);
        CACHE.removeWrite(connectionId, 11L);
        assertTrue(getHandleCache().get(connectionId).isEmpty());
        assertTrue(getIdCache().get(connectionId).isEmpty());
    }
    
    @Test
    void assertRemoveWriteWhenConnectionMissing() {
        CACHE.removeWrite(1, 21L);
        assertFalse(getIdCache().containsKey(1));
        assertFalse(getHandleCache().containsKey(1));
    }
    
    @Test
    void assertRemoveTransaction() {
        CACHE.registerBlob(1, 11, 21L, 31, false);
        CACHE.appendSegment(1, 11, new byte[]{1, 2, 3});
        CACHE.closeWrite(1, 11);
        CACHE.useBlobData(1, 21L, 31);
        CACHE.registerBlob(1, 11, 22L, 32, false);
        CACHE.registerBlob(1, 12, 23L, 31, false);
        CACHE.registerBlob(1, 13, 24L, 32, false);
        CACHE.closeWrite(1, 13);
        CACHE.useBlobData(1, 24L, 32);
        CACHE.registerBlob(2, 12, 21L, 31, false);
        Path actualCompletedFile = getBackingFile(1, 21L);
        Path actualOtherTransactionFile = getBackingFile(1, 24L);
        CACHE.removeTransaction(1, 31);
        assertFalse(Files.exists(actualCompletedFile));
        assertTrue(Files.exists(actualOtherTransactionFile));
        assertFalse(CACHE.getBlobData(1, 21L).isPresent());
        assertFalse(CACHE.getBlobData(1, 23L).isPresent());
        assertFalse(CACHE.getBlobId(1, 12).isPresent());
        assertThat(CACHE.getBlobId(1, 11), is(OptionalLong.of(22L)));
        assertTrue(CACHE.useBlobData(1, 24L, 32).isPresent());
        assertTrue(CACHE.getBlobData(2, 21L).isPresent());
        assertThat(CACHE.getBlobId(2, 12), is(OptionalLong.of(21L)));
    }
    
    @Test
    void assertRemoveTransactionWithoutWrites() {
        CACHE.registerConnection(1);
        CACHE.removeTransaction(1, 31);
        assertTrue(getHandleCache().get(1).isEmpty());
        assertTrue(getIdCache().get(1).isEmpty());
    }
    
    @Test
    void assertRemoveTransactionWhenConnectionMissing() {
        CACHE.removeTransaction(1, 31);
        assertFalse(getIdCache().containsKey(1));
        assertFalse(getHandleCache().containsKey(1));
    }
    
    @Test
    void assertRemoveTransactionDuringUnregisterConnection() throws Exception {
        registerClosedBlob(1, 11, 21L, 31, new byte[]{1, 2, 3});
        CACHE.useBlobData(1, 21L, 31);
        Path actualBackingFile = getBackingFile(1, 21L);
        Map<Long, FirebirdBlobWrite> idMap = spy(getIdCache().get(1));
        getIdCache().put(1, idMap);
        CountDownLatch writeCaptured = new CountDownLatch(1);
        CountDownLatch continueCleanup = new CountDownLatch(1);
        doAnswer(invocation -> {
            Object result = invocation.callRealMethod();
            pauseBlobAccess(writeCaptured, continueCleanup);
            return result;
        }).doCallRealMethod().when(idMap).get(21L);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> actual = executor.submit(() -> CACHE.removeTransaction(1, 31));
            assertTrue(writeCaptured.await(5, TimeUnit.SECONDS));
            CACHE.unregisterConnection(1);
            assertFalse(getIdCache().containsKey(1));
            assertFalse(getHandleCache().containsKey(1));
            assertFalse(Files.exists(actualBackingFile));
            continueCleanup.countDown();
            actual.get(5, TimeUnit.SECONDS);
            assertFalse(getIdCache().containsKey(1));
            assertFalse(getHandleCache().containsKey(1));
        } finally {
            continueCleanup.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Map<Integer, Map<Integer, FirebirdBlobWrite>> getHandleCache() {
        return (Map<Integer, Map<Integer, FirebirdBlobWrite>>) Plugins.getMemberAccessor().get(FirebirdBlobWriteCache.class.getDeclaredField("writesByHandle"), CACHE);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Map<Integer, Map<Long, FirebirdBlobWrite>> getIdCache() {
        return (Map<Integer, Map<Long, FirebirdBlobWrite>>) Plugins.getMemberAccessor().get(FirebirdBlobWriteCache.class.getDeclaredField("writesById"), CACHE);
    }
    
    private static Stream<Arguments> missingWriteProvider() {
        return Stream.of(
                Arguments.of("append missing write returns empty", (BooleanSupplier) () -> CACHE.appendSegment(3, 5, new byte[]{1}).isPresent()),
                Arguments.of("close missing write returns empty", (BooleanSupplier) () -> CACHE.closeWrite(4, 6).isPresent()),
                Arguments.of("get blob data missing write returns empty", (BooleanSupplier) () -> CACHE.getBlobData(5, 7L).isPresent()),
                Arguments.of("get blob id missing write returns empty", (BooleanSupplier) () -> CACHE.getBlobId(8, 10).isPresent()));
    }
}
