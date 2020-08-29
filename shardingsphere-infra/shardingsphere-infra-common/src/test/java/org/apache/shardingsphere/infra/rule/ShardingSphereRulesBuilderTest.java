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

package org.apache.shardingsphere.infra.rule;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.rule.fixture.TestRuleConfiguration;
import org.apache.shardingsphere.infra.rule.fixture.TestShardingSphereRuleBuilder;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingSphereRulesBuilderTest {
    
    @Test
    public void assertBuild() {
        RuleConfiguration ruleConfiguration = new TestRuleConfiguration();
        Collection<ShardingSphereRule> shardingSphereRules = ShardingSphereRulesBuilder.build(Collections.singletonList(ruleConfiguration), Collections.singletonList(""));
        assertThat(shardingSphereRules, is(Collections.singletonList(TestShardingSphereRuleBuilder.getShardingSphereRule())));
    }
}
