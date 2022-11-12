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
import org.apache.shardingsphere.encrypt.context.EncryptContextBuilder;
import org.apache.shardingsphere.encrypt.rewrite.aware.DatabaseNameAware;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.aware.EncryptRuleAware;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;
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
            int columnIndex = index;
            String encryptLogicColumnName = onDuplicateKeyUpdateValueContext.getColumn(columnIndex).getIdentifier().getValue();
            EncryptContext encryptContext = EncryptContextBuilder.build(databaseName, schemaName, tableName, encryptLogicColumnName);
            Optional<EncryptAlgorithm> encryptor = encryptRule.findEncryptor(tableName, encryptLogicColumnName);
            encryptor.ifPresent(optional -> {
                Object plainColumnValue = onDuplicateKeyUpdateValueContext.getValue(columnIndex);
                if (plainColumnValue instanceof FunctionSegment && "VALUES".equalsIgnoreCase(((FunctionSegment) plainColumnValue).getFunctionName())) {
                    return;
                }
                Object cipherColumnValue = encryptor.get().encrypt(plainColumnValue, encryptContext);
                groupedParamBuilder.getGenericParameterBuilder().addReplacedParameters(columnIndex, cipherColumnValue);
                Collection<Object> addedParams = new LinkedList<>();
                Optional<EncryptAlgorithm> assistedQueryEncryptor = encryptRule.findAssistedQueryEncryptor(tableName, encryptLogicColumnName);
                if (assistedQueryEncryptor.isPresent()) {
                    Optional<String> assistedColumnName = encryptRule.findAssistedQueryColumn(tableName, encryptLogicColumnName);
                    Preconditions.checkArgument(assistedColumnName.isPresent(), "Can not find assisted query Column Name");
                    addedParams.add(assistedQueryEncryptor.get().encrypt(plainColumnValue, encryptContext));
                }
                Optional<EncryptAlgorithm> likeQueryEncryptor = encryptRule.findLikeQueryEncryptor(tableName, encryptLogicColumnName);
                if (likeQueryEncryptor.isPresent()) {
                    Optional<String> likeColumnName = encryptRule.findLikeQueryColumn(tableName, encryptLogicColumnName);
                    Preconditions.checkArgument(likeColumnName.isPresent(), "Can not find assisted query Column Name");
                    addedParams.add(likeQueryEncryptor.get().encrypt(plainColumnValue, encryptContext));
                }
                if (encryptRule.findPlainColumn(tableName, encryptLogicColumnName).isPresent()) {
                    addedParams.add(plainColumnValue);
                }
                if (!addedParams.isEmpty()) {
                    if (!groupedParamBuilder.getGenericParameterBuilder().getAddedIndexAndParameters().containsKey(columnIndex)) {
                        groupedParamBuilder.getGenericParameterBuilder().getAddedIndexAndParameters().put(columnIndex, new LinkedList<>());
                    }
                    groupedParamBuilder.getGenericParameterBuilder().getAddedIndexAndParameters().get(columnIndex).addAll(addedParams);
                }
            });
        }
    }
}
