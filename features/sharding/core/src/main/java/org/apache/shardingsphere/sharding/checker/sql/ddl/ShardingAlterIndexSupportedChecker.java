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
import org.apache.shardingsphere.infra.exception.kernel.metadata.DuplicateIndexException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.IndexNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.checker.sql.util.ShardingSupportedCheckUtils;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.AlterIndexStatement;

import java.util.Optional;

/**
 * Alter index supported checker for sharding.
 */
public final class ShardingAlterIndexSupportedChecker implements SupportedSQLChecker<CommonSQLStatementContext, ShardingRule> {
    
    @Override
    public boolean isCheck(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement() instanceof AlterIndexStatement;
    }
    
    @Override
    public void check(final ShardingRule rule, final ShardingSphereDatabase database, final ShardingSphereSchema currentSchema, final CommonSQLStatementContext sqlStatementContext) {
        AlterIndexStatement alterIndexStatement = (AlterIndexStatement) sqlStatementContext.getSqlStatement();
        Optional<IndexSegment> index = alterIndexStatement.getIndex();
        ShardingSphereSchema schema = index.flatMap(optional -> optional.getOwner().map(owner -> database.getSchema(owner.getIdentifier().getValue()))).orElse(currentSchema);
        if (index.isPresent() && !ShardingSupportedCheckUtils.isSchemaContainsIndex(schema, index.get())) {
            throw new IndexNotFoundException(index.get().getIndexName().getIdentifier().getValue());
        }
        Optional<IndexSegment> renameIndex = alterIndexStatement.getRenameIndex();
        if (renameIndex.isPresent() && ShardingSupportedCheckUtils.isSchemaContainsIndex(schema, renameIndex.get())) {
            throw new DuplicateIndexException(renameIndex.get().getIndexName().getIdentifier().getValue());
        }
    }
}
