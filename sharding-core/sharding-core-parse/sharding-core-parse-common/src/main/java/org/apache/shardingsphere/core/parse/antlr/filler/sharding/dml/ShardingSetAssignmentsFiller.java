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

package org.apache.shardingsphere.core.parse.antlr.filler.sharding.dml;

import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingRuleAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingTableMetaDataAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.CommonExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.token.EncryptColumnToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.InsertSetToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.TableToken;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parse.old.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.old.parser.exception.SQLParsingException;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.rule.ShardingRule;

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
        String tableName = insertStatement.getTables().getSingleTableName();
        for (AssignmentSegment each : sqlSegment.getAssignments()) {
            fillColumn(each.getColumn(), insertStatement, tableName);
        }
        int columnCount = getColumnCountExcludeAssistedQueryColumns(insertStatement);
        if (sqlSegment.getAssignments().size() != columnCount) {
            throw new SQLParsingException("INSERT INTO column size mismatch value size.");
        }
        AndCondition andCondition = new AndCondition();
        Iterator<String> columnNames = insertStatement.getColumnNames().iterator();
        List<SQLExpression> columnValues = new LinkedList<>();
        for (AssignmentSegment each : sqlSegment.getAssignments()) {
            SQLExpression columnValue = getColumnValue(insertStatement, andCondition, columnNames.next(), each.getValue());
            columnValues.add(columnValue);
        }
        InsertValue insertValue = new InsertValue(columnValues);
        insertStatement.getValues().add(insertValue);
        insertStatement.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
        insertStatement.setParametersIndex(insertValue.getParametersCount());
        insertStatement.getSQLTokens().add(new InsertSetToken(sqlSegment.getStartIndex()));
    }
    
    private void fillColumn(final ColumnSegment sqlSegment, final InsertStatement insertStatement, final String tableName) {
        insertStatement.getColumnNames().add(sqlSegment.getName());
        if (sqlSegment.getOwner().isPresent() && tableName.equals(sqlSegment.getOwner().get())) {
            insertStatement.getSQLTokens().add(new TableToken(sqlSegment.getStartIndex(), tableName, sqlSegment.getOwnerQuoteCharacter(), 0));
        }
    }
    
    private int getColumnCountExcludeAssistedQueryColumns(final InsertStatement insertStatement) {
        String tableName = insertStatement.getTables().getSingleTableName();
        if (shardingTableMetaData.containsTable(tableName) && shardingTableMetaData.get(tableName).getColumns().size() == insertStatement.getColumnNames().size()) {
            return insertStatement.getColumnNames().size();
        }
        Integer assistedQueryColumnCount = shardingRule.getShardingEncryptorEngine().getAssistedQueryColumnCount(insertStatement.getTables().getSingleTableName());
        return insertStatement.getColumnNames().size() - assistedQueryColumnCount;
    }
    
    private SQLExpression getColumnValue(final InsertStatement insertStatement, final AndCondition andCondition, final String columnName, final CommonExpressionSegment expressionSegment) {
        SQLExpression result = expressionSegment.getSQLExpression(insertStatement.getLogicSQL());
        String tableName = insertStatement.getTables().getSingleTableName();
        fillShardingCondition(andCondition, columnName, tableName, result);
        return result;
    }
    
    private void fillShardingCondition(final AndCondition andCondition, final String columnName, final String tableName, final SQLExpression sqlExpression) {
        if (shardingRule.isShardingColumn(columnName, tableName)) {
            if (sqlExpression instanceof SQLPlaceholderExpression || sqlExpression instanceof SQLNumberExpression || sqlExpression instanceof SQLTextExpression) {
                andCondition.getConditions().add(new Condition(new Column(columnName, tableName), sqlExpression));
            } else {
                throw new SQLParsingException("INSERT INTO can not support complex expression value on sharding column '%s'.", columnName);
            }
        }
    }
    
    private void fillUpdate(final SetAssignmentsSegment sqlSegment, final UpdateStatement updateStatement) {
        String tableName = updateStatement.getTables().getSingleTableName();
        for (AssignmentSegment each : sqlSegment.getAssignments()) {
            Column column = new Column(each.getColumn().getName(), tableName);
            SQLExpression expression = each.getValue().getSQLExpression(updateStatement.getLogicSQL());
            updateStatement.getAssignments().put(column, expression);
            fillEncryptCondition(each, tableName, updateStatement);
        }
    }
    
    private void fillEncryptCondition(final AssignmentSegment assignment, final String tableName, final UpdateStatement updateStatement) {
        Column column = new Column(assignment.getColumn().getName(), tableName);
        SQLExpression expression = assignment.getValue().getSQLExpression(updateStatement.getLogicSQL());
        updateStatement.getAssignments().put(column, expression);
        if (shardingRule.getShardingEncryptorEngine().getShardingEncryptor(column.getTableName(), column.getName()).isPresent()) {
            updateStatement.getSQLTokens().add(new EncryptColumnToken(assignment.getColumn().getStartIndex(), assignment.getValue().getStopIndex(), column, false));
        }
    }
}
