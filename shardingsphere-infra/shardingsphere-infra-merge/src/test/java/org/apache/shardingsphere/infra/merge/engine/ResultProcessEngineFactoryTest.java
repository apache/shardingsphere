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

package org.apache.shardingsphere.infra.merge.engine;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.apache.shardingsphere.infra.merge.fixture.ResultProcessEngineFixture;
import org.apache.shardingsphere.infra.merge.fixture.rule.ResultProcessRuleFixture;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Test;

import com.google.common.collect.Lists;

public class ResultProcessEngineFactoryTest {
    
    @Test
    public void assertGetInstances() {
        ResultProcessRuleFixture ruleFixture = mock(ResultProcessRuleFixture.class);
        Map<ShardingSphereRule, ResultProcessEngine> instances = ResultProcessEngineFactory.getInstances(Lists.newArrayList(ruleFixture));
        assertThat(instances, IsMapContaining.hasKey(ruleFixture));
        assertThat(instances, IsMapContaining.hasValue(instanceOf(ResultProcessEngineFixture.class)));
    }
}
