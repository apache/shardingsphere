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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FirebirdBlobHandleGeneratorTest {
    
    private static final int CONNECTION_ID = 91;
    
    private static final int INVALID_OBJECT_HANDLE = 0xFFFF;
    
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
}
