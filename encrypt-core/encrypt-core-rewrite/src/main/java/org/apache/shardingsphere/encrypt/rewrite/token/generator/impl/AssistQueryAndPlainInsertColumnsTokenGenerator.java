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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.encrypt.strategy.EncryptTable;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.generic.InsertColumnsToken;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Assist query and plain insert columns token generator.
 *
 * @author panjuan
 * @author zhangliang
 */
public final class AssistQueryAndPlainInsertColumnsTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator {
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertSQLStatementContext && sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class).isPresent()
                && !((InsertStatement) sqlStatementContext.getSqlStatement()).useDefaultColumns();
    }
    
    @Override
    public Collection<InsertColumnsToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<InsertColumnsToken> result = new LinkedList<>();
        Optional<EncryptTable> encryptTable = getEncryptRule().findEncryptTable(sqlStatementContext.getTablesContext().getSingleTableName());
        Preconditions.checkState(encryptTable.isPresent());
        for (ColumnSegment each : ((InsertStatement) sqlStatementContext.getSqlStatement()).getColumns()) {
            List<String> columns = getColumns(encryptTable.get(), each);
            if (!columns.isEmpty()) {
                result.add(new InsertColumnsToken(each.getStopIndex() + 1, columns));
            }
        }
        return result;
    }
    
    private List<String> getColumns(final EncryptTable encryptTable, final ColumnSegment columnSegment) {
        List<String> result = new LinkedList<>();
        Optional<String> assistedQueryColumn = encryptTable.findAssistedQueryColumn(columnSegment.getName());
        if (assistedQueryColumn.isPresent()) {
            result.add(assistedQueryColumn.get());
        }
        Optional<String> plainColumn = encryptTable.findPlainColumn(columnSegment.getName());
        if (plainColumn.isPresent()) {
            result.add(plainColumn.get());
        }
        return result;
    }
}
