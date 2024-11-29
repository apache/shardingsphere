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
import org.apache.shardingsphere.infra.binder.context.statement.ddl.AlterIndexStatementContext;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.checker.sql.util.ShardingSupportedCheckUtils;
import org.apache.shardingsphere.sharding.exception.metadata.DuplicateIndexException;
import org.apache.shardingsphere.sharding.exception.metadata.IndexNotExistedException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterIndexStatement;

import java.util.Optional;

/**
 * Alter index supported checker for sharding.
 */
public final class ShardingAlterIndexSupportedChecker implements SupportedSQLChecker<AlterIndexStatementContext, ShardingRule> {
    
    @Override
    public boolean isCheck(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof AlterIndexStatementContext;
    }
    
    @Override
    public void check(final ShardingRule rule, final ShardingSphereDatabase database, final ShardingSphereSchema currentSchema, final AlterIndexStatementContext sqlStatementContext) {
        AlterIndexStatement alterIndexStatement = sqlStatementContext.getSqlStatement();
        Optional<IndexSegment> index = alterIndexStatement.getIndex();
        ShardingSphereSchema schema = index.flatMap(optional -> optional.getOwner().map(owner -> database.getSchema(owner.getIdentifier().getValue()))).orElse(currentSchema);
        if (index.isPresent() && !ShardingSupportedCheckUtils.isSchemaContainsIndex(schema, index.get())) {
            throw new IndexNotExistedException(index.get().getIndexName().getIdentifier().getValue());
        }
        Optional<IndexSegment> renameIndex = alterIndexStatement.getRenameIndex();
        if (renameIndex.isPresent() && ShardingSupportedCheckUtils.isSchemaContainsIndex(schema, renameIndex.get())) {
            throw new DuplicateIndexException(renameIndex.get().getIndexName().getIdentifier().getValue());
        }
    }
}
