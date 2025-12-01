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
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.RenameTableStatement;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Rename table supported checker for sharding.
 */
public final class ShardingRenameTableSupportedChecker implements SupportedSQLChecker<SQLStatementContext, ShardingRule> {
    
    @Override
    public boolean isCheck(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement() instanceof RenameTableStatement;
    }
    
    @Override
    public void check(final ShardingRule rule, final ShardingSphereDatabase database, final ShardingSphereSchema currentSchema, final SQLStatementContext sqlStatementContext) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getSimpleTables().stream()
                .map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList());
        List<SimpleTableSegment> renameTables = ((RenameTableStatement) sqlStatementContext.getSqlStatement())
                .getRenameTables().stream().map(RenameTableDefinitionSegment::getRenameTable).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(renameTables.isEmpty() || !rule.containsShardingTable(tableNames),
                () -> new UnsupportedShardingOperationException("RENAME TABLE", renameTables.get(0).getTableName().getIdentifier().getValue()));
    }
}
