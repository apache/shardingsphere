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

package org.apache.shardingsphere.sharding.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableNodesStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Result set for show sharding table nodes.
 */
public final class ShowShardingTableNodesExecutor implements RQLExecutor<ShowShardingTableNodesStatement> {
    
    private static final String NAME = "name";
    
    private static final String NODES = "nodes";
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowShardingTableNodesStatement sqlStatement) {
        Optional<ShardingRule> shardingRule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        if (!shardingRule.isPresent()) {
            return Collections.emptyList();
        }
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        String tableName = sqlStatement.getTableName();
        if (null == tableName) {
            for (Entry<String, TableRule> entry : shardingRule.get().getTableRules().entrySet()) {
                result.add(new LocalDataQueryResultRow(entry.getKey(), getTableNodes(entry.getValue())));
            }
        } else {
            result.add(new LocalDataQueryResultRow(tableName, getTableNodes(shardingRule.get().getTableRule(tableName))));
        }
        return result;
    }
    
    private String getTableNodes(final TableRule tableRule) {
        return tableRule.getActualDataNodes().stream().map(DataNode::format).collect(Collectors.joining(", "));
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList(NAME, NODES);
    }
    
    @Override
    public Class<ShowShardingTableNodesStatement> getType() {
        return ShowShardingTableNodesStatement.class;
    }
}
