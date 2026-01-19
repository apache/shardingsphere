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
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowDistVariablesExecutorTest {
    
    private final ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
    
    @Test
    void assertExecute() {
        when(contextManager.getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(PropertiesBuilder.build(new Property("agent-plugins-enabled", "false"))));
        ShowDistVariablesExecutor executor = new ShowDistVariablesExecutor();
        executor.setConnectionContext(new DistSQLConnectionContext(mock(QueryContext.class), 1,
                mock(DatabaseType.class), mock(DatabaseConnectionManager.class), mock(ExecutorStatementManager.class)));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowDistVariablesStatement.class), contextManager);
        // add config key
        assertThat(actual.size(), is(23));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("agent_plugins_enabled"));
        assertThat(row.getCell(2), is("false"));
    }
    
    @Test
    void assertExecuteWithLike() {
        ShowDistVariablesExecutor executor = new ShowDistVariablesExecutor();
        executor.setConnectionContext(new DistSQLConnectionContext(mock(QueryContext.class), 1,
                mock(DatabaseType.class), mock(DatabaseConnectionManager.class), mock(ExecutorStatementManager.class)));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowDistVariablesStatement(false, "sql_%"), contextManager);
        assertThat(actual.size(), is(2));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        assertThat(iterator.next().getCell(1), is("sql_show"));
        assertThat(iterator.next().getCell(1), is("sql_simple"));
    }
    
    @Test
    void assertExecuteTemporary() {
        when(contextManager.getMetaDataContexts().getMetaData().getTemporaryProps()).thenReturn(new TemporaryConfigurationProperties(new Properties()));
        ShowDistVariablesExecutor executor = new ShowDistVariablesExecutor();
        ShowDistVariablesStatement sqlStatement = new ShowDistVariablesStatement(true, null);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(sqlStatement, contextManager);
        assertThat(actual.size(), is(4));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("instance_connection_enabled"));
        assertThat(row.getCell(2), is("false"));
    }
    
    @Test
    void assertExecuteTemporaryWithLike() {
        ShowDistVariablesExecutor executor = new ShowDistVariablesExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowDistVariablesStatement(true, "proxy_%"), contextManager);
        assertThat(actual.size(), is(2));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        assertThat(iterator.next().getCell(1), is("proxy_meta_data_collector_cron"));
        assertThat(iterator.next().getCell(1), is("proxy_meta_data_collector_enabled"));
    }
}
