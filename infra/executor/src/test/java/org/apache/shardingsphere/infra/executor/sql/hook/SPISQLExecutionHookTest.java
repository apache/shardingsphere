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

package org.apache.shardingsphere.infra.executor.sql.hook;

import org.apache.shardingsphere.infra.executor.sql.hook.fixture.SQLExecutionHookFixture;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertTrue;

public final class SPISQLExecutionHookTest {
    
    private SPISQLExecutionHook spiSQLExecutionHook;
    
    @Before
    public void setUp() {
        SQLExecutionHookFixture.clearActions();
        spiSQLExecutionHook = new SPISQLExecutionHook();
    }
    
    @Test
    public void assertStart() {
        spiSQLExecutionHook.start("ds", "SELECT 1", Collections.emptyList(), null, true, null);
        assertTrue(SQLExecutionHookFixture.containsAction("start"));
    }
    
    @Test
    public void assertFinishSuccess() {
        spiSQLExecutionHook.finishSuccess();
        assertTrue(SQLExecutionHookFixture.containsAction("finishSuccess"));
    }
    
    @Test
    public void assertFinishFailure() {
        spiSQLExecutionHook.finishFailure(null);
        assertTrue(SQLExecutionHookFixture.containsAction("finishFailure"));
    }
}
