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

import com.cedarsoftware.util.CaseInsensitiveMap;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorHeldSQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.engine.merger.ResultMerger;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.stream.IteratorStreamMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.sharding.merge.ddl.fetch.FetchStreamMergedResult;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.statement.core.util.SQLUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * DDL result merger for Sharding.
 */
public final class ShardingDDLResultMerger implements ResultMerger {
    
    @Override
    public MergedResult merge(final List<QueryResult> queryResults, final SQLStatementContext sqlStatementContext,
                              final ShardingSphereDatabase database, final ConnectionContext connectionContext) throws SQLException {
        if (!(sqlStatementContext.getSqlStatement() instanceof FetchStatement)) {
            return new TransparentMergedResult(queryResults.get(0));
        }
        if (1 == queryResults.size()) {
            return new IteratorStreamMergedResult(queryResults);
        }
        CursorHeldSQLStatementContext cursorHeldSQLStatementContext = (CursorHeldSQLStatementContext) sqlStatementContext;
        cursorHeldSQLStatementContext.getCursorStatementContext().getSelectStatementContext().setIndexes(getColumnLabelIndexMap(queryResults.get(0)));
        return new FetchStreamMergedResult(queryResults, cursorHeldSQLStatementContext, getSchema(cursorHeldSQLStatementContext, database), connectionContext);
    }
    
    private Map<String, Integer> getColumnLabelIndexMap(final QueryResult queryResult) throws SQLException {
        Map<String, Integer> result = new CaseInsensitiveMap<>();
        for (int i = 0; i < queryResult.getMetaData().getColumnCount(); i++) {
            result.put(SQLUtils.getExactlyValue(queryResult.getMetaData().getColumnLabel(i + 1)), i + 1);
        }
        return result;
    }
    
    private ShardingSphereSchema getSchema(final SQLStatementContext sqlStatementContext, final ShardingSphereDatabase database) {
        String defaultSchemaName = new DatabaseTypeRegistry(sqlStatementContext.getSqlStatement().getDatabaseType()).getDefaultSchemaName(database.getName());
        return sqlStatementContext.getTablesContext().getSchemaName().map(database::getSchema).orElseGet(() -> database.getSchema(defaultSchemaName));
    }
}
