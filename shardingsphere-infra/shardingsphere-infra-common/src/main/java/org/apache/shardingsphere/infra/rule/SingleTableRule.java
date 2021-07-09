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

package org.apache.shardingsphere.infra.rule;

import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.CreateTableEvent;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.DropTableEvent;
import org.apache.shardingsphere.infra.rule.level.FeatureRule;
import org.apache.shardingsphere.infra.rule.scope.SchemaRule;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.type.TableContainedRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Single table rule.
 */
@Getter
public final class SingleTableRule implements FeatureRule, SchemaRule, DataNodeContainedRule, TableContainedRule {
    
    private final Collection<String> dataSourceNames;
    
    private final Map<String, SingleTableDataNode> singleTableDataNodes;
    
    public SingleTableRule(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        singleTableDataNodes = SingleTableDataNodeLoader.load(databaseType, dataSourceMap);
        dataSourceNames = dataSourceMap.keySet();
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    @Override
    public Map<String, Collection<DataNode>> getAllDataNodes() {
        Map<String, Collection<DataNode>> result = new LinkedHashMap<>();
        singleTableDataNodes.forEach((key, value) -> result.put(key, Collections.singleton(new DataNode(value.getDataSourceName(), value.getTableName()))));
        return result;
    }
    
    @Override
    public Collection<String> getAllActualTables() {
        return Collections.emptyList();
    }
    
    @Override
    public Optional<String> findFirstActualTable(final String logicTable) {
        return Optional.empty();
    }
    
    @Override
    public boolean isNeedAccumulate(final Collection<String> tables) {
        return false;
    }
    
    @Override
    public Optional<String> findLogicTableByActualTable(final String actualTable) {
        return Optional.empty();
    }
    
    @Override
    public Optional<String> findActualTableByCatalog(final String catalog, final String logicTable) {
        return Optional.empty();
    }
    
    @Override
    public Collection<String> getTables() {
        return Collections.emptyList();
    }
    
    /**
     * Judge whether all tables are in same data source or not.
     *
     * @param logicTableNames logic table names
     * @return whether all tables are in same data source or not
     */
    public boolean isAllTablesInSameDataSource(final Collection<String> logicTableNames) {
        long dataSourceCount = singleTableDataNodes.values().stream().filter(each 
            -> Sets.newHashSet(logicTableNames).contains(each.getTableName())).map(SingleTableDataNode::getDataSourceName).distinct().count();
        return 1 == dataSourceCount;
    }
    
    /**
     * Get sharding logic table names.
     *
     * @param logicTableNames logic table names
     * @return sharding logic table names
     */
    public Collection<String> getSingleTableNames(final Collection<String> logicTableNames) {
        return logicTableNames.stream().filter(singleTableDataNodes::containsKey).collect(Collectors.toCollection(LinkedList::new));
    }
    
    /**
     * Add single table.
     * 
     * @param event create table event
     */
    @Subscribe
    public void createSingleTable(final CreateTableEvent event) {
        singleTableDataNodes.put(event.getTableName(), new SingleTableDataNode(event.getTableName(), event.getDataSourceName()));
    }
    
    /**
     * Drop single table.
     *
     * @param event drop table event
     */
    @Subscribe
    public void dropSingleTable(final DropTableEvent event) {
        singleTableDataNodes.remove(event.getTableName());
    }
}
