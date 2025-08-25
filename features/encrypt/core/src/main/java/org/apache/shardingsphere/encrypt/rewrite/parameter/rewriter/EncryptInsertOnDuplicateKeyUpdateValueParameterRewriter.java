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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.OnDuplicateUpdateContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert on duplicate key update parameter rewriter for encrypt.
 */
@RequiredArgsConstructor
public final class EncryptInsertOnDuplicateKeyUpdateValueParameterRewriter implements ParameterRewriter {
    
    private final EncryptRule rule;
    
    private final String databaseName;
    
    @Override
    public boolean isNeedRewrite(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext
                && (((InsertStatementContext) sqlStatementContext).getSqlStatement()).getOnDuplicateKeyColumns().isPresent();
    }
    
    @Override
    public void rewrite(final ParameterBuilder paramBuilder, final SQLStatementContext sqlStatementContext, final List<Object> params) {
        InsertStatementContext insertStatementContext = (InsertStatementContext) sqlStatementContext;
        String tableName = insertStatementContext.getSqlStatement().getTable().map(optional -> optional.getTableName().getIdentifier().getValue()).orElse("");
        GroupedParameterBuilder groupedParamBuilder = (GroupedParameterBuilder) paramBuilder;
        OnDuplicateUpdateContext onDuplicateKeyUpdateValueContext = insertStatementContext.getOnDuplicateKeyUpdateValueContext();
        String schemaName = insertStatementContext.getTablesContext().getSchemaName()
                .orElseGet(() -> new DatabaseTypeRegistry(insertStatementContext.getSqlStatement().getDatabaseType()).getDefaultSchemaName(databaseName));
        for (int index = 0; index < onDuplicateKeyUpdateValueContext.getValueExpressions().size(); index++) {
            String logicColumnName = onDuplicateKeyUpdateValueContext.getColumn(index).getIdentifier().getValue();
            if (!rule.findEncryptTable(tableName).map(optional -> optional.isEncryptColumn(logicColumnName)).orElse(false)) {
                continue;
            }
            Object plainValue = onDuplicateKeyUpdateValueContext.getValue(index);
            if (plainValue instanceof FunctionSegment && "VALUES".equalsIgnoreCase(((FunctionSegment) plainValue).getFunctionName())) {
                return;
            }
            EncryptColumn encryptColumn = rule.getEncryptTable(tableName).getEncryptColumn(logicColumnName);
            Object cipherColumnValue = encryptColumn.getCipher().encrypt(databaseName, schemaName, tableName, logicColumnName, plainValue);
            groupedParamBuilder.getAfterGenericParameterBuilder().addReplacedParameters(index, cipherColumnValue);
            Collection<Object> addedParams = buildAddedParams(schemaName, tableName, encryptColumn, logicColumnName, plainValue);
            if (!addedParams.isEmpty()) {
                if (!groupedParamBuilder.getAfterGenericParameterBuilder().getAddedIndexAndParameters().containsKey(index)) {
                    groupedParamBuilder.getAfterGenericParameterBuilder().getAddedIndexAndParameters().put(index, new LinkedList<>());
                }
                groupedParamBuilder.getAfterGenericParameterBuilder().getAddedIndexAndParameters().get(index).addAll(addedParams);
            }
        }
    }
    
    private Collection<Object> buildAddedParams(final String schemaName, final String tableName, final EncryptColumn encryptColumn, final String logicColumnName, final Object plainValue) {
        Collection<Object> result = new LinkedList<>();
        if (encryptColumn.getAssistedQuery().isPresent()) {
            result.add(encryptColumn.getAssistedQuery().get().encrypt(databaseName, schemaName, tableName, logicColumnName, plainValue));
        }
        if (encryptColumn.getLikeQuery().isPresent()) {
            result.add(encryptColumn.getLikeQuery().get().encrypt(databaseName, schemaName, tableName, logicColumnName, plainValue));
        }
        return result;
    }
}
