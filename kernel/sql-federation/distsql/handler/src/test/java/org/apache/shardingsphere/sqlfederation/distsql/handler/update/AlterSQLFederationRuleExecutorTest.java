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

package org.apache.shardingsphere.sqlfederation.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecuteEngine;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.distsql.segment.CacheOptionSegment;
import org.apache.shardingsphere.sqlfederation.distsql.statement.updatable.AlterSQLFederationRuleStatement;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.rule.builder.DefaultSQLFederationRuleConfigurationBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.sql.SQLException;
import java.util.Collections;

import static org.apache.shardingsphere.test.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlterSQLFederationRuleExecutorTest {
    
    @Test
    void assertExecuteUpdate() throws SQLException {
        AlterSQLFederationRuleStatement sqlStatement = new AlterSQLFederationRuleStatement(true, true, new CacheOptionSegment(64, 512L));
        ContextManager contextManager = mockContextManager();
        new DistSQLUpdateExecuteEngine(sqlStatement, null, contextManager).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getMetaDataManagerPersistService();
        verify(metaDataManagerPersistService).alterGlobalRuleConfiguration(
                ArgumentMatchers.<SQLFederationRuleConfiguration>argThat(x -> assertRuleConfiguration(x, true, true, new CacheOption(64, 512L))));
    }
    
    @Test
    void assertExecuteUpdateWithNullStatement() throws SQLException {
        AlterSQLFederationRuleStatement sqlStatement = new AlterSQLFederationRuleStatement(null, null, null);
        ContextManager contextManager = mockContextManager();
        new DistSQLUpdateExecuteEngine(sqlStatement, null, contextManager).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getMetaDataManagerPersistService();
        verify(metaDataManagerPersistService).alterGlobalRuleConfiguration(
                ArgumentMatchers.<SQLFederationRuleConfiguration>argThat(x -> assertRuleConfiguration(x, false, false, new CacheOption(2000, 65535L))));
    }
    
    @Test
    void assertExecuteUpdateWithNullCacheOptionSegment() throws SQLException {
        AlterSQLFederationRuleStatement sqlStatement = new AlterSQLFederationRuleStatement(null, null, new CacheOptionSegment(null, null));
        ContextManager contextManager = mockContextManager();
        new DistSQLUpdateExecuteEngine(sqlStatement, null, contextManager).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getMetaDataManagerPersistService();
        verify(metaDataManagerPersistService).alterGlobalRuleConfiguration(
                ArgumentMatchers.<SQLFederationRuleConfiguration>argThat(x -> assertRuleConfiguration(x, false, false, new CacheOption(2000, 65535L))));
    }
    
    private boolean assertRuleConfiguration(final SQLFederationRuleConfiguration actual,
                                            final boolean expectedSQLFederationEnabled, final boolean expectedAllQueryUseSQLFederation, final CacheOption expectedExecutionPlanCache) {
        assertThat(actual.isSqlFederationEnabled(), is(expectedSQLFederationEnabled));
        assertThat(actual.isAllQueryUseSQLFederation(), is(expectedAllQueryUseSQLFederation));
        assertThat(actual.getExecutionPlanCache(), deepEqual(expectedExecutionPlanCache));
        return true;
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        SQLFederationRule rule = mock(SQLFederationRule.class);
        when(rule.getConfiguration()).thenReturn(new DefaultSQLFederationRuleConfigurationBuilder().build());
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
}
