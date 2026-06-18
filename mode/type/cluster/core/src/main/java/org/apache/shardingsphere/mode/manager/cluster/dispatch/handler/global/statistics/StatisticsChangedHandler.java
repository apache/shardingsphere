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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.statistics;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlRowStatistics;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.GlobalDataChangedEventHandler;
import org.apache.shardingsphere.mode.metadata.manager.statistics.StatisticsManager;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.database.statistics.StatisticsDataNodePath;
import org.apache.shardingsphere.mode.node.path.type.database.statistics.StatisticsDatabaseNodePath;
import org.apache.shardingsphere.mode.node.path.type.database.statistics.StatisticsSchemaNodePath;
import org.apache.shardingsphere.mode.node.path.type.database.statistics.StatisticsTableNodePath;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Statistics changed handler.
 */
public final class StatisticsChangedHandler implements GlobalDataChangedEventHandler {
    
    @Override
    public NodePath getSubscribedNodePath() {
        return new StatisticsDatabaseNodePath(null);
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED);
    }
    
    @Override
    public void handle(final ContextManager contextManager, final DataChangedEvent event) {
        StatisticsManager statisticsManager = contextManager.getMetaDataContextManager().getStatisticsManager();
        Optional<String> databaseName = NodePathSearcher.find(event.getKey(), StatisticsDatabaseNodePath.createDatabaseSearchCriteria(false));
        if (databaseName.isPresent()) {
            handleDatabaseChanged(statisticsManager, event.getType(), databaseName.get());
            return;
        }
        databaseName = NodePathSearcher.find(event.getKey(), StatisticsDatabaseNodePath.createDatabaseSearchCriteria(true));
        if (!databaseName.isPresent()) {
            return;
        }
        Optional<String> schemaName = NodePathSearcher.find(event.getKey(), StatisticsSchemaNodePath.createSchemaSearchCriteria(false));
        if (schemaName.isPresent()) {
            handleSchemaChanged(statisticsManager, event.getType(), databaseName.get(), schemaName.get());
            return;
        }
        schemaName = NodePathSearcher.find(event.getKey(), StatisticsSchemaNodePath.createSchemaSearchCriteria(true));
        if (!schemaName.isPresent()) {
            return;
        }
        Optional<String> tableName = NodePathSearcher.find(event.getKey(), StatisticsTableNodePath.createTableSearchCriteria(false));
        if (tableName.isPresent()) {
            handleTableChanged(statisticsManager, event.getType(), databaseName.get(), schemaName.get(), tableName.get());
            return;
        }
        tableName = NodePathSearcher.find(event.getKey(), StatisticsTableNodePath.createTableSearchCriteria(true));
        if (!tableName.isPresent()) {
            return;
        }
        Optional<String> uniqueKey = NodePathSearcher.find(event.getKey(), StatisticsDataNodePath.createRowUniqueKeySearchCriteria());
        if (uniqueKey.isPresent()) {
            handleRowDataChanged(statisticsManager, event.getType(), event.getValue(), databaseName.get(), schemaName.get(), tableName.get(), uniqueKey.get());
        }
    }
    
    private void handleDatabaseChanged(final StatisticsManager statisticsManager, final Type type, final String databaseName) {
        switch (type) {
            case ADDED:
            case UPDATED:
                statisticsManager.addDatabaseStatistics(databaseName);
                return;
            case DELETED:
                statisticsManager.dropDatabaseStatistics(databaseName);
                return;
            default:
        }
    }
    
    private void handleSchemaChanged(final StatisticsManager statisticsManager, final Type type, final String databaseName, final String schemaName) {
        switch (type) {
            case ADDED:
            case UPDATED:
                statisticsManager.addSchemaStatistics(databaseName, schemaName);
                return;
            case DELETED:
                statisticsManager.dropSchemaStatistics(databaseName, schemaName);
                return;
            default:
        }
    }
    
    private void handleTableChanged(final StatisticsManager statisticsManager, final Type type, final String databaseName, final String schemaName, final String tableName) {
        switch (type) {
            case ADDED:
            case UPDATED:
                statisticsManager.addTableStatistics(databaseName, schemaName, tableName);
                return;
            case DELETED:
                statisticsManager.dropTableStatistics(databaseName, schemaName, tableName);
                return;
            default:
        }
    }
    
    private void handleRowDataChanged(final StatisticsManager statisticsManager, final Type type, final String eventValue,
                                      final String databaseName, final String schemaName, final String tableName, final String uniqueKey) {
        if ((Type.ADDED == type || Type.UPDATED == type) && !Strings.isNullOrEmpty(eventValue)) {
            statisticsManager.alterRowStatistics(databaseName, schemaName, tableName, YamlEngine.unmarshal(eventValue, YamlRowStatistics.class));
        } else if (Type.DELETED == type) {
            statisticsManager.deleteRowStatistics(databaseName, schemaName, tableName, uniqueKey);
        }
    }
}
