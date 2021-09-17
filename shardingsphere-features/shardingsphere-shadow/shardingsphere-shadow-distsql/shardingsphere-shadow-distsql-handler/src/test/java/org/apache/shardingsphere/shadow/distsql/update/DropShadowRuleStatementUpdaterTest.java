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
import org.apache.shardingsphere.shadow.distsql.handler.update.DropShadowRuleStatementUpdater;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropShadowRuleStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DropShadowRuleStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShadowRuleConfiguration currentConfiguration;
    
    private final DropShadowRuleStatementUpdater updater = new DropShadowRuleStatementUpdater();
    
    @Before
    public void before() {
        when(currentConfiguration.getDataSources()).thenReturn(Collections.singletonMap("initRuleName", null));
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertExecuteWithoutRuleNameInMetaData() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("ruleSegment"), null);
    }
    
    @Test
    public void assertExecuteSuccess() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("initRuleName"), currentConfiguration);
    }
    
    private DropShadowRuleStatement createSQLStatement(final String... ruleName) {
        return new DropShadowRuleStatement(Arrays.asList(ruleName));
    }
}
