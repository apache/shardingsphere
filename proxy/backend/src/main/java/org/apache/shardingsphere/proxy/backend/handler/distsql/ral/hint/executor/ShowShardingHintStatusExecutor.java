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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.enums.HintShardingType;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.result.ShowShardingHintStatusResult;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.ShowShardingHintStatusStatement;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Show sharding hint status executor.
 */
@RequiredArgsConstructor
public final class ShowShardingHintStatusExecutor extends AbstractHintQueryExecutor<ShowShardingHintStatusStatement> {
    
    private final ConnectionSession connectionSession;
    
    @Override
    protected List<QueryHeader> createQueryHeaders() {
        List<QueryHeader> result = new ArrayList<>(4);
        result.add(new QueryHeader("", "", "table_name", "", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader("", "", "database_sharding_values", "", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader("", "", "table_sharding_values", "", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader("", "", "sharding_type", "", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        return result;
    }
    
    @Override
    protected MergedResult createMergedResult() {
        Map<String, ShowShardingHintStatusResult> results = new HashMap<>();
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(connectionSession.getDatabaseName());
        if (!database.isComplete()) {
            throw new RuleNotExistedException();
        }
        String schemaName = DatabaseTypeEngine.getDefaultSchemaName(connectionSession.getProtocolType(), connectionSession.getDatabaseName());
        Collection<String> tableNames = database.getSchema(schemaName).getAllTableNames();
        for (String each : tableNames) {
            if (HintManager.isDatabaseShardingOnly()) {
                fillShardingValues(results, each, HintManager.getDatabaseShardingValues(), Collections.emptyList());
            } else {
                fillShardingValues(results, each, HintManager.getDatabaseShardingValues(each), HintManager.getTableShardingValues(each));
            }
        }
        return convertToMergedResult(results.values());
    }
    
    private void fillShardingValues(final Map<String, ShowShardingHintStatusResult> results, final String logicTable,
                                    final Collection<Comparable<?>> databaseShardingValues, final Collection<Comparable<?>> tableShardingValues) {
        if (!results.containsKey(logicTable)) {
            results.put(logicTable, new ShowShardingHintStatusResult(logicTable));
        }
        for (Comparable<?> each : databaseShardingValues) {
            results.get(logicTable).getDatabaseShardingValues().add(each.toString());
        }
        for (Comparable<?> each : tableShardingValues) {
            results.get(logicTable).getTableShardingValues().add(each.toString());
        }
    }
    
    private MergedResult convertToMergedResult(final Collection<ShowShardingHintStatusResult> showShardingHintStatusResults) {
        return new LocalDataMergedResult(showShardingHintStatusResults.stream().map(this::createRow).collect(Collectors.toList()));
    }
    
    private LocalDataQueryResultRow createRow(final ShowShardingHintStatusResult showShardingHintStatusResult) {
        return new LocalDataQueryResultRow(showShardingHintStatusResult.getLogicTable(),
                String.join(",", showShardingHintStatusResult.getDatabaseShardingValues()), String.join(",", showShardingHintStatusResult.getTableShardingValues()),
                String.valueOf(HintManager.isDatabaseShardingOnly() ? HintShardingType.DATABASES_ONLY : HintShardingType.DATABASES_TABLES).toLowerCase());
    }
}
