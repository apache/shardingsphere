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
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertSetCipherColumnToken;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Insert set cipher column token generator.
 *
 * @author panjuan
 */
public final class InsertSetCipherColumnTokenGenerator implements CollectionSQLTokenGenerator<EncryptRule> {
    
    @Override
    public Collection<InsertSetCipherColumnToken> generateSQLTokens(
            final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final boolean isQueryWithCipherColumn) {
        if (!isNeedToGenerateSQLToken(optimizedStatement)) {
            return Collections.emptyList();
        }
        return createInsertSetEncryptValueTokens((InsertOptimizedStatement) optimizedStatement, encryptRule);
    }
    
    private boolean isNeedToGenerateSQLToken(final OptimizedStatement optimizedStatement) {
        Optional<SetAssignmentsSegment> setAssignmentsSegment = optimizedStatement.getSQLStatement().findSQLSegment(SetAssignmentsSegment.class);
        return optimizedStatement.getSQLStatement() instanceof InsertStatement && setAssignmentsSegment.isPresent();
    }
    
    private Collection<InsertSetCipherColumnToken> createInsertSetEncryptValueTokens(final InsertOptimizedStatement optimizedStatement, final EncryptRule encryptRule) {
        Optional<SetAssignmentsSegment> setAssignmentsSegment = optimizedStatement.getSQLStatement().findSQLSegment(SetAssignmentsSegment.class);
        if (!setAssignmentsSegment.isPresent()) {
            return Collections.emptyList();
        }
        Collection<InsertSetCipherColumnToken> result = new LinkedList<>();
        for (AssignmentSegment each : setAssignmentsSegment.get().getAssignments()) {
            Optional<InsertSetCipherColumnToken> insertSetEncryptValueToken = createInsertSetEncryptValueToken(optimizedStatement, encryptRule, each);
            if (insertSetEncryptValueToken.isPresent()) {
                result.add(insertSetEncryptValueToken.get());
            }
        }
        return result;
    }
    
    private Optional<InsertSetCipherColumnToken> createInsertSetEncryptValueToken(final InsertOptimizedStatement optimizedStatement, final EncryptRule encryptRule, final AssignmentSegment segment) {
        String tableName = optimizedStatement.getTables().getSingleTableName();
        Optional<ShardingEncryptor> shardingEncryptor = encryptRule.getShardingEncryptor(tableName, segment.getColumn().getName());
        if (shardingEncryptor.isPresent()) {
            String cipherColumnName = encryptRule.getCipherColumn(tableName, segment.getColumn().getName());
            ExpressionSegment cipherColumnValue = getCipherColumnValue(optimizedStatement, segment);
            return Optional.of(new InsertSetCipherColumnToken(segment.getStartIndex(), segment.getStopIndex(), cipherColumnName, cipherColumnValue));
        }
        return Optional.absent();
    }
    
    private ExpressionSegment getCipherColumnValue(final InsertOptimizedStatement optimizedStatement, final AssignmentSegment assignmentSegment) {
        return assignmentSegment.getValue() instanceof ParameterMarkerExpressionSegment
                ? assignmentSegment.getValue() : optimizedStatement.getOptimizedInsertValues().get(0).getValueExpression(assignmentSegment.getColumn().getName());
    }
}
