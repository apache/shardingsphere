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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowDistVariableStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationProperties;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.logging.rule.LoggingRule;
import org.apache.shardingsphere.logging.rule.builder.DefaultLoggingRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.exception.UnsupportedVariableException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowDistVariableExecutorTest {
    
    private final ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
    
    private final ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
    
    @Test
    void assertGetColumns() {
        ShowDistVariableExecutor executor = new ShowDistVariableExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(2));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("variable_name"));
        assertThat(iterator.next(), is("variable_value"));
    }
    
    @Test
    void assertShowCachedConnections() {
        when(connectionSession.getDatabaseConnectionManager().getConnectionSize()).thenReturn(1);
        ShowDistVariableExecutor executor = new ShowDistVariableExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(metaData, connectionSession, new ShowDistVariableStatement("CACHED_CONNECTIONS"));
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("cached_connections"));
        assertThat(row.getCell(2), is("1"));
    }
    
    @Test
    void assertShowPropsVariable() {
        when(metaData.getProps()).thenReturn(new ConfigurationProperties(PropertiesBuilder.build(new Property("sql-show", Boolean.TRUE.toString()))));
        when(metaData.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(new LoggingRule(new DefaultLoggingRuleConfigurationBuilder().build()))));
        ShowDistVariableExecutor executor = new ShowDistVariableExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(metaData, connectionSession, new ShowDistVariableStatement("SQL_SHOW"));
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("sql_show"));
        assertThat(row.getCell(2), is("true"));
    }
    
    @Test
    void assertShowTemporaryPropsVariable() {
        when(metaData.getTemporaryProps()).thenReturn(new TemporaryConfigurationProperties(PropertiesBuilder.build(new Property("proxy-meta-data-collector-enabled", Boolean.FALSE.toString()))));
        ShowDistVariableExecutor executor = new ShowDistVariableExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(metaData, connectionSession, new ShowDistVariableStatement("PROXY_META_DATA_COLLECTOR_ENABLED"));
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("proxy_meta_data_collector_enabled"));
        assertThat(row.getCell(2), is("false"));
    }
    
    @Test
    void assertExecuteWithInvalidVariableName() {
        assertThrows(UnsupportedVariableException.class, () -> new ShowDistVariableExecutor().getRows(metaData, connectionSession, new ShowDistVariableStatement("wrong_name")));
    }
}
