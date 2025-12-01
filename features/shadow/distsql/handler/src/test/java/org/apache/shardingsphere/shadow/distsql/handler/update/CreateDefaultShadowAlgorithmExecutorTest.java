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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.statement.CreateDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateDefaultShadowAlgorithmExecutorTest {
    
    private final CreateDefaultShadowAlgorithmExecutor executor = new CreateDefaultShadowAlgorithmExecutor();
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    void assertExecuteWithInvalidAlgorithm() {
        CreateDefaultShadowAlgorithmStatement statement = mock(CreateDefaultShadowAlgorithmStatement.class);
        when(statement.getShadowAlgorithmSegment()).thenReturn(new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("name", PropertiesBuilder.build(new Property("type", "value")))));
        ShadowRule rule = mock(ShadowRule.class);
        executor.setRule(rule);
        assertThrows(ServiceProviderNotFoundException.class, () -> executor.checkBeforeUpdate(statement));
    }
    
    @Test
    void assertExecuteSuccess() {
        CreateDefaultShadowAlgorithmStatement sqlStatement = mock(CreateDefaultShadowAlgorithmStatement.class);
        when(sqlStatement.getShadowAlgorithmSegment()).thenReturn(
                new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("SQL_HINT", PropertiesBuilder.build(new Property("type", "value")))));
        ShadowRule rule = mock(ShadowRule.class);
        executor.setRule(rule);
        executor.checkBeforeUpdate(sqlStatement);
    }
    
    @Test
    void assertExecuteWithIfNotExists() {
        ShadowAlgorithmSegment shadowAlgorithmSegment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("SQL_HINT", PropertiesBuilder.build(new Property("type", "value"))));
        CreateDefaultShadowAlgorithmStatement sqlStatement = new CreateDefaultShadowAlgorithmStatement(true, shadowAlgorithmSegment);
        executor.checkBeforeUpdate(sqlStatement);
    }
}
