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

package org.apache.shardingsphere.globalclock.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecuteEngine;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.global.GlobalRuleDefinitionExecutor;
import org.apache.shardingsphere.globalclock.distsql.statement.updatable.AlterGlobalClockRuleStatement;
import org.apache.shardingsphere.globalclock.rule.GlobalClockRule;
import org.apache.shardingsphere.globalclock.rule.builder.DefaultGlobalClockRuleConfigurationBuilder;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

class AlterGlobalClockRuleExecutorTest {
    
    private DistSQLUpdateExecuteEngine engine;
    
    @BeforeEach
    void setUp() {
        AlterGlobalClockRuleStatement sqlStatement = new AlterGlobalClockRuleStatement("TSO", "redis", Boolean.TRUE, new Properties());
        engine = new DistSQLUpdateExecuteEngine(sqlStatement, null, mockContextManager());
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        GlobalClockRule rule = mock(GlobalClockRule.class);
        GlobalRuleDefinitionExecutor executor = mock(GlobalRuleDefinitionExecutor.class);
        when(executor.getRuleClass()).thenReturn(GlobalClockRule.class);
        when(rule.getConfiguration()).thenReturn(new DefaultGlobalClockRuleConfigurationBuilder().build());
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(executor.getRuleClass())).thenReturn(rule);
        return result;
    }
    
    @Test
    void assertExecute() {
        assertDoesNotThrow(() -> engine.executeUpdate());
    }
}
