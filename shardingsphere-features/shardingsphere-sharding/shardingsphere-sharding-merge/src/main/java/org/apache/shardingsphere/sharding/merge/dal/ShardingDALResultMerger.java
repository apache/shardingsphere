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
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.infra.merge.engine.merger.ResultMerger;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.sharding.merge.dal.common.SingleLocalDataMergedResult;
import org.apache.shardingsphere.sharding.merge.dal.show.LogicTablesMergedResult;
import org.apache.shardingsphere.sharding.merge.dal.show.ShowCreateTableMergedResult;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.infra.schema.model.schema.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * DAL result merger for Sharding.
 */
@RequiredArgsConstructor
public final class ShardingDALResultMerger implements ResultMerger {
    
    private final ShardingRule shardingRule;
    
    @Override
    public MergedResult merge(final List<QueryResult> queryResults, final SQLStatementContext<?> sqlStatementContext, final PhysicalSchemaMetaData schemaMetaData) throws SQLException {
        SQLStatement dalStatement = sqlStatementContext.getSqlStatement();
        if (dalStatement instanceof MySQLShowDatabasesStatement) {
            return new SingleLocalDataMergedResult(Collections.singletonList(DefaultSchema.LOGIC_NAME));
        }
        if (dalStatement instanceof MySQLShowTablesStatement || dalStatement instanceof MySQLShowTableStatusStatement || dalStatement instanceof MySQLShowIndexStatement) {
            return new LogicTablesMergedResult(shardingRule, sqlStatementContext, schemaMetaData, queryResults);
        }
        if (dalStatement instanceof MySQLShowCreateTableStatement) {
            return new ShowCreateTableMergedResult(shardingRule, sqlStatementContext, schemaMetaData, queryResults);
        }
        return new TransparentMergedResult(queryResults.get(0));
    }
}
