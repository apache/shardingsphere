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

package org.apache.shardingsphere.infra.rule.single;

import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.CreateTableEvent;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.DropTableEvent;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.level.FeatureRule;
import org.apache.shardingsphere.infra.rule.scope.SchemaRule;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Single table rule.
 */
@Getter
public final class SingleTableRule implements FeatureRule, SchemaRule {
    
    private final Collection<String> dataSourceNames;
    
    private final Map<String, SingleTableDataNode> singleTableDataNodes;
    
    private final Collection<String> excludedTables;
    
    public SingleTableRule(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> rules) {
        dataSourceNames = dataSourceMap.keySet();
        excludedTables = getExcludedTables(rules);
        singleTableDataNodes = SingleTableDataNodeLoader.load(databaseType, dataSourceMap, excludedTables);
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Judge whether single table is in same data source or not.
     *
     * @param logicTableNames logic table names
     * @return whether single table is in same data source or not
     */
    public boolean isSingleTableInSameDataSource(final Collection<String> logicTableNames) {
        long dataSourceCount = singleTableDataNodes.values().stream().filter(each -> Sets.newHashSet(
                getSingleTableNames(logicTableNames)).contains(each.getTableName())).map(SingleTableDataNode::getDataSourceName).distinct().count();
        return dataSourceCount <= 1;
    }
    
    /**
     * Get sharding logic table names.
     *
     * @param logicTableNames logic table names
     * @return sharding logic table names
     */
    public Collection<String> getSingleTableNames(final Collection<String> logicTableNames) {
        return logicTableNames.stream().filter(each -> !excludedTables.contains(each)).collect(Collectors.toCollection(LinkedList::new));
    }
    
    /**
     * Add single table.
     * 
     * @param event create table event
     */
    @Subscribe
    public void createSingleTable(final CreateTableEvent event) {
        if (dataSourceNames.contains(event.getDataSourceName()) && !excludedTables.contains(event.getTableName())) {
            singleTableDataNodes.put(event.getTableName(), new SingleTableDataNode(event.getTableName(), event.getDataSourceName()));
        }
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
    
    private Collection<String> getExcludedTables(final Collection<ShardingSphereRule> rules) {
        return rules.stream().filter(each -> each instanceof DataNodeContainedRule).flatMap(each -> ((DataNodeContainedRule) each).getAllTables().stream()).collect(Collectors.toList());
    }
}
