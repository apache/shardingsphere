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

package org.apache.shardingsphere.sharding.checker.sql.ddl;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.checker.sql.common.ShardingSupportedCommonChecker;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;

/**
 * Drop table supported checker for sharding.
 */
public final class ShardingDropTableSupportedChecker implements SupportedSQLChecker<SQLStatementContext, ShardingRule> {
    
    @Override
    public boolean isCheck(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement() instanceof DropTableStatement;
    }
    
    @Override
    public void check(final ShardingRule rule, final ShardingSphereDatabase database, final ShardingSphereSchema currentSchema, final SQLStatementContext sqlStatementContext) {
        DropTableStatement dropTableStatement = (DropTableStatement) sqlStatementContext.getSqlStatement();
        if (!dropTableStatement.isIfExists()) {
            ShardingSphereSchema schema = sqlStatementContext.getTablesContext().getSchemaName().map(database::getSchema).orElse(currentSchema);
            ShardingSupportedCommonChecker.checkTableExist(schema, sqlStatementContext.getTablesContext().getSimpleTables());
        }
        ShardingSpherePreconditions.checkState(!dropTableStatement.isContainsCascade(), () -> new UnsupportedShardingOperationException("DROP TABLE ... CASCADE",
                sqlStatementContext.getTablesContext().getSimpleTables().iterator().next().getTableName().getIdentifier().getValue()));
    }
}
