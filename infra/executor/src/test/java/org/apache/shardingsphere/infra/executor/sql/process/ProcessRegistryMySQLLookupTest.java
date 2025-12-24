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

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class ProcessRegistryMySQLLookupTest {
    
    /**
     * Verifies that killing a process by MySQL thread ID cancels the bound JDBC statement.
     *
     * @throws SQLException if cancel operation fails
     */
    @Test
    public void assertKillByMySQLThreadIdCancelsStatement() throws SQLException {
        ProcessEngine engine = new ProcessEngine();
        String processId = engine.connect("test_db");
        Process process = ProcessRegistry.getInstance().get(processId);
        // bind MySQL handshake thread id (32-bit) into process
        process.setMySQLThreadId(123L);
        // add a mock JDBC Statement to process
        Statement stmt = mock(Statement.class);
        process.getProcessStatements().put(1, stmt);
        // kill by numeric thread id string should find the process and cancel the statement
        ProcessRegistry.getInstance().kill("123");
        verify(stmt).cancel();
    }
}
