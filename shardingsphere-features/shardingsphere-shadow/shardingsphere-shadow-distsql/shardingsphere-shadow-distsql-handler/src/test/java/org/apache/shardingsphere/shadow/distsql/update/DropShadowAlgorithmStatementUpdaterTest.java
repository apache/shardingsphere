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
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.update.DropShadowAlgorithmStatementUpdater;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropShadowAlgorithmStatement;
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

@RunWith(MockitoJUnitRunner.class)
public final class DropShadowAlgorithmStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    private final DropShadowAlgorithmStatementUpdater updater = new DropShadowAlgorithmStatementUpdater();
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertExecuteWithoutAlgorithmNameInMetaData() {
        updater.checkSQLStatement(database, createSQLStatement("ruleSegment"), null);
    }
    
    @Test
    public void assertExecuteWithIfExists() {
        DropShadowAlgorithmStatement sqlStatement = createSQLStatement(true, "ruleSegment");
        updater.checkSQLStatement(database, sqlStatement, mock(ShadowRuleConfiguration.class));
    }
    
    @Test
    public void assertUpdate() {
        DropShadowAlgorithmStatement sqlStatement = createSQLStatement(true, "ds_0");
        ShadowRuleConfiguration ruleConfig = new ShadowRuleConfiguration();
        ruleConfig.getTables().put("t_order", new ShadowTableConfiguration(new ArrayList<>(Collections.singleton("ds_0")), Collections.emptyList()));
        updater.checkSQLStatement(database, sqlStatement, ruleConfig);
        updater.updateCurrentRuleConfiguration(sqlStatement, ruleConfig);
        assertFalse(ruleConfig.getTables().containsKey("ds_0"));
    }
    
    private DropShadowAlgorithmStatement createSQLStatement(final String... ruleName) {
        return new DropShadowAlgorithmStatement(false, Arrays.asList(ruleName));
    }
    
    private DropShadowAlgorithmStatement createSQLStatement(final boolean ifExists, final String... ruleName) {
        return new DropShadowAlgorithmStatement(ifExists, Arrays.asList(ruleName));
    }
}
