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

package org.apache.shardingsphere.single.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.single.datanode.SingleTableDataNodeLoader;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowUnloadedSingleTableStatement;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.single.util.SingleTableLoadUtils;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Show unloaded single table executor.
 */
@Setter
public final class ShowUnloadedSingleTableExecutor implements DistSQLQueryExecutor<ShowUnloadedSingleTableStatement>, DistSQLExecutorDatabaseAware, DistSQLExecutorRuleAware<SingleRule> {
    
    private ShardingSphereDatabase database;
    
    private SingleRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowUnloadedSingleTableStatement sqlStatement) {
        return Arrays.asList("table_name", "storage_unit_name");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowUnloadedSingleTableStatement sqlStatement, final ContextManager contextManager) {
        Map<String, Collection<DataNode>> actualDataNodes = getActualDataNodes(database);
        for (String each : rule.getAttributes().getAttribute(TableMapperRuleAttribute.class).getLogicTableMapper().getTableNames()) {
            actualDataNodes.remove(each.toLowerCase());
        }
        return actualDataNodes.entrySet().stream().map(entry -> new LocalDataQueryResultRow(entry.getKey(), entry.getValue().iterator().next().getDataSourceName())).collect(Collectors.toList());
    }
    
    private Map<String, Collection<DataNode>> getActualDataNodes(final ShardingSphereDatabase database) {
        ResourceMetaData resourceMetaData = database.getResourceMetaData();
        Map<String, DataSource> aggregateDataSourceMap = SingleTableLoadUtils.getAggregatedDataSourceMap(
                resourceMetaData.getStorageUnits().entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)),
                database.getRuleMetaData().getRules());
        Collection<String> excludedTables = SingleTableLoadUtils.getExcludedTables(database.getRuleMetaData().getRules());
        return SingleTableDataNodeLoader.load(database.getName(), aggregateDataSourceMap, excludedTables);
    }
    
    @Override
    public Class<SingleRule> getRuleClass() {
        return SingleRule.class;
    }
    
    @Override
    public Class<ShowUnloadedSingleTableStatement> getType() {
        return ShowUnloadedSingleTableStatement.class;
    }
}
