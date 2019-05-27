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

package org.apache.shardingsphere.core.parse.filler.sharding.dml.update;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.exception.SQLParsingException;
import org.apache.shardingsphere.core.parse.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.filler.api.ShardingRuleAwareFiller;
import org.apache.shardingsphere.core.parse.filler.api.ShardingTableMetaDataAwareFiller;
import org.apache.shardingsphere.core.parse.sql.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.sql.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.parse.sql.token.impl.EncryptColumnToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.InsertSetAddItemsToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.InsertSetEncryptValueToken;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Set assignments filler.
 *
 * @author zhangliang
 */
@Setter
public final class ShardingSetAssignmentsFiller implements SQLSegmentFiller<SetAssignmentsSegment>, ShardingRuleAwareFiller, ShardingTableMetaDataAwareFiller {
    
    private ShardingRule shardingRule;
    
    private ShardingTableMetaData shardingTableMetaData;
    
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
        int columnCount = getColumnCountExcludeAssistedQueryColumns(insertStatement);
        if (sqlSegment.getAssignments().size() != columnCount) {
            throw new SQLParsingException("INSERT INTO column size mismatch value size.");
        }
        AndCondition andCondition = new AndCondition();
        Iterator<String> columnNames = insertStatement.getColumnNames().iterator();
        List<ExpressionSegment> columnValues = new LinkedList<>();
        for (AssignmentSegment each : sqlSegment.getAssignments()) {
            if (each.getValue() instanceof SimpleExpressionSegment) {
                fillShardingCondition(andCondition, columnNames.next(), insertStatement.getTables().getSingleTableName(), (SimpleExpressionSegment) each.getValue());
            }
            columnValues.add(each.getValue());
            fillWithInsertSetEncryptValueToken(insertStatement, each, each.getValue());
        }
        InsertValue insertValue = new InsertValue(columnValues);
        insertStatement.getValues().add(insertValue);
        insertStatement.getRouteCondition().getOrConditions().add(andCondition);
        insertStatement.setParametersIndex(insertValue.getParametersCount());
        fillWithInsertSetAddItemsToken(insertStatement, sqlSegment);
    }
    
    private void fillWithInsertSetEncryptValueToken(final InsertStatement insertStatement, final AssignmentSegment segment, final ExpressionSegment expressionSegment) {
        Optional<ShardingEncryptor> shardingEncryptor = shardingRule.getShardingEncryptorEngine().getShardingEncryptor(insertStatement.getTables().getSingleTableName(), segment.getColumn().getName());
        if (shardingEncryptor.isPresent() && !(expressionSegment instanceof ParameterMarkerExpressionSegment)) {
            insertStatement.getSQLTokens().add(new InsertSetEncryptValueToken(segment.getValue().getStartIndex(), segment.getValue().getStopIndex(), segment.getColumn().getName()));
        }
    }
    
    private void fillWithInsertSetAddItemsToken(final InsertStatement insertStatement, final SetAssignmentsSegment sqlSegment) {
        Collection<String> columnNames = getQueryAssistedColumn(insertStatement);
        if (getGeneratedKeyColumn(insertStatement).isPresent()) {
            columnNames.add(getGeneratedKeyColumn(insertStatement).get());
        }
        if (columnNames.isEmpty()) {
            return;
        }
        List<AssignmentSegment> assignments = new ArrayList<>(sqlSegment.getAssignments());
        insertStatement.getSQLTokens().add(new InsertSetAddItemsToken(assignments.get(assignments.size() - 1).getStopIndex() + 1, columnNames));
    }
    
    private Optional<String> getGeneratedKeyColumn(final InsertStatement insertStatement) {
        String tableName = insertStatement.getTables().getSingleTableName();
        Optional<String> generateKeyColumn = shardingRule.findGenerateKeyColumnName(tableName);
        return generateKeyColumn.isPresent() && !insertStatement.getColumnNames().contains(generateKeyColumn.get()) ? generateKeyColumn : Optional.<String>absent();
    }
    
    private Collection<String> getQueryAssistedColumn(final InsertStatement insertStatement) {
        Collection<String> result = new LinkedList<>();
        for (String each : insertStatement.getColumnNames()) {
            Optional<String> assistedColumnName = shardingRule.getShardingEncryptorEngine().getAssistedQueryColumn(insertStatement.getTables().getSingleTableName(), each);
            if (assistedColumnName.isPresent()) {
                result.add(assistedColumnName.get());
            }
        }
        return result;
    }
    
    private int getColumnCountExcludeAssistedQueryColumns(final InsertStatement insertStatement) {
        String tableName = insertStatement.getTables().getSingleTableName();
        if (shardingTableMetaData.containsTable(tableName) && shardingTableMetaData.get(tableName).getColumns().size() == insertStatement.getColumnNames().size()) {
            return insertStatement.getColumnNames().size();
        }
        Integer assistedQueryColumnCount = shardingRule.getShardingEncryptorEngine().getAssistedQueryColumnCount(insertStatement.getTables().getSingleTableName());
        return insertStatement.getColumnNames().size() - assistedQueryColumnCount;
    }
    
    private void fillShardingCondition(final AndCondition andCondition, final String columnName, final String tableName, final SimpleExpressionSegment simpleExpressionSegment) {
        if (shardingRule.isShardingColumn(columnName, tableName)) {
            andCondition.getConditions().add(new Condition(new Column(columnName, tableName), simpleExpressionSegment));
        }
    }
    
    private void fillUpdate(final SetAssignmentsSegment sqlSegment, final UpdateStatement updateStatement) {
        String tableName = updateStatement.getTables().getSingleTableName();
        for (AssignmentSegment each : sqlSegment.getAssignments()) {
            Column column = new Column(each.getColumn().getName(), tableName);
            updateStatement.getAssignments().put(column, each.getValue());
            fillEncryptCondition(each, tableName, updateStatement);
        }
    }
    
    private void fillEncryptCondition(final AssignmentSegment assignment, final String tableName, final UpdateStatement updateStatement) {
        Column column = new Column(assignment.getColumn().getName(), tableName);
        updateStatement.getAssignments().put(column, assignment.getValue());
        if (shardingRule.getShardingEncryptorEngine().getShardingEncryptor(column.getTableName(), column.getName()).isPresent()) {
            updateStatement.getSQLTokens().add(new EncryptColumnToken(assignment.getColumn().getStartIndex(), assignment.getValue().getStopIndex(), column, false));
        }
    }
}
