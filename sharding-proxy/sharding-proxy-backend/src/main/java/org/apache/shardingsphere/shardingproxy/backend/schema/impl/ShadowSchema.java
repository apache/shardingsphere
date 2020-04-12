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

package org.apache.shardingsphere.shardingproxy.backend.schema.impl;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.api.config.shadow.ShadowRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.log.ConfigurationLogger;
import org.apache.shardingsphere.core.rule.ShadowRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.orchestration.core.common.event.ShadowRuleChangedEvent;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * Shadow schema.
 */
@Getter
public final class ShadowSchema extends LogicSchema {
    
    private final ShardingRule shardingRule;
    
    private ShadowRule shadowRule;
    
    public ShadowSchema(final String name, final Map<String, YamlDataSourceParameter> dataSources, final ShadowRuleConfiguration shadowRuleConfiguration) throws SQLException {
        super(name, dataSources, Collections.singletonList(new ShadowRule(shadowRuleConfiguration)));
        shadowRule = new ShadowRule(shadowRuleConfiguration);
        shardingRule = new ShardingRule(new ShardingRuleConfiguration(), getDataSources().keySet());
    }
    
    /**
     * Renew shadow rule.
     *
     * @param shadowRuleChangedEvent shadow configuration changed event
     */
    @Subscribe
    @SneakyThrows
    public synchronized void renew(final ShadowRuleChangedEvent shadowRuleChangedEvent) {
        ConfigurationLogger.log(shadowRuleChangedEvent.getShadowRuleConfiguration());
        shadowRule = new ShadowRule(shadowRuleChangedEvent.getShadowRuleConfiguration());
    }
}
