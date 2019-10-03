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
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.rewrite.constant.EncryptDerivedColumnType;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.optional.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.impl.InsertSetQueryAndPlainColumnsToken;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;

import java.util.ArrayList;
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
    public Optional<InsertSetQueryAndPlainColumnsToken> generateSQLToken(final SQLStatementContext sqlStatementContext) {
        if (!isNeedToGenerateSQLToken(sqlStatementContext)) {
            return Optional.absent();
        }
        return createInsertSetAddItemsToken((InsertSQLStatementContext) sqlStatementContext);
    }
    
    private boolean isNeedToGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        Optional<SetAssignmentsSegment> setAssignmentsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(SetAssignmentsSegment.class);
        return sqlStatementContext instanceof InsertSQLStatementContext && setAssignmentsSegment.isPresent();
    }
    
    private Optional<InsertSetQueryAndPlainColumnsToken> createInsertSetAddItemsToken(final InsertSQLStatementContext insertSQLStatementContext) {
        String tableName = insertSQLStatementContext.getTablesContext().getSingleTableName();
        if (encryptRule.getAssistedQueryAndPlainColumns(tableName).isEmpty()) {
            return Optional.absent();
        }
        List<String> encryptDerivedColumnNames = getEncryptDerivedColumnNames(tableName);
        return Optional.of(new InsertSetQueryAndPlainColumnsToken(getStartIndex(insertSQLStatementContext), encryptDerivedColumnNames, getEncryptDerivedValues(insertSQLStatementContext)));
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
        List<AssignmentSegment> assignments = new ArrayList<>(setAssignmentsSegment.get().getAssignments());
        return assignments.get(assignments.size() - 1).getStopIndex() + 1;
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
