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
import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.rule.level.FeatureRule;
import org.apache.shardingsphere.infra.rule.scope.SchemaRule;

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
    
    public SingleTableRule(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final Collection<String> occupiedTables) {
        dataSourceNames = dataSourceMap.keySet();
        singleTableDataNodes = SingleTableDataNodeLoader.load(databaseType, dataSourceMap, occupiedTables);
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
        return logicTableNames.stream().filter(singleTableDataNodes::containsKey).collect(Collectors.toCollection(LinkedList::new));
    }
    
    /**
     * Add single table data node.
     * 
     * @param tableName table name
     * @param dataSourceName data source name
     */
    public void addSingleTableDataNode(final String tableName, final String dataSourceName) {
        if (dataSourceNames.contains(dataSourceName) && !singleTableDataNodes.containsKey(tableName)) {
            singleTableDataNodes.put(tableName, new SingleTableDataNode(tableName, dataSourceName));
        }
    }
    
    /**
     * Drop single table data node.
     *
     * @param tableName table name
     */
    public void dropSingleTableDataNode(final String tableName) {
        singleTableDataNodes.remove(tableName);
    }
}
