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

package org.apache.shardingsphere.sharding.merge.mysql;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.merge.dal.DialectShardingDALResultMerger;
import org.apache.shardingsphere.sharding.merge.mysql.type.MySQLShardingLogicTablesMergedResult;
import org.apache.shardingsphere.sharding.merge.mysql.type.MySQLShardingShowCreateTableMergedResult;
import org.apache.shardingsphere.sharding.merge.mysql.type.MySQLShardingShowIndexMergedResult;
import org.apache.shardingsphere.sharding.merge.mysql.type.MySQLShardingShowTableStatusMergedResult;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.database.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.index.MySQLShowIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowTablesStatement;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Sharding DAL result merger for MySQL.
 */
public final class MySQLShardingDALResultMerger implements DialectShardingDALResultMerger {
    
    @Override
    public Optional<MergedResult> merge(final String databaseName, final ShardingRule rule,
                                        final SQLStatementContext sqlStatementContext, final ShardingSphereSchema schema, final List<QueryResult> queryResults) throws SQLException {
        SQLStatement dalStatement = sqlStatementContext.getSqlStatement();
        if (dalStatement instanceof MySQLShowDatabasesStatement) {
            return Optional.of(new LocalDataMergedResult(Collections.singleton(new LocalDataQueryResultRow(databaseName))));
        }
        if (dalStatement instanceof MySQLShowTablesStatement) {
            return Optional.of(new MySQLShardingLogicTablesMergedResult(rule, sqlStatementContext, schema, queryResults));
        }
        if (dalStatement instanceof MySQLShowTableStatusStatement) {
            return Optional.of(new MySQLShardingShowTableStatusMergedResult(rule, sqlStatementContext, schema, queryResults));
        }
        if (dalStatement instanceof MySQLShowIndexStatement) {
            return Optional.of(new MySQLShardingShowIndexMergedResult(rule, sqlStatementContext, schema, queryResults));
        }
        if (dalStatement instanceof MySQLShowCreateTableStatement) {
            return Optional.of(new MySQLShardingShowCreateTableMergedResult(rule, sqlStatementContext, schema, queryResults));
        }
        return Optional.empty();
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
