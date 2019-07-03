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

package org.apache.shardingsphere.core.rewrite.rewriter.sql;

import org.apache.shardingsphere.core.optimize.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.insert.InsertOptimizeResultUnit;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.insert.ShardingInsertOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertAssistedColumnsPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertSetAddItemsPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertSetEncryptValuePlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertValuePlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertValuesPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.UpdateEncryptAssistedItemPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.UpdateEncryptItemPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.WhereEncryptColumnPlaceholder;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertAssistedColumnsToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertSetAddAssistedColumnsToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertSetEncryptValueToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertValuesToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.SQLToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.UpdateEncryptAssistedItemToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.UpdateEncryptItemToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.WhereEncryptColumnToken;
import org.apache.shardingsphere.core.strategy.encrypt.ShardingEncryptorEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL rewriter encrypt.
 * 
 * @author panjuan
 */
public final class EncryptSQLRewriter implements SQLRewriter {
    
    private final ShardingEncryptorEngine encryptorEngine;
    
    private final DMLStatement dmlStatement;
    
    private final OptimizedStatement optimizedStatement;
    
    public EncryptSQLRewriter(final ShardingEncryptorEngine encryptorEngine, final OptimizedStatement optimizedStatement) {
        this.encryptorEngine = encryptorEngine;
        this.dmlStatement = (DMLStatement) optimizedStatement.getSQLStatement();
        this.optimizedStatement = optimizedStatement;
    }
    
    @Override
    public void rewrite(final SQLBuilder sqlBuilder, final ParameterBuilder parameterBuilder, final SQLToken sqlToken) {
        if (sqlToken instanceof InsertValuesToken) {
            appendInsertValuesPlaceholder(sqlBuilder, (ShardingInsertOptimizedStatement) optimizedStatement);
        } else if (sqlToken instanceof InsertSetEncryptValueToken) {
            appendInsertSetEncryptValuePlaceholder(sqlBuilder, (InsertSetEncryptValueToken) sqlToken);
        } else if (sqlToken instanceof InsertSetAddAssistedColumnsToken) {
            appendInsertSetAddItemsPlaceholder(sqlBuilder, (InsertSetAddAssistedColumnsToken) sqlToken, (ShardingInsertOptimizedStatement) optimizedStatement);
        } else if (sqlToken instanceof InsertAssistedColumnsToken) {
            appendInsertAssistedColumnsPlaceholder(sqlBuilder, (InsertAssistedColumnsToken) sqlToken);
        } else if (sqlToken instanceof WhereEncryptColumnToken) {
            appendWhereEncryptColumnPlaceholder(sqlBuilder, (WhereEncryptColumnToken) sqlToken);
        } else if (sqlToken instanceof UpdateEncryptItemToken) {
            appendUpdateEncryptItemPlaceholder(sqlBuilder, (UpdateEncryptItemToken) sqlToken);
        } else if (sqlToken instanceof UpdateEncryptAssistedItemToken) {
            appendUpdateEncryptAssistedItemToken(sqlBuilder, (UpdateEncryptAssistedItemToken) sqlToken);
        }
    }
    
    private void appendInsertValuesPlaceholder(final SQLBuilder sqlBuilder, final ShardingInsertOptimizedStatement insertOptimizeResult) {
        List<InsertValuePlaceholder> insertValues = new LinkedList<>();
        for (InsertOptimizeResultUnit each : insertOptimizeResult.getUnits()) {
            insertValues.add(new InsertValuePlaceholder(new ArrayList<>(each.getColumnNames()), Arrays.asList(each.getValues()), each.getDataNodes()));
        }
        sqlBuilder.appendPlaceholder(new InsertValuesPlaceholder(insertValues));
    }
    
    private void appendInsertSetEncryptValuePlaceholder(final SQLBuilder sqlBuilder, final InsertSetEncryptValueToken insertSetEncryptValueToken) {
        sqlBuilder.appendPlaceholder(new InsertSetEncryptValuePlaceholder(insertSetEncryptValueToken.getEncryptColumnValue()));
    }
    
    private void appendInsertSetAddItemsPlaceholder(
            final SQLBuilder sqlBuilder, final InsertSetAddAssistedColumnsToken insertSetAddAssistedColumnsToken, final ShardingInsertOptimizedStatement optimizedStatement) {
        List<ExpressionSegment> columnValues = new LinkedList<>();
        for (String each : insertSetAddAssistedColumnsToken.getColumnNames()) {
            columnValues.add(optimizedStatement.getUnits().get(0).getColumnSQLExpression(each));
        }
        sqlBuilder.appendPlaceholder(new InsertSetAddItemsPlaceholder(new LinkedList<>(insertSetAddAssistedColumnsToken.getColumnNames()), columnValues));
    }
    
    private void appendInsertAssistedColumnsPlaceholder(final SQLBuilder sqlBuilder, final InsertAssistedColumnsToken insertAssistedColumnsToken) {
        sqlBuilder.appendPlaceholder(new InsertAssistedColumnsPlaceholder(insertAssistedColumnsToken.getColumns(), insertAssistedColumnsToken.isToAddCloseParenthesis()));
    }
    
    private void appendWhereEncryptColumnPlaceholder(final SQLBuilder sqlBuilder, final WhereEncryptColumnToken whereEncryptColumnToken) {
        sqlBuilder.appendPlaceholder(new WhereEncryptColumnPlaceholder(whereEncryptColumnToken.getColumnName(), 
                    whereEncryptColumnToken.getIndexValues(), whereEncryptColumnToken.getParameterMarkerIndexes(), whereEncryptColumnToken.getOperator()));
        
    }
    
    private void appendUpdateEncryptItemPlaceholder(final SQLBuilder sqlBuilder, final UpdateEncryptItemToken updateEncryptItemToken) {
        sqlBuilder.appendPlaceholder(new UpdateEncryptItemPlaceholder(updateEncryptItemToken.getColumnName(),
                    updateEncryptItemToken.getColumnValue(), updateEncryptItemToken.getParameterMarkerIndex()));
    }
    
    private void appendUpdateEncryptAssistedItemToken(final SQLBuilder sqlBuilder, final UpdateEncryptAssistedItemToken updateEncryptAssistedItemToken) {
        sqlBuilder.appendPlaceholder(new UpdateEncryptAssistedItemPlaceholder(updateEncryptAssistedItemToken.getColumnName(),
                    updateEncryptAssistedItemToken.getColumnValue(), updateEncryptAssistedItemToken.getAssistedColumnName(),
                    updateEncryptAssistedItemToken.getAssistedColumnValue(), updateEncryptAssistedItemToken.getParameterMarkerIndex()));
    }
}
