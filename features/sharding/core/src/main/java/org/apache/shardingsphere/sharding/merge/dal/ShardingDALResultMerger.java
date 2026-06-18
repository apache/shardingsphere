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

package org.apache.shardingsphere.sharding.merge.dal;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.engine.merger.ResultMerger;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * DAL result merger for Sharding.
 */
@RequiredArgsConstructor
public final class ShardingDALResultMerger implements ResultMerger {
    
    private final String databaseName;
    
    private final ShardingRule rule;
    
    @Override
    public MergedResult merge(final List<QueryResult> queryResults, final SQLStatementContext sqlStatementContext,
                              final ShardingSphereDatabase database, final ConnectionContext connectionContext) throws SQLException {
        ShardingSphereSchema schema = getSchema(sqlStatementContext, database);
        Optional<DialectShardingDALResultMerger> dialectResultMerger = DatabaseTypedSPILoader.findService(
                DialectShardingDALResultMerger.class, sqlStatementContext.getSqlStatement().getDatabaseType());
        if (dialectResultMerger.isPresent()) {
            Optional<MergedResult> mergedResult = dialectResultMerger.get().merge(databaseName, rule, sqlStatementContext, schema, queryResults);
            if (mergedResult.isPresent()) {
                return mergedResult.get();
            }
        }
        return new TransparentMergedResult(queryResults.get(0));
    }
    
    private ShardingSphereSchema getSchema(final SQLStatementContext sqlStatementContext, final ShardingSphereDatabase database) {
        String defaultSchemaName = new DatabaseTypeRegistry(sqlStatementContext.getSqlStatement().getDatabaseType()).getDefaultSchemaName(database.getName());
        return sqlStatementContext.getTablesContext().getSchemaName().map(database::getSchema).orElseGet(() -> database.getSchema(defaultSchemaName));
    }
}
