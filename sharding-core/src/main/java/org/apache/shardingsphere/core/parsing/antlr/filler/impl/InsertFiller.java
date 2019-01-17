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

package org.apache.shardingsphere.core.parsing.antlr.filler.impl;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.InsertSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.InsertValuesSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.column.ColumnSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.CommonExpressionSegment;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Literals;
import org.apache.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.GeneratedKeyCondition;
import org.apache.shardingsphere.core.parsing.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parsing.parser.exception.SQLParsingException;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.parsing.parser.token.InsertColumnToken;
import org.apache.shardingsphere.core.parsing.parser.token.InsertValuesToken;
import org.apache.shardingsphere.core.parsing.parser.token.ItemsToken;
import org.apache.shardingsphere.core.parsing.parser.token.TableToken;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.util.SQLUtil;

import java.util.Iterator;
import java.util.List;

/**
 * Insert filler.
 *
 * @author duhongjun
 */
public final class InsertFiller implements SQLStatementFiller<InsertSegment> {
    
    @Override
    public void fill(final InsertSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        InsertStatement insertStatement = (InsertStatement) sqlStatement;
        createColumn(sqlSegment, insertStatement, shardingRule, shardingTableMetaData);
        createValue(sqlSegment, insertStatement, sql, shardingRule, shardingTableMetaData);
        insertStatement.setColumnsListLastPosition(sqlSegment.getColumnsListLastPosition());
        insertStatement.setInsertValuesListLastPosition(sqlSegment.getInsertValuesListLastPosition());
        insertStatement.getSQLTokens().add(new InsertValuesToken(sqlSegment.getInsertValueStartPosition(), insertStatement.getTables().getSingleTableName()));
        processGeneratedKey(shardingRule, insertStatement);
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
                insertStatement.getSQLTokens().add(new TableToken(each.getStartPosition(), 0, tableName));
            }
            index++;
        }
    }
    
    private void createFromMeta(final InsertStatement insertStatement, final InsertSegment sqlSegment, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        int count = 0;
        String tableName = insertStatement.getTables().getSingleTableName();
        int beginPosition = sqlSegment.getColumnClauseStartPosition();
        insertStatement.addSQLToken(new InsertColumnToken(beginPosition, "("));
        ItemsToken columnsToken = new ItemsToken(beginPosition);
        columnsToken.setFirstOfItemsSpecial(true);
        if (shardingTableMetaData.containsTable(tableName)) {
            Optional<Column> generateKeyColumn = shardingRule.findGenerateKeyColumn(insertStatement.getTables().getSingleTableName());
            for (String each : shardingTableMetaData.getAllColumnNames(tableName)) {
                if (generateKeyColumn.isPresent() && generateKeyColumn.get().getName().equalsIgnoreCase(each)) {
                    insertStatement.setGenerateKeyColumnIndex(count);
                }
                Column column = new Column(each, tableName);
                insertStatement.getColumns().add(column);
                columnsToken.getItems().add(each);
                count++;
            }
        }
        insertStatement.addSQLToken(columnsToken);
        insertStatement.addSQLToken(new InsertColumnToken(beginPosition, ")"));
        insertStatement.setColumnsListLastPosition(beginPosition);
    }
    
    private void createValue(final InsertSegment insertSegment, final InsertStatement insertStatement, final String sql, final ShardingRule shardingRule,
                             final ShardingTableMetaData shardingTableMetaData) {
        if (insertSegment.getValuesList().isEmpty()) {
            return;
        }
        if (DefaultKeyword.VALUES == insertSegment.getValuesList().get(0).getType()) {
            removeGenerateKeyColumn(insertStatement, shardingRule, insertSegment.getValuesList().get(0).getValues().size());
        }
        OrConditionFiller orConditionFiller = new OrConditionFiller();
        int parameterIndex = 0;
        for (InsertValuesSegment each : insertSegment.getValuesList()) {
            if (each.getValues().size() != insertStatement.getColumns().size()) {
                throw new SQLParsingException("INSERT INTO column size mismatch value size.");
            }
            insertStatement.getInsertValues().getInsertValues().add(new InsertValue(each.getType(), sql.substring(each.getStartIndex(), each.getEndIndex() + 1), each.getParametersCount()));
            parameterIndex += each.getParametersCount();
            int index = 0;
            AndCondition andCondition = new AndCondition();
            Iterator<Column> iterator = insertStatement.getColumns().iterator();
            for (CommonExpressionSegment commonExpressionSegment : each.getValues()) {
                Column column = iterator.next();
                boolean shardingColumn = shardingRule.isShardingColumn(column);
                if (shardingColumn) {
                    if (!(-1 < commonExpressionSegment.getIndex() || null != commonExpressionSegment.getValue() || commonExpressionSegment.isText())) {
                        throw new SQLParsingException("INSERT INTO can not support complex expression value on sharding column '%s'.", column.getName());
                    }
                    andCondition.getConditions().add(orConditionFiller.buildEqualsCondition(column, commonExpressionSegment, sql).get());
                }
                if (index == insertStatement.getGenerateKeyColumnIndex()) {
                    insertStatement.getGeneratedKeyConditions().add(createGeneratedKeyCondition(column, commonExpressionSegment, sql));
                }
                index++;
            }
            insertStatement.setParametersIndex(parameterIndex);
            insertStatement.getConditions().getOrCondition().getAndConditions().add(andCondition);
        }
    }
    
    private void removeGenerateKeyColumn(final InsertStatement insertStatement, final ShardingRule shardingRule, final int valueCount) {
        Optional<Column> generateKeyColumn = shardingRule.findGenerateKeyColumn(insertStatement.getTables().getSingleTableName());
        if (generateKeyColumn.isPresent() && valueCount < insertStatement.getColumns().size()) {
            List<ItemsToken> itemsTokens = insertStatement.getItemsTokens();
            insertStatement.getColumns().remove(new Column(generateKeyColumn.get().getName(), insertStatement.getTables().getSingleTableName()));
            for (ItemsToken each : itemsTokens) {
                each.getItems().remove(generateKeyColumn.get().getName());
                insertStatement.setGenerateKeyColumnIndex(-1);
            }
        }
    }
    
    private GeneratedKeyCondition createGeneratedKeyCondition(final Column column, final CommonExpressionSegment sqlExpression, final String sql) {
        if (-1 < sqlExpression.getIndex()) {
            return new GeneratedKeyCondition(column, sqlExpression.getIndex(), null);
        }
        if (null != sqlExpression.getValue()) {
            return new GeneratedKeyCondition(column, -1, (Comparable<?>) sqlExpression.getValue());
        }
        return new GeneratedKeyCondition(column, -1, sql.substring(sqlExpression.getStartPosition(), sqlExpression.getEndPosition() + 1));
    }
    
    private void processGeneratedKey(final ShardingRule shardingRule, final InsertStatement insertStatement) {
        String tableName = insertStatement.getTables().getSingleTableName();
        Optional<Column> generateKeyColumn = shardingRule.findGenerateKeyColumn(tableName);
        if (-1 != insertStatement.getGenerateKeyColumnIndex() || !generateKeyColumn.isPresent()) {
            return;
        }
        if (DefaultKeyword.VALUES.equals(insertStatement.getInsertValues().getInsertValues().get(0).getType())) {
            if (!insertStatement.getItemsTokens().isEmpty()) {
                insertStatement.getItemsTokens().get(0).getItems().add(generateKeyColumn.get().getName());
            } else {
                ItemsToken columnsToken = new ItemsToken(insertStatement.getColumnsListLastPosition());
                columnsToken.getItems().add(generateKeyColumn.get().getName());
                insertStatement.addSQLToken(columnsToken);
            }
        }
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
