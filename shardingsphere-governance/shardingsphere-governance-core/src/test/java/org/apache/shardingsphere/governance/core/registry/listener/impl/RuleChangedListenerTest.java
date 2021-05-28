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

package org.apache.shardingsphere.governance.core.registry.listener.impl;

import org.apache.shardingsphere.governance.core.registry.listener.event.GovernanceEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.class)
public final class RuleChangedListenerTest extends GovernanceListenerTest {
    
    private static final String RULE_FILE = "yaml/rule.yaml";
    
    private RuleChangedListener ruleChangedListener;
    
    @Before
    public void setUp() {
        ruleChangedListener = new RuleChangedListener(getRegistryCenterRepository(), Arrays.asList("foo_db", "bar_db"));
    }
    
    @Test
    public void assertCreateEventWithoutSchema() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata", readYAML(RULE_FILE), Type.UPDATED);
        Optional<GovernanceEvent> actual = ruleChangedListener.createEvent(dataChangedEvent);
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertCreateEventWithEmptyValue() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/foo_db/rules", "", Type.UPDATED);
        Optional<GovernanceEvent> actual = ruleChangedListener.createEvent(dataChangedEvent);
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertCreateEventWithRule() {
        // TODO use RuleConfigurationFixture instead of ShardingRuleConfiguration for test case
//        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/foo_db/rules", readYAML(RULE_FILE), Type.UPDATED);
//        Optional<GovernanceEvent> actual = ruleChangedListener.createEvent(dataChangedEvent);
//        assertTrue(actual.isPresent());
//        assertThat(((RuleConfigurationsChangedEvent) actual.get()).getSchemaName(), is("foo_db"));
//        Collection<RuleConfiguration> ruleConfigs = ((RuleConfigurationsChangedEvent) actual.get()).getRuleConfigurations();
//        assertThat(ruleConfigs.size(), is(1));
//        assertThat(((ShardingRuleConfiguration) ruleConfigs.iterator().next()).getTables().size(), is(1));
    }
}
