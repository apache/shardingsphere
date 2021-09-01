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

package org.apache.shardingsphere.encrypt.rewrite.parameter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.shardingsphere.encrypt.rewrite.parameter.EncryptParameterRewriter;
import org.apache.shardingsphere.encrypt.rule.EncryptContext;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.QueryAssistedEncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * Insert value parameter rewriter for encrypt.
 */
public final class EncryptInsertValueParameterRewriter extends EncryptParameterRewriter<InsertStatementContext> {

    @Override
    protected boolean isNeedRewriteForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext && !InsertStatementHandler.getSetAssignmentSegment(((InsertStatementContext) sqlStatementContext).getSqlStatement()).isPresent();
    }

    @Override
    public void rewrite(final ParameterBuilder parameterBuilder, final InsertStatementContext insertStatementContext, final List<Object> parameters) {
        SimpleTableSegment simpleTableSegment = insertStatementContext.getSqlStatement().getTable();
        String tableName = simpleTableSegment.getTableName().getIdentifier().getValue();
        Iterator<String> descendingColumnNames = insertStatementContext.getDescendingColumnNames();
        while (descendingColumnNames.hasNext()) {
            String columnName = descendingColumnNames.next();
            getEncryptRule().findEncryptor(tableName, columnName).ifPresent(
                encryptAlgorithm -> encryptInsertValues((GroupedParameterBuilder) parameterBuilder, insertStatementContext, encryptAlgorithm, simpleTableSegment, columnName));
        }
    }

    private void encryptInsertValues(final GroupedParameterBuilder parameterBuilder,
                                     final InsertStatementContext insertStatementContext, final EncryptAlgorithm encryptAlgorithm, final SimpleTableSegment simpleTableSegment, final String encryptLogicColumnName) {
        int columnIndex = getColumnIndex(parameterBuilder, insertStatementContext, encryptLogicColumnName);
        int count = 0;
        for (List<Object> each : insertStatementContext.getGroupedParameters()) {
            int parameterIndex = insertStatementContext.getInsertValueContexts().get(count).getParameterIndex(columnIndex);
            if (!each.isEmpty()) {
                StandardParameterBuilder standardParameterBuilder = parameterBuilder.getParameterBuilders().get(count);
                ExpressionSegment expressionSegment = insertStatementContext.getInsertValueContexts().get(count).getValueExpressions().get(columnIndex);
                if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
                    encryptInsertValue(
                            encryptAlgorithm, simpleTableSegment, parameterIndex, insertStatementContext.getInsertValueContexts().get(count).getValue(columnIndex),
                            standardParameterBuilder, encryptLogicColumnName);
                }
            }
            count++;
        }
    }

    private int getColumnIndex(final GroupedParameterBuilder parameterBuilder, final InsertStatementContext insertStatementContext, final String encryptLogicColumnName) {
        List<String> columnNames;
        if (parameterBuilder.getDerivedColumnName().isPresent()) {
            columnNames = new ArrayList<>(insertStatementContext.getColumnNames());
            columnNames.remove(parameterBuilder.getDerivedColumnName().get());
        } else {
            columnNames = insertStatementContext.getColumnNames();
        }
        return columnNames.indexOf(encryptLogicColumnName);
    }

    private void encryptInsertValue(final EncryptAlgorithm encryptAlgorithm, final SimpleTableSegment simpleTableSegment, final int parameterIndex,
                                    final Object originalValue, final StandardParameterBuilder parameterBuilder, final String encryptLogicColumnName) {
        String tableName = simpleTableSegment.getTableName().getIdentifier().getValue();
        Optional<EncryptTable> encryptTable = getEncryptRule().findEncryptTable(tableName);
        Map<String, String> encryptContextMap = new EncryptContext(encryptTable, simpleTableSegment, encryptLogicColumnName).of();
        parameterBuilder.addReplacedParameters(parameterIndex, encryptAlgorithm.encrypt(originalValue, encryptContextMap));
        Collection<Object> addedParameters = new LinkedList<>();
        if (encryptAlgorithm instanceof QueryAssistedEncryptAlgorithm) {
            Optional<String> assistedColumnName = getEncryptRule().findAssistedQueryColumn(tableName, encryptLogicColumnName);
            Preconditions.checkArgument(assistedColumnName.isPresent(), "Can not find assisted query Column Name");
            addedParameters.add(((QueryAssistedEncryptAlgorithm) encryptAlgorithm).queryAssistedEncrypt(originalValue.toString(), encryptContextMap));
        }
        if (getEncryptRule().findPlainColumn(tableName, encryptLogicColumnName).isPresent()) {
            addedParameters.add(originalValue);
        }
        if (!addedParameters.isEmpty()) {
            if (!parameterBuilder.getAddedIndexAndParameters().containsKey(parameterIndex + 1)) {
                parameterBuilder.getAddedIndexAndParameters().put(parameterIndex + 1, new LinkedList<>());
            }
            parameterBuilder.getAddedIndexAndParameters().get(parameterIndex + 1).addAll(addedParameters);
        }
    }
}
