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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.impl;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.InsertColumnsToken;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Assist query and plain insert columns token generator.
 */
public final class AssistQueryAndPlainInsertColumnsTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator<InsertStatementContext> {
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext instanceof InsertStatementContext) {
            return (((InsertStatementContext) sqlStatementContext).getSqlStatement()).getInsertColumns().isPresent() && !((InsertStatement) sqlStatementContext.getSqlStatement()).useDefaultColumns();
        }
        return false;
    }
    
    @Override
    public Collection<InsertColumnsToken> generateSQLTokens(final InsertStatementContext insertStatementContext) {
        Collection<InsertColumnsToken> result = new LinkedList<>();
        Optional<EncryptTable> encryptTable = getEncryptRule().findEncryptTable(insertStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue());
        Preconditions.checkState(encryptTable.isPresent());
        for (ColumnSegment each : insertStatementContext.getSqlStatement().getColumns()) {
            List<String> columns = getColumns(encryptTable.get(), each);
            if (!columns.isEmpty()) {
                result.add(new InsertColumnsToken(each.getStopIndex() + 1, columns));
            }
        }
        return result;
    }
    
    private List<String> getColumns(final EncryptTable encryptTable, final ColumnSegment columnSegment) {
        List<String> result = new LinkedList<>();
        encryptTable.findAssistedQueryColumn(columnSegment.getIdentifier().getValue()).ifPresent(result::add);
        encryptTable.findPlainColumn(columnSegment.getIdentifier().getValue()).ifPresent(result::add);
        return result;
    }
}
