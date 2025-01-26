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

package org.apache.shardingsphere.test.it.distsql.handler.engine.update;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecuteEngine;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.GlobalRuleConfiguration;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;

import java.sql.SQLException;
import java.util.Collections;

import static org.apache.shardingsphere.test.matcher.ShardingSphereArgumentVerifyMatchers.deepEq;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RequiredArgsConstructor
public abstract class GlobalRuleDefinitionExecutorTest {
    
    private final GlobalRule mockedRule;
    
    protected void assertExecuteUpdate(final GlobalRuleConfiguration ruleConfig,
                                       final DistSQLStatement sqlStatement, final RuleConfiguration matchedRuleConfig, final Class<? extends Exception> expectedException) throws SQLException {
        ContextManager contextManager = mockContextManager(ruleConfig);
        DistSQLUpdateExecuteEngine engine = new DistSQLUpdateExecuteEngine(sqlStatement, null, contextManager);
        if (null != expectedException) {
            assertThrows(InvalidRuleConfigurationException.class, engine::executeUpdate);
            return;
        }
        engine.executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getMetaDataManagerPersistService();
        verify(metaDataManagerPersistService).alterGlobalRuleConfiguration(deepEq(matchedRuleConfig));
    }
    
    private ContextManager mockContextManager(final GlobalRuleConfiguration ruleConfig) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(mockedRule.getConfiguration()).thenReturn(ruleConfig);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(mockedRule)));
        return result;
    }
}
