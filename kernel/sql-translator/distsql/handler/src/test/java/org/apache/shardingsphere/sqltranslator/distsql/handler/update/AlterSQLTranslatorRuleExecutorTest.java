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

package org.apache.shardingsphere.sqltranslator.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecuteEngine;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.global.GlobalRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sqltranslator.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.distsql.statement.updateable.AlterSQLTranslatorRuleStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlterSQLTranslatorRuleExecutorTest {
    
    @Test
    void assertExecute() {
        AlterSQLTranslatorRuleStatement sqlStatement = new AlterSQLTranslatorRuleStatement(new AlgorithmSegment("Native", PropertiesBuilder.build(new Property("foo", "bar"))), true);
        DistSQLUpdateExecuteEngine engine = new DistSQLUpdateExecuteEngine(sqlStatement, null, mockContextManager());
        assertDoesNotThrow(engine::executeUpdate);
    }
    
    @Test
    void assertExecuteWithNullOriginalSQLWhenTranslatingFailed() {
        AlterSQLTranslatorRuleStatement sqlStatement = new AlterSQLTranslatorRuleStatement(new AlgorithmSegment("Native", PropertiesBuilder.build(new Property("foo", "bar"))), null);
        DistSQLUpdateExecuteEngine engine = new DistSQLUpdateExecuteEngine(sqlStatement, null, mockContextManager());
        assertDoesNotThrow(engine::executeUpdate);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        SQLTranslatorRule rule = mock(SQLTranslatorRule.class);
        GlobalRuleDefinitionExecutor executor = mock(GlobalRuleDefinitionExecutor.class);
        when(executor.getRuleClass()).thenReturn(SQLTranslatorRule.class);
        when(rule.getConfiguration()).thenReturn(new SQLTranslatorRuleConfiguration("NATIVE", new Properties(), true));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(executor.getRuleClass())).thenReturn(rule);
        return result;
    }
}
