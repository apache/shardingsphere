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

package org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter;

import lombok.Setter;
import org.apache.shardingsphere.encrypt.rewrite.aware.DatabaseNameAware;
import org.apache.shardingsphere.encrypt.rewrite.aware.EncryptRuleAware;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Insert value parameter rewriter for encrypt.
 */
@Setter
public final class EncryptInsertValueParameterRewriter implements ParameterRewriter, EncryptRuleAware, DatabaseNameAware {
    
    private EncryptRule encryptRule;
    
    private String databaseName;
    
    @Override
    public boolean isNeedRewrite(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext && !InsertStatementHandler.getSetAssignmentSegment(((InsertStatementContext) sqlStatementContext).getSqlStatement()).isPresent()
                && null == ((InsertStatementContext) sqlStatementContext).getInsertSelectContext();
    }
    
    @Override
    public void rewrite(final ParameterBuilder paramBuilder, final SQLStatementContext sqlStatementContext, final List<Object> params) {
        InsertStatementContext insertStatementContext = (InsertStatementContext) sqlStatementContext;
        String tableName = insertStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
        if (!encryptTable.isPresent()) {
            return;
        }
        Iterator<String> descendingColumnNames = insertStatementContext.getDescendingColumnNames();
        String schemaName = insertStatementContext.getTablesContext().getSchemaName().orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(insertStatementContext.getDatabaseType(), databaseName));
        while (descendingColumnNames.hasNext()) {
            String columnName = descendingColumnNames.next();
            if (encryptRule.findEncryptTable(tableName).map(optional -> optional.isEncryptColumn(columnName)).orElse(false)) {
                encryptInsertValues((GroupedParameterBuilder) paramBuilder, insertStatementContext, schemaName, tableName, columnName);
            }
        }
    }
    
    private void encryptInsertValues(final GroupedParameterBuilder paramBuilder, final InsertStatementContext insertStatementContext,
                                     final String schemaName, final String tableName, final String columnName) {
        EncryptColumn encryptColumn = encryptRule.getEncryptTable(tableName).getEncryptColumn(columnName);
        int columnIndex = getColumnIndex(paramBuilder, insertStatementContext, columnName);
        int count = 0;
        for (List<Object> each : insertStatementContext.getGroupedParameters()) {
            int paramIndex = insertStatementContext.getInsertValueContexts().get(count).getParameterIndex(columnIndex);
            if (!each.isEmpty()) {
                StandardParameterBuilder standardParamBuilder = paramBuilder.getParameterBuilders().get(count);
                ExpressionSegment expressionSegment = insertStatementContext.getInsertValueContexts().get(count).getValueExpressions().get(columnIndex);
                if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
                    Object literalValue = insertStatementContext.getInsertValueContexts().get(count).getLiteralValue(columnIndex).orElse(null);
                    encryptInsertValue(encryptColumn, paramIndex, literalValue, standardParamBuilder, schemaName, tableName);
                }
            }
            count++;
        }
    }
    
    private int getColumnIndex(final GroupedParameterBuilder paramBuilder, final InsertStatementContext insertStatementContext, final String encryptLogicColumnName) {
        List<String> columnNames;
        if (paramBuilder.getDerivedColumnName().isPresent()) {
            columnNames = new ArrayList<>(insertStatementContext.getColumnNames());
            columnNames.remove(paramBuilder.getDerivedColumnName().get());
        } else {
            columnNames = insertStatementContext.getColumnNames();
        }
        return columnNames.indexOf(encryptLogicColumnName);
    }
    
    private void encryptInsertValue(final EncryptColumn encryptColumn, final int paramIndex, final Object originalValue,
                                    final StandardParameterBuilder paramBuilder, final String schemaName, final String tableName) {
        String columnName = encryptColumn.getName();
        paramBuilder.addReplacedParameters(paramIndex, encryptColumn.getCipher().encrypt(databaseName, schemaName, tableName, columnName, originalValue));
        Collection<Object> addedParams = new LinkedList<>();
        if (encryptColumn.getAssistedQuery().isPresent()) {
            addedParams.add(encryptColumn.getAssistedQuery().get().encrypt(databaseName, schemaName, tableName, columnName, originalValue));
        }
        if (encryptColumn.getLikeQuery().isPresent()) {
            addedParams.add(encryptColumn.getLikeQuery().get().encrypt(databaseName, schemaName, tableName, columnName, originalValue));
        }
        if (!addedParams.isEmpty()) {
            if (!paramBuilder.getAddedIndexAndParameters().containsKey(paramIndex)) {
                paramBuilder.getAddedIndexAndParameters().put(paramIndex, new LinkedList<>());
            }
            paramBuilder.getAddedIndexAndParameters().get(paramIndex).addAll(addedParams);
        }
    }
}
