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

package org.apache.shardingsphere.core.parse.antlr.filler.sharding.dml.insert;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.filler.sharding.SQLSegmentShardingFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.CommonExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parse.parser.context.condition.GeneratedKeyCondition;
import org.apache.shardingsphere.core.parse.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.parser.exception.SQLParsingException;
import org.apache.shardingsphere.core.parse.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert values filler.
 *
 * @author zhangliang
 */
public final class InsertValuesFiller implements SQLSegmentShardingFiller<InsertValuesSegment> {
    
    @Override
    public void fill(final InsertValuesSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        InsertStatement insertStatement = (InsertStatement) sqlStatement;
        removeGenerateKeyColumn(insertStatement, shardingRule, sqlSegment.getValues().size());
        int columnCount = getColumnCountExcludeAssistedQueryColumns(insertStatement, shardingRule, shardingTableMetaData);
        if (sqlSegment.getValues().size() != columnCount) {
            throw new SQLParsingException("INSERT INTO column size mismatch value size.");
        }
        AndCondition andCondition = new AndCondition();
        Iterator<Column> columns = insertStatement.getColumns().iterator();
        int parametersCount = 0;
        List<SQLExpression> columnValues = new LinkedList<>();
        for (CommonExpressionSegment each : sqlSegment.getValues()) {
            SQLExpression columnValue = getColumnValue(insertStatement, sql, shardingRule, andCondition, columns.next(), each);
            columnValues.add(columnValue);
            if (columnValue instanceof SQLPlaceholderExpression) {
                parametersCount++;
            }
        }
        insertStatement.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
        InsertValue insertValue = new InsertValue(parametersCount, columnValues);
        insertStatement.getInsertValues().getValues().add(insertValue);
        insertStatement.setParametersIndex(insertStatement.getParametersIndex() + insertValue.getParametersCount());
    }
    
    private int getColumnCountExcludeAssistedQueryColumns(final InsertStatement insertStatement, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
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
    
    private void removeGenerateKeyColumn(final InsertStatement insertStatement, final ShardingRule shardingRule, final int valuesCount) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTables().getSingleTableName());
        if (generateKeyColumnName.isPresent() && valuesCount < insertStatement.getColumns().size()) {
            insertStatement.getColumns().remove(new Column(generateKeyColumnName.get(), insertStatement.getTables().getSingleTableName()));
        }
    }
    
    private SQLExpression getColumnValue(final InsertStatement insertStatement,
                                         final String sql, final ShardingRule shardingRule, final AndCondition andCondition, final Column column, final CommonExpressionSegment expressionSegment) {
        Optional<SQLExpression> result = expressionSegment.convertToSQLExpression(sql);
        Preconditions.checkState(result.isPresent());
        fillShardingCondition(shardingRule, andCondition, column, expressionSegment, result.get());
        fillGeneratedKeyCondition(insertStatement, sql, shardingRule, column, expressionSegment);
        return result.get();
    }
    
    private void fillShardingCondition(final ShardingRule shardingRule, 
                                       final AndCondition andCondition, final Column column, final CommonExpressionSegment expressionSegment, final SQLExpression sqlExpression) {
        if (shardingRule.isShardingColumn(column.getName(), column.getTableName())) {
            if (!(-1 < expressionSegment.getPlaceholderIndex() || null != expressionSegment.getValue() || expressionSegment.isText())) {
                throw new SQLParsingException("INSERT INTO can not support complex expression value on sharding column '%s'.", column.getName());
            }
            andCondition.getConditions().add(new Condition(column, sqlExpression));
        }
    }
    
    private void fillGeneratedKeyCondition(final InsertStatement insertStatement, 
                                           final String sql, final ShardingRule shardingRule, final Column column, final CommonExpressionSegment expressionSegment) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTables().getSingleTableName());
        if (generateKeyColumnName.isPresent() && generateKeyColumnName.get().equalsIgnoreCase(column.getName())) {
            insertStatement.getGeneratedKeyConditions().add(createGeneratedKeyCondition(column, expressionSegment, sql));
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
