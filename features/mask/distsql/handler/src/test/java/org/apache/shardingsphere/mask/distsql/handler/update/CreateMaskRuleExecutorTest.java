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

import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.segment.MaskColumnSegment;
import org.apache.shardingsphere.mask.distsql.segment.MaskRuleSegment;
import org.apache.shardingsphere.mask.distsql.statement.CreateMaskRuleStatement;
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

class CreateMaskRuleExecutorTest {
    
    private final CreateMaskRuleExecutor executor = new CreateMaskRuleExecutor();
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateMaskRule() {
        MaskRule rule = mock(MaskRule.class);
        when(rule.getConfiguration()).thenReturn(getCurrentRuleConfig());
        executor.setRule(rule);
        assertThrows(DuplicateRuleException.class, () -> executor.checkBeforeUpdate(createDuplicatedSQLStatement(false, "MD5")));
    }
    
    @Test
    void assertCheckSQLStatementWithInvalidAlgorithm() {
        assertThrows(ServiceProviderNotFoundException.class, () -> executor.checkBeforeUpdate(createSQLStatement(false, "INVALID_TYPE")));
    }
    
    @Test
    void assertCreateMaskRule() {
        MaskRuleConfiguration currentRuleConfig = getCurrentRuleConfig();
        CreateMaskRuleStatement sqlStatement = createSQLStatement(false, "MD5");
        MaskRule rule = mock(MaskRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        executor.checkBeforeUpdate(sqlStatement);
        MaskRuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(sqlStatement);
        assertThat(toBeCreatedRuleConfig.getTables().size(), is(2));
        assertTrue(toBeCreatedRuleConfig.getMaskAlgorithms().containsKey("t_mask_1_user_id_md5"));
        assertTrue(toBeCreatedRuleConfig.getMaskAlgorithms().containsKey("t_order_1_order_id_md5"));
    }
    
    @Test
    void assertCreateMaskRuleWithIfNotExists() {
        MaskRuleConfiguration currentRuleConfig = getCurrentRuleConfig();
        MaskRule rule = mock(MaskRule.class);
        CreateMaskRuleStatement sqlStatement = createSQLStatement(true, "MD5");
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        executor.checkBeforeUpdate(sqlStatement);
        MaskRuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(sqlStatement);
        assertThat(toBeCreatedRuleConfig.getTables().size(), is(2));
        assertTrue(toBeCreatedRuleConfig.getMaskAlgorithms().containsKey("t_mask_1_user_id_md5"));
        assertTrue(toBeCreatedRuleConfig.getMaskAlgorithms().containsKey("t_order_1_order_id_md5"));
    }
    
    private CreateMaskRuleStatement createSQLStatement(final boolean ifNotExists, final String algorithmType) {
        MaskColumnSegment tMaskColumnSegment = new MaskColumnSegment("user_id", new AlgorithmSegment(algorithmType, new Properties()));
        MaskColumnSegment tOrderColumnSegment = new MaskColumnSegment("order_id", new AlgorithmSegment(algorithmType, new Properties()));
        MaskRuleSegment tMaskRuleSegment = new MaskRuleSegment("t_mask_1", Collections.singleton(tMaskColumnSegment));
        MaskRuleSegment tOrderRuleSegment = new MaskRuleSegment("t_order_1", Collections.singleton(tOrderColumnSegment));
        Collection<MaskRuleSegment> rules = new LinkedList<>();
        rules.add(tMaskRuleSegment);
        rules.add(tOrderRuleSegment);
        return new CreateMaskRuleStatement(ifNotExists, rules);
    }
    
    private CreateMaskRuleStatement createDuplicatedSQLStatement(final boolean ifNotExists, final String algorithmType) {
        MaskColumnSegment tMaskColumnSegment = new MaskColumnSegment("user_id", new AlgorithmSegment(algorithmType, new Properties()));
        MaskColumnSegment tOrderColumnSegment = new MaskColumnSegment("order_id", new AlgorithmSegment(algorithmType, new Properties()));
        MaskRuleSegment tMaskRuleSegment = new MaskRuleSegment("t_mask", Collections.singleton(tMaskColumnSegment));
        MaskRuleSegment tOrderRuleSegment = new MaskRuleSegment("t_order", Collections.singleton(tOrderColumnSegment));
        Collection<MaskRuleSegment> rules = new LinkedList<>();
        rules.add(tMaskRuleSegment);
        rules.add(tOrderRuleSegment);
        return new CreateMaskRuleStatement(ifNotExists, rules);
    }
    
    private MaskRuleConfiguration getCurrentRuleConfig() {
        Collection<MaskTableRuleConfiguration> rules = new LinkedList<>();
        rules.add(new MaskTableRuleConfiguration("t_mask", Collections.emptyList()));
        rules.add(new MaskTableRuleConfiguration("t_order", Collections.emptyList()));
        return new MaskRuleConfiguration(rules, new HashMap<>());
    }
}
