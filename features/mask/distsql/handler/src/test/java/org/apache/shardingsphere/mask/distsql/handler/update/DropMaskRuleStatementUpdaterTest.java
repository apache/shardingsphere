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

package org.apache.shardingsphere.mask.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.parser.statement.DropMaskRuleStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DropMaskRuleStatementUpdaterTest {
    
    private final DropMaskRuleStatementUpdater updater = new DropMaskRuleStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test
    void assertCheckSQLStatementWithoutCurrentRule() {
        assertThrows(MissingRequiredRuleException.class, () -> updater.checkSQLStatement(database, createSQLStatement(false, "t_mask"), null));
    }
    
    @Test
    void assertCheckSQLStatementWithoutToBeDroppedRule() {
        assertThrows(MissingRequiredRuleException.class,
                () -> updater.checkSQLStatement(database, createSQLStatement(false, "t_mask"), new MaskRuleConfiguration(Collections.emptyList(), Collections.emptyMap())));
    }
    
    @Test
    void assertUpdateCurrentRuleConfiguration() {
        MaskRuleConfiguration ruleConfig = createCurrentRuleConfiguration();
        assertTrue(updater.updateCurrentRuleConfiguration(createSQLStatement(false, "t_mask"), ruleConfig));
        assertTrue(ruleConfig.getMaskAlgorithms().isEmpty());
        assertTrue(ruleConfig.getTables().isEmpty());
    }
    
    @Test
    void assertUpdateCurrentRuleConfigurationWithIfExists() {
        MaskRuleConfiguration ruleConfig = createCurrentRuleConfiguration();
        DropMaskRuleStatement statement = createSQLStatement(true, "t_user");
        updater.checkSQLStatement(database, statement, mock(MaskRuleConfiguration.class));
        assertFalse(updater.updateCurrentRuleConfiguration(statement, ruleConfig));
        assertThat(ruleConfig.getTables().size(), is(1));
    }
    
    private DropMaskRuleStatement createSQLStatement(final boolean ifExists, final String tableName) {
        return new DropMaskRuleStatement(ifExists, Collections.singleton(tableName));
    }
    
    private MaskRuleConfiguration createCurrentRuleConfiguration() {
        MaskColumnRuleConfiguration columnRuleConfig = new MaskColumnRuleConfiguration("user_id", "MD5");
        MaskTableRuleConfiguration tableRuleConfig = new MaskTableRuleConfiguration("t_mask", Collections.singleton(columnRuleConfig));
        return new MaskRuleConfiguration(new LinkedList<>(Collections.singleton(tableRuleConfig)), new HashMap<>());
    }
}
