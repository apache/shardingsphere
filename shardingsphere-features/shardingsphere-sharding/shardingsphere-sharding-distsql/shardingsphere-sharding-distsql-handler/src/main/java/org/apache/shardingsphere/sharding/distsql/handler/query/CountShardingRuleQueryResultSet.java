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

import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CountShardingRuleStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Query result set for count sharding rule.
 */
public final class CountShardingRuleQueryResultSet implements DatabaseDistSQLResultSet {
    
    private static final String SHARDING_TABLE = "sharding_table";
    
    private static final String SHARDING_BINDING_TABLE = "sharding_binding_table";
    
    private static final String SHARDING_BROADCAST_TABLE = "sharding_broadcast_table";
    
    private Iterator<Entry<String, LinkedList<Object>>> data = Collections.emptyIterator();
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("rule_name", "database", "count");
    }
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        Map<String, LinkedList<Object>> result = new LinkedHashMap<>();
        rule.ifPresent(optional -> addShardingData(result, rule.get(), database.getName()));
        data = result.entrySet().iterator();
    }
    
    private void addShardingData(final Map<String, LinkedList<Object>> rowMap, final ShardingRule rule, final String databaseName) {
        addData(rowMap, SHARDING_TABLE, databaseName, () -> rule.getTables().size());
        addData(rowMap, SHARDING_BINDING_TABLE, databaseName, () -> rule.getBindingTableRules().size());
        addData(rowMap, SHARDING_BROADCAST_TABLE, databaseName, () -> rule.getBroadcastTables().size());
    }
    
    private void addData(final Map<String, LinkedList<Object>> rowMap, final String dataKey, final String databaseName, final Supplier<Integer> apply) {
        rowMap.compute(dataKey, (key, value) -> buildRow(value, databaseName, apply.get()));
    }
    
    private LinkedList<Object> buildRow(final LinkedList<Object> value, final String databaseName, final int count) {
        if (null == value) {
            return new LinkedList<>(Arrays.asList(databaseName, count));
        } else {
            value.set(1, (Integer) value.get(1) + count);
            return value;
        }
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        Entry<String, LinkedList<Object>> entry = data.next();
        entry.getValue().addFirst(entry.getKey());
        return entry.getValue();
    }
    
    @Override
    public String getType() {
        return CountShardingRuleStatement.class.getName();
    }
}
