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
import org.apache.shardingsphere.core.parse.old.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.old.parser.exception.SQLParsingException;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.ArrayList;
import java.util.Collection;
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
        AndCondition andCondition = new AndCondition();
        Iterator<String> columnNames = getColumnNames(sqlSegment, insertStatement);
        List<SQLExpression> columnValues = new LinkedList<>();
        for (CommonExpressionSegment each : sqlSegment.getValues()) {
            String columnName = columnNames.next();
            SQLExpression columnValue = getColumnValue(insertStatement, andCondition, columnName, each);
            columnValues.add(columnValue);
        }
        insertStatement.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
        InsertValue insertValue = new InsertValue(columnValues);
        insertStatement.getValues().add(insertValue);
        insertStatement.setParametersIndex(insertStatement.getParametersIndex() + insertValue.getParametersCount());
    }
    
    private Iterator<String> getColumnNames(final InsertValuesSegment sqlSegment, final InsertStatement insertStatement) {
        Collection<String> result = new ArrayList<>(insertStatement.getColumnNames());
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTables().getSingleTableName());
        if (insertStatement.getColumnNames().size() != sqlSegment.getValues().size() && generateKeyColumnName.isPresent()) {
            result.remove(generateKeyColumnName.get());
        }
        return result.iterator();
    }
    
    private SQLExpression getColumnValue(final InsertStatement insertStatement, final AndCondition andCondition, final String columnName, final CommonExpressionSegment expressionSegment) {
        SQLExpression result = expressionSegment.getSQLExpression(insertStatement.getLogicSQL());
        String tableName = insertStatement.getTables().getSingleTableName();
        fillShardingCondition(andCondition, tableName, columnName, result);
        return result;
    }
    
    private void fillShardingCondition(final AndCondition andCondition, final String tableName, final String columnName, final SQLExpression sqlExpression) {
        if (shardingRule.isShardingColumn(columnName, tableName)) {
            if (sqlExpression instanceof SQLPlaceholderExpression || sqlExpression instanceof SQLNumberExpression || sqlExpression instanceof SQLTextExpression) {
                andCondition.getConditions().add(new Condition(new Column(columnName, tableName), sqlExpression));
            } else {
                throw new SQLParsingException("INSERT INTO can not support complex expression value on sharding column '%s'.", columnName);
            }
        }
    }
}
