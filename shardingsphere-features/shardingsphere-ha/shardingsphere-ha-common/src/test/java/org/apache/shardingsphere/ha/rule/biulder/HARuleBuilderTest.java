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

import org.apache.shardingsphere.ha.api.config.HARuleConfiguration;
import org.apache.shardingsphere.ha.api.config.rule.HADataSourceRuleConfiguration;
import org.apache.shardingsphere.ha.rule.HARule;
import org.apache.shardingsphere.infra.rule.builder.ShardingSphereRuleBuilder;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class HARuleBuilderTest {
    
    static {
        ShardingSphereServiceLoader.register(ShardingSphereRuleBuilder.class);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test(expected = IllegalArgumentException.class)
    public void assertBuild() {
        HARuleConfiguration ruleConfig = mock(HARuleConfiguration.class);
        HADataSourceRuleConfiguration dataSourceRuleConfig = new HADataSourceRuleConfiguration(
                "name", Collections.singletonList("name"), "loadBalancerName", true, "haTypeName");
        when(ruleConfig.getDataSources()).thenReturn(Collections.singletonList(dataSourceRuleConfig));
        ShardingSphereRuleBuilder builder = OrderedSPIRegistry.getRegisteredServices(Collections.singletonList(ruleConfig), ShardingSphereRuleBuilder.class).get(ruleConfig);
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("primaryDataSourceName", mock(DataSource.class));
        ((HARuleBuilder) builder).setDataSourceMap(dataSourceMap);
        assertThat(builder.build(ruleConfig), instanceOf(HARule.class));
    }
}
