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

package org.apache.shardingsphere.single.rule.builder;

import org.apache.shardingsphere.infra.rule.builder.database.DefaultDatabaseRuleConfigurationBuilder;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.constant.SingleOrder;

/**
 * Default single rule configuration builder.
 */
public final class DefaultSingleRuleConfigurationBuilder implements DefaultDatabaseRuleConfigurationBuilder<SingleRuleConfiguration, SingleRuleBuilder> {
    
    @Override
    public SingleRuleConfiguration build() {
        return new SingleRuleConfiguration();
    }
    
    @Override
    public int getOrder() {
        return SingleOrder.ORDER;
    }
    
    @Override
    public Class<SingleRuleBuilder> getTypeClass() {
        return SingleRuleBuilder.class;
    }
}
