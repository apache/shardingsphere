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
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.update.CreateShadowAlgorithmStatementUpdater;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateShadowAlgorithmStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateShadowAlgorithmStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShadowRuleConfiguration currentConfig;
    
    private final CreateShadowAlgorithmStatementUpdater updater = new CreateShadowAlgorithmStatementUpdater();
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithDuplicateAlgorithm() throws DistSQLException {
        Properties props = new Properties();
        props.setProperty("type", "value");
        CreateShadowAlgorithmStatement sqlStatement = createSQLStatement(new ShadowAlgorithmSegment("simpleNoteAlgorithm", new AlgorithmSegment("SIMPLE_HINT", props)),
                new ShadowAlgorithmSegment("simpleNoteAlgorithm", new AlgorithmSegment("SIMPLE_HINT", props)));
        updater.checkSQLStatement(shardingSphereMetaData, sqlStatement, currentConfig);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithExistAlgorithm() throws DistSQLException {
        when(currentConfig.getShadowAlgorithms()).thenReturn(Collections.singletonMap("simpleNoteAlgorithm", null));
        Properties props = new Properties();
        props.setProperty("type", "value");
        CreateShadowAlgorithmStatement sqlStatement = createSQLStatement(new ShadowAlgorithmSegment("simpleNoteAlgorithm", new AlgorithmSegment("SIMPLE_HINT", props)));
        updater.checkSQLStatement(shardingSphereMetaData, sqlStatement, currentConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithAlgorithmCompleteness() throws DistSQLException {
        Properties props = new Properties();
        props.setProperty("type", "value");
        CreateShadowAlgorithmStatement sqlStatement = createSQLStatement(new ShadowAlgorithmSegment("simpleNoteAlgorithm1", new AlgorithmSegment("", props)));
        updater.checkSQLStatement(shardingSphereMetaData, sqlStatement, currentConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithInvalidAlgorithmType() throws DistSQLException {
        Properties props = new Properties();
        props.setProperty("type", "value");
        CreateShadowAlgorithmStatement sqlStatement = createSQLStatement(new ShadowAlgorithmSegment("simpleNoteAlgorithm1", new AlgorithmSegment("HINT_TEST_1", props)));
        updater.checkSQLStatement(shardingSphereMetaData, sqlStatement, currentConfig);
    }
    
    private CreateShadowAlgorithmStatement createSQLStatement(final ShadowAlgorithmSegment... ruleSegments) {
        return new CreateShadowAlgorithmStatement(Arrays.asList(ruleSegments));
    }
}
