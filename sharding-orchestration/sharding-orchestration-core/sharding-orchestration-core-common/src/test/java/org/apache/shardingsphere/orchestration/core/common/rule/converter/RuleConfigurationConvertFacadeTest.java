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
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class RuleConfigurationConvertFacadeTest extends AbstractRuleConfigurationConverterTest {
    
    @Test
    public void testMatch() {
        assertThat(RuleConfigurationConvertFacade.match(ShardingRuleConfiguration.class, SHARDING_RULE_YAML), is(true));
        assertThat(RuleConfigurationConvertFacade.match(EncryptRuleConfiguration.class, ENCRYPT_RULE_YAML), is(true));
        assertThat(RuleConfigurationConvertFacade.match(ShadowRuleConfiguration.class, SHADOW_RULE_YAML), is(true));
        assertThat(RuleConfigurationConvertFacade.match(MasterSlaveRuleConfiguration.class, MASTER_SLAVE_RULE_YAML), is(true));
    }
    
    @Test
    public void testConvert() {
        assertNotNull(RuleConfigurationConvertFacade.convert(ShardingRuleConfiguration.class, SHARDING_RULE_YAML));
        assertNotNull(RuleConfigurationConvertFacade.convert(EncryptRuleConfiguration.class, ENCRYPT_RULE_YAML));
        assertNotNull(RuleConfigurationConvertFacade.convert(ShadowRuleConfiguration.class, SHADOW_RULE_YAML));
        assertNotNull(RuleConfigurationConvertFacade.convert(MasterSlaveRuleConfiguration.class, MASTER_SLAVE_RULE_YAML));
    }
    
    @Test
    public void testMatchAndConvert() {
        assertThat(RuleConfigurationConvertFacade.matchAndConvert(SHARDING_RULE_YAML).isPresent(), is(true));
        assertThat(RuleConfigurationConvertFacade.matchAndConvert(ENCRYPT_RULE_YAML).isPresent(), is(true));
        assertThat(RuleConfigurationConvertFacade.matchAndConvert(SHADOW_RULE_YAML).isPresent(), is(true));
        assertThat(RuleConfigurationConvertFacade.matchAndConvert(MASTER_SLAVE_RULE_YAML).isPresent(), is(true));
    }
    
    @Test
    public void testMarshal() {
        assertNotNull(RuleConfigurationConvertFacade.marshal(RuleConfigurationConvertFacade.convert(ShardingRuleConfiguration.class, SHARDING_RULE_YAML), SHARDING_NAME));
        assertNotNull(RuleConfigurationConvertFacade.marshal(RuleConfigurationConvertFacade.convert(EncryptRuleConfiguration.class, ENCRYPT_RULE_YAML), SHARDING_NAME));
        assertNotNull(RuleConfigurationConvertFacade.marshal(RuleConfigurationConvertFacade.convert(ShadowRuleConfiguration.class, SHADOW_RULE_YAML), SHARDING_NAME));
        assertNotNull(RuleConfigurationConvertFacade.marshal(RuleConfigurationConvertFacade.convert(MasterSlaveRuleConfiguration.class, MASTER_SLAVE_RULE_YAML), SHARDING_NAME));
    }
}
