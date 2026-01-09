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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class NoResourceShowExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertExecuteWithEmptyExpressions() throws SQLException {
        NoResourceShowExecutor executor = new NoResourceShowExecutor(createSelectStatement(Arrays.asList(new ShorthandProjectionSegment(0, 0), mock(ProjectionSegment.class))));
        executor.execute(mock(ConnectionSession.class), mock(ShardingSphereMetaData.class));
        MergedResult mergedResult = executor.getMergedResult();
        assertTrue(mergedResult.next());
        assertThat(mergedResult.getValue(1, Object.class), is(""));
        assertFalse(mergedResult.next());
        QueryResultMetaData actualMetaData = executor.getQueryResultMetaData();
        assertThat(actualMetaData.getColumnCount(), is(1));
        assertThat(actualMetaData.getColumnLabel(1), is(""));
        assertThat(actualMetaData.getColumnName(1), is(""));
        assertThat(actualMetaData.getColumnType(1), is(Types.VARCHAR));
        assertThat(actualMetaData.getColumnTypeName(1), is("VARCHAR"));
    }
    
    @Test
    void assertExecuteWithAliasAndPlainExpression() throws SQLException {
        ExpressionProjectionSegment projectionWithAlias = new ExpressionProjectionSegment(0, 0, "column_expr");
        IdentifierValue aliasColumn = new IdentifierValue("alias_column");
        projectionWithAlias.setAlias(new AliasSegment(0, 0, aliasColumn));
        ExpressionProjectionSegment projectionWithoutAlias = new ExpressionProjectionSegment(0, 0, "plain_expr");
        NoResourceShowExecutor executor = new NoResourceShowExecutor(createSelectStatement(Arrays.asList(projectionWithAlias, projectionWithoutAlias)));
        executor.execute(mock(ConnectionSession.class), mock(ShardingSphereMetaData.class));
        MergedResult mergedResult = executor.getMergedResult();
        assertTrue(mergedResult.next());
        Object actualFirstValue = mergedResult.getValue(1, Object.class);
        assertThat(actualFirstValue, is(""));
        Object actualSecondValue = mergedResult.getValue(2, Object.class);
        assertThat(actualSecondValue, is(""));
        assertFalse(mergedResult.next());
        QueryResultMetaData actualMetaData = executor.getQueryResultMetaData();
        assertThat(actualMetaData.getColumnCount(), is(2));
        assertThat(actualMetaData.getColumnLabel(1), is(aliasColumn.toString()));
        assertThat(actualMetaData.getColumnLabel(2), is("plain_expr"));
        assertThat(actualMetaData.getColumnName(1), is(aliasColumn.toString()));
        assertThat(actualMetaData.getColumnName(2), is("plain_expr"));
    }
    
    private SelectStatement createSelectStatement(final Collection<ProjectionSegment> projections) {
        SelectStatement result = new SelectStatement(databaseType);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().addAll(projections);
        result.setProjections(projectionsSegment);
        return result;
    }
}
