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

package org.apache.shardingsphere.single.rule.builder;

import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRuleBuilder;
import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class SingleRuleBuilderTest {
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertBuild() {
        DatabaseRuleBuilder builder = OrderedSPILoader.getServices(DatabaseRuleBuilder.class).iterator().next();
        DatabaseRule actual = builder.build(mock(SingleRuleConfiguration.class), "", Collections.emptyMap(), Collections.singleton(mock(ShardingSphereRule.class)), mock(InstanceContext.class));
        assertThat(actual, instanceOf(SingleRule.class));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertBuildWithDefaultDataSource() {
        DatabaseRuleBuilder builder = OrderedSPILoader.getServices(DatabaseRuleBuilder.class).iterator().next();
        DatabaseRule actual = builder.build(
                new SingleRuleConfiguration(Collections.emptyList(), "foo_ds"), "", Collections.emptyMap(), Collections.singleton(mock(ShardingSphereRule.class)), mock(InstanceContext.class));
        assertThat(actual, instanceOf(SingleRule.class));
    }
}
