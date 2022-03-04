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

import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.update.DropShadowAlgorithmStatementUpdater;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropShadowAlgorithmStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public final class DropShadowAlgorithmStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    private final DropShadowAlgorithmStatementUpdater updater = new DropShadowAlgorithmStatementUpdater();
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertExecuteWithoutAlgorithmNameInMetaData() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("ruleSegment"), null);
    }
    
    @Test
    public void assertExecuteWithIfExists() throws DistSQLException {
        DropShadowAlgorithmStatement sqlStatement = createSQLStatement("ruleSegment");
        sqlStatement.setContainsExistClause(true);
        updater.checkSQLStatement(shardingSphereMetaData, sqlStatement, mock(ShadowRuleConfiguration.class));
    }
    
    @Test
    public void assertUpdate() throws DistSQLException {
        DropShadowAlgorithmStatement sqlStatement = createSQLStatement("ds_0");
        sqlStatement.setContainsExistClause(true);
        ShadowRuleConfiguration configuration = new ShadowRuleConfiguration();
        configuration.getTables().put("t_order", new ShadowTableConfiguration(new ArrayList<>(Collections.singletonList("ds_0")), Collections.emptyList()));
        updater.checkSQLStatement(shardingSphereMetaData, sqlStatement, configuration);
        updater.updateCurrentRuleConfiguration(sqlStatement, configuration);
        assertFalse(configuration.getTables().containsKey("ds_0"));
    }
    
    private DropShadowAlgorithmStatement createSQLStatement(final String... ruleName) {
        return new DropShadowAlgorithmStatement(Arrays.asList(ruleName));
    }
}
