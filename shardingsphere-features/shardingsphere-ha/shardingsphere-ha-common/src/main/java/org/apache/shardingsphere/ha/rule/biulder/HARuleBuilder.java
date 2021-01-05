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

import lombok.Setter;
import org.apache.shardingsphere.ha.api.config.HARuleConfiguration;
import org.apache.shardingsphere.ha.api.config.rule.HADataSourceRuleConfiguration;
import org.apache.shardingsphere.ha.constant.HAOrder;
import org.apache.shardingsphere.ha.rule.HARule;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.rule.builder.ShardingSphereRuleBuilder;
import org.apache.shardingsphere.infra.rule.builder.aware.ResourceAware;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * HA rule builder.
 */
@Setter
public final class HARuleBuilder implements ShardingSphereRuleBuilder<HARule, HARuleConfiguration>, ResourceAware {
    
    private DatabaseType databaseType;
    
    private Map<String, DataSource> dataSourceMap;
    
    private String schemaName;
    
    @Override
    public HARule build(final HARuleConfiguration ruleConfig) {
        Set<String> dataSourceSet = new HashSet<>(128, 1);
        for (HADataSourceRuleConfiguration each : ruleConfig.getDataSources()) {
            dataSourceSet.addAll(each.getDataSourceNames());
        }
        dataSourceMap.entrySet().removeIf(stringDataSourceEntry -> !dataSourceSet.contains(stringDataSourceEntry.getKey()));
        return new HARule(ruleConfig, databaseType, dataSourceMap, schemaName);
    }
    
    @Override
    public int getOrder() {
        return HAOrder.ORDER;
    }
    
    @Override
    public Class<HARuleConfiguration> getTypeClass() {
        return HARuleConfiguration.class;
    }
}
