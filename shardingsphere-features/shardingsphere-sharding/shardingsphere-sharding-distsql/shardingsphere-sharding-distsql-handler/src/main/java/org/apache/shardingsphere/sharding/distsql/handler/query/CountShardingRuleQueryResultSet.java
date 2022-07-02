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

import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CountShardingRuleStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

/**
 * Query result set for count sharding rule.
 */
public final class CountShardingRuleQueryResultSet implements DistSQLResultSet {

    private Iterator<Entry<String, LocalDataQueryResultRow>> data = Collections.emptyIterator();

    private static final String SHARDING_TABLE = "sharding_table";

    private static final String SHARDING_BINDING_TABLE = "sharding_binding_table";

    private static final String SHARDING_BROADCAST_TABLE = "sharding_broadcast_table";

    private static final String SHARDING_SCALING = "sharding_scaling";

    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("rule_name", "database", "count");
    }

    @Override
    public void init(ShardingSphereDatabase database, SQLStatement sqlStatement) {
        
    }

    private void addDatabaseData(final Map<String, LocalDataQueryResultRow> rowMap, final ShardingSphereDatabase database) {
        for (ShardingSphereRule each : database.getRuleMetaData().getRules()) {
            if (each instanceof ShardingRule) {
                addShardingData(rowMap, (ShardingRule) each);
            }
        }
    }

    private void addShardingData(final Map<String, LocalDataQueryResultRow> rowMap, final ShardingRule rule) {
        addData(rowMap, SHARDING_TABLE, () -> rule.getTables().size());
        addData(rowMap, SHARDING_BINDING_TABLE, () -> rule.getBindingTableRules().size());
        addData(rowMap, SHARDING_BROADCAST_TABLE, () -> rule.getBroadcastTables().size());
        addData(rowMap, SHARDING_SCALING, () -> ((ShardingRuleConfiguration) rule.getConfiguration()).getScaling().size());
    }

    private void addData(final Map<String, LocalDataQueryResultRow> rowMap, final String dataKey, final Supplier<Integer> apply) {
        rowMap.compute(dataKey, (key, value) -> buildRow(value, dataKey, apply.get()));
    }

    private LocalDataQueryResultRow buildRow(final LocalDataQueryResultRow value, final String ruleName, final int count) {
        return null == value ? new LocalDataQueryResultRow(ruleName, count) : new LocalDataQueryResultRow(ruleName, Integer.sum((Integer) value.getCell(2), count));
    }

    @Override
    public boolean next() {
        return false;
    }

    @Override
    public Collection<Object> getRowData() {
        return null;
    }

    @Override
    public String getType() {
        return CountShardingRuleStatement.class.getName();
    }
}
