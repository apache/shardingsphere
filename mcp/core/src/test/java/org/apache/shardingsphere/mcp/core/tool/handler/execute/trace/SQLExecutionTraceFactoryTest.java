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

package org.apache.shardingsphere.mcp.core.tool.handler.execute.trace;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLExecutionTraceFactoryTest {
    
    @Test
    void assertCreateSuccessTrace() {
        SQLExecutionTraceRecord actual = new SQLExecutionTraceFactory().create("session-1", "logic_db", "SELECT * FROM orders", true, "QUERY");
        assertTrue(actual.isSuccess());
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getSqlDigest().length(), is(64));
    }
    
    @Test
    void assertCreateFailureTrace() {
        SQLExecutionTraceRecord actual = new SQLExecutionTraceFactory().create("session-1", "logic_db", "SELECT * FROM orders", false, "QUERY");
        assertThat(actual.getStatementMarker(), is("QUERY"));
        assertFalse(actual.isSuccess());
    }
}
