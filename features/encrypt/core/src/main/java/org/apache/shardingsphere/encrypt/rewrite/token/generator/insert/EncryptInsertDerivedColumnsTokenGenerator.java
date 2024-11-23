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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.insert;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.InsertColumnsToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert derived columns token generator for encrypt.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
@Setter
public final class EncryptInsertDerivedColumnsTokenGenerator implements CollectionSQLTokenGenerator<InsertStatementContext> {
    
    private final EncryptRule rule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext && ((InsertStatementContext) sqlStatementContext).containsInsertColumns();
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final InsertStatementContext insertStatementContext) {
        Collection<SQLToken> result = new LinkedList<>();
        EncryptTable encryptTable = rule.getEncryptTable(insertStatementContext.getSqlStatement().getTable().map(optional -> optional.getTableName().getIdentifier().getValue()).orElse(""));
        for (ColumnSegment each : insertStatementContext.getSqlStatement().getColumns()) {
            List<String> derivedColumnNames = getDerivedColumnNames(encryptTable, each);
            if (!derivedColumnNames.isEmpty()) {
                result.add(new InsertColumnsToken(each.getStopIndex() + 1, derivedColumnNames));
            }
        }
        return result;
    }
    
    private List<String> getDerivedColumnNames(final EncryptTable encryptTable, final ColumnSegment columnSegment) {
        String columnName = columnSegment.getIdentifier().getValue();
        if (!encryptTable.isEncryptColumn(columnName)) {
            return Collections.emptyList();
        }
        List<String> result = new LinkedList<>();
        EncryptColumn encryptColumn = encryptTable.getEncryptColumn(columnName);
        encryptColumn.getAssistedQuery().ifPresent(optional -> result.add(optional.getName()));
        encryptColumn.getLikeQuery().ifPresent(optional -> result.add(optional.getName()));
        return result;
    }
}
