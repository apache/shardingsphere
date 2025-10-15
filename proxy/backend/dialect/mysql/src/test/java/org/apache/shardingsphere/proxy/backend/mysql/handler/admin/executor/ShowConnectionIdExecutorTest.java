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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor;

import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.ShowConnectionIdExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowConnectionIdExecutorTest {
    
    @Test
    void assertExecute() throws SQLException {
        ShowConnectionIdExecutor executor = new ShowConnectionIdExecutor(mockSelectStatement());
        executor.execute(mockConnectionSession(), mock());
        QueryResultMetaData metaData = executor.getQueryResultMetaData();
        assertThat(metaData.getColumnCount(), is(1));
        assertThat(metaData.getColumnName(1), is(ShowConnectionIdExecutor.FUNCTION_NAME));
        assertThat(metaData.getColumnLabel(1), is(ShowConnectionIdExecutor.FUNCTION_NAME));
        while (executor.getMergedResult().next()) {
            assertThat(executor.getMergedResult().getValue(1, Object.class), is("109"));
        }
    }
    
    @Test
    void assertExecuteWithAlias() throws SQLException {
        ShowConnectionIdExecutor executor = new ShowConnectionIdExecutor(mockSelectStatementWithAlias());
        executor.execute(mockConnectionSession(), mock());
        QueryResultMetaData metaData = executor.getQueryResultMetaData();
        assertThat(metaData.getColumnCount(), is(1));
        assertThat(metaData.getColumnName(1), is(ShowConnectionIdExecutor.FUNCTION_NAME));
        assertThat(metaData.getColumnLabel(1), is("test_alias"));
        while (executor.getMergedResult().next()) {
            assertThat(executor.getMergedResult().getValue(1, Object.class), is("109"));
        }
    }
    
    private ConnectionSession mockConnectionSession() {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getConnectionId()).thenReturn(109);
        return result;
    }
    
    private SelectStatement mockSelectStatement() {
        List<ProjectionSegment> projections = new LinkedList<>();
        ProjectionsSegment segment = mock(ProjectionsSegment.class);
        when(segment.getProjections()).thenReturn(projections);
        SelectStatement result = mock(SelectStatement.class);
        when(result.getProjections()).thenReturn(segment);
        return result;
    }
    
    private SelectStatement mockSelectStatementWithAlias() {
        List<ProjectionSegment> projections = new LinkedList<>();
        ExpressionProjectionSegment projectionSegment = new ExpressionProjectionSegment(0, 0, "connection_id()");
        projectionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("test_alias")));
        projections.add(projectionSegment);
        ProjectionsSegment segment = mock(ProjectionsSegment.class);
        when(segment.getProjections()).thenReturn(projections);
        SelectStatement result = mock(SelectStatement.class);
        when(result.getProjections()).thenReturn(segment);
        return result;
    }
}
