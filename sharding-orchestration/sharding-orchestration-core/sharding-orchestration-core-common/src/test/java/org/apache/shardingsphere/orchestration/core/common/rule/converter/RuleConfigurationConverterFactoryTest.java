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

package org.apache.shardingsphere.orchestration.core.common.rule.converter;

import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.shadow.ShadowRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptRuleConfiguration;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class RuleConfigurationConverterFactoryTest extends AbstractRuleConfigurationConverterTest {
    
    private EncryptRuleConfiguration encryptRuleConfiguration;
    
    private ShardingRuleConfiguration shardingRuleConfiguration;
    
    private ShadowRuleConfiguration shadowRuleConfiguration;
    
    private MasterSlaveRuleConfiguration masterSlaveRuleConfiguration;
    
    @Before
    public void before() {
        encryptRuleConfiguration = mock(EncryptRuleConfiguration.class);
        shardingRuleConfiguration = mock(ShardingRuleConfiguration.class);
        shadowRuleConfiguration = mock(ShadowRuleConfiguration.class);
        masterSlaveRuleConfiguration = mock(MasterSlaveRuleConfiguration.class);
    }
    
    @Test
    public void testNewInstance() {
        assertNotNull(RuleConfigurationConvertFactory.newInstance(encryptRuleConfiguration));
        assertNotNull(RuleConfigurationConvertFactory.newInstance(EncryptRuleConfiguration.class));
        assertNotNull(RuleConfigurationConvertFactory.newInstance(shardingRuleConfiguration));
        assertNotNull(RuleConfigurationConvertFactory.newInstance(ShardingRuleConfiguration.class));
        assertNotNull(RuleConfigurationConvertFactory.newInstance(shadowRuleConfiguration));
        assertNotNull(RuleConfigurationConvertFactory.newInstance(ShadowRuleConfiguration.class));
        assertNotNull(RuleConfigurationConvertFactory.newInstance(masterSlaveRuleConfiguration));
        assertNotNull(RuleConfigurationConvertFactory.newInstance(MasterSlaveRuleConfiguration.class));
    }
    
    @Test
    public void testGetRuleConfigurationConverters() {
        assertThat(RuleConfigurationConvertFactory.getRuleConfigurationConverters().size(), is(4));
    }
}

