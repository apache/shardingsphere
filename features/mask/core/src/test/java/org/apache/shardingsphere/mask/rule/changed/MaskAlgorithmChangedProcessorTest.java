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
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mode.event.dispatch.rule.alter.AlterNamedRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.mode.spi.RuleItemConfigurationChangedProcessor;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Properties;

import static org.apache.shardingsphere.test.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskAlgorithmChangedProcessorTest {
    
    @SuppressWarnings("unchecked")
    private final RuleItemConfigurationChangedProcessor<MaskRuleConfiguration, AlgorithmConfiguration> processor = TypedSPILoader.getService(
            RuleItemConfigurationChangedProcessor.class, "mask.mask_algorithms");
    
    @Test
    void assertFindRuleConfigurationWhenAbsent() {
        assertThat(processor.findRuleConfiguration(mockDatabase()), deepEqual(new MaskRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>())));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        return result;
    }
    
    @Test
    void assertChangeRuleItemConfiguration() {
        AlterNamedRuleItemEvent event = mock(AlterNamedRuleItemEvent.class);
        when(event.getItemName()).thenReturn("bar_algo");
        MaskRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        AlgorithmConfiguration toBeChangedItemConfig = new AlgorithmConfiguration("BAR_FIXTURE", new Properties());
        processor.changeRuleItemConfiguration(event, currentRuleConfig, toBeChangedItemConfig);
        assertThat(currentRuleConfig.getMaskAlgorithms().size(), is(2));
        assertThat(currentRuleConfig.getMaskAlgorithms().get("foo_algo").getType(), is("FOO_FIXTURE"));
        assertThat(currentRuleConfig.getMaskAlgorithms().get("bar_algo").getType(), is("BAR_FIXTURE"));
    }
    
    @Test
    void assertDropRuleItemConfiguration() {
        DropNamedRuleItemEvent event = mock(DropNamedRuleItemEvent.class);
        when(event.getItemName()).thenReturn("foo_algo");
        MaskRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        processor.dropRuleItemConfiguration(event, currentRuleConfig);
        assertTrue(currentRuleConfig.getMaskAlgorithms().isEmpty());
    }
    
    private MaskRuleConfiguration createCurrentRuleConfiguration() {
        return new MaskRuleConfiguration(Collections.emptyList(), new HashMap<>(Collections.singletonMap("foo_algo", new AlgorithmConfiguration("FOO_FIXTURE", new Properties()))));
    }
}
