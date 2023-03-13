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

package org.apache.shardingsphere.globalclock.core.rule.builder;

import org.apache.shardingsphere.globalclock.api.config.GlobalClockRuleConfiguration;
import org.apache.shardingsphere.globalclock.core.rule.constant.GlobalClockOrder;
import org.apache.shardingsphere.infra.rule.builder.global.DefaultGlobalRuleConfigurationBuilder;

import java.util.Properties;

/**
 * Default global clock rule configuration builder.
 */
public final class DefaultGlobalClockRuleConfigurationBuilder implements DefaultGlobalRuleConfigurationBuilder<GlobalClockRuleConfiguration, GlobalClockRuleBuilder> {
    
    @Override
    public GlobalClockRuleConfiguration build() {
        return new GlobalClockRuleConfiguration("TSO", "local", false, new Properties());
    }
    
    @Override
    public int getOrder() {
        return GlobalClockOrder.ORDER;
    }
    
    @Override
    public Class<GlobalClockRuleBuilder> getTypeClass() {
        return GlobalClockRuleBuilder.class;
    }
}
