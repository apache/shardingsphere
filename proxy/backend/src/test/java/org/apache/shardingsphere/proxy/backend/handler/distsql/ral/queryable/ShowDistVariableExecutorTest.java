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
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.exception.UnsupportedVariableException;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.proxy.backend.util.SystemPropertyUtil;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowDistVariableExecutorTest extends ProxyContextRestorer {
    
    private final ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
    
    private final ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
    
    @Test
    public void assertGetColumns() {
        ShowDistVariableExecutor executor = new ShowDistVariableExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(2));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("variable_name"));
        assertThat(iterator.next(), is("variable_value"));
    }
    
    @Test
    public void assertShowTransactionType() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        ShowDistVariableExecutor executor = new ShowDistVariableExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(metaData, connectionSession, new ShowDistVariableStatement("TRANSACTION_TYPE"));
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("transaction_type"));
        assertThat(row.getCell(2), is("LOCAL"));
    }
    
    @Test
    public void assertShowCachedConnections() {
        when(connectionSession.getBackendConnection().getConnectionSize()).thenReturn(1);
        ShowDistVariableExecutor executor = new ShowDistVariableExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(metaData, connectionSession, new ShowDistVariableStatement("CACHED_CONNECTIONS"));
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("cached_connections"));
        assertThat(row.getCell(2), is("1"));
    }
    
    @Test
    public void assertShowAgentPluginsEnabled() {
        SystemPropertyUtil.setSystemProperty(VariableEnum.AGENT_PLUGINS_ENABLED.name(), Boolean.FALSE.toString());
        ShowDistVariableExecutor executor = new ShowDistVariableExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(metaData, connectionSession, new ShowDistVariableStatement("AGENT_PLUGINS_ENABLED"));
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("agent_plugins_enabled"));
        assertThat(row.getCell(2), is("false"));
    }
    
    @Test
    public void assertShowPropsVariable() {
        when(metaData.getProps()).thenReturn(new ConfigurationProperties(PropertiesBuilder.build(new Property("sql-show", Boolean.TRUE.toString()))));
        ShowDistVariableExecutor executor = new ShowDistVariableExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(metaData, connectionSession, new ShowDistVariableStatement("SQL_SHOW"));
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("sql_show"));
        assertThat(row.getCell(2), is("true"));
    }
    
    @Test(expected = UnsupportedVariableException.class)
    public void assertExecuteWithInvalidVariableName() {
        new ShowDistVariableExecutor().getRows(metaData, connectionSession, new ShowDistVariableStatement("wrong_name"));
    }
}
