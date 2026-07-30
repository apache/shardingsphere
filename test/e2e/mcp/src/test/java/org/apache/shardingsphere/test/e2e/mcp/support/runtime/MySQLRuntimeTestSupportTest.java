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

package org.apache.shardingsphere.test.e2e.mcp.support.runtime;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.testcontainers.containers.GenericContainer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class MySQLRuntimeTestSupportTest {
    
    @Test
    void assertCreateLLMRuntimeFixturePreservesInitializationFailure() {
        GenericContainer<?> container = mock(GenericContainer.class);
        IllegalStateException initializationFailure = new IllegalStateException("start failed");
        IllegalStateException cleanupFailure = new IllegalStateException("stop failed");
        doThrow(initializationFailure).when(container).start();
        doThrow(cleanupFailure).when(container).stop();
        try (MockedStatic<MySQLRuntimeTestSupport> mocked = mockStatic(MySQLRuntimeTestSupport.class, CALLS_REAL_METHODS)) {
            mocked.when(MySQLRuntimeTestSupport::createContainer).thenReturn(container);
            IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MySQLRuntimeTestSupport.createLLMRuntimeFixture("logic_db"));
            assertThat(actual, sameInstance(initializationFailure));
            assertThat(actual.getSuppressed().length, is(1));
            assertThat(actual.getSuppressed()[0], sameInstance(cleanupFailure));
        }
    }
}
