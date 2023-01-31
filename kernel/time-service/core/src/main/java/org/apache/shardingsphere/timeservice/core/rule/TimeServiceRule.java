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

package org.apache.shardingsphere.timeservice.core.rule;

import lombok.Getter;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.timeservice.api.config.TimeServiceRuleConfiguration;
import org.apache.shardingsphere.timeservice.spi.ShardingSphereTimeService;

import java.util.Date;
import java.util.Properties;

/**
 * Time service rule.
 */
public final class TimeServiceRule implements GlobalRule {
    
    @Getter
    private final TimeServiceRuleConfiguration configuration;
    
    private final ShardingSphereTimeService timeService;
    
    public TimeServiceRule(final TimeServiceRuleConfiguration ruleConfig) {
        configuration = ruleConfig;
        timeService = TypedSPILoader.getService(ShardingSphereTimeService.class, ruleConfig.getType(), null == ruleConfig.getProps() ? new Properties() : ruleConfig.getProps());
    }
    
    /**
     * Get datetime.
     *
     * @return datetime
     */
    public Date getDatetime() {
        return timeService.getDatetime();
    }
    
    @Override
    public String getType() {
        return TimeServiceRule.class.getSimpleName();
    }
}
