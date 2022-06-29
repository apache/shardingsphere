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

import com.google.common.base.Strings;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.queryable.CountSingleTableRuleStatement;
import org.apache.shardingsphere.infra.exception.DatabaseNotExistedException;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Count single table rule handler.
 */
public final class CountSingleTableRuleHandler extends QueryableRALBackendHandler<CountSingleTableRuleStatement> {

    private static final String SINGLE_TABLE = "single_table";

    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("rule_name", "database", "count");
    }

    @Override
    protected Collection<LocalDataQueryResultRow> getRows(final ContextManager contextManager) {
        String databaseName = getDatabaseName();
        Map<String, LocalDataQueryResultRow> result = initRows(databaseName);
        addDatabaseData(result, ProxyContext.getInstance().getDatabase(databaseName));
        return result.values();
    }

    private String getDatabaseName() {
        String result = getSqlStatement().getDatabase().isPresent() ? getSqlStatement().getDatabase().get().getIdentifier().getValue() : getConnectionSession().getDatabaseName();
        if (Strings.isNullOrEmpty(result)) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().getAllDatabaseNames().contains(result)) {
            throw new DatabaseNotExistedException(result);
        }
        return result;
    }

    private Map<String, LocalDataQueryResultRow> initRows(final String databaseName) {
        Map<String, LocalDataQueryResultRow> result = new LinkedHashMap<>();
        result.put(SINGLE_TABLE, new LocalDataQueryResultRow(SINGLE_TABLE, databaseName, 0));
        return result;
    }

    private void addDatabaseData(final Map<String, LocalDataQueryResultRow> rowMap, final ShardingSphereDatabase database) {
        for (ShardingSphereRule each : database.getRuleMetaData().getRules()) {
            if (each instanceof SingleTableRule) {
                addSingleTableData(rowMap, (SingleTableRule) each, database.getName());
            }
        }
    }

    private void addSingleTableData(final Map<String, LocalDataQueryResultRow> rowMap, final SingleTableRule rule, final String databaseName) {
        rowMap.compute(SINGLE_TABLE, (key, value) -> buildRow(value, databaseName, rule.getAllTables().size()));
    }

    private LocalDataQueryResultRow buildRow(final LocalDataQueryResultRow value, final String databaseName, final int count) {
        return null == value ? new LocalDataQueryResultRow(SINGLE_TABLE, databaseName, count) : new LocalDataQueryResultRow(SINGLE_TABLE, databaseName, Integer.sum((Integer) value.getCell(3), count));
    }
}
