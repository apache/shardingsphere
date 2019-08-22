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
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingInsertOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertSetGeneratedKeyColumnToken;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Insert set add items token generator.
 *
 * @author panjuan
 */
public final class InsertSetGeneratedKeyColumnTokenGenerator implements OptionalSQLTokenGenerator<ShardingRule> {
    
    @Override
    public Optional<InsertSetGeneratedKeyColumnToken> generateSQLToken(
            final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final ShardingRule shardingRule, final boolean isQueryWithCipherColumn) {
        Optional<SetAssignmentsSegment> setAssignmentsSegment = optimizedStatement.getSQLStatement().findSQLSegment(SetAssignmentsSegment.class);
        if (!(optimizedStatement instanceof ShardingInsertOptimizedStatement && setAssignmentsSegment.isPresent())) {
            return Optional.absent();
        }
        return createInsertSetAddGeneratedKeyToken((ShardingInsertOptimizedStatement) optimizedStatement, shardingRule, setAssignmentsSegment.get());
    }
    
    private Optional<InsertSetGeneratedKeyColumnToken> createInsertSetAddGeneratedKeyToken(
            final ShardingInsertOptimizedStatement optimizedStatement, final ShardingRule shardingRule, final SetAssignmentsSegment segment) {
        Optional<String> generatedKeyColumn = getGeneratedKeyColumn(optimizedStatement, shardingRule);
        if (generatedKeyColumn.isPresent()) {
            return Optional.of(createInsertSetAddGeneratedKeyToken(optimizedStatement, generatedKeyColumn.get(), new ArrayList<>(segment.getAssignments())));
        }
        return Optional.absent();
    }
    
    private InsertSetGeneratedKeyColumnToken createInsertSetAddGeneratedKeyToken(
            final InsertOptimizedStatement optimizedStatement, final String generatedKeyColumn, final List<AssignmentSegment> assignments) {
        return new InsertSetGeneratedKeyColumnToken(
                assignments.get(assignments.size() - 1).getStopIndex() + 1, generatedKeyColumn, optimizedStatement.getOptimizedInsertValues().get(0).getValueExpression(generatedKeyColumn));
    }
    
    private Optional<String> getGeneratedKeyColumn(final ShardingInsertOptimizedStatement optimizedStatement, final ShardingRule shardingRule) {
        Optional<String> generateKeyColumn = shardingRule.findGenerateKeyColumnName(optimizedStatement.getTables().getSingleTableName());
        return generateKeyColumn.isPresent() && !optimizedStatement.getInsertColumns().getRegularColumnNames().contains(generateKeyColumn.get()) ? generateKeyColumn : Optional.<String>absent();
    }
}
