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

package org.apache.shardingsphere.core.parse.antlr.filler.encrypt.segment.impl;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.antlr.filler.encrypt.SQLSegmentEncryptFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.CommonExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parse.parser.token.InsertValuesToken;
import org.apache.shardingsphere.core.parse.parser.token.TableToken;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.LinkedList;
import java.util.List;

/**
 * Set assignments filler for encrypt.
 *
 * @author zhangliang
 */
public final class EncryptSetAssignmentsFiller implements SQLSegmentEncryptFiller<SetAssignmentsSegment> {
    
    @Override
    public void fill(final SetAssignmentsSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final EncryptRule encryptRule, final ShardingTableMetaData shardingTableMetaData) {
        InsertStatement insertStatement = (InsertStatement) sqlStatement;
        String tableName = insertStatement.getTables().getSingleTableName();
        for (ColumnSegment each : sqlSegment.getColumns()) {
            fillColumn(each, insertStatement, tableName);
        }
        InsertValue insertValue = getInsertValue(sqlSegment, sql);
        insertStatement.getInsertValues().getValues().add(insertValue);
        insertStatement.setParametersIndex(insertValue.getParametersCount());
        insertStatement.getSQLTokens().add(new InsertValuesToken(sqlSegment.getStartIndex(), DefaultKeyword.SET));
    }
    
    private void fillColumn(final ColumnSegment sqlSegment, final InsertStatement insertStatement, final String tableName) {
        insertStatement.getColumns().add(new Column(sqlSegment.getName(), tableName));
        if (sqlSegment.getOwner().isPresent() && tableName.equals(sqlSegment.getOwner().get())) {
            insertStatement.getSQLTokens().add(new TableToken(sqlSegment.getStartIndex(), tableName, QuoteCharacter.getQuoteCharacter(tableName), 0));
        }
    }
    
    private InsertValue getInsertValue(final SetAssignmentsSegment sqlSegment, final String sql) {
        int parametersCount = 0;
        List<SQLExpression> columnValues = new LinkedList<>();
        for (CommonExpressionSegment each : sqlSegment.getValues()) {
            Optional<SQLExpression> sqlExpression = each.convertToSQLExpression(sql);
            if (sqlExpression.isPresent()) {
                columnValues.add(sqlExpression.get());
                if (sqlExpression.get() instanceof SQLPlaceholderExpression) {
                    parametersCount++;
                }
            }
        }
        return new InsertValue(parametersCount, columnValues);
    }
}
