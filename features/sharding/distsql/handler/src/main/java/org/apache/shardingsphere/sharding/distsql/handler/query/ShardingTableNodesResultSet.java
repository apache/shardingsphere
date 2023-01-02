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

import org.apache.shardingsphere.distsql.handler.resultset.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableNodesStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Result set for show sharding table nodes.
 */
public final class ShardingTableNodesResultSet implements DatabaseDistSQLResultSet {
    
    private static final String NAME = "name";
    
    private static final String NODES = "nodes";
    
    private Iterator<Entry<String, String>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        ShardingRule shardingRule = (ShardingRule) database.getRuleMetaData().getRules().stream().filter(each -> each instanceof ShardingRule).findFirst().orElse(null);
        if (null == shardingRule) {
            return;
        }
        Map<String, String> result = new LinkedHashMap<>();
        String tableName = ((ShowShardingTableNodesStatement) sqlStatement).getTableName();
        if (null == tableName) {
            for (Entry<String, TableRule> entry : shardingRule.getTableRules().entrySet()) {
                result.put(entry.getKey(), getTableNodes(entry.getValue()));
            }
        } else {
            result.put(tableName, getTableNodes(shardingRule.getTableRule(tableName)));
        }
        data = result.entrySet().iterator();
    }
    
    private String getTableNodes(final TableRule tableRule) {
        return tableRule.getActualDataNodes().stream().map(DataNode::format).collect(Collectors.joining(", "));
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList(NAME, NODES);
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        if (!data.hasNext()) {
            return Collections.emptyList();
        }
        Entry<String, String> entry = data.next();
        return Arrays.asList(entry.getKey(), entry.getValue());
    }
    
    @Override
    public String getType() {
        return ShowShardingTableNodesStatement.class.getName();
    }
}
