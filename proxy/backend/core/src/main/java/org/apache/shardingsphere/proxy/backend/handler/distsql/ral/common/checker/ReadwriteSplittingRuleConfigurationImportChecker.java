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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker;

import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Readwrite-splitting rule configuration import checker.
 */
public final class ReadwriteSplittingRuleConfigurationImportChecker {
    
    /**
     * Check readwrite-splitting rule configuration.
     *
     * @param database          database
     * @param currentRuleConfig current rule configuration
     */
    public void check(final ShardingSphereDatabase database, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        if (null == database || null == currentRuleConfig) {
            return;
        }
        String databaseName = database.getName();
        checkDataSources(databaseName, database, currentRuleConfig);
        checkLoadBalancers(currentRuleConfig);
    }
    
    private void checkDataSources(final String databaseName, final ShardingSphereDatabase database, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        Collection<String> requiredDataSources = new LinkedHashSet<>();
        for (ReadwriteSplittingDataSourceRuleConfiguration each : currentRuleConfig.getDataSources()) {
            if (null != each.getWriteDataSourceName()) {
                requiredDataSources.add(each.getWriteDataSourceName());
            }
            if (!each.getReadDataSourceNames().isEmpty()) {
                requiredDataSources.addAll(each.getReadDataSourceNames());
            }
        }
        Collection<String> notExistedDataSources = database.getResourceMetaData().getNotExistedDataSources(requiredDataSources);
        ShardingSpherePreconditions.checkState(notExistedDataSources.isEmpty(), () -> new MissingRequiredStorageUnitsException(databaseName, notExistedDataSources));
    }
    
    private void checkLoadBalancers(final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getLoadBalancers().values().forEach(each -> TypedSPILoader.checkService(ReadQueryLoadBalanceAlgorithm.class, each.getType(), each.getProps()));
    }
}
