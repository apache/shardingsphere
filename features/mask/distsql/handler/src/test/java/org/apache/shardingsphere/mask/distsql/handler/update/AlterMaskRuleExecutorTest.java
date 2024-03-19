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
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.segment.MaskColumnSegment;
import org.apache.shardingsphere.mask.distsql.segment.MaskRuleSegment;
import org.apache.shardingsphere.mask.distsql.statement.AlterMaskRuleStatement;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlterMaskRuleExecutorTest {
    
    private final AlterMaskRuleExecutor executor = new AlterMaskRuleExecutor();
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    void assertCheckBeforeUpdateWithoutToBeAlteredRules() {
        MaskRule rule = mock(MaskRule.class);
        when(rule.getConfiguration()).thenReturn(new MaskRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
        executor.setRule(rule);
        assertThrows(MissingRequiredRuleException.class, () -> executor.checkBeforeUpdate(createSQLStatement("MD5")));
    }
    
    @Test
    void assertCheckBeforeUpdateWithoutToBeAlteredAlgorithm() {
        MaskRule rule = mock(MaskRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfig());
        executor.setRule(rule);
        assertThrows(MissingRequiredRuleException.class, () -> executor.checkBeforeUpdate(createSQLStatement("INVALID_TYPE")));
    }
    
    @Test
    void assertCheckBeforeUpdateWithIncompleteDataType() {
        MaskColumnSegment columnSegment = new MaskColumnSegment("user_id", new AlgorithmSegment("test", new Properties()));
        MaskRuleSegment ruleSegment = new MaskRuleSegment("t_mask", Collections.singleton(columnSegment));
        AlterMaskRuleStatement statement = new AlterMaskRuleStatement(Collections.singleton(ruleSegment));
        MaskRule rule = mock(MaskRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfig());
        executor.setRule(rule);
        assertThrows(MissingRequiredRuleException.class, () -> executor.checkBeforeUpdate(statement));
    }
    
    @Test
    void assertUpdate() {
        MaskRuleConfiguration currentRuleConfig = createCurrentRuleConfig();
        MaskColumnSegment columnSegment = new MaskColumnSegment("order_id",
                new AlgorithmSegment("MD5", new Properties()));
        MaskRuleSegment ruleSegment = new MaskRuleSegment("t_order", Collections.singleton(columnSegment));
        AlterMaskRuleStatement sqlStatement = new AlterMaskRuleStatement(Collections.singleton(ruleSegment));
        MaskRule rule = mock(MaskRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        MaskRuleConfiguration toBeAlteredRuleConfig = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        assertThat(toBeAlteredRuleConfig.getMaskAlgorithms().size(), is(1));
        assertTrue(toBeAlteredRuleConfig.getMaskAlgorithms().containsKey("t_order_order_id_md5"));
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
