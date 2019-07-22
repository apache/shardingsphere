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
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertSetAddAssistedColumnsToken;
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
public final class InsertSetAddAssistedColumnsTokenGenerator implements OptionalSQLTokenGenerator<EncryptRule> {
    
    @Override
    public Optional<InsertSetAddAssistedColumnsToken> generateSQLToken(
            final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final boolean isQueryWithCipherColumn) {
        Optional<SetAssignmentsSegment> setAssignmentsSegment = optimizedStatement.getSQLStatement().findSQLSegment(SetAssignmentsSegment.class);
        if (!(optimizedStatement instanceof InsertOptimizedStatement && setAssignmentsSegment.isPresent())) {
            return Optional.absent();
        }
        return createInsertSetAddItemsToken((InsertOptimizedStatement) optimizedStatement, encryptRule, setAssignmentsSegment.get());
    }
    
    private Optional<InsertSetAddAssistedColumnsToken> createInsertSetAddItemsToken(
            final InsertOptimizedStatement optimizedStatement, final EncryptRule encryptRule, final SetAssignmentsSegment segment) {
        List<String> columnNames = getQueryAssistedColumnNames(optimizedStatement, encryptRule);
        if (columnNames.isEmpty()) {
            return Optional.absent();
        }
        List<AssignmentSegment> assignments = new ArrayList<>(segment.getAssignments());
        return Optional.of(new InsertSetAddAssistedColumnsToken(
                assignments.get(assignments.size() - 1).getStopIndex() + 1, columnNames, getQueryAssistedColumnValues(columnNames, optimizedStatement)));
    }
    
    private List<String> getQueryAssistedColumnNames(final InsertOptimizedStatement optimizedStatement, final EncryptRule encryptRule) {
        List<String> result = new LinkedList<>();
        for (String each : optimizedStatement.getInsertColumns().getRegularColumnNames()) {
            Optional<String> assistedColumnName = encryptRule.getEncryptEngine().getAssistedQueryColumn(optimizedStatement.getTables().getSingleTableName(), each);
            if (assistedColumnName.isPresent()) {
                result.add(assistedColumnName.get());
            }
        }
        return result;
    }
    
    private List<ExpressionSegment> getQueryAssistedColumnValues(final Collection<String> columnNames, final InsertOptimizedStatement optimizedStatement) {
        List<ExpressionSegment> result = new LinkedList<>();
        for (String each : columnNames) {
            result.add(optimizedStatement.getUnits().get(0).getColumnSQLExpression(each));
        }
        return result;
    }
}
