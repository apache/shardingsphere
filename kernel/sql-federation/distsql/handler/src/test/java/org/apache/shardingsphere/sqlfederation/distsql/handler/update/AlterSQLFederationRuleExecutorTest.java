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
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.global.GlobalRuleDefinitionExecutor;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sqlfederation.distsql.segment.CacheOptionSegment;
import org.apache.shardingsphere.sqlfederation.distsql.statement.updatable.AlterSQLFederationRuleStatement;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.rule.builder.DefaultSQLFederationRuleConfigurationBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlterSQLFederationRuleExecutorTest {
    
    private DistSQLUpdateExecuteEngine engine;
    
    @Test
    void assertExecute() {
        AlterSQLFederationRuleStatement sqlStatement = new AlterSQLFederationRuleStatement(true, true, new CacheOptionSegment(64, 512L));
        engine = new DistSQLUpdateExecuteEngine(sqlStatement, null, mockContextManager());
        assertDoesNotThrow(() -> engine.executeUpdate());
    }
    
    @Test
    void assertExecuteWithNullStatement() {
        AlterSQLFederationRuleStatement sqlStatement = new AlterSQLFederationRuleStatement(null, null, null);
        engine = new DistSQLUpdateExecuteEngine(sqlStatement, null, mockContextManager());
        assertDoesNotThrow(() -> engine.executeUpdate());
    }
    
    @Test
    void assertExecuteWithNullCacheOptionSegment() {
        AlterSQLFederationRuleStatement sqlStatement = new AlterSQLFederationRuleStatement(null, null, new CacheOptionSegment(null, null));
        engine = new DistSQLUpdateExecuteEngine(sqlStatement, null, mockContextManager());
        assertDoesNotThrow(() -> engine.executeUpdate());
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        SQLFederationRule rule = mock(SQLFederationRule.class);
        GlobalRuleDefinitionExecutor executor = mock(GlobalRuleDefinitionExecutor.class);
        when(executor.getRuleClass()).thenReturn(SQLFederationRule.class);
        when(rule.getConfiguration()).thenReturn(new DefaultSQLFederationRuleConfigurationBuilder().build());
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(executor.getRuleClass())).thenReturn(rule);
        return result;
    }
}
