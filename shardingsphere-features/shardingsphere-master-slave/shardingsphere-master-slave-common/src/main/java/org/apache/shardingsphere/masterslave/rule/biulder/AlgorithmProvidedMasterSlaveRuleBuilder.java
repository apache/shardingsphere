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

package org.apache.shardingsphere.masterslave.rule.biulder;

import org.apache.shardingsphere.infra.rule.ShardingSphereRuleBuilder;
import org.apache.shardingsphere.masterslave.constant.MasterSlaveOrder;
import org.apache.shardingsphere.masterslave.rule.MasterSlaveRule;
import org.apache.shardingsphere.masterslave.algorithm.config.AlgorithmProvidedMasterSlaveRuleConfiguration;

import java.util.Collection;

/**
 * Algorithm provided master-slave rule builder.
 */
public final class AlgorithmProvidedMasterSlaveRuleBuilder implements ShardingSphereRuleBuilder<MasterSlaveRule, AlgorithmProvidedMasterSlaveRuleConfiguration> {
    
    @Override
    public MasterSlaveRule build(final AlgorithmProvidedMasterSlaveRuleConfiguration ruleConfiguration, final Collection<String> dataSourceNames) {
        return new MasterSlaveRule(ruleConfiguration);
    }
    
    @Override
    public int getOrder() {
        return MasterSlaveOrder.ORDER + 1;
    }
    
    @Override
    public Class<AlgorithmProvidedMasterSlaveRuleConfiguration> getTypeClass() {
        return AlgorithmProvidedMasterSlaveRuleConfiguration.class;
    }
}
