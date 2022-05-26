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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable;

import org.apache.shardingsphere.distsql.parser.segment.CacheOptionSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AlterSQLParserRuleHandlerTest extends ProxyContextRestorer {
    
    @Test
    public void assertExecuteWithoutCurrentRuleConfiguration() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findRuleConfigurations(SQLParserRuleConfiguration.class)).thenReturn(Collections.emptyList());
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getConfigurations()).thenReturn(new LinkedList<>());
        ProxyContext.init(contextManager);
        new AlterSQLParserRuleHandler().initStatement(createSQLStatement()).execute();
        SQLParserRuleConfiguration actual = (SQLParserRuleConfiguration) contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getConfigurations().iterator().next();
        assertTrue(actual.isSqlCommentParseEnabled());
        assertThat(actual.getSqlStatementCache().getInitialCapacity(), is(1000));
        assertThat(actual.getSqlStatementCache().getMaximumSize(), is(1000L));
        assertThat(actual.getParseTreeCache().getInitialCapacity(), is(64));
        assertThat(actual.getParseTreeCache().getMaximumSize(), is(512L));
    }
    
    @Test
    public void assertExecuteWithDefaultRuleConfiguration() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        SQLParserRuleConfiguration sqlParserRuleConfig = new DefaultSQLParserRuleConfigurationBuilder().build();
        Collection<RuleConfiguration> globalRuleConfigs = new LinkedList<>(Collections.singleton(sqlParserRuleConfig));
        when(contextManager.getMetaDataContexts()
                .getMetaData().getGlobalRuleMetaData().findRuleConfigurations(SQLParserRuleConfiguration.class)).thenReturn(Collections.singleton(sqlParserRuleConfig));
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getConfigurations()).thenReturn(globalRuleConfigs);
        ProxyContext.init(contextManager);
        new AlterSQLParserRuleHandler().initStatement(createSQLStatement()).execute();
        SQLParserRuleConfiguration actual = (SQLParserRuleConfiguration) contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getConfigurations().iterator().next();
        assertTrue(actual.isSqlCommentParseEnabled());
        assertThat(actual.getSqlStatementCache().getInitialCapacity(), is(1000));
        assertThat(actual.getSqlStatementCache().getMaximumSize(), is(1000L));
        assertThat(actual.getParseTreeCache().getInitialCapacity(), is(64));
        assertThat(actual.getParseTreeCache().getMaximumSize(), is(512L));
    }
    
    private AlterSQLParserRuleStatement createSQLStatement() {
        AlterSQLParserRuleStatement result = new AlterSQLParserRuleStatement();
        result.setSqlCommentParseEnable(true);
        result.setSqlStatementCache(new CacheOptionSegment(1000, 1000L));
        result.setParseTreeCache(new CacheOptionSegment(64, 512L));
        return result;
    }
}
