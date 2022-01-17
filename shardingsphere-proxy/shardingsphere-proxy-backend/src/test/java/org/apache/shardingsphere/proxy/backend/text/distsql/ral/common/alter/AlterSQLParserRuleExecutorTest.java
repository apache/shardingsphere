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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.alter;

import org.apache.shardingsphere.distsql.parser.segment.CacheOptionSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.alter.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.alter.excutor.AlterSQLParserRuleExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterSQLParserRuleExecutorTest {
    
    @Test
    public void assertExecuteWithoutCurrentRuleConfiguration() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().getConfigurations()).thenReturn(new LinkedList<>());
        ProxyContext.getInstance().init(contextManager);
        new AlterSQLParserRuleExecutor(getSQLStatement()).execute();
        Collection<RuleConfiguration> globalRuleConfigurations = contextManager.getMetaDataContexts().getGlobalRuleMetaData().getConfigurations();
        RuleConfiguration ruleConfiguration = globalRuleConfigurations.stream().filter(configuration -> configuration instanceof SQLParserRuleConfiguration).findAny().orElse(null);
        assertNotNull(ruleConfiguration);
        SQLParserRuleConfiguration sqlParserRuleConfiguration = (SQLParserRuleConfiguration) ruleConfiguration;
        assertTrue(sqlParserRuleConfiguration.isSqlCommentParseEnabled());
        assertThat(sqlParserRuleConfiguration.getSqlStatementCache().getInitialCapacity(), is(1000));
        assertThat(sqlParserRuleConfiguration.getSqlStatementCache().getMaximumSize(), is(1000L));
        assertThat(sqlParserRuleConfiguration.getSqlStatementCache().getConcurrencyLevel(), is(3));
        assertThat(sqlParserRuleConfiguration.getParseTreeCache().getInitialCapacity(), is(64));
        assertThat(sqlParserRuleConfiguration.getParseTreeCache().getMaximumSize(), is(512L));
        assertThat(sqlParserRuleConfiguration.getParseTreeCache().getConcurrencyLevel(), is(3));
    }
    
    @Test
    public void assertExecuteWithDefaultRuleConfiguration() {
        Collection<RuleConfiguration> globalRuleConfiguration = new LinkedList<>();
        globalRuleConfiguration.add(new DefaultSQLParserRuleConfigurationBuilder().build());
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().getConfigurations()).thenReturn(globalRuleConfiguration);
        ProxyContext.getInstance().init(contextManager);
        new AlterSQLParserRuleExecutor(getSQLStatement()).execute();
        Collection<RuleConfiguration> globalRuleConfigurations = contextManager.getMetaDataContexts().getGlobalRuleMetaData().getConfigurations();
        RuleConfiguration ruleConfiguration = globalRuleConfigurations.stream().filter(configuration -> configuration instanceof SQLParserRuleConfiguration).findAny().orElse(null);
        assertNotNull(ruleConfiguration);
        SQLParserRuleConfiguration sqlParserRuleConfiguration = (SQLParserRuleConfiguration) ruleConfiguration;
        assertTrue(sqlParserRuleConfiguration.isSqlCommentParseEnabled());
        assertThat(sqlParserRuleConfiguration.getSqlStatementCache().getInitialCapacity(), is(1000));
        assertThat(sqlParserRuleConfiguration.getSqlStatementCache().getMaximumSize(), is(1000L));
        assertThat(sqlParserRuleConfiguration.getSqlStatementCache().getConcurrencyLevel(), is(3));
        assertThat(sqlParserRuleConfiguration.getParseTreeCache().getInitialCapacity(), is(64));
        assertThat(sqlParserRuleConfiguration.getParseTreeCache().getMaximumSize(), is(512L));
        assertThat(sqlParserRuleConfiguration.getParseTreeCache().getConcurrencyLevel(), is(3));
    }
    
    private AlterSQLParserRuleStatement getSQLStatement() {
        AlterSQLParserRuleStatement result = new AlterSQLParserRuleStatement();
        result.setSqlCommentParseEnable(Boolean.TRUE);
        result.setSqlStatementCache(getCacheOption(1000, 1000L, 3));
        result.setParseTreeCache(getCacheOption(64, 512L, 3));
        return result;
    }
    
    private CacheOptionSegment getCacheOption(final Integer initialCapacity, final Long maximumSize, final Integer concurrencyLevel) {
        return new CacheOptionSegment(initialCapacity, maximumSize, concurrencyLevel);
    }
}
