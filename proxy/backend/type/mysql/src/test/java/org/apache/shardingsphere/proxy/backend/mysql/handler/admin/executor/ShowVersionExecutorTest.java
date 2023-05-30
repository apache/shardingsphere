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

import org.apache.shardingsphere.db.protocol.constant.DatabaseProtocolServerInfo;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowVersionExecutorTest {
    
    private String previousVersion;
    
    @BeforeEach
    void setUp() {
        previousVersion = DatabaseProtocolServerInfo.getProtocolVersion("foo_db", "MySQL");
        DatabaseProtocolServerInfo.setProtocolVersion("foo_db", "8.0.26");
    }
    
    @AfterEach
    void tearDown() {
        DatabaseProtocolServerInfo.setProtocolVersion("foo_db", previousVersion);
    }
    
    @Test
    void assertExecute() throws SQLException {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getProjections()).thenReturn(createProjectionsSegmentWithoutAlias());
        ShowVersionExecutor executor = new ShowVersionExecutor(selectStatement);
        executor.execute(mockConnectionSession());
        assertQueryResult(executor, ShowVersionExecutor.FUNCTION_NAME);
    }
    
    private ProjectionsSegment createProjectionsSegmentWithoutAlias() {
        ExpressionProjectionSegment expressionProjectionSegment = new ExpressionProjectionSegment(0, 0, "version()");
        ProjectionsSegment result = new ProjectionsSegment(0, 0);
        result.getProjections().add(expressionProjectionSegment);
        return result;
    }
    
    private ConnectionSession mockConnectionSession() {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getDatabaseName()).thenReturn("foo_db");
        return result;
    }
    
    @Test
    void assertExecuteWithAlias() throws SQLException {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getProjections()).thenReturn(createProjectionsSegmentWithAlias());
        ShowVersionExecutor executor = new ShowVersionExecutor(selectStatement);
        executor.execute(mockConnectionSession());
        assertQueryResult(executor, "foo_alias");
    }
    
    private ProjectionsSegment createProjectionsSegmentWithAlias() {
        ExpressionProjectionSegment expressionProjectionSegment = new ExpressionProjectionSegment(0, 0, "version()");
        expressionProjectionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("foo_alias")));
        ProjectionsSegment result = new ProjectionsSegment(0, 0);
        result.getProjections().add(expressionProjectionSegment);
        return result;
    }
    
    private void assertQueryResult(final ShowVersionExecutor executor, final String expectedColumnLabel) throws SQLException {
        QueryResultMetaData actualQueryResultMetaData = executor.getQueryResultMetaData();
        assertThat(actualQueryResultMetaData.getColumnCount(), is(1));
        assertThat(actualQueryResultMetaData.getColumnName(1), is(ShowVersionExecutor.FUNCTION_NAME));
        assertThat(actualQueryResultMetaData.getColumnLabel(1), is(expectedColumnLabel));
        while (executor.getMergedResult().next()) {
            assertThat(executor.getMergedResult().getValue(1, Object.class), is(DatabaseProtocolServerInfo.getProtocolVersion("foo_db", "MySQL")));
        }
    }
}
