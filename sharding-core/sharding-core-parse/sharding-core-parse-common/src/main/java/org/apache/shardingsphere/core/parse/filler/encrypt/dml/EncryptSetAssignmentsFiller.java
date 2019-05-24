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

package org.apache.shardingsphere.core.parse.filler.encrypt.dml;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.parse.filler.api.EncryptRuleAwareFiller;
import org.apache.shardingsphere.core.parse.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.context.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.sql.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.complex.ComplexExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.parse.sql.token.impl.EncryptColumnToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.InsertSetAddItemsToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.InsertSetEncryptValueToken;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Set assignments filler for encrypt.
 *
 * @author zhangliang
 */
@Setter
public final class EncryptSetAssignmentsFiller implements SQLSegmentFiller<SetAssignmentsSegment>, EncryptRuleAwareFiller {
    
    private EncryptRule encryptRule;
    
    @Override
    public void fill(final SetAssignmentsSegment sqlSegment, final SQLStatement sqlStatement) {
        if (sqlStatement instanceof InsertStatement) {
            fillInsert(sqlSegment, (InsertStatement) sqlStatement);
        } else if (sqlStatement instanceof UpdateStatement) {
            fillUpdate(sqlSegment, (UpdateStatement) sqlStatement);
        }
    }
    
    private void fillInsert(final SetAssignmentsSegment sqlSegment, final InsertStatement insertStatement) {
        for (AssignmentSegment each : sqlSegment.getAssignments()) {
            insertStatement.getColumnNames().add(each.getColumn().getName());
        }
        InsertValue insertValue = getInsertValue(sqlSegment, insertStatement);
        insertStatement.getValues().add(insertValue);
        insertStatement.setParametersIndex(insertValue.getParametersCount());
        fillWithInsertSetAddItemsToken(insertStatement, sqlSegment);
    }
    
    private InsertValue getInsertValue(final SetAssignmentsSegment sqlSegment, final InsertStatement insertStatement) {
        List<ExpressionSegment> columnValues = new LinkedList<>();
        for (AssignmentSegment each : sqlSegment.getAssignments()) {
            columnValues.add(each.getValue());
            fillWithInsertSetEncryptValueToken(insertStatement, each, each.getValue());
        }
        return new InsertValue(columnValues);
    }
    
    private void fillWithInsertSetEncryptValueToken(final InsertStatement insertStatement, final AssignmentSegment segment, final ExpressionSegment columnValue) {
        Optional<ShardingEncryptor> shardingEncryptor = encryptRule.getEncryptorEngine().getShardingEncryptor(insertStatement.getTables().getSingleTableName(), segment.getColumn().getName());
        if (shardingEncryptor.isPresent() && !(columnValue instanceof ParameterMarkerExpressionSegment)) {
            insertStatement.getSQLTokens().add(new InsertSetEncryptValueToken(segment.getValue().getStartIndex(), segment.getValue().getStopIndex(), segment.getColumn().getName()));
        }
    }
    
    private void fillWithInsertSetAddItemsToken(final InsertStatement insertStatement, final SetAssignmentsSegment sqlSegment) {
        Collection<String> columnNames = getQueryAssistedColumn(insertStatement);
        if (columnNames.isEmpty()) {
            return;
        }
        List<AssignmentSegment> assignments = new ArrayList<>(sqlSegment.getAssignments());
        insertStatement.getSQLTokens().add(new InsertSetAddItemsToken(assignments.get(assignments.size() - 1).getStopIndex() + 1, columnNames));
    }
    
    private Collection<String> getQueryAssistedColumn(final InsertStatement insertStatement) {
        Collection<String> result = new LinkedList<>();
        for (String each : insertStatement.getColumnNames()) {
            Optional<String> assistedColumnName = encryptRule.getEncryptorEngine().getAssistedQueryColumn(insertStatement.getTables().getSingleTableName(), each);
            if (assistedColumnName.isPresent()) {
                result.add(assistedColumnName.get());
            }
        }
        return result;
    }
    
    private void fillUpdate(final SetAssignmentsSegment sqlSegment, final UpdateStatement updateStatement) {
        String tableName = updateStatement.getTables().getSingleTableName();
        for (AssignmentSegment each : sqlSegment.getAssignments()) {
            fillEncryptCondition(each, tableName, updateStatement);
        }
    }
    
    private void fillEncryptCondition(final AssignmentSegment assignment, final String tableName, final UpdateStatement updateStatement) {
        Column column = new Column(assignment.getColumn().getName(), tableName);
        SQLExpression sqlExpression = assignment.getValue() instanceof SimpleExpressionSegment
                ? ((SimpleExpressionSegment) assignment.getValue()).getSQLExpression() : ((ComplexExpressionSegment) assignment.getValue()).getSQLExpression(updateStatement.getLogicSQL());
        updateStatement.getAssignments().put(column, sqlExpression);
        if (encryptRule.getEncryptorEngine().getShardingEncryptor(column.getTableName(), column.getName()).isPresent()) {
            updateStatement.getSQLTokens().add(new EncryptColumnToken(assignment.getColumn().getStartIndex(), assignment.getValue().getStopIndex(), column, false));
        }
    }
}
