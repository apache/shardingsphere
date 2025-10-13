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
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;

import java.util.Optional;

/**
 * Alter table supported checker for sharding.
 */
public final class ShardingAlterTableSupportedChecker implements SupportedSQLChecker<CommonSQLStatementContext, ShardingRule> {
    
    @Override
    public boolean isCheck(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement() instanceof AlterTableStatement;
    }
    
    @Override
    public void check(final ShardingRule rule, final ShardingSphereDatabase database, final ShardingSphereSchema currentSchema, final CommonSQLStatementContext sqlStatementContext) {
        AlterTableStatement sqlStatement = (AlterTableStatement) sqlStatementContext.getSqlStatement();
        Optional<SimpleTableSegment> renameTable = sqlStatement.getRenameTable();
        if (!renameTable.isPresent()) {
            return;
        }
        String newTableName = renameTable.get().getTableName().getIdentifier().getValue();
        // Validate new table name does not conflict with existing sharding tables
        ShardingSpherePreconditions.checkState(!rule.isShardingTable(newTableName),
                () -> new UnsupportedShardingOperationException("ALTER TABLE ... RENAME TO ...", newTableName));
        // Validate new table name does not exist in current schema
        ShardingSpherePreconditions.checkState(!currentSchema.containsTable(newTableName),
                () -> new UnsupportedShardingOperationException("ALTER TABLE ... RENAME TO ...", newTableName));
    }
}
