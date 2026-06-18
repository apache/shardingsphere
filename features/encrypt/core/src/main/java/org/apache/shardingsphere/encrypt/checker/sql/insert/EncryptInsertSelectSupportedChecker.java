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

package org.apache.shardingsphere.encrypt.checker.sql.insert;

import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.extractor.SQLStatementContextExtractor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;

import java.util.Collection;

/**
 * Insert select supported checker for encrypt.
 */
@HighFrequencyInvocation
public final class EncryptInsertSelectSupportedChecker implements SupportedSQLChecker<InsertStatementContext, EncryptRule> {
    
    @Override
    public boolean isCheck(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext && ((InsertStatementContext) sqlStatementContext).getSqlStatement().getInsertSelect().isPresent();
    }
    
    @Override
    public void check(final EncryptRule rule, final ShardingSphereDatabase database, final ShardingSphereSchema currentSchema, final InsertStatementContext sqlStatementContext) {
        Collection<String> tableNames = SQLStatementContextExtractor.getTableNames(database, sqlStatementContext);
        for (String each : tableNames) {
            ShardingSpherePreconditions.checkState(!rule.findEncryptTable(each).isPresent(), () -> new UnsupportedEncryptSQLException("INSERT ... SELECT ..."));
        }
    }
}
