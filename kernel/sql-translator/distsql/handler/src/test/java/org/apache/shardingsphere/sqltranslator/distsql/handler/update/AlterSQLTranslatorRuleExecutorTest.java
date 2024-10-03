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
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sqltranslator.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.distsql.statement.updateable.AlterSQLTranslatorRuleStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlterSQLTranslatorRuleExecutorTest {
    
    @Test
    void assertExecuteWithTrueOriginalSQLWhenTranslatingFailed() throws SQLException {
        AlterSQLTranslatorRuleStatement sqlStatement = new AlterSQLTranslatorRuleStatement(new AlgorithmSegment("Native", PropertiesBuilder.build(new Property("foo", "bar"))), true);
        ContextManager contextManager = mockContextManager();
        DistSQLUpdateExecuteEngine engine = new DistSQLUpdateExecuteEngine(sqlStatement, null, contextManager);
        engine.executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getMetaDataManagerPersistService();
        verify(metaDataManagerPersistService).alterGlobalRuleConfiguration(ArgumentMatchers.argThat(this::assertRuleConfiguration));
    }
    
    @Test
    void assertExecuteWithNullOriginalSQLWhenTranslatingFailed() throws SQLException {
        AlterSQLTranslatorRuleStatement sqlStatement = new AlterSQLTranslatorRuleStatement(new AlgorithmSegment("Native", PropertiesBuilder.build(new Property("foo", "bar"))), null);
        ContextManager contextManager = mockContextManager();
        DistSQLUpdateExecuteEngine engine = new DistSQLUpdateExecuteEngine(sqlStatement, null, contextManager);
        engine.executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getMetaDataManagerPersistService();
        verify(metaDataManagerPersistService).alterGlobalRuleConfiguration(ArgumentMatchers.argThat(this::assertRuleConfiguration));
    }
    
    private boolean assertRuleConfiguration(final SQLTranslatorRuleConfiguration actual) {
        assertThat(actual.getProps().size(), is(1));
        assertThat(actual.getProps().getProperty("foo"), is("bar"));
        assertTrue(actual.isUseOriginalSQLWhenTranslatingFailed());
        return true;
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        SQLTranslatorRule rule = mock(SQLTranslatorRule.class);
        when(rule.getConfiguration()).thenReturn(new SQLTranslatorRuleConfiguration("NATIVE", new Properties(), true));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
}
