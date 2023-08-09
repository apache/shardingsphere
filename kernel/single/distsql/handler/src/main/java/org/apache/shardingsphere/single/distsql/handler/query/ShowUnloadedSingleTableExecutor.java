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

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.single.datanode.SingleTableDataNodeLoader;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowUnloadedSingleTableStatement;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.single.util.SingleTableLoadUtils;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Show unloaded single table executor.
 */
public final class ShowUnloadedSingleTableExecutor implements RQLExecutor<ShowUnloadedSingleTableStatement> {
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("table_name", "storage_unit_name");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowUnloadedSingleTableStatement sqlStatement) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        Map<String, Collection<DataNode>> actualDataNodes = getActualDataNodes(database);
        Optional<SingleRule> singleRule = database.getRuleMetaData().findSingleRule(SingleRule.class);
        if (singleRule.isPresent()) {
            for (String each : singleRule.get().getLogicTableMapper().getTableNames()) {
                actualDataNodes.remove(each);
            }
        }
        actualDataNodes.forEach((key, value) -> result.add(new LocalDataQueryResultRow(key, value.iterator().next().getDataSourceName())));
        return result;
    }
    
    private Map<String, Collection<DataNode>> getActualDataNodes(final ShardingSphereDatabase database) {
        ResourceMetaData resourceMetaData = database.getResourceMetaData();
        Map<String, DataSource> aggregateDataSourceMap = SingleTableLoadUtils.getAggregatedDataSourceMap(resourceMetaData.getDataSources(), database.getRuleMetaData().getRules());
        Collection<String> excludedTables = SingleTableLoadUtils.getExcludedTables(database.getRuleMetaData().getRules());
        return SingleTableDataNodeLoader.load(database.getName(), database.getProtocolType(), aggregateDataSourceMap, excludedTables);
    }
    
    @Override
    public Class<ShowUnloadedSingleTableStatement> getType() {
        return ShowUnloadedSingleTableStatement.class;
    }
}
