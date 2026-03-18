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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.state.coordinator;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlTableSwapper;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.GlobalDataChangedEventHandler;
import org.apache.shardingsphere.mode.manager.cluster.statistics.StatisticsRefreshEngine;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearchCriteria;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.global.state.coordinator.CoordinatorType;
import org.apache.shardingsphere.mode.node.path.type.global.state.coordinator.table.TableCoordinatorNodePath;
import org.apache.shardingsphere.mode.node.path.type.global.state.coordinator.table.TableCoordinatorTypeNodePath;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Table coordinator changed handler.
 */
public final class TableCoordinatorChangedHandler implements GlobalDataChangedEventHandler {
    
    private final YamlTableSwapper swapper = new YamlTableSwapper();
    
    @Override
    public NodePath getSubscribedNodePath() {
        return new TableCoordinatorNodePath(null);
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Collections.singletonList(Type.ADDED);
    }
    
    @Override
    public void handle(final ContextManager contextManager, final DataChangedEvent event) {
        String eventKey = event.getKey();
        if (!NodePathSearcher.isMatchedPath(eventKey, TableCoordinatorTypeNodePath.createTableSearchCriteria())) {
            return;
        }
        NodePath path = new TableCoordinatorTypeNodePath(NodePathPattern.QUALIFIED_IDENTIFIER, NodePathPattern.IDENTIFIER);
        String qualifiedTableName = NodePathSearcher.get(eventKey, new NodePathSearchCriteria(path, true, 1));
        String coordinatorType = NodePathSearcher.get(eventKey, new NodePathSearchCriteria(path, false, 2));
        if (Strings.isNullOrEmpty(qualifiedTableName) || Strings.isNullOrEmpty(coordinatorType)) {
            return;
        }
        List<String> qualifiedTableNames = Splitter.on(".").splitToList(qualifiedTableName);
        if (qualifiedTableNames.size() != 3) {
            return;
        }
        handle(contextManager, qualifiedTableNames.get(0), qualifiedTableNames.get(1), qualifiedTableNames.get(2), event.getValue(), CoordinatorType.valueOf(coordinatorType));
    }
    
    private void handle(final ContextManager contextManager, final String databaseName, final String schemaName, final String tableName, final String tableContent,
                        final CoordinatorType type) {
        switch (type) {
            case CREATE:
                handleCreatedOrAltered(contextManager, databaseName, schemaName, tableContent);
                break;
            case DROP:
                handleDropped(contextManager, databaseName, schemaName, tableName);
                break;
            default:
                break;
        }
    }
    
    private void handleCreatedOrAltered(final ContextManager contextManager, final String databaseName, final String schemaName, final String tableContent) {
        ShardingSphereTable alteredTable = swapper.swapToObject(YamlEngine.unmarshal(tableContent, YamlShardingSphereTable.class));
        contextManager.getMetaDataContextManager().getDatabaseMetaDataManager().alterTable(databaseName, schemaName, alteredTable);
        new StatisticsRefreshEngine(contextManager).asyncRefresh();
    }
    
    private void handleDropped(final ContextManager contextManager, final String databaseName, final String schemaName, final String tableName) {
        contextManager.getMetaDataContextManager().getDatabaseMetaDataManager().dropTable(databaseName, schemaName, tableName);
        new StatisticsRefreshEngine(contextManager).asyncRefresh();
    }
}
