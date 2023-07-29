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

import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProcessRegistry.class)
class ProcessEngineTest {
    
    @Mock
    private ProcessRegistry processRegistry;
    
    @BeforeEach
    void setUp() {
        when(ProcessRegistry.getInstance()).thenReturn(processRegistry);
    }
    
    @Test
    void assertExecuteSQL() {
        ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext = mockExecutionGroupContext();
        new ProcessEngine().executeSQL(executionGroupContext, new QueryContext(new UpdateStatementContext(getSQLStatement()), null, null));
        verify(processRegistry).add(any());
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionGroupContext<? extends SQLExecutionUnit> mockExecutionGroupContext() {
        ExecutionGroupContext<? extends SQLExecutionUnit> result = mock(ExecutionGroupContext.class);
        ExecutionGroupReportContext reportContext = mock(ExecutionGroupReportContext.class);
        when(reportContext.getProcessId()).thenReturn(UUID.fromString("00000000-000-0000-0000-000000000001").toString());
        when(result.getReportContext()).thenReturn(reportContext);
        return result;
    }
    
    private MySQLUpdateStatement getSQLStatement() {
        MySQLUpdateStatement result = new MySQLUpdateStatement();
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl"))));
        result.setSetAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()));
        return result;
    }
    
    @Test
    void assertCompleteSQLUnitExecution() {
        ProcessIdContext.set("foo_id");
        when(processRegistry.get("foo_id")).thenReturn(mock(Process.class));
        new ProcessEngine().completeSQLUnitExecution();
        verify(processRegistry).get("foo_id");
        ProcessIdContext.remove();
    }
}
