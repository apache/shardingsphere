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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DropShadowRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShadowRuleConfiguration currentConfig;
    
    private final DropShadowRuleStatementUpdater updater = new DropShadowRuleStatementUpdater();
    
    @Before
    public void before() {
        when(currentConfig.getDataSources()).thenReturn(Collections.singletonList(new ShadowDataSourceConfiguration("initRuleName", "ds", "ds_shadow")));
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertExecuteWithoutRuleNameInMetaData() {
        updater.checkSQLStatement(database, createSQLStatement("ruleSegment"), null);
    }
    
    @Test
    public void assertExecuteWithIfExists() {
        DropShadowRuleStatement sqlStatement = createSQLStatement(true, "ruleSegment");
        updater.checkSQLStatement(database, sqlStatement, mock(ShadowRuleConfiguration.class));
    }
    
    @Test
    public void assertUpdate() {
        DropShadowRuleStatement sqlStatement = createSQLStatement(true, "ds_0");
        ShadowRuleConfiguration ruleConfig = new ShadowRuleConfiguration();
        ruleConfig.getTables().put("t_order", new ShadowTableConfiguration(new ArrayList<>(Collections.singleton("ds_0")), Collections.emptyList()));
        updater.checkSQLStatement(database, sqlStatement, ruleConfig);
        updater.updateCurrentRuleConfiguration(sqlStatement, ruleConfig);
        assertFalse(ruleConfig.getTables().containsKey("ds_0"));
    }
    
    @Test
    public void assertExecuteSuccess() {
        updater.checkSQLStatement(database, createSQLStatement("initRuleName"), currentConfig);
    }
    
    private DropShadowRuleStatement createSQLStatement(final String... ruleName) {
        return new DropShadowRuleStatement(false, Arrays.asList(ruleName));
    }
    
    private DropShadowRuleStatement createSQLStatement(final boolean ifExists, final String... ruleName) {
        return new DropShadowRuleStatement(ifExists, Arrays.asList(ruleName));
    }
}
