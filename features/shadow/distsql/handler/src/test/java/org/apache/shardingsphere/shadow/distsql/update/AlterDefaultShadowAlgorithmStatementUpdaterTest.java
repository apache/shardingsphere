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

import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.update.AlterDefaultShadowAlgorithmStatementUpdater;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.AlterDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlterDefaultShadowAlgorithmStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShadowRuleConfiguration currentConfig;
    
    private final AlterDefaultShadowAlgorithmStatementUpdater updater = new AlterDefaultShadowAlgorithmStatementUpdater();
    
    @Test
    void assertExecuteAlgorithmWithoutConfiguration() {
        AlterDefaultShadowAlgorithmStatement sqlStatement = new AlterDefaultShadowAlgorithmStatement(
                new ShadowAlgorithmSegment("sqlHintAlgorithm", new AlgorithmSegment("SQL_HINT", PropertiesBuilder.build(new Property("type", "value")))));
        assertThrows(MissingRequiredRuleException.class, () -> updater.checkSQLStatement(database, sqlStatement, null));
    }
    
    @Test
    void assertExecuteAlgorithmNotInMetaData() {
        Properties props = PropertiesBuilder.build(new Property("type", "value"));
        when(currentConfig.getShadowAlgorithms()).thenReturn(Collections.singletonMap("sqlHintAlgorithm", new AlgorithmConfiguration("type", props)));
        AlterDefaultShadowAlgorithmStatement sqlStatement = new AlterDefaultShadowAlgorithmStatement(
                new ShadowAlgorithmSegment("default_shadow_algorithm", new AlgorithmSegment("SQL_HINT", props)));
        assertThrows(MissingRequiredAlgorithmException.class, () -> updater.checkSQLStatement(database, sqlStatement, currentConfig));
    }
    
    @Test
    void assertExecuteInvalidAlgorithmType() {
        AlterDefaultShadowAlgorithmStatement sqlStatement = new AlterDefaultShadowAlgorithmStatement(
                new ShadowAlgorithmSegment("default_shadow_algorithm", new AlgorithmSegment("NOT_EXIST_SQL_HINT", PropertiesBuilder.build(new Property("type", "value")))));
        assertThrows(ServiceProviderNotFoundException.class, () -> updater.checkSQLStatement(database, sqlStatement, currentConfig));
    }
    
    @Test
    void assertExecuteIncompletenessAlgorithm() {
        AlterDefaultShadowAlgorithmStatement sqlStatement = new AlterDefaultShadowAlgorithmStatement(
                new ShadowAlgorithmSegment("default_shadow_algorithm", new AlgorithmSegment("", PropertiesBuilder.build(new Property("type", "value")))));
        assertThrows(InvalidAlgorithmConfigurationException.class, () -> updater.checkSQLStatement(database, sqlStatement, currentConfig));
    }
    
    @Test
    void assertExecuteSuccess() {
        Properties props = PropertiesBuilder.build(new Property("type", "value"));
        when(currentConfig.getShadowAlgorithms()).thenReturn(Collections.singletonMap("default_shadow_algorithm", new AlgorithmConfiguration("type", props)));
        AlterDefaultShadowAlgorithmStatement sqlStatement = new AlterDefaultShadowAlgorithmStatement(
                new ShadowAlgorithmSegment("default_shadow_algorithm", new AlgorithmSegment("SQL_HINT", props)));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
}
