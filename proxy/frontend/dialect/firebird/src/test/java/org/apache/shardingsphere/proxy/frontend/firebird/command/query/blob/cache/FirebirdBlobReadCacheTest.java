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

import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobReadCache.BlobSegment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.OptionalInt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdBlobReadCacheTest {
    
    private static final FirebirdBlobReadCache CACHE = FirebirdBlobReadCache.getInstance();
    
    private static final int CONNECTION_ID = 1;
    
    private static final int OTHER_CONNECTION_ID = 2;
    
    private static final int BLOB_HANDLE = 3;
    
    private static final int SEEK_MODE_FROM_HEAD = 0;
    
    private static final int SEEK_MODE_RELATIVE = 1;
    
    private static final int SEEK_MODE_FROM_TAIL = 2;
    
    @AfterEach
    void tearDown() {
        CACHE.unregisterConnection(CONNECTION_ID);
        CACHE.unregisterConnection(OTHER_CONNECTION_ID);
    }
    
    @Test
    void assertUnregisterConnectionRemovesBlobs() {
        CACHE.registerConnection(CONNECTION_ID);
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1}, true);
        CACHE.unregisterConnection(CONNECTION_ID);
        assertFalse(CACHE.getTotalSize(CONNECTION_ID, BLOB_HANDLE).isPresent());
    }
    
    @Test
    void assertRegisterBlobKeepsContentReference() {
        CACHE.registerConnection(CONNECTION_ID);
        byte[] content = new byte[]{1, 2};
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE, content, true);
        content[0] = 9;
        Optional<BlobSegment> actual = CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, content.length);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getData(), is(new byte[]{9, 2}));
        assertThat(actual.get().getData(), not(sameInstance(content)));
    }
    
    @Test
    void assertReadMissingBlob() {
        CACHE.registerConnection(CONNECTION_ID);
        assertFalse(CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, 1).isPresent());
    }
    
    @Test
    void assertReadEmptyBlob() {
        CACHE.registerConnection(CONNECTION_ID);
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[0], true);
        assertFalse(CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, 1).isPresent());
    }
    
    @Test
    void assertReadPartialSegmentAdvancesCursor() {
        CACHE.registerConnection(CONNECTION_ID);
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2, 3}, true);
        Optional<BlobSegment> actual = CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, 2);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getData(), is(new byte[]{1, 2}));
        assertFalse(actual.get().isComplete());
        assertThat(CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, 2).get().getData(), is(new byte[]{3}));
    }
    
    @Test
    void assertReadCompleteSegmentKeepsCursor() {
        CACHE.registerConnection(CONNECTION_ID);
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2}, true);
        Optional<BlobSegment> actual = CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, 2);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getData(), is(new byte[]{1, 2}));
        assertTrue(actual.get().isComplete());
        assertFalse(CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, 2).isPresent());
        assertTrue(CACHE.getTotalSize(CONNECTION_ID, BLOB_HANDLE).isPresent());
    }
    
    @Test
    void assertReadZeroLengthDoesNotAdvanceCursor() {
        CACHE.registerConnection(CONNECTION_ID);
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2}, true);
        Optional<BlobSegment> actual = CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, 0);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getData(), is(new byte[0]));
        assertFalse(actual.get().isComplete());
        assertThat(CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, 2).get().getData(), is(new byte[]{1, 2}));
    }
    
    @Test
    void assertGetTotalSize() {
        CACHE.registerConnection(CONNECTION_ID);
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2, 3}, true);
        CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, 2);
        OptionalInt actual = CACHE.getTotalSize(CONNECTION_ID, BLOB_HANDLE);
        assertTrue(actual.isPresent());
        assertThat(actual.getAsInt(), is(3));
    }
    
    @Test
    void assertGetTotalSizeOfMissingBlob() {
        CACHE.registerConnection(CONNECTION_ID);
        assertFalse(CACHE.getTotalSize(CONNECTION_ID, BLOB_HANDLE).isPresent());
    }
    
    @Test
    void assertIsStreamBlob() {
        CACHE.registerConnection(CONNECTION_ID);
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1}, true);
        assertThat(CACHE.isStreamBlob(CONNECTION_ID, BLOB_HANDLE), is(Optional.of(true)));
    }
    
    @Test
    void assertIsStreamBlobWithSegmentedBlob() {
        CACHE.registerConnection(CONNECTION_ID);
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1}, false);
        assertThat(CACHE.isStreamBlob(CONNECTION_ID, BLOB_HANDLE), is(Optional.of(false)));
    }
    
    @Test
    void assertIsStreamBlobWithMissingBlob() {
        CACHE.registerConnection(CONNECTION_ID);
        assertThat(CACHE.isStreamBlob(CONNECTION_ID, BLOB_HANDLE), is(Optional.empty()));
    }
    
    @Test
    void assertSeekFromHead() {
        CACHE.registerConnection(CONNECTION_ID);
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2, 3, 4}, true);
        assertThat(CACHE.seek(CONNECTION_ID, BLOB_HANDLE, SEEK_MODE_FROM_HEAD, 2).getAsInt(), is(2));
        assertThat(CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, 4).get().getData(), is(new byte[]{3, 4}));
    }
    
    @Test
    void assertSeekRelativeToCurrentPosition() {
        CACHE.registerConnection(CONNECTION_ID);
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2, 3, 4}, true);
        CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, 1);
        assertThat(CACHE.seek(CONNECTION_ID, BLOB_HANDLE, SEEK_MODE_RELATIVE, 2).getAsInt(), is(3));
        assertThat(CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, 4).get().getData(), is(new byte[]{4}));
    }
    
    @Test
    void assertSeekFromTail() {
        CACHE.registerConnection(CONNECTION_ID);
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2, 3, 4}, true);
        assertThat(CACHE.seek(CONNECTION_ID, BLOB_HANDLE, SEEK_MODE_FROM_TAIL, -1).getAsInt(), is(3));
        assertThat(CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, 4).get().getData(), is(new byte[]{4}));
    }
    
    @Test
    void assertSeekBeyondContent() {
        CACHE.registerConnection(CONNECTION_ID);
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2}, true);
        assertThat(CACHE.seek(CONNECTION_ID, BLOB_HANDLE, SEEK_MODE_FROM_HEAD, 5).getAsInt(), is(2));
        assertFalse(CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, 2).isPresent());
    }
    
    @Test
    void assertSeekBeforeContent() {
        CACHE.registerConnection(CONNECTION_ID);
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2}, true);
        assertThat(CACHE.seek(CONNECTION_ID, BLOB_HANDLE, SEEK_MODE_FROM_TAIL, -5).getAsInt(), is(0));
        assertThat(CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, 2).get().getData(), is(new byte[]{1, 2}));
    }
    
    @Test
    void assertSeekWithUnknownModePositionsFromHead() {
        CACHE.registerConnection(CONNECTION_ID);
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2, 3}, true);
        CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, 2);
        assertThat(CACHE.seek(CONNECTION_ID, BLOB_HANDLE, 3, 1).getAsInt(), is(1));
    }
    
    @Test
    void assertSeekAfterFullReadAllowsReadingAgain() {
        CACHE.registerConnection(CONNECTION_ID);
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2}, true);
        CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, 2);
        assertThat(CACHE.seek(CONNECTION_ID, BLOB_HANDLE, SEEK_MODE_FROM_HEAD, 0).getAsInt(), is(0));
        assertThat(CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE, 2).get().getData(), is(new byte[]{1, 2}));
    }
    
    @Test
    void assertSeekMissingBlob() {
        CACHE.registerConnection(CONNECTION_ID);
        assertFalse(CACHE.seek(CONNECTION_ID, BLOB_HANDLE, SEEK_MODE_FROM_HEAD, 0).isPresent());
    }
    
    @Test
    void assertRemoveBlob() {
        CACHE.registerConnection(CONNECTION_ID);
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1}, true);
        CACHE.removeBlob(CONNECTION_ID, BLOB_HANDLE);
        assertFalse(CACHE.getTotalSize(CONNECTION_ID, BLOB_HANDLE).isPresent());
    }
    
    @Test
    void assertConnectionsAndHandlesAreIsolated() {
        CACHE.registerConnection(CONNECTION_ID);
        CACHE.registerConnection(OTHER_CONNECTION_ID);
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1}, true);
        CACHE.registerBlob(CONNECTION_ID, BLOB_HANDLE + 1, new byte[]{2, 3}, true);
        CACHE.registerBlob(OTHER_CONNECTION_ID, BLOB_HANDLE, new byte[]{4, 5, 6}, true);
        CACHE.seek(CONNECTION_ID, BLOB_HANDLE, SEEK_MODE_FROM_TAIL, 0);
        assertThat(CACHE.readSegment(CONNECTION_ID, BLOB_HANDLE + 1, 2).get().getData(), is(new byte[]{2, 3}));
        assertThat(CACHE.readSegment(OTHER_CONNECTION_ID, BLOB_HANDLE, 3).get().getData(), is(new byte[]{4, 5, 6}));
    }
}
