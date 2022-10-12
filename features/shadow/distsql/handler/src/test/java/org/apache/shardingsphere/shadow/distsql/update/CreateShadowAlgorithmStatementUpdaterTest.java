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
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.update.CreateShadowAlgorithmStatementUpdater;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateShadowAlgorithmStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateShadowAlgorithmStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShadowRuleConfiguration currentConfig;
    
    private final CreateShadowAlgorithmStatementUpdater updater = new CreateShadowAlgorithmStatementUpdater();
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithDuplicateAlgorithm() {
        Properties props = new Properties();
        props.setProperty("type", "value");
        CreateShadowAlgorithmStatement sqlStatement = createSQLStatement(new ShadowAlgorithmSegment("foo_algorithm", new AlgorithmSegment("SIMPLE_HINT", props)),
                new ShadowAlgorithmSegment("foo_algorithm", new AlgorithmSegment("SIMPLE_HINT", props)));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithExistAlgorithm() {
        when(currentConfig.getShadowAlgorithms()).thenReturn(Collections.singletonMap("foo_algorithm", null));
        Properties props = new Properties();
        props.setProperty("type", "value");
        CreateShadowAlgorithmStatement sqlStatement = createSQLStatement(new ShadowAlgorithmSegment("foo_algorithm", new AlgorithmSegment("SIMPLE_HINT", props)));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithAlgorithmCompleteness() {
        Properties props = new Properties();
        props.setProperty("type", "value");
        CreateShadowAlgorithmStatement sqlStatement = createSQLStatement(new ShadowAlgorithmSegment("foo_algorithm", new AlgorithmSegment("", props)));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithInvalidAlgorithmType() {
        Properties props = new Properties();
        props.setProperty("type", "value");
        CreateShadowAlgorithmStatement sqlStatement = createSQLStatement(new ShadowAlgorithmSegment("foo_algorithm", new AlgorithmSegment("NOT_EXISTED_ALGORITHM", props)));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    private CreateShadowAlgorithmStatement createSQLStatement(final ShadowAlgorithmSegment... ruleSegments) {
        return new CreateShadowAlgorithmStatement(Arrays.asList(ruleSegments));
    }
}
