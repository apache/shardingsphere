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

package org.apache.shardingsphere.traffic.executor;

import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrafficExecutorTest {
    
    @Test
    void assertClose() throws SQLException {
        Statement statement = mock(Statement.class, RETURNS_DEEP_STUBS);
        try (TrafficExecutor trafficExecutor = new TrafficExecutor()) {
            JDBCExecutionUnit executionUnit = mock(JDBCExecutionUnit.class);
            when(executionUnit.getExecutionUnit()).thenReturn(new ExecutionUnit("oltp_proxy_instance_id", new SQLUnit("SELECT 1", Collections.emptyList())));
            when(executionUnit.getStorageResource()).thenReturn(statement);
            trafficExecutor.execute(executionUnit, Statement::executeQuery);
        }
        verify(statement).close();
        verify(statement, times(0)).getConnection();
    }
}
