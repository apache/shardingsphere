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

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.update.AlterDefaultShadowAlgorithmStatementUpdater;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.AlterDefaultShadowAlgorithmStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Properties;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AlterDefaultShadowAlgorithmStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShadowRuleConfiguration currentConfig;
    
    private final AlterDefaultShadowAlgorithmStatementUpdater updater = new AlterDefaultShadowAlgorithmStatementUpdater();
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertExecuteAlgorithmWithoutConfiguration() {
        Properties prop = new Properties();
        prop.setProperty("type", "value");
        AlterDefaultShadowAlgorithmStatement sqlStatement = createSQLStatement(new ShadowAlgorithmSegment("simpleHintAlgorithm", new AlgorithmSegment("SIMPLE_HINT", prop)));
        updater.checkSQLStatement(database, sqlStatement, null);
    }
    
    @Test(expected = MissingRequiredAlgorithmException.class)
    public void assertExecuteAlgorithmNotInMetaData() {
        Properties prop = new Properties();
        prop.setProperty("type", "value");
        when(currentConfig.getShadowAlgorithms()).thenReturn(Collections.singletonMap("simpleHintAlgorithm", new AlgorithmConfiguration("type", prop)));
        AlterDefaultShadowAlgorithmStatement sqlStatement = createSQLStatement(new ShadowAlgorithmSegment("default_shadow_algorithm", new AlgorithmSegment("SIMPLE_HINT", prop)));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteInvalidAlgorithmType() {
        Properties prop = new Properties();
        prop.setProperty("type", "value");
        when(currentConfig.getShadowAlgorithms()).thenReturn(Collections.singletonMap("default_shadow_algorithm", new AlgorithmConfiguration("type", prop)));
        AlterDefaultShadowAlgorithmStatement sqlStatement = createSQLStatement(new ShadowAlgorithmSegment("default_shadow_algorithm", new AlgorithmSegment("NOT_EXIST_SIMPLE_HINT", prop)));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteIncompletenessAlgorithm() {
        Properties prop = new Properties();
        prop.setProperty("type", "value");
        when(currentConfig.getShadowAlgorithms()).thenReturn(Collections.singletonMap("default_shadow_algorithm", new AlgorithmConfiguration("type", prop)));
        AlterDefaultShadowAlgorithmStatement sqlStatement = createSQLStatement(new ShadowAlgorithmSegment("default_shadow_algorithm", new AlgorithmSegment("", prop)));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    @Test
    public void assertExecuteSuccess() {
        Properties prop = new Properties();
        prop.setProperty("type", "value");
        when(currentConfig.getShadowAlgorithms()).thenReturn(Collections.singletonMap("default_shadow_algorithm", new AlgorithmConfiguration("type", prop)));
        AlterDefaultShadowAlgorithmStatement sqlStatement = createSQLStatement(new ShadowAlgorithmSegment("default_shadow_algorithm", new AlgorithmSegment("SIMPLE_HINT", prop)));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    private AlterDefaultShadowAlgorithmStatement createSQLStatement(final ShadowAlgorithmSegment shadowAlgorithmSegment) {
        return new AlterDefaultShadowAlgorithmStatement(shadowAlgorithmSegment);
    }
}
