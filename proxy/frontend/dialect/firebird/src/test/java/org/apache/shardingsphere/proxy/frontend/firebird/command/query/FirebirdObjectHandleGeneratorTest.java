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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FirebirdObjectHandleGeneratorTest {
    
    private static final int CONNECTION_ID = 1;
    
    private static final FirebirdObjectHandleGenerator GENERATOR = FirebirdObjectHandleGenerator.getInstance();
    
    @AfterEach
    void tearDown() {
        GENERATOR.unregisterConnection(CONNECTION_ID);
        GENERATOR.unregisterConnection(2);
    }
    
    @Test
    void assertNextHandle() {
        assertThat(GENERATOR.nextHandle(CONNECTION_ID), is(1));
        assertThat(GENERATOR.nextHandle(CONNECTION_ID), is(2));
        assertThat(GENERATOR.nextHandle(2), is(1));
    }
    
    @Test
    void assertLastHandle() {
        assertThat(GENERATOR.getLastHandle(CONNECTION_ID), is(0));
        assertThat(GENERATOR.nextHandle(CONNECTION_ID), is(1));
        assertThat(GENERATOR.nextHandle(CONNECTION_ID), is(2));
        GENERATOR.releaseHandle(CONNECTION_ID, 2);
        assertThat(GENERATOR.getLastHandle(CONNECTION_ID), is(2));
        assertThat(GENERATOR.getLastHandle(2), is(0));
        GENERATOR.unregisterConnection(CONNECTION_ID);
        assertThat(GENERATOR.getLastHandle(CONNECTION_ID), is(0));
    }
    
    @Test
    void assertNextHandleConcurrently() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            List<Callable<Integer>> tasks = new ArrayList<>();
            for (int i = 0; i < 128; i++) {
                tasks.add(() -> GENERATOR.nextHandle(CONNECTION_ID));
            }
            Set<Integer> actual = new HashSet<>();
            for (Future<Integer> each : executor.invokeAll(tasks)) {
                actual.add(each.get());
            }
            assertThat(actual.size(), is(128));
        } finally {
            executor.shutdownNow();
        }
    }
    
    @Test
    void assertReleaseHandle() {
        GENERATOR.nextHandle(CONNECTION_ID);
        GENERATOR.nextHandle(CONNECTION_ID);
        GENERATOR.nextHandle(CONNECTION_ID);
        GENERATOR.releaseHandle(CONNECTION_ID, 2);
        GENERATOR.releaseHandle(CONNECTION_ID, 1);
        assertThat(GENERATOR.nextHandle(CONNECTION_ID), is(1));
        assertThat(GENERATOR.nextHandle(CONNECTION_ID), is(2));
        assertThat(GENERATOR.nextHandle(CONNECTION_ID), is(4));
    }
    
    @Test
    void assertReleaseHandleWithoutConnection() {
        GENERATOR.releaseHandle(CONNECTION_ID, 1);
        assertThat(GENERATOR.nextHandle(CONNECTION_ID), is(1));
    }
    
    @Test
    void assertUnregisterConnection() {
        GENERATOR.nextHandle(CONNECTION_ID);
        GENERATOR.unregisterConnection(CONNECTION_ID);
        assertThat(GENERATOR.nextHandle(CONNECTION_ID), is(1));
    }
}
