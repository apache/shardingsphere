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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class DropMaskRuleStatementUpdaterTest {
    
    private final DropMaskRuleStatementUpdater updater = new DropMaskRuleStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() {
        updater.checkSQLStatement(database, createSQLStatement(false, "t_mask"), null);
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithoutToBeDroppedRule() {
        updater.checkSQLStatement(database, createSQLStatement(false, "t_mask"), new MaskRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfiguration() {
        MaskRuleConfiguration ruleConfig = createCurrentRuleConfiguration();
        assertTrue(updater.updateCurrentRuleConfiguration(createSQLStatement(false, "t_mask"), ruleConfig));
        assertTrue(ruleConfig.getMaskAlgorithms().isEmpty());
        assertTrue(ruleConfig.getTables().isEmpty());
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
