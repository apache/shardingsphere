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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.variable;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowDistVariableStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationProperties;
import org.apache.shardingsphere.infra.exception.kernel.syntax.UnsupportedVariableException;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DatabaseConnectionManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.ExecutorStatementManager;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.DistSQLVariable;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ShowDistVariableExecutorTest {
    
    private final ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
    
    private final ShowDistVariableExecutor executor = (ShowDistVariableExecutor) TypedSPILoader.getService(DistSQLQueryExecutor.class, ShowDistVariableStatement.class);
    
    @Test
    void assertGetColumnNames() {
        Collection<String> actual = executor.getColumnNames(new ShowDistVariableStatement("SQL_SHOW"));
        assertThat(actual.size(), is(2));
        Iterator<String> iterator = actual.iterator();
        assertThat(iterator.next(), is("variable_name"));
        assertThat(iterator.next(), is("variable_value"));
    }
    
    @Test
    void assertShowCachedConnections() {
        executor.setConnectionContext(new DistSQLConnectionContext(mock(QueryContext.class), 1,
                mock(DatabaseType.class), mock(DatabaseConnectionManager.class), mock(ExecutorStatementManager.class)));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowDistVariableStatement("CACHED_CONNECTIONS"), contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("cached_connections"));
        assertThat(row.getCell(2), is("1"));
    }
    
    @Test
    void assertShowPropsVariable() {
        when(contextManager.getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(PropertiesBuilder.build(new Property("sql-show", Boolean.TRUE.toString()))));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowDistVariableStatement("SQL_SHOW"), contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("sql_show"));
        assertThat(row.getCell(2), is("true"));
    }
    
    @Test
    void assertShowPropsVariableWithNullValue() {
        when(contextManager.getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowDistVariableStatement("PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE"), contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("proxy_frontend_database_protocol_type"));
        assertThat(row.getCell(2), is(""));
    }
    
    @Test
    void assertShowPropsVariableForTypedSPI() {
        when(contextManager.getMetaDataContexts().getMetaData().getProps())
                .thenReturn(new ConfigurationProperties(PropertiesBuilder.build(new Property("proxy-frontend-database-protocol-type", "MySQL"))));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowDistVariableStatement("PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE"), contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("proxy_frontend_database_protocol_type"));
        assertThat(row.getCell(2), is("MySQL"));
    }
    
    @Test
    void assertShowTemporaryPropsVariable() {
        when(contextManager.getMetaDataContexts().getMetaData().getTemporaryProps())
                .thenReturn(new TemporaryConfigurationProperties(PropertiesBuilder.build(new Property("proxy-meta-data-collector-enabled", Boolean.FALSE.toString()))));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowDistVariableStatement("PROXY_META_DATA_COLLECTOR_ENABLED"), contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("proxy_meta_data_collector_enabled"));
        assertThat(row.getCell(2), is("false"));
    }
    
    @Test
    void assertExecuteWithInvalidVariableName() {
        assertThrows(UnsupportedVariableException.class, () -> executor.getRows(new ShowDistVariableStatement("wrong_name"), contextManager));
    }
    
    @Test
    void assertExecuteWithUnsupportedDistSQLVariable() {
        try (MockedStatic<DistSQLVariable> mockedStatic = mockStatic(DistSQLVariable.class)) {
            mockedStatic.when(() -> DistSQLVariable.valueOf("CACHED_CONNECTIONS")).thenReturn(null);
            assertThrows(UnsupportedVariableException.class, () -> executor.getRows(new ShowDistVariableStatement("CACHED_CONNECTIONS"), contextManager));
        }
    }
}
