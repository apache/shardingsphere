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
import lombok.Setter;
import org.apache.shardingsphere.core.parse.antlr.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingRuleAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.CommonExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.GeneratedKeyCondition;
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
 * Insert values filler.
 *
 * @author zhangliang
 */
@Setter
public final class InsertValuesFiller implements SQLSegmentFiller<InsertValuesSegment>, ShardingRuleAwareFiller {
    
    private ShardingRule shardingRule;
    
    @Override
    public void fill(final InsertValuesSegment sqlSegment, final SQLStatement sqlStatement) {
        InsertStatement insertStatement = (InsertStatement) sqlStatement;
        removeGenerateKeyColumn(insertStatement, shardingRule, sqlSegment.getValues().size());
        AndCondition andCondition = new AndCondition();
        Iterator<String> columnNames = insertStatement.getColumnNames().iterator();
        int parametersCount = 0;
        List<SQLExpression> columnValues = new LinkedList<>();
        for (CommonExpressionSegment each : sqlSegment.getValues()) {
            SQLExpression columnValue = getColumnValue(insertStatement, shardingRule, andCondition, columnNames.next(), each);
            columnValues.add(columnValue);
            if (columnValue instanceof SQLPlaceholderExpression) {
                parametersCount++;
            }
        }
        insertStatement.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
        InsertValue insertValue = new InsertValue(parametersCount, columnValues);
        insertStatement.getValues().add(insertValue);
        insertStatement.setParametersIndex(insertStatement.getParametersIndex() + insertValue.getParametersCount());
    }
    
    private void removeGenerateKeyColumn(final InsertStatement insertStatement, final ShardingRule shardingRule, final int valuesCount) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTables().getSingleTableName());
        if (generateKeyColumnName.isPresent() && valuesCount < insertStatement.getColumnNames().size()) {
            insertStatement.getColumnNames().remove(generateKeyColumnName.get());
        }
    }
    
    private SQLExpression getColumnValue(final InsertStatement insertStatement,
                                         final ShardingRule shardingRule, final AndCondition andCondition, final String columnName, final CommonExpressionSegment expressionSegment) {
        SQLExpression result = expressionSegment.getSQLExpression(insertStatement.getLogicSQL());
        String tableName = insertStatement.getTables().getSingleTableName();
        fillShardingCondition(shardingRule, andCondition, tableName, columnName, expressionSegment, result);
        fillGeneratedKeyCondition(insertStatement, columnName, tableName, result);
        return result;
    }
    
    private void fillShardingCondition(final ShardingRule shardingRule, final AndCondition andCondition, 
                                       final String tableName, final String columnName, final CommonExpressionSegment expressionSegment, final SQLExpression sqlExpression) {
        if (shardingRule.isShardingColumn(columnName, tableName)) {
            if (!(-1 < expressionSegment.getPlaceholderIndex() || null != expressionSegment.getLiterals())) {
                throw new SQLParsingException("INSERT INTO can not support complex expression value on sharding column '%s'.", columnName);
            }
            andCondition.getConditions().add(new Condition(new Column(columnName, tableName), sqlExpression));
        }
    }
    
    private void fillGeneratedKeyCondition(final InsertStatement insertStatement, final String columnName, final String tableName, final SQLExpression sqlExpression) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTables().getSingleTableName());
        if (generateKeyColumnName.isPresent() && generateKeyColumnName.get().equalsIgnoreCase(columnName)) {
            Optional<GeneratedKeyCondition> generatedKeyCondition = createGeneratedKeyCondition(new Column(columnName, tableName), sqlExpression);
            if (generatedKeyCondition.isPresent()) {
                insertStatement.getGeneratedKeyConditions().add(generatedKeyCondition.get());
            }
        }
    }
    
    private Optional<GeneratedKeyCondition> createGeneratedKeyCondition(final Column column, final SQLExpression sqlExpression) {
        if (sqlExpression instanceof SQLPlaceholderExpression) {
            return Optional.of(new GeneratedKeyCondition(column, ((SQLPlaceholderExpression) sqlExpression).getIndex(), null));
        }
        if (sqlExpression instanceof SQLNumberExpression) {
            return Optional.of(new GeneratedKeyCondition(column, -1, (Comparable<?>) ((SQLNumberExpression) sqlExpression).getNumber()));
        }
        if (sqlExpression instanceof SQLTextExpression) {
            return Optional.of(new GeneratedKeyCondition(column, -1, ((SQLTextExpression) sqlExpression).getText()));
        }
        return Optional.absent();
    }
}
