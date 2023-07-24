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

package org.apache.shardingsphere.sharding.merge.ddl;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.FetchStatementContext;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.engine.merger.ResultMerger;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.sharding.merge.common.IteratorStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.ddl.fetch.FetchStreamMergedResult;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * DDL result merger for Sharding.
 */
public final class ShardingDDLResultMerger implements ResultMerger {
    
    @Override
    public MergedResult merge(final List<QueryResult> queryResults, final SQLStatementContext sqlStatementContext,
                              final ShardingSphereDatabase database, final ConnectionContext connectionContext) throws SQLException {
        if (!(sqlStatementContext instanceof FetchStatementContext)) {
            return new TransparentMergedResult(queryResults.get(0));
        }
        if (1 == queryResults.size()) {
            return new IteratorStreamMergedResult(queryResults);
        }
        FetchStatementContext fetchStatementContext = (FetchStatementContext) sqlStatementContext;
        Map<String, Integer> columnLabelIndexMap = getColumnLabelIndexMap(queryResults.get(0));
        fetchStatementContext.getCursorStatementContext().getSelectStatementContext().setIndexes(columnLabelIndexMap);
        return new FetchStreamMergedResult(queryResults, fetchStatementContext, getSchema(sqlStatementContext, database), connectionContext);
    }
    
    private ShardingSphereSchema getSchema(final SQLStatementContext sqlStatementContext, final ShardingSphereDatabase database) {
        String defaultSchemaName = DatabaseTypeEngine.getDefaultSchemaName(sqlStatementContext.getDatabaseType(), database.getName());
        return sqlStatementContext.getTablesContext().getSchemaName().map(database::getSchema).orElseGet(() -> database.getSchema(defaultSchemaName));
    }
    
    private Map<String, Integer> getColumnLabelIndexMap(final QueryResult queryResult) throws SQLException {
        Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < queryResult.getMetaData().getColumnCount(); i++) {
            result.put(SQLUtils.getExactlyValue(queryResult.getMetaData().getColumnLabel(i + 1)), i + 1);
        }
        return result;
    }
}
