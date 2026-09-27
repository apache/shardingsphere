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

package org.apache.shardingsphere.sqlfederation.rule;

import lombok.Getter;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.constant.SQLFederationOrder;
import org.apache.shardingsphere.sqlfederation.exception.SQLFederationProviderDuplicatedException;
import org.apache.shardingsphere.sqlfederation.exception.SQLFederationProviderNotFoundException;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * SQL federation rule.
 */
@Getter
public final class SQLFederationRule implements GlobalRule {
    
    private final SQLFederationRuleConfiguration configuration;
    
    private final SQLFederationProvider provider;
    
    public SQLFederationRule(final SQLFederationRuleConfiguration ruleConfig, final Collection<ShardingSphereDatabase> databases) {
        configuration = ruleConfig;
        provider = ruleConfig.isSqlFederationEnabled() ? createProvider(databases) : null;
    }
    
    private SQLFederationProvider createProvider(final Collection<ShardingSphereDatabase> databases) {
        String providerType = null == configuration.getProviderType() ? "CALCITE" : configuration.getProviderType();
        Set<String> types = new HashSet<>();
        for (SQLFederationProvider each : ShardingSphereServiceLoader.getServiceInstances(SQLFederationProvider.class)) {
            if (!types.add(each.getType().toUpperCase(Locale.ROOT))) {
                throw new SQLFederationProviderDuplicatedException(each.getType());
            }
        }
        SQLFederationProvider result = TypedSPILoader.findService(SQLFederationProvider.class, providerType)
                .orElseThrow(() -> new SQLFederationProviderNotFoundException(providerType));
        result.initialize(configuration, databases);
        return result;
    }
    
    @Override
    public void refresh(final Collection<ShardingSphereDatabase> databases, final GlobalRuleChangedType changedType) {
        if (null != provider) {
            provider.refresh(databases);
        }
    }
    
    @Override
    public int getOrder() {
        return SQLFederationOrder.ORDER;
    }
}
