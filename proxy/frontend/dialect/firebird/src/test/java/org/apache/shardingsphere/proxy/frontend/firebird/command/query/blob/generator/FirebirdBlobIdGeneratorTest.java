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

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FirebirdBlobIdGeneratorTest {
    
    @Test
    void assertSignedTransactionHandle() {
        int connectionId = 10925;
        FirebirdBlobIdGenerator actualGenerator = FirebirdBlobIdGenerator.getInstance();
        actualGenerator.registerConnection(connectionId);
        try {
            assertThat(actualGenerator.nextBlobId(connectionId, 32768), is(1L));
            assertThat(actualGenerator.nextBlobId(connectionId, -32768), is(2L));
            actualGenerator.unregisterTransaction(connectionId, -32768);
            assertThat(actualGenerator.nextBlobId(connectionId, 32768), is(1L));
        } finally {
            actualGenerator.unregisterConnection(connectionId);
        }
    }
    
    @Test
    void assertTransactionCountersAreIndependentAndReleased() {
        FirebirdBlobIdGenerator actualGenerator = FirebirdBlobIdGenerator.getInstance();
        actualGenerator.registerConnection(32);
        try {
            assertThat(actualGenerator.nextBlobId(32, 1), is(1L));
            assertThat(actualGenerator.nextBlobId(32, 2), is(1L));
            assertThat(actualGenerator.nextBlobId(32, 1), is(2L));
            actualGenerator.unregisterTransaction(32, 1);
            assertThat(actualGenerator.nextBlobId(32, 1), is(1L));
            assertThat(actualGenerator.nextBlobId(32, 2), is(2L));
        } finally {
            actualGenerator.unregisterConnection(32);
        }
    }
}
