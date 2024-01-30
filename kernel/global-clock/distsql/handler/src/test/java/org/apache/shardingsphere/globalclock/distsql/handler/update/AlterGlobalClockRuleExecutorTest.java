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

package org.apache.shardingsphere.globalclock.distsql.handler.update;

import org.apache.shardingsphere.globalclock.api.config.GlobalClockRuleConfiguration;
import org.apache.shardingsphere.globalclock.core.rule.GlobalClockRule;
import org.apache.shardingsphere.globalclock.core.rule.builder.DefaultGlobalClockRuleConfigurationBuilder;
import org.apache.shardingsphere.globalclock.distsql.statement.updatable.AlterGlobalClockRuleStatement;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlterGlobalClockRuleExecutorTest {
    
    @Test
    void assertExecute() {
        AlterGlobalClockRuleExecutor executor = new AlterGlobalClockRuleExecutor();
        AlterGlobalClockRuleStatement sqlStatement = new AlterGlobalClockRuleStatement("TSO", "redis", Boolean.TRUE, PropertiesBuilder.build(new Property("host", "127.0.0.1")));
        GlobalClockRule rule = mock(GlobalClockRule.class);
        when(rule.getConfiguration()).thenReturn(getSQLParserRuleConfiguration());
        executor.setRule(rule);
        GlobalClockRuleConfiguration actual = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        assertThat(actual.getType(), is("TSO"));
        assertThat(actual.getProvider(), is("redis"));
        assertTrue(actual.isEnabled());
        assertThat(actual.getProps().size(), is(1));
    }
    
    private GlobalClockRuleConfiguration getSQLParserRuleConfiguration() {
        return new DefaultGlobalClockRuleConfigurationBuilder().build();
    }
}
