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

package org.apache.shardingsphere.globallogicaltime.rule;

import lombok.Getter;
import org.apache.shardingsphere.globallogicaltime.GlobalLogicalTimeEngine;
import org.apache.shardingsphere.globallogicaltime.config.GlobalLogicalTimeRuleConfiguration;
import org.apache.shardingsphere.globallogicaltime.config.RedisConnectionOptionConfiguration;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;

/**
 * Global logical time rule.
 */
@Getter
public final class GlobalLogicalTimeRule implements GlobalRule {
    
    private final GlobalLogicalTimeRuleConfiguration configuration;
    
    private final boolean globalLogicalTimeEnabled;
    
    private final RedisConnectionOptionConfiguration redisOption;
    
    private volatile GlobalLogicalTimeEngine globalLogicalTimeEngine;
    
    public GlobalLogicalTimeRule(final GlobalLogicalTimeRuleConfiguration configuration) {
        this.configuration = configuration;
        this.globalLogicalTimeEnabled = configuration.isGlobalLogicalTimeEnabled();
        this.redisOption = configuration.getRedisOption();
        this.globalLogicalTimeEngine = createGlobalLogicalTimeRuleEngine();
    }
    
    private synchronized GlobalLogicalTimeEngine createGlobalLogicalTimeRuleEngine() {
        if (configuration == null) {
            return new GlobalLogicalTimeEngine();
        } else {
            return new GlobalLogicalTimeEngine(configuration);
        }
    }
    
    @Override
    public String getType() {
        return GlobalLogicalTimeRule.class.getSimpleName();
    }
    
    /**
     * Get global logical time engine.
     *
     * @return globalLogicalTimeEngine global logical time engine
     */
    public GlobalLogicalTimeEngine getGlobalLogicalTimeEngine() {
        return globalLogicalTimeEngine;
    }
}
