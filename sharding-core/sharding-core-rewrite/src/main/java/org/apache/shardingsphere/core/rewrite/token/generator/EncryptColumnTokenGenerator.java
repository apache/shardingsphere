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

import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.token.impl.EncryptColumnToken;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Encrypt column token generator.
 *
 * @author panjuan
 */
public final class EncryptColumnTokenGenerator implements CollectionSQLTokenGenerator<ShardingRule> {
    
    @Override
    public Collection<EncryptColumnToken> generateSQLTokens(final SQLStatement sqlStatement, final ShardingRule shardingRule) {
        Collection<EncryptColumnToken> result = new LinkedList<>();
        for (SQLSegment each : sqlStatement.getSqlSegments()) {
            if (each instanceof SetAssignmentsSegment) {
                result.addAll(createEncryptColumnTokensFromSetAssignment(sqlStatement, shardingRule, (SetAssignmentsSegment) each));
            }
        }
        result.addAll(createEncryptColumnTokensFromWhereCondition(sqlStatement));
        return result;
    }
    
    private Collection<EncryptColumnToken> createEncryptColumnTokensFromSetAssignment(final SQLStatement sqlStatement, final ShardingRule shardingRule, final SetAssignmentsSegment segment) {
        Collection<EncryptColumnToken> result = new LinkedList<>();
        for (AssignmentSegment each : segment.getAssignments()) {
            Column column = new Column(each.getColumn().getName(), sqlStatement.getTables().getSingleTableName());
            if (shardingRule.getShardingEncryptorEngine().getShardingEncryptor(column.getTableName(), column.getName()).isPresent()) {
                result.add(new EncryptColumnToken(each.getColumn().getStartIndex(), each.getValue().getStopIndex(), column, false));
            }
        }
        return result;
    }
    
    private Collection<EncryptColumnToken> createEncryptColumnTokensFromWhereCondition(final SQLStatement sqlStatement) {
        Collection<EncryptColumnToken> result = new LinkedList<>();
        if (sqlStatement.getEncryptCondition().getOrConditions().isEmpty()) {
            return result;
        }
        for (Condition each : sqlStatement.getEncryptCondition().getOrConditions().get(0).getConditions()) {
            result.add(new EncryptColumnToken(each.getColumnSegment().getStartIndex(), each.getColumnSegment().getStopIndex(), each.getColumn(), true));
        }
        return result;
    }
}
