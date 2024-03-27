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

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.rule.event.DataSourceStatusChangedEvent;
import org.apache.shardingsphere.infra.rule.attribute.datasource.StaticDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.mode.event.storage.StorageNodeDataSourceChangedEvent;
import org.apache.shardingsphere.mode.event.storage.StorageNodeDataSourceDeletedEvent;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceRule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Readwrite-splitting static data source rule attribute.
 */
@RequiredArgsConstructor
public final class ReadwriteSplittingStaticDataSourceRuleAttribute implements StaticDataSourceRuleAttribute {
    
    private final String databaseName;
    
    private final Map<String, ReadwriteSplittingDataSourceRule> dataSourceRules;
    
    private final InstanceContext instanceContext;
    
    @Override
    public Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> result = new HashMap<>();
        for (Entry<String, ReadwriteSplittingDataSourceRule> entry : dataSourceRules.entrySet()) {
            result.put(entry.getValue().getName(), entry.getValue().getReadwriteSplittingGroup().getAllDataSources());
        }
        return result;
    }
    
    @Override
    public void updateStatus(final DataSourceStatusChangedEvent event) {
        StorageNodeDataSourceChangedEvent dataSourceEvent = (StorageNodeDataSourceChangedEvent) event;
        QualifiedDatabase qualifiedDatabase = dataSourceEvent.getQualifiedDatabase();
        ReadwriteSplittingDataSourceRule dataSourceRule = dataSourceRules.get(qualifiedDatabase.getGroupName());
        Preconditions.checkNotNull(dataSourceRule, "Can not find readwrite-splitting data source rule in database `%s`", qualifiedDatabase.getDatabaseName());
        if (DataSourceState.DISABLED == dataSourceEvent.getDataSource().getStatus()) {
            dataSourceRule.disableDataSource(dataSourceEvent.getQualifiedDatabase().getDataSourceName());
        } else {
            dataSourceRule.enableDataSource(dataSourceEvent.getQualifiedDatabase().getDataSourceName());
        }
    }
    
    @Override
    public void cleanStorageNodeDataSource(final String groupName) {
        Preconditions.checkNotNull(dataSourceRules.get(groupName), String.format("`%s` group name not exist in database `%s`", groupName, databaseName));
        deleteStorageNodeDataSources(dataSourceRules.get(groupName));
    }
    
    private void deleteStorageNodeDataSources(final ReadwriteSplittingDataSourceRule rule) {
        rule.getReadwriteSplittingGroup().getReadDataSources()
                .forEach(each -> instanceContext.getEventBusContext().post(new StorageNodeDataSourceDeletedEvent(new QualifiedDatabase(databaseName, rule.getName(), each))));
    }
    
    @Override
    public void cleanStorageNodeDataSources() {
        for (Entry<String, ReadwriteSplittingDataSourceRule> entry : dataSourceRules.entrySet()) {
            deleteStorageNodeDataSources(entry.getValue());
        }
    }
    
}
