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

package org.apache.shardingsphere.transaction.rule.builder;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.builder.level.KernelRuleBuilder;
import org.apache.shardingsphere.infra.rule.builder.scope.GlobalRuleBuilder;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.constant.TransactionOrder;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import java.util.Map;

/**
 * Authority rule builder.
 */
public final class TransactionRuleBuilder implements KernelRuleBuilder, GlobalRuleBuilder<TransactionRuleConfiguration> {
    
    @Override
    public TransactionRule build(final TransactionRuleConfiguration ruleConfig, final Map<String, ShardingSphereMetaData> mataDataMap) {
        return new TransactionRule(ruleConfig);
    }
    
    @Override
    public int getOrder() {
        return TransactionOrder.ORDER;
    }
    
    @Override
    public Class<TransactionRuleConfiguration> getTypeClass() {
        return TransactionRuleConfiguration.class;
    }
}
