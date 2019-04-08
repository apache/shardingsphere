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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.antlr.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingRuleAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingTableMetaDataAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.CommonExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parse.parser.context.condition.GeneratedKeyCondition;
import org.apache.shardingsphere.core.parse.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.parser.exception.SQLParsingException;
import org.apache.shardingsphere.core.parse.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parse.parser.token.InsertValuesToken;
import org.apache.shardingsphere.core.parse.parser.token.TableToken;
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
public final class SetAssignmentsFiller implements SQLSegmentFiller<SetAssignmentsSegment>, ShardingRuleAwareFiller, ShardingTableMetaDataAwareFiller {
    
    private ShardingRule shardingRule;
    
    private ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public void fill(final SetAssignmentsSegment sqlSegment, final SQLStatement sqlStatement) {
        InsertStatement insertStatement = (InsertStatement) sqlStatement;
        String tableName = insertStatement.getTables().getSingleTableName();
        for (AssignmentSegment each : sqlSegment.getAssignments()) {
            fillColumn(each.getColumn(), insertStatement, tableName);
        }
        int columnCount = getColumnCountExcludeAssistedQueryColumns(insertStatement);
        if (sqlSegment.getAssignments().size() != columnCount) {
            throw new SQLParsingException("INSERT INTO column size mismatch value size.");
        }
        AndCondition andCondition = new AndCondition();
        Iterator<Column> columns = insertStatement.getColumns().iterator();
        int parametersCount = 0;
        List<SQLExpression> columnValues = new LinkedList<>();
        for (AssignmentSegment each : sqlSegment.getAssignments()) {
            SQLExpression columnValue = getColumnValue(insertStatement, andCondition, columns.next(), each.getValue());
            columnValues.add(columnValue);
            if (columnValue instanceof SQLPlaceholderExpression) {
                parametersCount++;
            }
        }
        InsertValue insertValue = new InsertValue(parametersCount, columnValues);
        insertStatement.getInsertValues().getValues().add(insertValue);
        insertStatement.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
        insertStatement.setParametersIndex(insertValue.getParametersCount());
        insertStatement.getSQLTokens().add(new InsertValuesToken(sqlSegment.getStartIndex(), DefaultKeyword.SET));
    }
    
    private void fillColumn(final ColumnSegment sqlSegment, final InsertStatement insertStatement, final String tableName) {
        insertStatement.getColumns().add(new Column(sqlSegment.getName(), tableName));
        if (sqlSegment.getOwner().isPresent() && tableName.equals(sqlSegment.getOwner().get())) {
            insertStatement.getSQLTokens().add(new TableToken(sqlSegment.getStartIndex(), tableName, QuoteCharacter.getQuoteCharacter(tableName), 0));
        }
    }
    
    private int getColumnCountExcludeAssistedQueryColumns(final InsertStatement insertStatement) {
        String tableName = insertStatement.getTables().getSingleTableName();
        if (shardingTableMetaData.containsTable(tableName) && shardingTableMetaData.get(tableName).getColumns().size() == insertStatement.getColumns().size()) {
            return insertStatement.getColumns().size();
        }
        Optional<Integer> assistedQueryColumnCount = shardingRule.getShardingEncryptorEngine().getAssistedQueryColumnCount(insertStatement.getTables().getSingleTableName());
        if (assistedQueryColumnCount.isPresent()) {
            return insertStatement.getColumns().size() - assistedQueryColumnCount.get();
        }
        return insertStatement.getColumns().size();
    }
    
    private SQLExpression getColumnValue(final InsertStatement insertStatement, final AndCondition andCondition, final Column column, final CommonExpressionSegment expressionSegment) {
        Optional<SQLExpression> result = expressionSegment.convertToSQLExpression(insertStatement.getLogicSQL());
        Preconditions.checkState(result.isPresent());
        fillShardingCondition(andCondition, column, expressionSegment, result.get());
        fillGeneratedKeyCondition(insertStatement, column, expressionSegment);
        return result.get();
    }
    
    private void fillShardingCondition(final AndCondition andCondition, final Column column, final CommonExpressionSegment expressionSegment, final SQLExpression sqlExpression) {
        if (shardingRule.isShardingColumn(column.getName(), column.getTableName())) {
            if (!(-1 < expressionSegment.getPlaceholderIndex() || null != expressionSegment.getValue() || expressionSegment.isText())) {
                throw new SQLParsingException("INSERT INTO can not support complex expression value on sharding column '%s'.", column.getName());
            }
            andCondition.getConditions().add(new Condition(column, sqlExpression));
        }
    }
    
    private void fillGeneratedKeyCondition(final InsertStatement insertStatement, final Column column, final CommonExpressionSegment expressionSegment) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTables().getSingleTableName());
        if (generateKeyColumnName.isPresent() && generateKeyColumnName.get().equalsIgnoreCase(column.getName())) {
            insertStatement.getGeneratedKeyConditions().add(createGeneratedKeyCondition(column, expressionSegment, insertStatement.getLogicSQL()));
        }
    }
    
    private GeneratedKeyCondition createGeneratedKeyCondition(final Column column, final CommonExpressionSegment sqlExpression, final String sql) {
        if (-1 < sqlExpression.getPlaceholderIndex()) {
            return new GeneratedKeyCondition(column, sqlExpression.getPlaceholderIndex(), null);
        }
        if (null != sqlExpression.getValue()) {
            return new GeneratedKeyCondition(column, -1, (Comparable<?>) sqlExpression.getValue());
        }
        return new GeneratedKeyCondition(column, -1, sql.substring(sqlExpression.getStartIndex(), sqlExpression.getStopIndex() + 1));
    }
}
