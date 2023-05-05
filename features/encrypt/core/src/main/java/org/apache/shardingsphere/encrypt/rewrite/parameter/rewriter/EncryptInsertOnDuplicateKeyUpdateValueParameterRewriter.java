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

import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.api.context.EncryptContext;
import org.apache.shardingsphere.encrypt.context.EncryptContextBuilder;
import org.apache.shardingsphere.encrypt.rewrite.aware.DatabaseNameAware;
import org.apache.shardingsphere.encrypt.rewrite.aware.EncryptRuleAware;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.LikeEncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.segment.insert.values.OnDuplicateUpdateContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Insert on duplicate key update parameter rewriter for encrypt.
 */
@Setter
public final class EncryptInsertOnDuplicateKeyUpdateValueParameterRewriter implements ParameterRewriter<InsertStatementContext>, EncryptRuleAware, DatabaseNameAware {
    
    private EncryptRule encryptRule;
    
    private String databaseName;
    
    @Override
    public boolean isNeedRewrite(final SQLStatementContext<?> sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext
                && InsertStatementHandler.getOnDuplicateKeyColumnsSegment(((InsertStatementContext) sqlStatementContext).getSqlStatement()).isPresent();
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void rewrite(final ParameterBuilder paramBuilder, final InsertStatementContext insertStatementContext, final List<Object> params) {
        String tableName = insertStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        GroupedParameterBuilder groupedParamBuilder = (GroupedParameterBuilder) paramBuilder;
        OnDuplicateUpdateContext onDuplicateKeyUpdateValueContext = insertStatementContext.getOnDuplicateKeyUpdateValueContext();
        String schemaName = insertStatementContext.getTablesContext().getSchemaName().orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(insertStatementContext.getDatabaseType(), databaseName));
        for (int index = 0; index < onDuplicateKeyUpdateValueContext.getValueExpressions().size(); index++) {
            String encryptLogicColumnName = onDuplicateKeyUpdateValueContext.getColumn(index).getIdentifier().getValue();
            Optional<EncryptAlgorithm> encryptor = encryptRule.findEncryptor(tableName, encryptLogicColumnName);
            if (!encryptor.isPresent()) {
                continue;
            }
            Object plainValue = onDuplicateKeyUpdateValueContext.getValue(index);
            if (plainValue instanceof FunctionSegment && "VALUES".equalsIgnoreCase(((FunctionSegment) plainValue).getFunctionName())) {
                return;
            }
            EncryptContext encryptContext = EncryptContextBuilder.build(databaseName, schemaName, tableName, encryptLogicColumnName);
            Object cipherColumnValue = encryptor.get().encrypt(plainValue, encryptContext);
            groupedParamBuilder.getGenericParameterBuilder().addReplacedParameters(index, cipherColumnValue);
            Collection<Object> addedParams = buildAddedParams(tableName, encryptLogicColumnName, plainValue, encryptContext);
            if (!addedParams.isEmpty()) {
                if (!groupedParamBuilder.getGenericParameterBuilder().getAddedIndexAndParameters().containsKey(index)) {
                    groupedParamBuilder.getGenericParameterBuilder().getAddedIndexAndParameters().put(index, new LinkedList<>());
                }
                groupedParamBuilder.getGenericParameterBuilder().getAddedIndexAndParameters().get(index).addAll(addedParams);
            }
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Collection<Object> buildAddedParams(final String tableName, final String logicColumnName, final Object plainValue, final EncryptContext encryptContext) {
        Collection<Object> result = new LinkedList<>();
        Optional<EncryptAlgorithm> assistedQueryEncryptor = encryptRule.findAssistedQueryEncryptor(tableName, logicColumnName);
        if (assistedQueryEncryptor.isPresent()) {
            Optional<String> assistedColumnName = encryptRule.findAssistedQueryColumn(tableName, logicColumnName);
            Preconditions.checkArgument(assistedColumnName.isPresent(), "Can not find assisted query Column Name");
            result.add(assistedQueryEncryptor.get().encrypt(plainValue, encryptContext));
        }
        Optional<LikeEncryptAlgorithm> likeQueryEncryptor = encryptRule.findLikeQueryEncryptor(tableName, logicColumnName);
        if (likeQueryEncryptor.isPresent()) {
            Optional<String> likeColumnName = encryptRule.findLikeQueryColumn(tableName, logicColumnName);
            Preconditions.checkArgument(likeColumnName.isPresent(), "Can not find assisted query Column Name");
            result.add(likeQueryEncryptor.get().encrypt(plainValue, encryptContext));
        }
        return result;
    }
}
