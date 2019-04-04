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

package org.apache.shardingsphere.core.parse.antlr.filler.sharding.statement.impl.dml.insert;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.filler.sharding.SQLSegmentShardingFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.expr.CommonExpressionSegment;
import org.apache.shardingsphere.core.parse.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parse.parser.context.condition.GeneratedKeyCondition;
import org.apache.shardingsphere.core.parse.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.parser.exception.SQLParsingException;
import org.apache.shardingsphere.core.parse.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parse.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Iterator;

/**
 * Insert values filler.
 *
 * @author zhangliang
 */
public final class InsertValuesFiller implements SQLSegmentShardingFiller<InsertValuesSegment> {
    
    @Override
    public void fill(final InsertValuesSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        InsertStatement insertStatement = (InsertStatement) sqlStatement;
        createValue(sqlSegment, insertStatement, sql, shardingRule, shardingTableMetaData);
    }
    
    private void createValue(final InsertValuesSegment sqlSegment,
                             final InsertStatement insertStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        if (DefaultKeyword.VALUES == sqlSegment.getType()) {
            removeGenerateKeyColumn(insertStatement, shardingRule, sqlSegment.getValues().size());
        }
        int columnCount = getColumnCountExcludeAssistedQueryColumns(insertStatement, shardingRule, shardingTableMetaData);
        if (sqlSegment.getValues().size() != columnCount) {
            throw new SQLParsingException("INSERT INTO column size mismatch value size.");
        }
        InsertValue insertValue = new InsertValue(sqlSegment.getType(), sqlSegment.getParametersCount());
        insertStatement.getInsertValues().getInsertValues().add(insertValue);
        AndCondition andCondition = new AndCondition();
        Iterator<Column> iterator = insertStatement.getColumns().iterator();
        for (CommonExpressionSegment commonExpressionSegment : sqlSegment.getValues()) {
            Column column = iterator.next();
            boolean shardingColumn = shardingRule.isShardingColumn(column.getName(), column.getTableName());
            SQLExpression sqlExpression = commonExpressionSegment.convertToSQLExpression(sql).get();
            insertValue.getColumnValues().add(sqlExpression);
            if (shardingColumn) {
                if (!(-1 < commonExpressionSegment.getPlaceholderIndex() || null != commonExpressionSegment.getValue() || commonExpressionSegment.isText())) {
                    throw new SQLParsingException("INSERT INTO can not support complex expression value on sharding column '%s'.", column.getName());
                }
                andCondition.getConditions().add(new Condition(column, sqlExpression));
            }
            Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTables().getSingleTableName());
            if (generateKeyColumnName.isPresent() && generateKeyColumnName.get().equals(column.getName())) {
                insertStatement.getGeneratedKeyConditions().add(createGeneratedKeyCondition(column, commonExpressionSegment, sql));
            }
        }
        insertStatement.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
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
    
    private void removeGenerateKeyColumn(final InsertStatement insertStatement, final ShardingRule shardingRule, final int valueCount) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTables().getSingleTableName());
        if (generateKeyColumnName.isPresent() && valueCount < insertStatement.getColumns().size()) {
            insertStatement.getColumns().remove(new Column(generateKeyColumnName.get(), insertStatement.getTables().getSingleTableName()));
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
