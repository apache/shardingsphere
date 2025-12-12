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

package org.apache.shardingsphere.shadow.distsql.handler.update;

import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.statement.AlterDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlterDefaultShadowAlgorithmExecutorTest {
    
    private final AlterDefaultShadowAlgorithmExecutor executor = new AlterDefaultShadowAlgorithmExecutor();
    
    @Mock
    private ShadowRuleConfiguration currentConfig;
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    void assertExecuteAlgorithmNotInMetaData() {
        Properties props = PropertiesBuilder.build(new Property("type", "value"));
        when(currentConfig.getShadowAlgorithms()).thenReturn(Collections.singletonMap("sqlHintAlgorithm", new AlgorithmConfiguration("type", props)));
        AlterDefaultShadowAlgorithmStatement sqlStatement = new AlterDefaultShadowAlgorithmStatement(
                new ShadowAlgorithmSegment("default_shadow_algorithm", new AlgorithmSegment("SQL_HINT", props)));
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        assertThrows(UnregisteredAlgorithmException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertExecuteInvalidAlgorithmType() {
        AlterDefaultShadowAlgorithmStatement sqlStatement = new AlterDefaultShadowAlgorithmStatement(
                new ShadowAlgorithmSegment("default_shadow_algorithm", new AlgorithmSegment("NOT_EXIST_SQL_HINT", PropertiesBuilder.build(new Property("type", "value")))));
        assertThrows(ServiceProviderNotFoundException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertExecuteIncompletenessAlgorithm() {
        AlterDefaultShadowAlgorithmStatement sqlStatement = new AlterDefaultShadowAlgorithmStatement(
                new ShadowAlgorithmSegment("default_shadow_algorithm", new AlgorithmSegment("", PropertiesBuilder.build(new Property("type", "value")))));
        assertThrows(MissingRequiredAlgorithmException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertExecuteSuccess() {
        Properties props = PropertiesBuilder.build(new Property("type", "value"));
        when(currentConfig.getShadowAlgorithms()).thenReturn(Collections.singletonMap("default_shadow_algorithm", new AlgorithmConfiguration("type", props)));
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        AlterDefaultShadowAlgorithmStatement sqlStatement = new AlterDefaultShadowAlgorithmStatement(new ShadowAlgorithmSegment("default_shadow_algorithm", new AlgorithmSegment("SQL_HINT", props)));
        executor.checkBeforeUpdate(sqlStatement);
    }
}
