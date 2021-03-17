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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.impl;

import com.google.common.base.Joiner;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.rewrite.aware.QueryWithCipherColumnAware;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptCreateTableToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptInsertValuesToken;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.QueryAssistedEncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.expression.DerivedLiteralExpressionSegment;
import org.apache.shardingsphere.infra.binder.segment.insert.values.expression.DerivedParameterMarkerExpressionSegment;
import org.apache.shardingsphere.infra.binder.segment.insert.values.expression.DerivedSimpleExpressionSegment;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.PreviousSQLTokensAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.InsertValue;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.InsertValuesToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.UseDefaultInsertColumnsToken;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;

import java.util.*;

/**
 * Create table token generator for encrypt.
 */
public final class EncryptCreateTableTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator<CreateTableStatementContext> {

    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof CreateTableStatementContext && !(((CreateTableStatementContext) sqlStatementContext).getSqlStatement()).getColumnDefinitions().isEmpty();
    }

    @Override
    public Collection<EncryptCreateTableToken> generateSQLTokens(final CreateTableStatementContext createTableStatementContext) {
          Collection<EncryptCreateTableToken> result = new LinkedList<>();
          String tableName = createTableStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
          Collection<ColumnDefinitionSegment> columnDefinitionSegments = createTableStatementContext.getSqlStatement().getColumnDefinitions();
          for (ColumnDefinitionSegment each : columnDefinitionSegments) {
            if (getEncryptRule().findEncryptor(tableName, each.getColumnName().getIdentifier().getValue()).isPresent()) {
                generateSQLToken(tableName, each).ifPresent(result::add);
            }
        }

//        String tableName = insertStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
//        Collection<InsertValuesSegment> insertValuesSegments = insertStatementContext.getSqlStatement().getValues();
//        InsertValuesToken result = new EncryptInsertValuesToken(getStartIndex(insertValuesSegments), getStopIndex(insertValuesSegments));
//        for (InsertValueContext each : insertStatementContext.getInsertValueContexts()) {
//            InsertValue insertValueToken = new InsertValue(each.getValueExpressions());
//            encryptToken(insertValueToken, tableName, insertStatementContext, each);
//            result.getInsertValues().add(insertValueToken);
//        }
//        return result;
    }

    private Optional<EncryptCreateTableToken> generateSQLToken(final String tableName, final ColumnDefinitionSegment segment){
        if (segment.getValue() instanceof ParameterMarkerExpressionSegment) {
            return Optional.of(generateParameterSQLToken(tableName, assignmentSegment));
        }
        if (segment.getValue() instanceof LiteralExpressionSegment) {
            return Optional.of(generateLiteralSQLToken(tableName, assignmentSegment));
        }
        return Optional.empty();
    }

    private int getStartIndex(final Collection<InsertValuesSegment> segments) {
        int result = segments.iterator().next().getStartIndex();
        for (InsertValuesSegment each : segments) {
            result = Math.min(result, each.getStartIndex());
        }
        return result;
    }

    private int getStopIndex(final Collection<InsertValuesSegment> segments) {
        int result = segments.iterator().next().getStopIndex();
        for (InsertValuesSegment each : segments) {
            result = Math.max(result, each.getStopIndex());
        }
        return result;
    }

    private void encryptToken(final InsertValue insertValueToken, final String tableName, final InsertStatementContext insertStatementContext, final InsertValueContext insertValueContext) {
        Optional<SQLToken> useDefaultInsertColumnsToken = findPreviousSQLToken(UseDefaultInsertColumnsToken.class);
        Iterator<String> descendingColumnNames = insertStatementContext.getDescendingColumnNames();
        while (descendingColumnNames.hasNext()) {
            String columnName = descendingColumnNames.next();
            Optional<EncryptAlgorithm> encryptor = getEncryptRule().findEncryptor(tableName, columnName);
            if (encryptor.isPresent()) {
                int columnIndex = useDefaultInsertColumnsToken.map(sqlToken -> ((UseDefaultInsertColumnsToken) sqlToken).getColumns().indexOf(columnName))
                        .orElseGet(() -> insertStatementContext.getColumnNames().indexOf(columnName));
                Object originalValue = insertValueContext.getValue(columnIndex);
                addPlainColumn(insertValueToken, columnIndex, tableName, columnName, insertValueContext, originalValue);
                addAssistedQueryColumn(insertValueToken, encryptor.get(), columnIndex, tableName, columnName, insertValueContext, originalValue);
                setCipherColumn(insertValueToken, encryptor.get(), columnIndex, insertValueContext.getValueExpressions().get(columnIndex), originalValue);
            }
        }
    }

    private void addPlainColumn(final InsertValue insertValueToken, final int columnIndex,
                                final String tableName, final String columnName, final InsertValueContext insertValueContext, final Object originalValue) {
        if (getEncryptRule().findPlainColumn(tableName, columnName).isPresent()) {
            DerivedSimpleExpressionSegment derivedExpressionSegment = isAddLiteralExpressionSegment(insertValueContext, columnIndex)
                    ? new DerivedLiteralExpressionSegment(originalValue) : new DerivedParameterMarkerExpressionSegment(getParameterIndexCount(insertValueToken));
            insertValueToken.getValues().add(columnIndex + 1, derivedExpressionSegment);
        }
    }

    private void addAssistedQueryColumn(final InsertValue insertValueToken, final EncryptAlgorithm encryptAlgorithm, final int columnIndex,
                                        final String tableName, final String columnName, final InsertValueContext insertValueContext, final Object originalValue) {
        if (getEncryptRule().findAssistedQueryColumn(tableName, columnName).isPresent()) {
            DerivedSimpleExpressionSegment derivedExpressionSegment = isAddLiteralExpressionSegment(insertValueContext, columnIndex)
                    ? new DerivedLiteralExpressionSegment(((QueryAssistedEncryptAlgorithm) encryptAlgorithm).queryAssistedEncrypt(null == originalValue ? null : originalValue.toString()))
                    : new DerivedParameterMarkerExpressionSegment(getParameterIndexCount(insertValueToken));
            insertValueToken.getValues().add(columnIndex + 1, derivedExpressionSegment);
        }
    }

    private boolean isAddLiteralExpressionSegment(final InsertValueContext insertValueContext, final int columnIndex) {
        return insertValueContext.getParameters().isEmpty()
                || insertValueContext.getValueExpressions().get(columnIndex) instanceof LiteralExpressionSegment;
    }

    private int getParameterIndexCount(final InsertValue insertValueToken) {
        int result = 0;
        for (ExpressionSegment each : insertValueToken.getValues()) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                result++;
            }
        }
        return result;
    }

    private void setCipherColumn(final InsertValue insertValueToken,
                                 final EncryptAlgorithm encryptAlgorithm, final int columnIndex, final ExpressionSegment valueExpression, final Object originalValue) {
        if (valueExpression instanceof LiteralExpressionSegment) {
            insertValueToken.getValues().set(columnIndex, new LiteralExpressionSegment(valueExpression.getStartIndex(), valueExpression.getStopIndex(), encryptAlgorithm.encrypt(originalValue)));
        }
    }
}
