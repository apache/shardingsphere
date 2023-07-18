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

package org.apache.shardingsphere.sharding.merge.dql.pagination;

import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.merge.dql.ShardingDQLResultMerger;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TopAndRowNumberDecoratorMergedResultTest {
    
    private ShardingSphereSchema schema;
    
    @BeforeEach
    void setUp() {
        schema = mock(ShardingSphereSchema.class);
    }
    
    @Test
    void assertNextForSkipAll() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        SQLServerSelectStatement sqlStatement = new SQLServerSelectStatement();
        sqlStatement.setProjections(new ProjectionsSegment(0, 0));
        sqlStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralRowNumberValueSegment(0, 0, Integer.MAX_VALUE, true), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), sqlStatement, DefaultDatabase.LOGIC_NAME);
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "SQLServer"));
        MergedResult actual = resultMerger.merge(Arrays.asList(mockQueryResult(), mockQueryResult(),
                mockQueryResult(), mockQueryResult()), selectStatementContext, mockShardingSphereDatabase(), mock(ConnectionContext.class));
        assertFalse(actual.next());
    }
    
    @Test
    void assertNextWithoutOffsetWithRowCount() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "SQLServer"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        SQLServerSelectStatement sqlStatement = new SQLServerSelectStatement();
        sqlStatement.setProjections(new ProjectionsSegment(0, 0));
        sqlStatement.setLimit(new LimitSegment(0, 0, null, new NumberLiteralLimitValueSegment(0, 0, 5)));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), sqlStatement, DefaultDatabase.LOGIC_NAME);
        MergedResult actual = resultMerger.merge(Arrays.asList(mockQueryResult(), mockQueryResult(),
                mockQueryResult(), mockQueryResult()), selectStatementContext, mockShardingSphereDatabase(), mock(ConnectionContext.class));
        for (int i = 0; i < 5; i++) {
            assertTrue(actual.next());
        }
        assertFalse(actual.next());
    }
    
    @Test
    void assertNextWithOffsetWithoutRowCount() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        SQLServerSelectStatement sqlStatement = new SQLServerSelectStatement();
        sqlStatement.setProjections(new ProjectionsSegment(0, 0));
        sqlStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralRowNumberValueSegment(0, 0, 2, true), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), sqlStatement, DefaultDatabase.LOGIC_NAME);
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "SQLServer"));
        MergedResult actual = resultMerger.merge(Arrays.asList(mockQueryResult(), mockQueryResult(),
                mockQueryResult(), mockQueryResult()), selectStatementContext, mockShardingSphereDatabase(), mock(ConnectionContext.class));
        for (int i = 0; i < 7; i++) {
            assertTrue(actual.next());
        }
        assertFalse(actual.next());
    }
    
    @Test
    void assertNextWithOffsetBoundOpenedFalse() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "SQLServer"));
        final ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        SQLServerSelectStatement sqlStatement = new SQLServerSelectStatement();
        sqlStatement.setProjections(new ProjectionsSegment(0, 0));
        sqlStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralRowNumberValueSegment(0, 0, 2, false), new NumberLiteralLimitValueSegment(0, 0, 4)));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), sqlStatement, DefaultDatabase.LOGIC_NAME);
        MergedResult actual = resultMerger.merge(Arrays.asList(mockQueryResult(), mockQueryResult(),
                mockQueryResult(), mockQueryResult()), selectStatementContext, mockShardingSphereDatabase(), mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    void assertNextWithOffsetBoundOpenedTrue() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "SQLServer"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        SQLServerSelectStatement sqlStatement = new SQLServerSelectStatement();
        sqlStatement.setProjections(new ProjectionsSegment(0, 0));
        sqlStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralRowNumberValueSegment(0, 0, 2, true), new NumberLiteralLimitValueSegment(0, 0, 4)));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database),
                Collections.emptyList(), sqlStatement, DefaultDatabase.LOGIC_NAME);
        MergedResult actual = resultMerger.merge(Arrays.asList(mockQueryResult(), mockQueryResult(),
                mockQueryResult(), mockQueryResult()), selectStatementContext, mockShardingSphereDatabase(), mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData(final ShardingSphereDatabase database) {
        return new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
    }
    
    private QueryResult mockQueryResult() throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.next()).thenReturn(true, true, false);
        return result;
    }
    
    private ShardingSphereDatabase mockShardingSphereDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("db_schema");
        return result;
    }
}
