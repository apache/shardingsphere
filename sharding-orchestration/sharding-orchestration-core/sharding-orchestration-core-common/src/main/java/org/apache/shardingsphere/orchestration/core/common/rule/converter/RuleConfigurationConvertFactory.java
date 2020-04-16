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
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Rule configuration convert factory.
 */
public final class RuleConfigurationConvertFactory {
    
    private static final Map<Class, RuleConfigurationConverter> REGISTRY = new LinkedHashMap<>(4);
    
    static {
        REGISTRY.put(EncryptRuleConfiguration.class, new EncryptRuleConfigurationConverter());
        REGISTRY.put(ShardingRuleConfiguration.class, new ShardingRuleConfigurationConverter());
        REGISTRY.put(ShadowRuleConfiguration.class, new ShadowRuleConfigurationConverter());
        REGISTRY.put(MasterSlaveRuleConfiguration.class, new MasterSlaveRuleConfigurationConverter());
    }
    
    /**
     * Create new instance of rule configuration converter.
     *
     * @param ruleConfiguration rule configuration
     * @return rule configuration converter
     */
    public static RuleConfigurationConverter newInstance(final RuleConfiguration ruleConfiguration) {
        return REGISTRY.get(ruleConfiguration.getClass());
    }
    
    /**
     * Create new instance of rule configuration converter.
     *
     * @param classType class type
     * @return rule configuration converter
     */
    public static RuleConfigurationConverter newInstance(final Class classType) {
        return REGISTRY.get(classType);
    }
    
    /**
     * Get rule configuration converters.
     *
     * @return rule configuration converters
     */
    public static Collection<RuleConfigurationConverter> getRuleConfigurationConverters() {
        return REGISTRY.values();
    }
}

