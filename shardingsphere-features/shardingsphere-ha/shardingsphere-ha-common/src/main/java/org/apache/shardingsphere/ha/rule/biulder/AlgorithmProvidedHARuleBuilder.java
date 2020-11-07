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

package org.apache.shardingsphere.ha.rule.biulder;

import org.apache.shardingsphere.ha.rule.HARule;
import org.apache.shardingsphere.infra.rule.builder.ShardingSphereRuleBuilder;
import org.apache.shardingsphere.ha.algorithm.config.AlgorithmProvidedHARuleConfiguration;
import org.apache.shardingsphere.ha.constant.HAOrder;

import java.util.Collection;

/**
 * Algorithm provided HA rule builder.
 */
public final class AlgorithmProvidedHARuleBuilder implements ShardingSphereRuleBuilder<HARule, AlgorithmProvidedHARuleConfiguration> {
    
    @Override
    public HARule build(final AlgorithmProvidedHARuleConfiguration ruleConfig, final Collection<String> dataSourceNames) {
        return new HARule(ruleConfig);
    }
    
    @Override
    public int getOrder() {
        return HAOrder.ORDER + 1;
    }
    
    @Override
    public Class<AlgorithmProvidedHARuleConfiguration> getTypeClass() {
        return AlgorithmProvidedHARuleConfiguration.class;
    }
}
