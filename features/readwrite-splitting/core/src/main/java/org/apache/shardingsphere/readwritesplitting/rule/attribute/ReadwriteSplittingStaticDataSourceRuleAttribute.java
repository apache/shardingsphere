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

package org.apache.shardingsphere.readwritesplitting.rule.attribute;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;
import org.apache.shardingsphere.infra.rule.attribute.datasource.StaticDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.readwritesplitting.deliver.QualifiedDataSourceDeletedEvent;
import org.apache.shardingsphere.readwritesplitting.exception.logic.ReadwriteSplittingDataSourceRuleNotFoundException;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceGroupRule;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Readwrite-splitting static data source rule attribute.
 */
@RequiredArgsConstructor
public final class ReadwriteSplittingStaticDataSourceRuleAttribute implements StaticDataSourceRuleAttribute {
    
    private final String databaseName;
    
    private final Map<String, ReadwriteSplittingDataSourceGroupRule> dataSourceGroupRules;
    
    private final ComputeNodeInstanceContext computeNodeInstanceContext;
    
    @Override
    public void updateStatus(final QualifiedDataSource qualifiedDataSource, final DataSourceState status) {
        ReadwriteSplittingDataSourceGroupRule dataSourceGroupRule = dataSourceGroupRules.get(qualifiedDataSource.getGroupName());
        ShardingSpherePreconditions.checkNotNull(dataSourceGroupRule,
                () -> new ReadwriteSplittingDataSourceRuleNotFoundException(qualifiedDataSource.getGroupName(), qualifiedDataSource.getDatabaseName()));
        if (DataSourceState.DISABLED == status) {
            dataSourceGroupRule.disableDataSource(qualifiedDataSource.getDataSourceName());
        } else {
            dataSourceGroupRule.enableDataSource(qualifiedDataSource.getDataSourceName());
        }
    }
    
    @Override
    public void cleanStorageNodeDataSource(final String groupName) {
        ShardingSpherePreconditions.checkContainsKey(dataSourceGroupRules, groupName, () -> new ReadwriteSplittingDataSourceRuleNotFoundException(groupName, databaseName));
        deleteStorageNodeDataSources(dataSourceGroupRules.get(groupName));
    }
    
    @Override
    public void cleanStorageNodeDataSources() {
        for (Entry<String, ReadwriteSplittingDataSourceGroupRule> entry : dataSourceGroupRules.entrySet()) {
            deleteStorageNodeDataSources(entry.getValue());
        }
    }
    
    private void deleteStorageNodeDataSources(final ReadwriteSplittingDataSourceGroupRule rule) {
        rule.getReadwriteSplittingGroup().getReadDataSources().forEach(each -> computeNodeInstanceContext.getEventBusContext()
                .post(new QualifiedDataSourceDeletedEvent(new QualifiedDataSource(databaseName, rule.getName(), each))));
    }
}
