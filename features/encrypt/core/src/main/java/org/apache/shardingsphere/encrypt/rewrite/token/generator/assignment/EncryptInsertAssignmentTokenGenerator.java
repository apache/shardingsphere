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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.assignment;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.aware.DatabaseAware;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;

import java.util.Collection;

/**
 * Insert assignment generator for encrypt.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
@Setter
public final class EncryptInsertAssignmentTokenGenerator implements CollectionSQLTokenGenerator<InsertStatementContext>, DatabaseAware {
    
    private final EncryptRule rule;
    
    private ShardingSphereDatabase database;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext && (((InsertStatementContext) sqlStatementContext).getSqlStatement()).getSetAssignment().isPresent()
                && rule.findEncryptTable(((TableAvailable) sqlStatementContext).getTablesContext().getSimpleTables().iterator().next().getTableName().getIdentifier().getValue()).isPresent();
    }
    
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public Collection<SQLToken> generateSQLTokens(final InsertStatementContext sqlStatementContext) {
        return new EncryptAssignmentTokenGenerator(rule, database.getName(), sqlStatementContext.getDatabaseType()).generateSQLTokens(
                sqlStatementContext.getTablesContext(), sqlStatementContext.getSqlStatement().getSetAssignment().get());
    }
}
