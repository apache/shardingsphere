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

package org.apache.shardingsphere.core.rewrite.token.generator;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.optimize.api.segment.expression.DerivedSimpleExpressionSegment;
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.encrypt.constant.EncryptDerivedColumnType;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.rewrite.builder.parameter.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertSetQueryAndPlainColumnsToken;
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
public final class InsertSetQueryAndPlainColumnsTokenGenerator implements OptionalSQLTokenGenerator<EncryptRule> {
    
    @Override
    public Optional<InsertSetQueryAndPlainColumnsToken> generateSQLToken(
            final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final boolean isQueryWithCipherColumn) {
        if (!isNeedToGenerateSQLToken(optimizedStatement)) {
            return Optional.absent();
        }
        return createInsertSetAddItemsToken((InsertOptimizedStatement) optimizedStatement, encryptRule);
    }
    
    private boolean isNeedToGenerateSQLToken(final OptimizedStatement optimizedStatement) {
        Optional<SetAssignmentsSegment> setAssignmentsSegment = optimizedStatement.getSQLStatement().findSQLSegment(SetAssignmentsSegment.class);
        return optimizedStatement instanceof InsertOptimizedStatement && setAssignmentsSegment.isPresent();
    }
    
    private Optional<InsertSetQueryAndPlainColumnsToken> createInsertSetAddItemsToken(final InsertOptimizedStatement optimizedStatement, final EncryptRule encryptRule) {
        String tableName = optimizedStatement.getTables().getSingleTableName();
        if (encryptRule.getAssistedQueryAndPlainColumns(tableName).isEmpty()) {
            return Optional.absent();
        }
        List<String> encryptDerivedColumnNames = getEncryptDerivedColumnNames(tableName, encryptRule);
        return Optional.of(new InsertSetQueryAndPlainColumnsToken(getStartIndex(optimizedStatement), encryptDerivedColumnNames, getEncryptDerivedValues(optimizedStatement)));
    }
    
    private List<String> getEncryptDerivedColumnNames(final String tableName, final EncryptRule encryptRule) {
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
    
    private int getStartIndex(final InsertOptimizedStatement optimizedStatement) {
        Optional<SetAssignmentsSegment> setAssignmentsSegment = optimizedStatement.getSQLStatement().findSQLSegment(SetAssignmentsSegment.class);
        Preconditions.checkState(setAssignmentsSegment.isPresent());
        List<AssignmentSegment> assignments = new ArrayList<>(setAssignmentsSegment.get().getAssignments());
        return assignments.get(assignments.size() - 1).getStopIndex() + 1;
    }
    
    private List<ExpressionSegment> getEncryptDerivedValues(final InsertOptimizedStatement optimizedStatement) {
        List<ExpressionSegment> result = new LinkedList<>();
        for (ExpressionSegment each : optimizedStatement.getInsertValues().get(0).getValueExpressions()) {
            if (each instanceof DerivedSimpleExpressionSegment && EncryptDerivedColumnType.ENCRYPT.equals(((DerivedSimpleExpressionSegment) each).getType())) { 
                result.add(each);
            }
        }
        return result;
    }
}
