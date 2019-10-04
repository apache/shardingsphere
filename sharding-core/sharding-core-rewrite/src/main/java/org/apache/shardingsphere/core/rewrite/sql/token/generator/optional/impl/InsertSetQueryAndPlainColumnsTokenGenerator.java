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

package org.apache.shardingsphere.core.rewrite.sql.token.generator.optional.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.segment.insert.expression.DerivedSimpleExpressionSegment;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.rewrite.constant.EncryptDerivedColumnType;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.optional.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.impl.InsertSetQueryAndPlainColumnsToken;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert set add items token generator.
 *
 * @author panjuan
 */
@Setter
public final class InsertSetQueryAndPlainColumnsTokenGenerator implements OptionalSQLTokenGenerator, EncryptRuleAware {
    
    private EncryptRule encryptRule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        Optional<SetAssignmentsSegment> setAssignmentsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(SetAssignmentsSegment.class);
        return sqlStatementContext instanceof InsertSQLStatementContext && setAssignmentsSegment.isPresent()
                && !encryptRule.getAssistedQueryAndPlainColumns(sqlStatementContext.getTablesContext().getSingleTableName()).isEmpty();
    }
    
    @Override
    public InsertSetQueryAndPlainColumnsToken generateSQLToken(final SQLStatementContext sqlStatementContext) {
        return new InsertSetQueryAndPlainColumnsToken(getStartIndex((InsertSQLStatementContext) sqlStatementContext), 
                getEncryptDerivedColumnNames(sqlStatementContext.getTablesContext().getSingleTableName()), getEncryptDerivedValues((InsertSQLStatementContext) sqlStatementContext));
    }
    
    private List<String> getEncryptDerivedColumnNames(final String tableName) {
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
        if (!encryptTable.isPresent()) {
            return Collections.emptyList();
        }
        List<String> result = new LinkedList<>();
        for (String each : encryptTable.get().getLogicColumns()) {
            Optional<String> assistedQueryColumn = encryptRule.findAssistedQueryColumn(tableName, each);
            if (assistedQueryColumn.isPresent()) {
                result.add(assistedQueryColumn.get());
            }
            Optional<String> plainColumn = encryptRule.findPlainColumn(tableName, each);
            if (plainColumn.isPresent()) {
                result.add(plainColumn.get());
            }
        }
        return result;
    }
    
    private int getStartIndex(final InsertSQLStatementContext insertSQLStatementContext) {
        Optional<SetAssignmentsSegment> setAssignmentsSegment = insertSQLStatementContext.getSqlStatement().findSQLSegment(SetAssignmentsSegment.class);
        Preconditions.checkState(setAssignmentsSegment.isPresent());
        return setAssignmentsSegment.get().getStopIndex() + 1;
    }
    
    private List<ExpressionSegment> getEncryptDerivedValues(final InsertSQLStatementContext insertSQLStatementContext) {
        List<ExpressionSegment> result = new LinkedList<>();
        for (ExpressionSegment each : insertSQLStatementContext.getInsertValueContexts().get(0).getValueExpressions()) {
            if (each instanceof DerivedSimpleExpressionSegment && EncryptDerivedColumnType.ENCRYPT.equals(((DerivedSimpleExpressionSegment) each).getType())) { 
                result.add(each);
            }
        }
        return result;
    }
}
