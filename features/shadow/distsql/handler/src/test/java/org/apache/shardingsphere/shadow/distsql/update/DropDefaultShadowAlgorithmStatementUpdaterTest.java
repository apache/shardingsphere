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

package org.apache.shardingsphere.shadow.distsql.update;

import org.apache.shardingsphere.distsql.handler.exception.algorithm.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.update.DropDefaultShadowAlgorithmStatementUpdater;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropDefaultShadowAlgorithmStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DropDefaultShadowAlgorithmStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShadowRuleConfiguration currentConfig;
    
    private final DropDefaultShadowAlgorithmStatementUpdater updater = new DropDefaultShadowAlgorithmStatementUpdater();
    
    @Test
    void assertCheckWithoutDefaultAlgorithm() {
        assertThrows(MissingRequiredAlgorithmException.class, () -> updater.checkSQLStatement(database, new DropDefaultShadowAlgorithmStatement(false), currentConfig));
    }
    
    @Test
    void assertCheckWithIfExists() {
        updater.checkSQLStatement(database, new DropDefaultShadowAlgorithmStatement(true), currentConfig);
        updater.checkSQLStatement(database, new DropDefaultShadowAlgorithmStatement(true), null);
    }
    
    @Test
    void assertUpdate() {
        ShadowRuleConfiguration ruleConfig = new ShadowRuleConfiguration();
        ruleConfig.setDefaultShadowAlgorithmName("default");
        ruleConfig.getShadowAlgorithms().put(ruleConfig.getDefaultShadowAlgorithmName(), mock(AlgorithmConfiguration.class));
        DropDefaultShadowAlgorithmStatement statement = new DropDefaultShadowAlgorithmStatement(false);
        updater.checkSQLStatement(database, new DropDefaultShadowAlgorithmStatement(true), ruleConfig);
        assertTrue(updater.hasAnyOneToBeDropped(statement, ruleConfig));
        updater.updateCurrentRuleConfiguration(statement, ruleConfig);
        assertNull(ruleConfig.getDefaultShadowAlgorithmName());
        assertTrue(ruleConfig.getShadowAlgorithms().isEmpty());
    }
}
