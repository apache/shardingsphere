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

package org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor;

import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.core.merge.MergedResult;
import org.apache.shardingsphere.core.merge.dal.show.ShowShardingCTLMergedResult;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.result.HintShowTableStatusResult;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Hint show table status command executor.
 *
 * @author liya
 */
@RequiredArgsConstructor
public final class HintShowTableStatusExecutor extends AbstractHintQueryExecutor {
    
    private final BackendConnection backendConnection;
    
    protected List<QueryHeader> createQueryHeaders() {
        List<QueryHeader> queryHeaders = new ArrayList<>(3);
        queryHeaders.add(new QueryHeader("", "", "table_name", "", 255, Types.CHAR, 0));
        queryHeaders.add(new QueryHeader("", "", "database_sharding_values", "", 255, Types.CHAR, 0));
        queryHeaders.add(new QueryHeader("", "", "table_sharding_values", "", 255, Types.CHAR, 0));
        return queryHeaders;
    }
    
    protected MergedResult createMergedResult() {
        Map<String, HintShowTableStatusResult> results = new HashMap<>();
        if (HintManager.isDatabaseShardingOnly()) {
            fillShardingValuesDatabaseShardingOnly(results, HintManager.getDatabaseShardingValues());
        } else {
            fillDatabaseShardingValues(results, HintManager.getDatabaseShardingValuesMap());
            fillTableShardingValues(results, HintManager.getTableShardingValuesMap());
        }
        return convert2MergedResult(results.values());
    }
    
    private void fillShardingValuesDatabaseShardingOnly(final Map<String, HintShowTableStatusResult> results, final Collection<Comparable<?>> databaseShardingValues) {
        List<String> stringDatabaseShardingValues = new LinkedList<>();
        for (Comparable<?> each : databaseShardingValues) {
            stringDatabaseShardingValues.add(String.valueOf(each));
        }
        for (String each : getLogicTableNames()) {
            HintShowTableStatusResult hintShowTableStatusResult = new HintShowTableStatusResult(each);
            hintShowTableStatusResult.getDatabaseShardingValues().addAll(stringDatabaseShardingValues);
            results.put(each, hintShowTableStatusResult);
        }
    }
    
    private Collection<String> getLogicTableNames() {
        Collection<String> result = new LinkedList<>();
        Collection<TableRule> tableRules = backendConnection.getLogicSchema().getShardingRule().getTableRules();
        for (TableRule each : tableRules) {
            result.add(each.getLogicTable());
        }
        return result;
    }
    
    private void fillDatabaseShardingValues(final Map<String, HintShowTableStatusResult> results, final Map<String, Collection<Comparable<?>>> databaseShardingValuesMap) {
        for (Map.Entry<String, Collection<Comparable<?>>> entry : databaseShardingValuesMap.entrySet()) {
            String logicTable = entry.getKey();
            if (!results.containsKey(logicTable)) {
                results.put(logicTable, new HintShowTableStatusResult(logicTable));
            }
            for (Comparable<?> each : entry.getValue()) {
                results.get(logicTable).getDatabaseShardingValues().add(each.toString());
            }
        }
    }
    
    private void fillTableShardingValues(final Map<String, HintShowTableStatusResult> results, final Map<String, Collection<Comparable<?>>> tableShardingValuesMap) {
        for (Map.Entry<String, Collection<Comparable<?>>> entry : tableShardingValuesMap.entrySet()) {
            String logicTable = entry.getKey();
            if (!results.containsKey(logicTable)) {
                results.put(logicTable, new HintShowTableStatusResult(logicTable));
            }
            for (Comparable<?> each : entry.getValue()) {
                results.get(logicTable).getTableShardingValues().add(each.toString());
            }
        }
    }
    
    private MergedResult convert2MergedResult(final Collection<HintShowTableStatusResult> hintShowTableStatusResults) {
        List<List<Object>> values = new ArrayList<>(hintShowTableStatusResults.size());
        for (HintShowTableStatusResult each : hintShowTableStatusResults) {
            List<Object> row = new ArrayList<>(3);
            row.add(each.getLogicTable());
            row.add(Joiner.on(",").join(each.getDatabaseShardingValues()));
            row.add(Joiner.on(",").join(each.getTableShardingValues()));
            values.add(row);
        }
        return new ShowShardingCTLMergedResult(values);
    }
}
