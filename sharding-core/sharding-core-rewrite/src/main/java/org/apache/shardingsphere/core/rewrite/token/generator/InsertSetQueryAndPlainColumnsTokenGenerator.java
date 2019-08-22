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
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.encrypt.statement.EncryptInsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingInsertOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertSetQueryAndPlainColumnsToken;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.ArrayList;
import java.util.Collection;
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
        if (0 == encryptRule.getAssistedQueryAndPlainColumnCount(optimizedStatement.getTables().getSingleTableName())) {
            return Optional.absent();
        }
        List<String> assistedQueryAndPlainColumnNames = getAssistedQueryAndPlainColumnNames(optimizedStatement, encryptRule);
        return Optional.of(new InsertSetQueryAndPlainColumnsToken(
                getStartIndex(optimizedStatement), assistedQueryAndPlainColumnNames, getAssistedQueryAndPlainColumnValues(optimizedStatement, assistedQueryAndPlainColumnNames)));
    }
    
    private List<String> getAssistedQueryAndPlainColumnNames(final InsertOptimizedStatement optimizedStatement, final EncryptRule encryptRule) {
        List<String> result = new LinkedList<>();
        result.addAll(getAssistedQueryColumnNames(optimizedStatement, encryptRule));
        result.addAll(getPlainColumnNames(optimizedStatement, encryptRule));
        return result;
    }
    
    private List<String> getAssistedQueryColumnNames(final InsertOptimizedStatement optimizedStatement, final EncryptRule encryptRule) {
        List<String> result = new LinkedList<>();
        Collection<String> columnNames = optimizedStatement instanceof ShardingInsertOptimizedStatement
                ? ((ShardingInsertOptimizedStatement) optimizedStatement).getInsertColumns().getRegularColumnNames()
                : ((EncryptInsertOptimizedStatement) optimizedStatement).getColumnNames();
        for (String each : columnNames) {
            Optional<String> assistedQueryColumn = encryptRule.getAssistedQueryColumn(optimizedStatement.getTables().getSingleTableName(), each);
            if (assistedQueryColumn.isPresent()) {
                result.add(assistedQueryColumn.get());
            }
        }
        return result;
    }
    
    private List<String> getPlainColumnNames(final InsertOptimizedStatement optimizedStatement, final EncryptRule encryptRule) {
        List<String> result = new LinkedList<>();
        Collection<String> columnNames = optimizedStatement instanceof ShardingInsertOptimizedStatement
                ? ((ShardingInsertOptimizedStatement) optimizedStatement).getInsertColumns().getRegularColumnNames()
                : ((EncryptInsertOptimizedStatement) optimizedStatement).getColumnNames();
        for (String each : columnNames) {
            Optional<String> plainColumn = encryptRule.getPlainColumn(optimizedStatement.getTables().getSingleTableName(), each);
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
    
    private List<ExpressionSegment> getAssistedQueryAndPlainColumnValues(final InsertOptimizedStatement optimizedStatement, final Collection<String> assistedQueryAndPlainColumnNames) {
        List<ExpressionSegment> result = new LinkedList<>();
        for (String each : assistedQueryAndPlainColumnNames) {
            result.add(optimizedStatement.getOptimizedInsertValues().get(0).getValueExpression(each));
        }
        return result;
    }
}
