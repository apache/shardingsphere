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
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.global.GlobalRuleDefinitionExecutor;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.distsql.segment.CacheOptionSegment;
import org.apache.shardingsphere.parser.distsql.statement.updatable.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlterSQLParserRuleExecutorTest {
    
    private DistSQLUpdateExecuteEngine engine;
    
    @Test
    void assertExecute() {
        AlterSQLParserRuleStatement sqlStatement = new AlterSQLParserRuleStatement(new CacheOptionSegment(64, 512L), new CacheOptionSegment(1000, 1000L));
        engine = new DistSQLUpdateExecuteEngine(sqlStatement, null, mockContextManager());
        assertDoesNotThrow(() -> engine.executeUpdate());
    }
    
    @Test
    void assertExecuteWithNullStatement() {
        AlterSQLParserRuleStatement sqlStatement = new AlterSQLParserRuleStatement(null, null);
        engine = new DistSQLUpdateExecuteEngine(sqlStatement, null, mockContextManager());
        assertDoesNotThrow(() -> engine.executeUpdate());
    }
    
    @Test
    void assertExecuteWithNullCacheOptionSegment() {
        AlterSQLParserRuleStatement sqlStatement = new AlterSQLParserRuleStatement(new CacheOptionSegment(null, null), new CacheOptionSegment(null, null));
        engine = new DistSQLUpdateExecuteEngine(sqlStatement, null, mockContextManager());
        assertDoesNotThrow(() -> engine.executeUpdate());
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        SQLParserRule rule = mock(SQLParserRule.class);
        GlobalRuleDefinitionExecutor executor = mock(GlobalRuleDefinitionExecutor.class);
        when(executor.getRuleClass()).thenReturn(SQLParserRule.class);
        when(rule.getConfiguration()).thenReturn(new DefaultSQLParserRuleConfigurationBuilder().build());
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(executor.getRuleClass())).thenReturn(rule);
        return result;
    }
}
