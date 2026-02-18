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

package org.apache.shardingsphere.driver.jdbc.core.statement;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.rule.builder.DefaultSQLFederationRuleConfigurationBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShardingSphereStatementTest {
    
    @Test
    void assertGetGeneratedKeysByColumnName() throws SQLException, ReflectiveOperationException {
        ShardingSphereStatement statement = createStatement(TypedSPILoader.getService(DatabaseType.class, "SQL92"));
        ResultSet generatedKeys = mock(ResultSet.class);
        when(generatedKeys.next()).thenReturn(true, false);
        when(generatedKeys.getObject("id")).thenReturn(1L);
        setInsertStatementContext(statement, "id");
        addStatement(statement, generatedKeys);
        ResultSet actual = statement.getGeneratedKeys();
        assertTrue(actual.next());
        assertThat(actual.getObject(1), is(1L));
        verify(generatedKeys).getObject("id");
        verify(generatedKeys, never()).getObject(1);
    }
    
    @Test
    void assertGetGeneratedKeysByColumnIndexWhenColumnNameMissing() throws SQLException, ReflectiveOperationException {
        ResultSet generatedKeys = mock(ResultSet.class);
        when(generatedKeys.next()).thenReturn(true, false);
        when(generatedKeys.getObject("id")).thenThrow(new SQLException("Column not found"));
        when(generatedKeys.getObject(1)).thenReturn(2L);
        ShardingSphereStatement statement = createStatement(TypedSPILoader.getService(DatabaseType.class, "SQL92"));
        setInsertStatementContext(statement, "id");
        addStatement(statement, generatedKeys);
        ResultSet actual = statement.getGeneratedKeys();
        assertTrue(actual.next());
        assertThat(actual.getObject(1), is(2L));
        verify(generatedKeys).getObject("id");
        verify(generatedKeys).getObject(1);
    }
    
    @Test
    void assertGetGeneratedKeysByDialectGeneratedKeyColumn() throws SQLException, ReflectiveOperationException {
        ShardingSphereStatement statement = createStatement(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ResultSet generatedKeys = mock(ResultSet.class);
        when(generatedKeys.next()).thenReturn(true, false);
        when(generatedKeys.getObject("GENERATED_KEY")).thenReturn(3L);
        setInsertStatementContext(statement, "id");
        addStatement(statement, generatedKeys);
        ResultSet actual = statement.getGeneratedKeys();
        assertTrue(actual.next());
        assertThat(actual.getObject(1), is(3L));
        verify(generatedKeys).getObject("GENERATED_KEY");
        verify(generatedKeys, never()).getObject("id");
        verify(generatedKeys, never()).getObject(1);
    }
    
    private ShardingSphereStatement createStatement(final DatabaseType databaseType) throws SQLException {
        ShardingSphereMetaData metaData = createMetaData(databaseType);
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        when(connection.getContextManager().getMetaDataContexts().getMetaData()).thenReturn(metaData);
        when(connection.getCurrentDatabaseName()).thenReturn("foo_db");
        return new ShardingSphereStatement(connection);
    }
    
    private ShardingSphereMetaData createMetaData(final DatabaseType databaseType) {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getProtocolType()).thenReturn(databaseType);
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        when(result.getDatabase("foo_db")).thenReturn(database);
        when(result.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Arrays.asList(
                new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()),
                new SQLFederationRule(new DefaultSQLFederationRuleConfigurationBuilder().build(), Collections.emptyList()))));
        when(result.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        return result;
    }
    
    private void setInsertStatementContext(final ShardingSphereStatement statement, final String columnName) throws ReflectiveOperationException {
        GeneratedKeyContext generatedKeyContext = mock(GeneratedKeyContext.class);
        when(generatedKeyContext.getColumnName()).thenReturn(columnName);
        when(generatedKeyContext.getGeneratedValues()).thenReturn(Collections.emptyList());
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(generatedKeyContext));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(insertStatementContext);
        Plugins.getMemberAccessor().set(ShardingSphereStatement.class.getDeclaredField("queryContext"), statement, queryContext);
        Plugins.getMemberAccessor().set(ShardingSphereStatement.class.getDeclaredField("returnGeneratedKeys"), statement, true);
    }
    
    private void addStatement(final ShardingSphereStatement shardingSphereStatement, final ResultSet generatedKeys) throws ReflectiveOperationException, SQLException {
        Statement statement = mock(Statement.class);
        when(statement.getGeneratedKeys()).thenReturn(generatedKeys);
        getStatements(shardingSphereStatement).add(statement);
    }
    
    @SuppressWarnings("unchecked")
    private Collection<Statement> getStatements(final ShardingSphereStatement statement) throws ReflectiveOperationException {
        Field statementsField = ShardingSphereStatement.class.getDeclaredField("statements");
        return (Collection<Statement>) Plugins.getMemberAccessor().get(statementsField, statement);
    }
}
