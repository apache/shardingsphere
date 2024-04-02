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

package org.apache.shardingsphere.mask.rule.changed;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterNamedRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskAlgorithmChangedProcessorTest {
    
    @Test
    void assertSwapRuleItemConfiguration() {
        assertThat(new MaskAlgorithmChangedProcessor().swapRuleItemConfiguration(mock(AlterNamedRuleItemEvent.class), "type: TEST").getType(), is("TEST"));
    }
    
    @Test
    void assertFindRuleConfigurationWhenRuleDoesNotExist() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData().findSingleRule(MaskRule.class)).thenReturn(Optional.empty());
        assertTrue(new MaskAlgorithmChangedProcessor().findRuleConfiguration(database).getMaskAlgorithms().isEmpty());
    }
    
    @Test
    void assertFindRuleConfigurationWhenMaskAlgorithmDoesNotExist() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData().findSingleRule(MaskRule.class)).thenReturn(Optional.of(new MaskRule(new MaskRuleConfiguration(Collections.emptyList(), Collections.emptyMap()))));
        assertTrue(new MaskAlgorithmChangedProcessor().findRuleConfiguration(database).getMaskAlgorithms().isEmpty());
    }
    
    @Test
    void assertFindRuleConfigurationWhenRuleExists() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        MaskRule maskRule = mock(MaskRule.class, RETURNS_DEEP_STUBS);
        when(maskRule.getConfiguration().getMaskAlgorithms()).thenReturn(Collections.singletonMap("foo", new AlgorithmConfiguration("FOO", new Properties())));
        when(database.getRuleMetaData().findSingleRule(MaskRule.class)).thenReturn(Optional.of(maskRule));
        assertFalse(new MaskAlgorithmChangedProcessor().findRuleConfiguration(database).getMaskAlgorithms().isEmpty());
    }
    
    @Test
    void assertDropRuleItemConfiguration() {
        MaskRuleConfiguration currentRuleConfig = new MaskRuleConfiguration(Collections.emptyList(), new HashMap<>(Collections.singletonMap("type: TEST", mock(AlgorithmConfiguration.class))));
        new MaskAlgorithmChangedProcessor().dropRuleItemConfiguration(new DropNamedRuleItemEvent("foo_db", "type: TEST", ""), currentRuleConfig);
        assertTrue(currentRuleConfig.getMaskAlgorithms().isEmpty());
    }
    
    @Test
    void assertGetType() {
        assertThat(new MaskAlgorithmChangedProcessor().getType(), is("mask.mask_algorithms"));
    }
}
