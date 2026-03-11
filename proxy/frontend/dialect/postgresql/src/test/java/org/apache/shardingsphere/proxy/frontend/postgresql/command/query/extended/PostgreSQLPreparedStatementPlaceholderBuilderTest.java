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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostgreSQLPreparedStatementPlaceholderBuilderTest {
    
    private static final String DATABASE_NAME = "postgres";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(databaseType, new CacheOption(2000, 65535L), new CacheOption(128, 1024L));
    
    @Test
    void assertBuildWithWhereColumn() {
        ShardingSphereMetaData metaData = mockMetaData(false);
        PostgreSQLServerPreparedStatement preparedStatement = createPreparedStatement("SELECT id FROM foo_tbl WHERE name=?", PostgreSQLBinaryColumnType.UNSPECIFIED);
        SQLStatementContext sqlStatementContext = bind(metaData, preparedStatement.getSql());
        Optional<List<Object>> actual = PostgreSQLPreparedStatementPlaceholderBuilder.build(metaData, DATABASE_NAME, preparedStatement, sqlStatementContext);
        assertThat(actual.get(), is(Collections.<Object>singletonList("")));
    }
    
    @Test
    void assertBuildWithInsertColumn() {
        ShardingSphereMetaData metaData = mockMetaData(false);
        PostgreSQLServerPreparedStatement preparedStatement = createPreparedStatement("INSERT INTO foo_tbl (id) VALUES (?)", PostgreSQLBinaryColumnType.UNSPECIFIED);
        SQLStatementContext sqlStatementContext = bind(metaData, preparedStatement.getSql());
        Optional<List<Object>> actual = PostgreSQLPreparedStatementPlaceholderBuilder.build(metaData, DATABASE_NAME, preparedStatement, sqlStatementContext);
        assertThat(actual.get(), is(Collections.<Object>singletonList(0)));
    }
    
    @Test
    void assertBuildWithBigIntColumn() {
        ShardingSphereMetaData metaData = mockMetaData(true);
        PostgreSQLServerPreparedStatement preparedStatement = createPreparedStatement("INSERT INTO t_shadow (order_id) VALUES (?)", PostgreSQLBinaryColumnType.UNSPECIFIED);
        SQLStatementContext sqlStatementContext = bind(metaData, preparedStatement.getSql());
        Optional<List<Object>> actual = PostgreSQLPreparedStatementPlaceholderBuilder.build(metaData, DATABASE_NAME, preparedStatement, sqlStatementContext);
        assertThat(actual.get().get(0), isA(Integer.class));
    }
    
    @Test
    void assertBuildWithInExpression() {
        ShardingSphereMetaData metaData = mockMetaData(false);
        PostgreSQLServerPreparedStatement preparedStatement =
                createPreparedStatement("SELECT id FROM foo_tbl WHERE id IN (?, ?)", PostgreSQLBinaryColumnType.UNSPECIFIED, PostgreSQLBinaryColumnType.UNSPECIFIED);
        SQLStatementContext sqlStatementContext = bind(metaData, preparedStatement.getSql());
        Optional<List<Object>> actual = PostgreSQLPreparedStatementPlaceholderBuilder.build(metaData, DATABASE_NAME, preparedStatement, sqlStatementContext);
        assertThat(actual.get(), is(Arrays.<Object>asList(0, 0)));
    }
    
    @Test
    void assertBuildWithBetweenExpression() {
        ShardingSphereMetaData metaData = mockMetaData(false);
        PostgreSQLServerPreparedStatement preparedStatement =
                createPreparedStatement("SELECT id FROM foo_tbl WHERE id BETWEEN ? AND ?", PostgreSQLBinaryColumnType.UNSPECIFIED, PostgreSQLBinaryColumnType.UNSPECIFIED);
        SQLStatementContext sqlStatementContext = bind(metaData, preparedStatement.getSql());
        Optional<List<Object>> actual = PostgreSQLPreparedStatementPlaceholderBuilder.build(metaData, DATABASE_NAME, preparedStatement, sqlStatementContext);
        assertThat(actual.get(), is(Arrays.<Object>asList(0, 0)));
    }
    
    @Test
    void assertBuildWithoutInferablePlaceholders() {
        ShardingSphereMetaData metaData = mockMetaData(false);
        PostgreSQLServerPreparedStatement preparedStatement =
                createPreparedStatement("SELECT id FROM foo_tbl WHERE ? = ?", PostgreSQLBinaryColumnType.UNSPECIFIED, PostgreSQLBinaryColumnType.UNSPECIFIED);
        SQLStatementContext sqlStatementContext = bind(metaData, preparedStatement.getSql());
        assertFalse(PostgreSQLPreparedStatementPlaceholderBuilder.build(metaData, DATABASE_NAME, preparedStatement, sqlStatementContext).isPresent());
    }
    
    private PostgreSQLServerPreparedStatement createPreparedStatement(final String sql, final PostgreSQLBinaryColumnType... parameterTypes) {
        SQLStatement sqlStatement = sqlParserEngine.parse(sql, false);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        return new PostgreSQLServerPreparedStatement(sql, sqlStatementContext, new HintValueContext(), Arrays.asList(parameterTypes), createParameterMarkerIndexes(parameterTypes.length));
    }
    
    private SQLStatementContext bind(final ShardingSphereMetaData metaData, final String sql) {
        SQLStatement sqlStatement = sqlParserEngine.parse(sql, false);
        return new SQLBindEngine(metaData, DATABASE_NAME, new HintValueContext()).bind(sqlStatement);
    }
    
    private ShardingSphereMetaData mockMetaData(final boolean withShadowTable) {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereMetaData result = contextManager.getMetaDataContexts().getMetaData();
        when(result.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build())));
        when(result.getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(result.containsDatabase(DATABASE_NAME)).thenReturn(true);
        when(result.getDatabase(DATABASE_NAME).getProtocolType()).thenReturn(databaseType);
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getStorageType()).thenReturn(databaseType);
        when(result.getDatabase(DATABASE_NAME).getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", storageUnit));
        when(result.getDatabase(DATABASE_NAME).containsSchema("public")).thenReturn(true);
        if (withShadowTable) {
            ShardingSphereTable shadowTable = new ShardingSphereTable("t_shadow", Arrays.asList(
                    new ShardingSphereColumn("order_id", Types.BIGINT, true, false, false, true, false, false),
                    new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList());
            when(result.getDatabase(DATABASE_NAME).getSchema("public").containsTable("t_shadow")).thenReturn(true);
            when(result.getDatabase(DATABASE_NAME).getSchema("public").getTable("t_shadow")).thenReturn(shadowTable);
        } else {
            ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Arrays.asList(
                    new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                    new ShardingSphereColumn("name", Types.VARCHAR, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList());
            when(result.getDatabase(DATABASE_NAME).getSchema("public").containsTable("foo_tbl")).thenReturn(true);
            when(result.getDatabase(DATABASE_NAME).getSchema("public").getTable("foo_tbl")).thenReturn(table);
        }
        ShardingSphereDatabase database = result.getDatabase(DATABASE_NAME);
        when(contextManager.getDatabase(DATABASE_NAME)).thenReturn(database);
        return result;
    }
    
    private List<Integer> createParameterMarkerIndexes(final int count) {
        List<Integer> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(i);
        }
        return result;
    }
}
