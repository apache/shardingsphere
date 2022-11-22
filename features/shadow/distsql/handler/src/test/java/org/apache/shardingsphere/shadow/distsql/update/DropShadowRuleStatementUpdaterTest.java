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

import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.update.DropShadowRuleStatementUpdater;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropShadowRuleStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public final class DropShadowRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckWithNullConfiguration() {
        new DropShadowRuleStatementUpdater().checkSQLStatement(database, createSQLStatement("anyRuleName"), null);
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckWithRuleNotExisted() {
        new DropShadowRuleStatementUpdater().checkSQLStatement(database, createSQLStatement("notExistedRuleName"), mock(ShadowRuleConfiguration.class));
    }
    
    @Test
    public void assertCheckWithIfExists() {
        new DropShadowRuleStatementUpdater().checkSQLStatement(database, createSQLStatement(true, "notExistedRuleName"), mock(ShadowRuleConfiguration.class));
    }
    
    @Test
    public void assertUpdate() {
        DropShadowRuleStatement sqlStatement = createSQLStatement("shadow_group");
        ShadowRuleConfiguration ruleConfig = new ShadowRuleConfiguration();
        ruleConfig.getDataSources().add(createShadowDataSourceConfiguration("shadow_group"));
        ruleConfig.getTables().put("t_order", new ShadowTableConfiguration(new ArrayList<>(Collections.singleton("shadow_group")), Collections.emptyList()));
        DropShadowRuleStatementUpdater updater = new DropShadowRuleStatementUpdater();
        updater.checkSQLStatement(database, sqlStatement, ruleConfig);
        assertTrue(updater.updateCurrentRuleConfiguration(sqlStatement, ruleConfig));
        assertTrue(ruleConfig.getDataSources().isEmpty());
        assertTrue(ruleConfig.getTables().isEmpty());
    }
    
    private DropShadowRuleStatement createSQLStatement(final String... ruleName) {
        return new DropShadowRuleStatement(false, Arrays.asList(ruleName));
    }
    
    private DropShadowRuleStatement createSQLStatement(final boolean ifExists, final String... ruleName) {
        return new DropShadowRuleStatement(ifExists, Arrays.asList(ruleName));
    }
    
    private ShadowDataSourceConfiguration createShadowDataSourceConfiguration(final String ruleName) {
        return new ShadowDataSourceConfiguration(ruleName, "production", "shadow");
    }
}
