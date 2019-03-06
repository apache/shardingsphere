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

package org.apache.shardingsphere.core.parsing.antlr.filler.encrypt.segment.impl;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.antlr.filler.encrypt.SQLSegmentEncryptFiller;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.InsertSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.InsertValuesSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.column.ColumnSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.CommonExpressionSegment;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parsing.parser.exception.SQLParsingException;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.parsing.parser.token.InsertColumnToken;
import org.apache.shardingsphere.core.parsing.parser.token.InsertValuesToken;
import org.apache.shardingsphere.core.parsing.parser.token.ItemsToken;
import org.apache.shardingsphere.core.rule.EncryptRule;

/**
 * Encrypt insert filler.
 *
 * @author duhongjun
 */
public class EncryptInsertFiller implements SQLSegmentEncryptFiller<InsertSegment> {
    
    @Override
    public void fill(final InsertSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final EncryptRule encryptRule, final ShardingTableMetaData shardingTableMetaData) {
        InsertStatement insertStatement = (InsertStatement) sqlStatement;
        insertStatement.getUpdateTableAlias().put(insertStatement.getTables().getSingleTableName(), insertStatement.getTables().getSingleTableName());
        createColumn(sqlSegment, insertStatement, encryptRule, shardingTableMetaData);
        createValue(sqlSegment, insertStatement, sql, encryptRule, shardingTableMetaData);
        insertStatement.setColumnsListLastIndex(sqlSegment.getColumnsListLastIndex());
        insertStatement.setInsertValuesListLastIndex(sqlSegment.getInsertValuesListLastIndex());
        insertStatement.getSQLTokens().add(
                new InsertValuesToken(sqlSegment.getInsertValueStartIndex(), DefaultKeyword.VALUES == sqlSegment.getValuesList().get(0).getType() ? DefaultKeyword.VALUES : DefaultKeyword.SET));
    }
    
    private void createColumn(final InsertSegment sqlSegment, final InsertStatement insertStatement, final EncryptRule encryptRule, final ShardingTableMetaData shardingTableMetaData) {
        if (sqlSegment.getColumns().isEmpty()) {
            createFromMeta(insertStatement, sqlSegment, encryptRule, shardingTableMetaData);
            return;
        }
        String tableName = insertStatement.getTables().getSingleTableName();
        for (ColumnSegment each : sqlSegment.getColumns()) {
            Column column = new Column(each.getName(), tableName);
            insertStatement.getColumns().add(column);
        }
    }
    
    private void createFromMeta(final InsertStatement insertStatement, final InsertSegment sqlSegment, final EncryptRule encryptRule, final ShardingTableMetaData shardingTableMetaData) {
        String tableName = insertStatement.getTables().getSingleTableName();
        int startIndex = sqlSegment.getColumnClauseStartIndex();
        insertStatement.addSQLToken(new InsertColumnToken(startIndex, "("));
        ItemsToken columnsToken = new ItemsToken(startIndex);
        columnsToken.setFirstOfItemsSpecial(true);
        if (shardingTableMetaData.containsTable(tableName)) {
            for (String each : shardingTableMetaData.getAllColumnNames(tableName)) {
                Column column = new Column(each, tableName);
                insertStatement.getColumns().add(column);
                columnsToken.getItems().add(each);
            }
        }
        insertStatement.addSQLToken(columnsToken);
        insertStatement.addSQLToken(new InsertColumnToken(startIndex, ")"));
        insertStatement.setColumnsListLastIndex(startIndex);
    }
    
    private void createValue(final InsertSegment insertSegment, final InsertStatement insertStatement, final String sql, final EncryptRule encryptRule,
                             final ShardingTableMetaData shardingTableMetaData) {
        if (insertSegment.getValuesList().isEmpty()) {
            return;
        }
        int parameterIndex = 0;
        for (InsertValuesSegment each : insertSegment.getValuesList()) {
            if (each.getValues().size() != insertStatement.getColumns().size()) {
                throw new SQLParsingException("INSERT INTO column size mismatch value size.");
            }
            InsertValue insertValue = new InsertValue(each.getType(), sql.substring(each.getStartIndex(), each.getStopIndex() + 1), each.getParametersCount());
            insertStatement.getInsertValues().getInsertValues().add(insertValue);
            parameterIndex += each.getParametersCount();
            for (CommonExpressionSegment commonExpressionSegment : each.getValues()) {
                SQLExpression sqlExpression = commonExpressionSegment.convertToSQLExpression(sql).get();
                insertValue.getColumnValues().add(sqlExpression);
            }
            insertStatement.setParametersIndex(parameterIndex);
        }
    }
}
