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

package org.apache.shardingsphere.core.parsing.antlr.filler.sharding.statement.impl.dml;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.antlr.filler.sharding.SQLSegmentShardingFiller;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.InsertSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.InsertValuesSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.column.ColumnSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.CommonExpressionSegment;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Literals;
import org.apache.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.GeneratedKeyCondition;
import org.apache.shardingsphere.core.parsing.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parsing.parser.exception.SQLParsingException;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.parsing.parser.token.InsertValuesToken;
import org.apache.shardingsphere.core.parsing.parser.token.TableToken;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.util.SQLUtil;

import java.util.Iterator;

/**
 * Insert filler.
 *
 * @author duhongjun
 * @author panjuan
 */
public final class InsertFiller implements SQLSegmentShardingFiller<InsertSegment> {
    
    @Override
    public void fill(final InsertSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        InsertStatement insertStatement = (InsertStatement) sqlStatement;
        insertStatement.getUpdateTableAlias().put(insertStatement.getTables().getSingleTableName(), insertStatement.getTables().getSingleTableName());
        createColumn(sqlSegment, insertStatement, shardingRule, shardingTableMetaData);
        createValue(sqlSegment, insertStatement, sql, shardingRule, shardingTableMetaData);
        insertStatement.setInsertValuesListLastIndex(sqlSegment.getInsertValuesListLastIndex());
        insertStatement.getSQLTokens().add(
                new InsertValuesToken(sqlSegment.getColumnClauseStartIndex(), DefaultKeyword.VALUES == sqlSegment.getValuesList().get(0).getType() ? DefaultKeyword.VALUES : DefaultKeyword.SET));
        processDuplicateKey(shardingRule, sqlSegment, sqlStatement.getTables().getSingleTableName());
    }
    
    private void createColumn(final InsertSegment sqlSegment, final InsertStatement insertStatement, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        if (sqlSegment.getColumns().isEmpty()) {
            createFromMeta(insertStatement, sqlSegment, shardingRule, shardingTableMetaData);
            return;
        }
        String tableName = insertStatement.getTables().getSingleTableName();
        int index = 0;
        Optional<Column> shardingColumn = shardingRule.findGenerateKeyColumn(tableName);
        for (ColumnSegment each : sqlSegment.getColumns()) {
            Column column = new Column(each.getName(), tableName);
            insertStatement.getColumns().add(column);
            if (shardingColumn.isPresent() && shardingColumn.get().getName().equalsIgnoreCase(each.getName())) {
                insertStatement.setGenerateKeyColumnIndex(index);
            }
            if (each.getOwner().isPresent() && tableName.equals(each.getOwner().get())) {
                insertStatement.getSQLTokens().add(new TableToken(each.getStartIndex(), 
                        0, SQLUtil.getExactlyValue(tableName), SQLUtil.getLeftDelimiter(tableName), SQLUtil.getRightDelimiter(tableName)));
            }
            index++;
        }
    }
    
    private void createFromMeta(final InsertStatement insertStatement, final InsertSegment sqlSegment, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        int count = 0;
        String tableName = insertStatement.getTables().getSingleTableName();
        if (shardingTableMetaData.containsTable(tableName)) {
            Optional<Column> generateKeyColumn = shardingRule.findGenerateKeyColumn(insertStatement.getTables().getSingleTableName());
            for (String each : shardingTableMetaData.getAllColumnNames(tableName)) {
                if (generateKeyColumn.isPresent() && generateKeyColumn.get().getName().equalsIgnoreCase(each)) {
                    insertStatement.setGenerateKeyColumnIndex(count);
                }
                Column column = new Column(each, tableName);
                insertStatement.getColumns().add(column);
                count++;
            }
        }
    }
    
    private void createValue(final InsertSegment insertSegment, final InsertStatement insertStatement, final String sql, final ShardingRule shardingRule,
                             final ShardingTableMetaData shardingTableMetaData) {
        if (insertSegment.getValuesList().isEmpty()) {
            return;
        }
        if (DefaultKeyword.VALUES == insertSegment.getValuesList().get(0).getType()) {
            removeGenerateKeyColumn(insertStatement, shardingRule, insertSegment.getValuesList().get(0).getValues().size());
        }
        int parameterIndex = 0;
        for (InsertValuesSegment each : insertSegment.getValuesList()) {
            if (each.getValues().size() != insertStatement.getColumns().size()) {
                throw new SQLParsingException("INSERT INTO column size mismatch value size.");
            }
            InsertValue insertValue = new InsertValue(each.getType(), sql.substring(each.getStartIndex(), each.getStopIndex() + 1), each.getParametersCount());
            insertStatement.getInsertValues().getInsertValues().add(insertValue);
            parameterIndex += each.getParametersCount();
            int index = 0;
            AndCondition andCondition = new AndCondition();
            Iterator<Column> iterator = insertStatement.getColumns().iterator();
            for (CommonExpressionSegment commonExpressionSegment : each.getValues()) {
                Column column = iterator.next();
                boolean shardingColumn = shardingRule.isShardingColumn(column);
                SQLExpression sqlExpression = commonExpressionSegment.convertToSQLExpression(sql).get();
                insertValue.getColumnValues().add(sqlExpression);
                if (shardingColumn) {
                    if (!(-1 < commonExpressionSegment.getPlaceholderIndex() || null != commonExpressionSegment.getValue() || commonExpressionSegment.isText())) {
                        throw new SQLParsingException("INSERT INTO can not support complex expression value on sharding column '%s'.", column.getName());
                    }
                    andCondition.getConditions().add(new Condition(column, sqlExpression));
                }
                if (index == insertStatement.getGenerateKeyColumnIndex()) {
                    insertStatement.getGeneratedKeyConditions().add(createGeneratedKeyCondition(column, commonExpressionSegment, sql));
                }
                index++;
            }
            insertStatement.setParametersIndex(parameterIndex);
            insertStatement.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
        }
    }
    
    private void removeGenerateKeyColumn(final InsertStatement insertStatement, final ShardingRule shardingRule, final int valueCount) {
        Optional<Column> generateKeyColumn = shardingRule.findGenerateKeyColumn(insertStatement.getTables().getSingleTableName());
        if (generateKeyColumn.isPresent() && valueCount < insertStatement.getColumns().size()) {
            insertStatement.getColumns().remove(new Column(generateKeyColumn.get().getName(), insertStatement.getTables().getSingleTableName()));
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
    
    private void processDuplicateKey(final ShardingRule shardingRule, final InsertSegment insertSegment, final String tableName) {
        for (String each : insertSegment.getDuplicateKeyColumns()) {
            if (shardingRule.isShardingColumn(new Column(SQLUtil.getExactlyValue(each), tableName))) {
                throw new SQLParsingException("INSERT INTO .... ON DUPLICATE KEY UPDATE can not support on sharding column, token is '%s', literals is '%s'.",
                        Literals.IDENTIFIER, each);
            }
        }
    }
}
