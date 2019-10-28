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

package org.apache.shardingsphere.core.rewrite.feature.encrypt.token.generator.impl;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.preprocessor.segment.insert.InsertValueContext;
import org.apache.shardingsphere.core.preprocessor.segment.insert.expression.DerivedLiteralExpressionSegment;
import org.apache.shardingsphere.core.preprocessor.segment.insert.expression.DerivedParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.preprocessor.segment.insert.expression.DerivedSimpleExpressionSegment;
import org.apache.shardingsphere.core.preprocessor.statement.SQLStatementContext;
import org.apache.shardingsphere.core.preprocessor.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.aware.PreviousSQLTokensAware;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.generic.InsertColumnsToken;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.generic.InsertValuesToken;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.generic.InsertValuesToken.InsertValueToken;
import org.apache.shardingsphere.core.rule.DataNode;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.spi.encrypt.ShardingQueryAssistedEncryptor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Insert values token generator for encrypt.
 *
 * @author panjuan
 */
@Setter
public final class EncryptInsertValuesTokenGenerator implements OptionalSQLTokenGenerator, EncryptRuleAware, PreviousSQLTokensAware {
    
    private EncryptRule encryptRule;
    
    private List<SQLToken> previousSQLTokens;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement() instanceof InsertStatement && !sqlStatementContext.getSqlStatement().findSQLSegments(InsertValuesSegment.class).isEmpty()
                && encryptRule.findEncryptTable(sqlStatementContext.getTablesContext().getSingleTableName()).isPresent();
    }
    
    @Override
    public InsertValuesToken generateSQLToken(final SQLStatementContext sqlStatementContext) {
        Optional<SQLToken> insertValuesToken = findPreviousSQLToken(InsertValuesToken.class);
        if (insertValuesToken.isPresent()) {
            processPreviousSQLToken((InsertSQLStatementContext) sqlStatementContext, (InsertValuesToken) insertValuesToken.get());
            return (InsertValuesToken) insertValuesToken.get();
        }
        return generateNewSQLToken(sqlStatementContext);
    }
    
    private Optional<SQLToken> findPreviousSQLToken(final Class<?> sqlToken) {
        for (SQLToken each : previousSQLTokens) {
            if (each.getClass().equals(sqlToken)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private void processPreviousSQLToken(final InsertSQLStatementContext sqlStatementContext, final InsertValuesToken insertValuesToken) {
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        int count = 0;
        for (InsertValueContext each : sqlStatementContext.getInsertValueContexts()) {
            encryptInsertValueToken(insertValuesToken.getInsertValueTokens().get(count), tableName, sqlStatementContext, each);
            count++;
        }
    }
    
    private InsertValuesToken generateNewSQLToken(final SQLStatementContext sqlStatementContext) {
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        Collection<InsertValuesSegment> insertValuesSegments = sqlStatementContext.getSqlStatement().findSQLSegments(InsertValuesSegment.class);
        InsertValuesToken result = new InsertValuesToken(getStartIndex(insertValuesSegments), getStopIndex(insertValuesSegments));
        for (InsertValueContext each : ((InsertSQLStatementContext) sqlStatementContext).getInsertValueContexts()) {
            InsertValueToken insertValueToken = result.addInsertValue(each.getValueExpressions(), Collections.<DataNode>emptyList());
            encryptInsertValueToken(insertValueToken, tableName, (InsertSQLStatementContext) sqlStatementContext, each);
        }
        return result;
    }
    
    private int getStartIndex(final Collection<InsertValuesSegment> segments) {
        int result = segments.iterator().next().getStartIndex();
        for (InsertValuesSegment each : segments) {
            result = result > each.getStartIndex() ? each.getStartIndex() : result;
        }
        return result;
    }
    
    private int getStopIndex(final Collection<InsertValuesSegment> segments) {
        int result = segments.iterator().next().getStopIndex();
        for (InsertValuesSegment each : segments) {
            result = result < each.getStopIndex() ? each.getStopIndex() : result;
        }
        return result;
    }
    
    private void encryptInsertValueToken(final InsertValueToken insertValueToken, 
                                         final String tableName, final InsertSQLStatementContext sqlStatementContext, final InsertValueContext insertValueContext) {
        Optional<SQLToken> insertColumnsToken = findPreviousSQLToken(InsertColumnsToken.class);
        for (String each : sqlStatementContext.getColumnNames()) {
            Optional<ShardingEncryptor> encryptor = encryptRule.findShardingEncryptor(tableName, each);
            if (!encryptor.isPresent()) {
                continue;
            }
            int columnIndex = insertColumnsToken.isPresent()
                    ? ((InsertColumnsToken) insertColumnsToken.get()).getColumns().indexOf(encryptRule.getCipherColumn(tableName, each)) : sqlStatementContext.getColumnNames().indexOf(each);
            ExpressionSegment valueExpression = insertValueContext.getValueExpressions().get(columnIndex);
            Object originalValue = insertValueContext.getValue(columnIndex);
            if (valueExpression instanceof LiteralExpressionSegment) {
                LiteralExpressionSegment encryptedLiteralExpressionSegment = new LiteralExpressionSegment(
                        valueExpression.getStartIndex(), valueExpression.getStopIndex(), encryptor.get().encrypt(originalValue));
                insertValueToken.getValues().set(columnIndex, encryptedLiteralExpressionSegment);
            }
            if (encryptRule.findAssistedQueryColumn(tableName, each).isPresent()) {
                DerivedSimpleExpressionSegment derivedExpressionSegment = insertValueContext.getParameters().isEmpty()
                        ? new DerivedLiteralExpressionSegment(((ShardingQueryAssistedEncryptor) encryptor.get()).queryAssistedEncrypt(null == originalValue ? null : originalValue.toString()))
                        : new DerivedParameterMarkerExpressionSegment(getParameterIndexCount(insertValueToken));
                insertValueToken.getValues().add(derivedExpressionSegment);
            }
            if (encryptRule.findPlainColumn(tableName, each).isPresent()) {
                DerivedSimpleExpressionSegment derivedExpressionSegment = insertValueContext.getParameters().isEmpty()
                        ? new DerivedLiteralExpressionSegment(originalValue) : new DerivedParameterMarkerExpressionSegment(getParameterIndexCount(insertValueToken));
                insertValueToken.getValues().add(derivedExpressionSegment);
            }
        }
    }
    
    private int getParameterIndexCount(final InsertValueToken insertValueToken) {
        int result = 0;
        for (ExpressionSegment each : insertValueToken.getValues()) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                result++;
            }
        }
        return result;
    }
}
