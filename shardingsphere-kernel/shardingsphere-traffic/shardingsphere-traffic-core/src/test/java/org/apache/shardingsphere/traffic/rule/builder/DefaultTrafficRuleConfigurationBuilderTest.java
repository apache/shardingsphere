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

package org.apache.shardingsphere.traffic.rule.builder;

import org.apache.shardingsphere.traffic.config.TrafficRuleConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class DefaultTrafficRuleConfigurationBuilderTest {
    
    private final DefaultTrafficRuleConfigurationBuilder builder = new DefaultTrafficRuleConfigurationBuilder();
    
    @Test
    public void assertBuild() {
        TrafficRuleConfiguration configuration = builder.build();
        assertNotNull(configuration);
        assertThat(configuration.getTrafficStrategies().size(), is(0));
        assertThat(configuration.getTrafficAlgorithms().size(), is(0));
        assertThat(configuration.getLoadBalancers().size(), is(0));
    }
    
    @Test
    public void assertGetOrder() {
        assertThat(builder.getOrder(), is(800));
    }
    
    @Test
    public void assertGetTypeClass() {
        assertThat(builder.getTypeClass(), equalTo(TrafficRuleBuilder.class));
    }
}
