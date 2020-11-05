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

package org.apache.shardingsphere.ha.rule.biulder;

import org.apache.shardingsphere.ha.rule.HARule;
import org.apache.shardingsphere.infra.rule.ShardingSphereRuleBuilder;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.ha.api.config.rule.HADataSourceRuleConfiguration;
import org.apache.shardingsphere.ha.algorithm.config.AlgorithmProvidedHARuleConfiguration;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AlgorithmProvidedHARuleBuilderTest {
    
    static {
        ShardingSphereServiceLoader.register(ShardingSphereRuleBuilder.class);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void assertBuild() {
        AlgorithmProvidedHARuleConfiguration algorithmProvidedRuleConfig = mock(AlgorithmProvidedHARuleConfiguration.class);
        HADataSourceRuleConfiguration ruleConfig = new HADataSourceRuleConfiguration(
                "name", "primaryDataSourceName", Collections.singletonList("name"), "loadBalancerName");
        when(algorithmProvidedRuleConfig.getDataSources()).thenReturn(Collections.singletonList(ruleConfig));
        ShardingSphereRuleBuilder builder = OrderedSPIRegistry.getRegisteredServices(
                Collections.singletonList(algorithmProvidedRuleConfig), ShardingSphereRuleBuilder.class).get(algorithmProvidedRuleConfig);
        assertThat(builder.build(algorithmProvidedRuleConfig, Collections.emptyList()), instanceOf(HARule.class));
    }
}
