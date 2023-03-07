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

package org.apache.shardingsphere.infra.executor.sql.process;

import org.junit.After;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ExecuteIDContextTest {
    
    @After
    public void tearDown() {
        ExecuteIDContext.remove();
    }
    
    @Test
    public void assertIsEmpty() {
        assertTrue(ExecuteIDContext.isEmpty());
        ExecuteIDContext.set("123e4567e89b12d3a456426655440000");
        assertFalse(ExecuteIDContext.isEmpty());
    }
    
    @Test
    public void assertGet() {
        assertNull(ExecuteIDContext.get());
        ExecuteIDContext.set("123e4567e89b12d3a456426655440000");
        assertThat(ExecuteIDContext.get(), is("123e4567e89b12d3a456426655440000"));
    }
    
    @Test
    public void assertSet() {
        assertNull(ExecuteIDContext.get());
        ExecuteIDContext.set("123e4567e89b12d3a456426655440000");
        assertThat(ExecuteIDContext.get(), is("123e4567e89b12d3a456426655440000"));
        ExecuteIDContext.set("123e4567e89b12d3a456426655440001");
        assertThat(ExecuteIDContext.get(), is("123e4567e89b12d3a456426655440001"));
    }
    
    @Test
    public void assertRemove() {
        assertNull(ExecuteIDContext.get());
        ExecuteIDContext.set("123e4567e89b12d3a456426655440000");
        assertThat(ExecuteIDContext.get(), is("123e4567e89b12d3a456426655440000"));
        ExecuteIDContext.remove();
        assertNull(ExecuteIDContext.get());
    }
}
