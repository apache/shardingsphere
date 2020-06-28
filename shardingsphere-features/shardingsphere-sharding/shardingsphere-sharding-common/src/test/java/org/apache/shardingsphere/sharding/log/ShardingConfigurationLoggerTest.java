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

package org.apache.shardingsphere.sharding.log;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.log.ConfigurationLogger;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingConfigurationLoggerTest {
    
    @Mock
    private Logger log;
    
    @Before
    @SneakyThrows
    public void setLog() {
        setFinalStaticField(ConfigurationLogger.class.getDeclaredField("log"), log);
    }
    
    @SneakyThrows
    private void setFinalStaticField(final Field field, final Object newValue) {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
    
    @Test
    public void assertLogShardingRuleConfiguration() {
        String yaml = "rules:\n"
                + "- !SHARDING\n"
                + "  tables:\n"
                + "    user:\n"
                + "      actualDataNodes: ds_${0}.user_${0..1}\n"
                + "      logicTable: user\n"
                + "      tableStrategy:\n"
                + "        none: ''\n";
        assertLogInfo(yaml);
        ConfigurationLogger.log(Collections.singletonList(getShardingRuleConfiguration()));
    }
    
    private ShardingRuleConfiguration getShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfiguration = new ShardingTableRuleConfiguration("user", "ds_${0}.user_${0..1}");
        shardingTableRuleConfiguration.setTableShardingStrategy(new NoneShardingStrategyConfiguration());
        result.getTables().add(shardingTableRuleConfiguration);
        return result;
    }
    
    private void assertLogInfo(final String logContent) {
        doAnswer(invocationOnMock -> {
            assertThat(invocationOnMock.getArgument(1).toString(), is("Rule configurations: "));
            assertThat(invocationOnMock.getArgument(2).toString(), is(logContent));
            return null;
        }).when(log).info(anyString(), anyString(), anyString());
    }
}
