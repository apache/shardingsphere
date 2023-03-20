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

package org.apache.shardingsphere.globalclock.core.rule;

import lombok.Getter;
import org.apache.shardingsphere.globalclock.api.config.GlobalClockRuleConfiguration;
import org.apache.shardingsphere.globalclock.core.exception.GlobalClockNotEnabledException;
import org.apache.shardingsphere.globalclock.core.provider.GlobalClockProvider;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.transaction.spi.TransactionHook;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Global clock rule.
 */
public final class GlobalClockRule implements GlobalRule {
    
    @Getter
    private final GlobalClockRuleConfiguration configuration;
    
    private final GlobalClockProvider globalClockProvider;
    
    private final boolean enabled;
    
    public GlobalClockRule(final GlobalClockRuleConfiguration ruleConfig, final Map<String, ShardingSphereDatabase> databases) {
        configuration = ruleConfig;
        enabled = ruleConfig.isEnabled();
        globalClockProvider = enabled ? TypedSPILoader.getService(GlobalClockProvider.class, String.join(".", ruleConfig.getType(), ruleConfig.getProvider()),
                null == ruleConfig.getProps() ? new Properties() : ruleConfig.getProps()) : null;
        TypedSPILoader.getService(TransactionHook.class, "GLOBAL_CLOCK", getProps(ruleConfig, databases));
    }
    
    private Properties getProps(final GlobalClockRuleConfiguration ruleConfig, final Map<String, ShardingSphereDatabase> databases) {
        Properties result = new Properties();
        result.setProperty("trunkType", DatabaseTypeEngine.getTrunkDatabaseTypeName(DatabaseTypeEngine.getStorageType(getDataSources(databases))));
        result.setProperty("enabled", String.valueOf(ruleConfig.isEnabled()));
        result.setProperty("type", ruleConfig.getType());
        result.setProperty("provider", ruleConfig.getProvider());
        return result;
    }
    
    private Collection<DataSource> getDataSources(final Map<String, ShardingSphereDatabase> databases) {
        return databases.values().stream().filter(each -> !each.getResourceMetaData().getDataSources().isEmpty())
                .flatMap(each -> each.getResourceMetaData().getDataSources().values().stream()).collect(Collectors.toList());
    }
    
    /**
     * Get current timestamp.
     *
     * @return current timestamp
     */
    public long getCurrentTimestamp() {
        ShardingSpherePreconditions.checkState(enabled, GlobalClockNotEnabledException::new);
        return globalClockProvider.getCurrentTimestamp();
    }
    
    /**
     * Get next timestamp.
     *
     * @return next timestamp
     */
    public long getNextTimestamp() {
        ShardingSpherePreconditions.checkState(enabled, GlobalClockNotEnabledException::new);
        return globalClockProvider.getNextTimestamp();
    }
    
    @Override
    public String getType() {
        return GlobalClockRule.class.getSimpleName();
    }
}
