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

package org.apache.shardingsphere.globalclock.rule;

import lombok.Getter;
import org.apache.shardingsphere.globalclock.config.GlobalClockRuleConfiguration;
import org.apache.shardingsphere.globalclock.provider.GlobalClockProvider;
import org.apache.shardingsphere.globalclock.rule.constant.GlobalClockOrder;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Optional;

/**
 * Global clock rule.
 */
@Getter
public final class GlobalClockRule implements GlobalRule {
    
    private final GlobalClockRuleConfiguration configuration;
    
    private final GlobalClockProvider globalClockProvider;
    
    public GlobalClockRule(final GlobalClockRuleConfiguration ruleConfig) {
        configuration = ruleConfig;
        globalClockProvider = ruleConfig.isEnabled()
                ? TypedSPILoader.getService(GlobalClockProvider.class, String.join(".", ruleConfig.getType(), ruleConfig.getProvider()), configuration.getProps())
                : null;
    }
    
    /**
     * Get global clock provider.
     *
     * @return global clock provider
     */
    public Optional<GlobalClockProvider> getGlobalClockProvider() {
        return Optional.ofNullable(globalClockProvider);
    }
    
    @Override
    public int getOrder() {
        return GlobalClockOrder.ORDER;
    }
}
