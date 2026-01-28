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

package org.apache.shardingsphere.distsql.handler.ral.queryable.variable;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowDistVariableStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationProperties;
import org.apache.shardingsphere.infra.exception.kernel.syntax.UnsupportedVariableException;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DatabaseConnectionManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.ExecutorStatementManager;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowDistVariableExecutorTest {
    
    private final ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
    
    @Test
    void assertShowCachedConnections() {
        ShowDistVariableExecutor executor = new ShowDistVariableExecutor();
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
        ShowDistVariableExecutor executor = new ShowDistVariableExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowDistVariableStatement("SQL_SHOW"), contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("sql_show"));
        assertThat(row.getCell(2), is("true"));
    }
    
    @Test
    void assertShowPropsVariableForTypedSPI() {
        when(contextManager.getMetaDataContexts().getMetaData().getProps())
                .thenReturn(new ConfigurationProperties(PropertiesBuilder.build(new Property("proxy-frontend-database-protocol-type", "MySQL"))));
        ShowDistVariableExecutor executor = new ShowDistVariableExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowDistVariableStatement("PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE"), contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("proxy_frontend_database_protocol_type"));
        assertThat(row.getCell(2), is("MySQL"));
    }
    
    @Test
    void assertShowTemporaryPropsVariable() {
        when(contextManager.getMetaDataContexts().getMetaData().getTemporaryProps()).thenReturn(
                new TemporaryConfigurationProperties(PropertiesBuilder.build(new Property("proxy-meta-data-collector-enabled", Boolean.TRUE.toString()))));
        ShowDistVariableExecutor executor = new ShowDistVariableExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowDistVariableStatement("PROXY_META_DATA_COLLECTOR_ENABLED"), contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("proxy_meta_data_collector_enabled"));
        assertThat(row.getCell(2), is("true"));
    }
    
    @Test
    void assertShowUnsupportedVariable() {
        ShowDistVariableExecutor executor = new ShowDistVariableExecutor();
        assertThrows(UnsupportedVariableException.class, () -> executor.getRows(new ShowDistVariableStatement("unsupported"), contextManager));
    }
}
