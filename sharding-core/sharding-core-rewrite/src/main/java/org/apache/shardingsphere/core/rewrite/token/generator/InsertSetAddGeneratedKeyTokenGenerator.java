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
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.token.impl.InsertSetAddGeneratedKeyToken;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Insert set add items token generator.
 *
 * @author panjuan
 */
public final class InsertSetAddGeneratedKeyTokenGenerator implements OptionalSQLTokenGenerator<ShardingRule> {
    
    @Override
    public Optional<InsertSetAddGeneratedKeyToken> generateSQLToken(final SQLStatement sqlStatement, final ShardingRule shardingRule) {
        Optional<SetAssignmentsSegment> setAssignmentsSegment = sqlStatement.findSQLSegment(SetAssignmentsSegment.class);
        if (!(sqlStatement instanceof InsertStatement && setAssignmentsSegment.isPresent())) {
            return Optional.absent();
        }
        return createInsertSetAddGeneratedKeyToken((InsertStatement) sqlStatement, shardingRule, setAssignmentsSegment.get());
    }
    
    private Optional<InsertSetAddGeneratedKeyToken> createInsertSetAddGeneratedKeyToken(final InsertStatement insertStatement, final ShardingRule shardingRule, final SetAssignmentsSegment segment) {
        Optional<String> generatedKeyColumn = getGeneratedKeyColumn(insertStatement, shardingRule);
        if (generatedKeyColumn.isPresent()) {
            List<AssignmentSegment> assignments = new ArrayList<>(segment.getAssignments());
            return Optional.of(new InsertSetAddGeneratedKeyToken(assignments.get(assignments.size() - 1).getStopIndex() + 1, generatedKeyColumn.get()));
        }
        return Optional.absent();
    }
    
    private Optional<String> getGeneratedKeyColumn(final InsertStatement insertStatement, final ShardingRule shardingRule) {
        String tableName = insertStatement.getTables().getSingleTableName();
        Optional<String> generateKeyColumn = shardingRule.findGenerateKeyColumnName(tableName);
        return generateKeyColumn.isPresent() && !insertStatement.getColumnNames().contains(generateKeyColumn.get()) ? generateKeyColumn : Optional.<String>absent();
    }
}
