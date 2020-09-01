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
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Insert cipher column name token generator.
 */
public final class InsertCipherNameTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator<InsertStatementContext> {
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext instanceof InsertStatementContext)) {
            return false;
        }
        Optional<InsertColumnsSegment> insertColumnsSegment = (((InsertStatementContext) sqlStatementContext).getSqlStatement()).getInsertColumns();
        return insertColumnsSegment.isPresent() && !insertColumnsSegment.get().getColumns().isEmpty();
    }
    
    @Override
    public Collection<SubstitutableColumnNameToken> generateSQLTokens(final InsertStatementContext insertStatementContext) {
        Optional<InsertColumnsSegment> sqlSegment = insertStatementContext.getSqlStatement().getInsertColumns();
        Preconditions.checkState(sqlSegment.isPresent());
        Map<String, String> logicAndCipherColumns = getEncryptRule().getLogicAndCipherColumns(insertStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue());
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        for (ColumnSegment each : sqlSegment.get().getColumns()) {
            if (logicAndCipherColumns.containsKey(each.getIdentifier().getValue())) {
                result.add(new SubstitutableColumnNameToken(each.getStartIndex(), each.getStopIndex(), logicAndCipherColumns.get(each.getIdentifier().getValue())));
            }
        }
        return result;
    }
}
