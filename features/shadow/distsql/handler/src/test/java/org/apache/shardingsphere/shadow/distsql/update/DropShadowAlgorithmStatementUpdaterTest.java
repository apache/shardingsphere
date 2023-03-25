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

import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.update.DropShadowAlgorithmStatementUpdater;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropShadowAlgorithmStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DropShadowAlgorithmStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    private final DropShadowAlgorithmStatementUpdater updater = new DropShadowAlgorithmStatementUpdater();
    
    @Test
    void assertExecuteWithoutAlgorithmNameInMetaData() {
        assertThrows(MissingRequiredRuleException.class, () -> updater.checkSQLStatement(database, createSQLStatement("ruleSegment"), null));
    }
    
    @Test
    void assertExecuteWithIfExists() {
        DropShadowAlgorithmStatement sqlStatement = createSQLStatement(true, "ruleSegment");
        updater.checkSQLStatement(database, sqlStatement, mock(ShadowRuleConfiguration.class));
    }
    
    @Test
    void assertUpdate() {
        DropShadowAlgorithmStatement sqlStatement = createSQLStatement("shadow_algorithm");
        ShadowRuleConfiguration ruleConfig = new ShadowRuleConfiguration();
        ruleConfig.getShadowAlgorithms().put("shadow_algorithm", new AlgorithmConfiguration("type", null));
        updater.checkSQLStatement(database, sqlStatement, ruleConfig);
        updater.updateCurrentRuleConfiguration(sqlStatement, ruleConfig);
        assertTrue(ruleConfig.getShadowAlgorithms().isEmpty());
    }
    
    private DropShadowAlgorithmStatement createSQLStatement(final String... ruleName) {
        return new DropShadowAlgorithmStatement(false, Arrays.asList(ruleName));
    }
    
    private DropShadowAlgorithmStatement createSQLStatement(final boolean ifExists, final String... ruleName) {
        return new DropShadowAlgorithmStatement(ifExists, Arrays.asList(ruleName));
    }
}
