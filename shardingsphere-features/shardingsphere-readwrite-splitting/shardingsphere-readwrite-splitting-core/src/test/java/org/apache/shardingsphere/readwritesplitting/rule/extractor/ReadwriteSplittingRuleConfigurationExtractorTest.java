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

package org.apache.shardingsphere.readwritesplitting.rule.extractor;

import org.apache.shardingsphere.infra.rule.extractor.RuleConfigurationExtractor;
import org.apache.shardingsphere.infra.rule.extractor.RuleConfigurationExtractorFactory;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ReadwriteSplittingRuleConfigurationExtractorTest {
    
    static {
        ShardingSphereServiceLoader.register(RuleConfigurationExtractor.class);
    }
    
    @Test
    public void assertExtractOneDataSource() {
        ReadwriteSplittingRuleConfiguration ruleConfig = mock(ReadwriteSplittingRuleConfiguration.class);
        ReadwriteSplittingDataSourceRuleConfiguration ds0 = mock(ReadwriteSplittingDataSourceRuleConfiguration.class);
        when(ds0.getAutoAwareDataSourceName()).thenReturn("ds0");
        when(ruleConfig.getDataSources()).thenReturn(Collections.singletonList(ds0));
        RuleConfigurationExtractor extractor = RuleConfigurationExtractorFactory.newInstance(ruleConfig);
        assertNotNull(extractor);
        assertThat(extractor, instanceOf(ReadwriteSplittingRuleConfigurationExtractor.class));
        assertThat(extractor.extractLogicDataSources(ruleConfig).iterator().next(), is("ds0"));
    }
    
    @Test
    public void assertExtractMultiDataSources() {
        ReadwriteSplittingRuleConfiguration ruleConfig = mock(ReadwriteSplittingRuleConfiguration.class);
        ReadwriteSplittingDataSourceRuleConfiguration ds0 = mock(ReadwriteSplittingDataSourceRuleConfiguration.class);
        when(ds0.getName()).thenReturn("ds0");
        ReadwriteSplittingDataSourceRuleConfiguration ds1 = mock(ReadwriteSplittingDataSourceRuleConfiguration.class);
        when(ds1.getName()).thenReturn("ds1");
        when(ruleConfig.getDataSources()).thenReturn(Arrays.asList(ds0, ds1));
        RuleConfigurationExtractor extractor = OrderedSPIRegistry.getRegisteredServices(Collections.singletonList(ruleConfig), RuleConfigurationExtractor.class).get(ruleConfig);
        assertNotNull(extractor);
        assertThat(extractor, instanceOf(ReadwriteSplittingRuleConfigurationExtractor.class));
        assertThat(extractor.extractLogicDataSources(ruleConfig).size(), is(2));
        assertTrue(extractor.extractLogicDataSources(ruleConfig).containsAll(Arrays.asList("ds0", "ds1")));
    }
}
