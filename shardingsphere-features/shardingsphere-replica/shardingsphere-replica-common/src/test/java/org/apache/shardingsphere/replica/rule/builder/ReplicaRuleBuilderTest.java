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

package org.apache.shardingsphere.replica.rule.builder;

import org.apache.shardingsphere.infra.rule.ShardingSphereRuleBuilder;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.order.OrderedSPIRegistry;
import org.apache.shardingsphere.replica.api.config.ReplicaRuleConfiguration;
import org.apache.shardingsphere.replica.rule.ReplicaRule;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ReplicaRuleBuilderTest {
    
    static {
        ShardingSphereServiceLoader.register(ShardingSphereRuleBuilder.class);
    }
    
    @Test
    public void assertBuild() {
        ReplicaRuleConfiguration ruleConfig = mock(ReplicaRuleConfiguration.class);
        ShardingSphereRuleBuilder builder = OrderedSPIRegistry.getRegisteredServices(Collections.singletonList(ruleConfig), ShardingSphereRuleBuilder.class).get(ruleConfig);
        assertThat(builder.build("sharding_db", ruleConfig, Collections.emptyList()), instanceOf(ReplicaRule.class));
    }
}
