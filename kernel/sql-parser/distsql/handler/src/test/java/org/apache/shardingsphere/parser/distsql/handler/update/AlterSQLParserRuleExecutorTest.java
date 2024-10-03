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

package org.apache.shardingsphere.parser.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecuteEngine;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.distsql.segment.CacheOptionSegment;
import org.apache.shardingsphere.parser.distsql.statement.updatable.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.sql.SQLException;
import java.util.Collections;

import static org.apache.shardingsphere.test.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlterSQLParserRuleExecutorTest {
    
    @Test
    void assertExecute() throws SQLException {
        AlterSQLParserRuleStatement sqlStatement = new AlterSQLParserRuleStatement(new CacheOptionSegment(64, 512L), new CacheOptionSegment(1000, 1000L));
        ContextManager contextManager = mockContextManager();
        new DistSQLUpdateExecuteEngine(sqlStatement, null, contextManager).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getMetaDataManagerPersistService();
        verify(metaDataManagerPersistService).alterGlobalRuleConfiguration(
                ArgumentMatchers.<SQLParserRuleConfiguration>argThat(x -> assertRuleConfiguration(x, new CacheOption(64, 512L), new CacheOption(1000, 1000L))));
    }
    
    @Test
    void assertExecuteWithNullStatement() throws SQLException {
        AlterSQLParserRuleStatement sqlStatement = new AlterSQLParserRuleStatement(null, null);
        ContextManager contextManager = mockContextManager();
        new DistSQLUpdateExecuteEngine(sqlStatement, null, contextManager).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getMetaDataManagerPersistService();
        verify(metaDataManagerPersistService).alterGlobalRuleConfiguration(
                ArgumentMatchers.<SQLParserRuleConfiguration>argThat(x -> assertRuleConfiguration(x, new CacheOption(128, 1024L), new CacheOption(2000, 65535L))));
    }
    
    @Test
    void assertExecuteWithNullCacheOptionSegment() throws SQLException {
        AlterSQLParserRuleStatement sqlStatement = new AlterSQLParserRuleStatement(new CacheOptionSegment(null, null), new CacheOptionSegment(null, null));
        ContextManager contextManager = mockContextManager();
        new DistSQLUpdateExecuteEngine(sqlStatement, null, contextManager).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getMetaDataManagerPersistService();
        verify(metaDataManagerPersistService).alterGlobalRuleConfiguration(
                ArgumentMatchers.<SQLParserRuleConfiguration>argThat(x -> assertRuleConfiguration(x, new CacheOption(128, 1024L), new CacheOption(2000, 65535L))));
    }
    
    private boolean assertRuleConfiguration(final SQLParserRuleConfiguration actual, final CacheOption expectedParseTreeCache, final CacheOption expectedSQLStatementCache) {
        assertThat(actual.getParseTreeCache(), deepEqual(expectedParseTreeCache));
        assertThat(actual.getSqlStatementCache(), deepEqual(expectedSQLStatementCache));
        return true;
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        SQLParserRule rule = mock(SQLParserRule.class);
        when(rule.getConfiguration()).thenReturn(new DefaultSQLParserRuleConfigurationBuilder().build());
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
}
