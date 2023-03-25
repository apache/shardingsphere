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

package org.apache.shardingsphere.mask.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.parser.segment.MaskColumnSegment;
import org.apache.shardingsphere.mask.distsql.parser.segment.MaskRuleSegment;
import org.apache.shardingsphere.mask.distsql.parser.statement.AlterMaskRuleStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AlterMaskRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    private final AlterMaskRuleStatementUpdater updater = new AlterMaskRuleStatementUpdater();
    
    @Test
    void assertCheckSQLStatementWithoutCurrentRule() {
        assertThrows(MissingRequiredRuleException.class, () -> updater.checkSQLStatement(database, createSQLStatement("MD5"), null));
    }
    
    @Test
    void assertCheckSQLStatementWithoutToBeAlteredRules() {
        assertThrows(MissingRequiredRuleException.class,
                () -> updater.checkSQLStatement(database, createSQLStatement("MD5"), new MaskRuleConfiguration(Collections.emptyList(), Collections.emptyMap())));
    }
    
    @Test
    void assertCheckSQLStatementWithoutToBeAlteredAlgorithm() {
        assertThrows(MissingRequiredRuleException.class, () -> updater.checkSQLStatement(database, createSQLStatement("INVALID_TYPE"), createCurrentRuleConfig()));
    }
    
    @Test
    void assertCheckSQLStatementWithIncompleteDataType() {
        MaskColumnSegment columnSegment = new MaskColumnSegment("user_id", new AlgorithmSegment("test", new Properties()));
        MaskRuleSegment ruleSegment = new MaskRuleSegment("t_mask", Collections.singleton(columnSegment));
        AlterMaskRuleStatement statement = new AlterMaskRuleStatement(Collections.singleton(ruleSegment));
        assertThrows(MissingRequiredRuleException.class, () -> updater.checkSQLStatement(database, statement, createCurrentRuleConfig()));
    }
    
    @Test
    void assertUpdate() {
        MaskRuleConfiguration currentRuleConfig = createCurrentRuleConfig();
        MaskColumnSegment columnSegment = new MaskColumnSegment("order_id",
                new AlgorithmSegment("MD5", new Properties()));
        MaskRuleSegment ruleSegment = new MaskRuleSegment("t_order", Collections.singleton(columnSegment));
        AlterMaskRuleStatement sqlStatement = new AlterMaskRuleStatement(Collections.singleton(ruleSegment));
        MaskRuleConfiguration toBeAlteredRuleConfig = updater.buildToBeAlteredRuleConfiguration(sqlStatement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        assertThat(currentRuleConfig.getMaskAlgorithms().size(), is(1));
        assertTrue(currentRuleConfig.getMaskAlgorithms().containsKey("t_order_order_id_md5"));
    }
    
    private AlterMaskRuleStatement createSQLStatement(final String algorithmName) {
        MaskColumnSegment columnSegment = new MaskColumnSegment("user_id",
                new AlgorithmSegment(algorithmName, new Properties()));
        MaskRuleSegment ruleSegment = new MaskRuleSegment("t_mask", Collections.singleton(columnSegment));
        return new AlterMaskRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private MaskRuleConfiguration createCurrentRuleConfig() {
        Collection<MaskTableRuleConfiguration> tableRuleConfigs = new LinkedList<>();
        tableRuleConfigs.add(new MaskTableRuleConfiguration("t_order", Collections.emptyList()));
        return new MaskRuleConfiguration(tableRuleConfigs, new HashMap<>());
    }
}
