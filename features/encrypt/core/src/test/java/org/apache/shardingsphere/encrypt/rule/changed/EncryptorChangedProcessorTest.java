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

package org.apache.shardingsphere.encrypt.rule.changed;

import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Properties;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptorChangedProcessorTest {
    
    @SuppressWarnings("unchecked")
    private final RuleItemConfigurationChangedProcessor<EncryptRuleConfiguration, AlgorithmConfiguration> processor = TypedSPILoader.getService(
            RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("encrypt", "encryptors"));
    
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    @Test
    void assertFindRuleConfigurationWhenAbsent() {
        assertThat(processor.findRuleConfiguration(mockDatabase()), deepEqual(new EncryptRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>())));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        return result;
    }
    
    @Test
    void assertChangeRuleItemConfiguration() {
        EncryptRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        AlgorithmConfiguration toBeChangedItemConfig = new AlgorithmConfiguration("BAR_FIXTURE", new Properties());
        processor.changeRuleItemConfiguration("bar_algo", currentRuleConfig, toBeChangedItemConfig);
        assertThat(currentRuleConfig.getEncryptors().size(), is(2));
        assertThat(currentRuleConfig.getEncryptors().get("foo_algo").getType(), is("FOO_FIXTURE"));
        assertThat(currentRuleConfig.getEncryptors().get("bar_algo").getType(), is("BAR_FIXTURE"));
    }
    
    @Test
    void assertDropRuleItemConfiguration() {
        EncryptRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        processor.dropRuleItemConfiguration("foo_algo", currentRuleConfig);
        assertTrue(currentRuleConfig.getEncryptors().isEmpty());
    }
    
    private EncryptRuleConfiguration createCurrentRuleConfiguration() {
        return new EncryptRuleConfiguration(Collections.emptyList(), new HashMap<>(Collections.singletonMap("foo_algo", new AlgorithmConfiguration("FOO_FIXTURE", new Properties()))));
    }
}
