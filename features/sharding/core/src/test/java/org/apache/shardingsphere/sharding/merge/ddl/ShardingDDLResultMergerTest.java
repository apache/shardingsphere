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

package org.apache.shardingsphere.sharding.merge.ddl;

import org.apache.shardingsphere.infra.binder.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.FetchStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.connection.cursor.CursorConnectionContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.merge.common.IteratorStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.ddl.fetch.FetchStreamMergedResult;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussFetchStatement;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingDDLResultMergerTest {
    
    @Test
    void assertBuildIteratorStreamMergedResult() throws SQLException {
        ShardingDDLResultMerger merger = new ShardingDDLResultMerger();
        assertThat(merger.merge(createSingleQueryResults(), mock(FetchStatementContext.class), mock(ShardingSphereDatabase.class), mock(ConnectionContext.class)),
                instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    void assertBuildFetchStreamMergedResult() throws SQLException {
        ShardingDDLResultMerger merger = new ShardingDDLResultMerger();
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCursorContext()).thenReturn(new CursorConnectionContext());
        assertThat(merger.merge(createMultiQueryResults(), createFetchStatementContext(database), mock(ShardingSphereDatabase.class), connectionContext),
                instanceOf(FetchStreamMergedResult.class));
    }
    
    @Test
    void assertBuildTransparentMergedResult() throws SQLException {
        ShardingDDLResultMerger merger = new ShardingDDLResultMerger();
        assertThat(merger.merge(createMultiQueryResults(), mock(SelectStatementContext.class), mock(ShardingSphereDatabase.class), mock(ConnectionContext.class)),
                instanceOf(TransparentMergedResult.class));
    }
    
    private FetchStatementContext createFetchStatementContext(final ShardingSphereDatabase database) {
        OpenGaussFetchStatement fetchStatement = createFetchStatement();
        FetchStatementContext result = new FetchStatementContext(fetchStatement);
        CursorStatementContext cursorStatementContext = createCursorStatementContext(database);
        result.setUpCursorDefinition(cursorStatementContext);
        return result;
    }
    
    private CursorStatementContext createCursorStatementContext(final ShardingSphereDatabase database) {
        CursorStatementContext result = mock(CursorStatementContext.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = createSelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        when(result.getSelectStatementContext()).thenReturn(selectStatementContext);
        when(result.getSqlStatement().getSelect()).thenReturn(selectStatement);
        return result;
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData(final ShardingSphereDatabase database) {
        return new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
    }
    
    private List<QueryResult> createSingleQueryResults() throws SQLException {
        List<QueryResult> result = new LinkedList<>();
        QueryResult queryResult = createQueryResult();
        result.add(queryResult);
        return result;
    }
    
    private List<QueryResult> createMultiQueryResults() throws SQLException {
        List<QueryResult> result = new LinkedList<>();
        QueryResult queryResult = createQueryResult();
        result.add(queryResult);
        result.add(mock(QueryResult.class, RETURNS_DEEP_STUBS));
        result.add(mock(QueryResult.class, RETURNS_DEEP_STUBS));
        result.add(mock(QueryResult.class, RETURNS_DEEP_STUBS));
        return result;
    }
    
    private QueryResult createQueryResult() throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(1);
        when(result.getMetaData().getColumnLabel(1)).thenReturn("count(*)");
        when(result.getValue(1, Object.class)).thenReturn(0);
        return result;
    }
    
    private OpenGaussFetchStatement createFetchStatement() {
        OpenGaussFetchStatement result = new OpenGaussFetchStatement();
        result.setCursorName(new CursorNameSegment(0, 0, new IdentifierValue("t_order_cursor")));
        return result;
    }
    
    private SelectStatement createSelectStatement() {
        SelectStatement result = new MySQLSelectStatement();
        result.setFrom(new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("tbl"))));
        result.setProjections(new ProjectionsSegment(0, 0));
        return result;
    }
}
