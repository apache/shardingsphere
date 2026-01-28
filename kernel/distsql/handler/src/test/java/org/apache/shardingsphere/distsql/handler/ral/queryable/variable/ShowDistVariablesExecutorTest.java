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
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowDistVariablesStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationProperties;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DatabaseConnectionManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.ExecutorStatementManager;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowDistVariablesExecutorTest {
    
    private final ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
    
    @Test
    void assertExecute() {
        when(contextManager.getMetaDataContexts().getMetaData().getProps())
                .thenReturn(new ConfigurationProperties(PropertiesBuilder.build(new Property("sql-show", Boolean.TRUE.toString()), new Property("sql-simple", Boolean.TRUE.toString()),
                        new Property("agent-plugins-enabled", Boolean.TRUE.toString()))));
        ShowDistVariablesExecutor executor = new ShowDistVariablesExecutor();
        executor.setConnectionContext(new DistSQLConnectionContext(mock(QueryContext.class), 1,
                mock(DatabaseType.class), mock(DatabaseConnectionManager.class), mock(ExecutorStatementManager.class)));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowDistVariablesStatement(false, null), contextManager);
        Collection<String> actualNames = actual.stream().map(each -> each.getCell(1).toString()).collect(Collectors.toList());
        assertThat(actual.size(), is(22));
        assertThat(actualNames.contains("cached_connections"), is(true));
        assertThat(actualNames.contains("agent_plugins_enabled"), is(true));
        assertThat(actualNames.contains("sql_show"), is(true));
        assertThat(actualNames.contains("sql_simple"), is(true));
    }
    
    @Test
    void assertExecuteTemporary() {
        Properties props = PropertiesBuilder.build(new Property("proxy-meta-data-collector-enabled", Boolean.TRUE.toString()), new Property("instance-connection-enabled", Boolean.TRUE.toString()));
        when(contextManager.getMetaDataContexts().getMetaData().getTemporaryProps()).thenReturn(new TemporaryConfigurationProperties(props));
        ShowDistVariablesExecutor executor = new ShowDistVariablesExecutor();
        ShowDistVariablesStatement sqlStatement = new ShowDistVariablesStatement(true, null);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(sqlStatement, contextManager);
        Collection<String> actualNames = actual.stream().map(each -> each.getCell(1).toString()).collect(Collectors.toList());
        assertThat(actual.size(), is(4));
        assertThat(actualNames.contains("instance_connection_enabled"), is(true));
        assertThat(actualNames.contains("proxy_meta_data_collector_cron"), is(true));
        assertThat(actualNames.contains("proxy_meta_data_collector_enabled"), is(true));
        assertThat(actualNames.contains("system_schema_metadata_assembly_enabled"), is(true));
    }
    
    @Test
    void assertExecuteTemporaryWithLike() {
        when(contextManager.getMetaDataContexts().getMetaData().getProps())
                .thenReturn(new ConfigurationProperties(PropertiesBuilder.build(new Property("sql-show", Boolean.TRUE.toString()))));
        ShowDistVariablesExecutor executor = new ShowDistVariablesExecutor();
        executor.setConnectionContext(new DistSQLConnectionContext(mock(QueryContext.class), 0,
                mock(DatabaseType.class), mock(DatabaseConnectionManager.class), mock(ExecutorStatementManager.class)));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowDistVariablesStatement(false, "sql_%"), contextManager);
        Collection<String> actualNames = actual.stream().map(each -> each.getCell(1).toString()).collect(Collectors.toList());
        assertThat(actual.size(), is(2));
        assertThat(actualNames.contains("sql_show"), is(true));
        assertThat(actualNames.contains("sql_simple"), is(true));
    }
}
