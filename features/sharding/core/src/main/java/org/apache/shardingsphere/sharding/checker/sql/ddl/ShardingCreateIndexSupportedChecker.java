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
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CreateIndexStatementContext;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.checker.sql.common.ShardingSupportedCommonChecker;
import org.apache.shardingsphere.infra.exception.kernel.metadata.DuplicateIndexException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateIndexStatement;

import java.util.Collections;

/**
 * Create index supported checker for sharding.
 */
public final class ShardingCreateIndexSupportedChecker implements SupportedSQLChecker<CreateIndexStatementContext, ShardingRule> {
    
    @Override
    public boolean isCheck(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof CreateIndexStatementContext;
    }
    
    @Override
    public void check(final ShardingRule rule, final ShardingSphereDatabase database, final ShardingSphereSchema currentSchema, final CreateIndexStatementContext sqlStatementContext) {
        CreateIndexStatement createIndexStatement = sqlStatementContext.getSqlStatement();
        if (createIndexStatement.isIfNotExists()) {
            return;
        }
        ShardingSphereSchema schema = sqlStatementContext.getTablesContext().getSchemaName().map(database::getSchema).orElse(currentSchema);
        ShardingSupportedCommonChecker.checkTableExist(schema, Collections.singleton(createIndexStatement.getTable()));
        String tableName = createIndexStatement.getTable().getTableName().getIdentifier().getValue();
        String indexName = sqlStatementContext.getIndexes().stream().map(each -> each.getIndexName().getIdentifier().getValue()).findFirst().orElse(null);
        ShardingSpherePreconditions.checkState(!schema.containsIndex(tableName, indexName), () -> new DuplicateIndexException(indexName));
    }
}
