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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.queryable.CountSingleTableRuleStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Count single table rule handler.
 */
public final class CountSingleTableRuleHandler extends QueryableRALBackendHandler<CountSingleTableRuleStatement> {
    
    private static final String SINGLE_TABLE = "single_table";
    
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("rule_name", "count");
    }
    
    @Override
    protected Collection<LocalDataQueryResultRow> getRows(final ContextManager contextManager) {
        Map<String, LocalDataQueryResultRow> result = initRows();
        ProxyContext.getInstance().getAllDatabaseNames().forEach(each -> addDatabaseData(result, ProxyContext.getInstance().getDatabase(each)));
        return result.values();
    }

    private Map<String, LocalDataQueryResultRow> initRows() {
        return Collections.singletonMap(SINGLE_TABLE, new LocalDataQueryResultRow(SINGLE_TABLE, 0));
    }
    
    private void addDatabaseData(final Map<String, LocalDataQueryResultRow> rowMap, final ShardingSphereDatabase database) {
        for (ShardingSphereRule each : database.getRuleMetaData().getRules()) {
            if (each instanceof SingleTableRule) {
                addSingleTableData(rowMap, (SingleTableRule) each);
            }
        }
    }
    
    private void addSingleTableData(final Map<String, LocalDataQueryResultRow> rowMap, final SingleTableRule rule) {
        rowMap.compute(SINGLE_TABLE, (key, value) -> buildRow(value, SINGLE_TABLE, rule.getAllTables().size()));
    }
    
    private LocalDataQueryResultRow buildRow(final LocalDataQueryResultRow value, final String ruleName, final int count) {
        return null == value ? new LocalDataQueryResultRow(ruleName, count) : new LocalDataQueryResultRow(ruleName, Integer.sum((Integer) value.getCell(2), count));
    }
}
