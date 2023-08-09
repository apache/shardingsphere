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

import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowDistVariablesStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationProperties;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.logging.rule.LoggingRule;
import org.apache.shardingsphere.logging.rule.builder.DefaultLoggingRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowDistVariablesExecutorTest {
    
    private final ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
    
    private final ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
    
    @Test
    void assertGetColumns() {
        ShowDistVariablesExecutor executor = new ShowDistVariablesExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(2));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("variable_name"));
        assertThat(iterator.next(), is("variable_value"));
    }
    
    @Test
    void assertExecute() {
        when(metaData.getProps()).thenReturn(new ConfigurationProperties(PropertiesBuilder.build(new Property("system_log_level", "INFO"))));
        when(metaData.getTemporaryProps()).thenReturn(new TemporaryConfigurationProperties(PropertiesBuilder.build(new Property("proxy-meta-data-collector-enabled", Boolean.FALSE.toString()))));
        when(metaData.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(new LoggingRule(new DefaultLoggingRuleConfigurationBuilder().build()))));
        ShowDistVariablesExecutor executor = new ShowDistVariablesExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(metaData, connectionSession, mock(ShowDistVariablesStatement.class));
        assertThat(actual.size(), is(20));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("agent_plugins_enabled"));
        assertThat(row.getCell(2), is("true"));
    }
    
    @Test
    void assertExecuteWithLike() {
        when(metaData.getProps()).thenReturn(new ConfigurationProperties(PropertiesBuilder.build(new Property("system_log_level", "INFO"))));
        when(metaData.getTemporaryProps()).thenReturn(new TemporaryConfigurationProperties(PropertiesBuilder.build(new Property("proxy-meta-data-collector-enabled", Boolean.FALSE.toString()))));
        when(metaData.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(new LoggingRule(new DefaultLoggingRuleConfigurationBuilder().build()))));
        ShowDistVariablesExecutor executor = new ShowDistVariablesExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(metaData, connectionSession, new ShowDistVariablesStatement("sql_%"));
        assertThat(actual.size(), is(2));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        assertThat(iterator.next().getCell(1), is("sql_show"));
        assertThat(iterator.next().getCell(1), is("sql_simple"));
    }
}
