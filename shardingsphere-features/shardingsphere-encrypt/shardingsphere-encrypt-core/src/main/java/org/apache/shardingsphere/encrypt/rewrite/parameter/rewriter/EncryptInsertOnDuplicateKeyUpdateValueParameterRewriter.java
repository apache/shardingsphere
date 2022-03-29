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
import org.apache.shardingsphere.encrypt.rewrite.aware.SchemaNameAware;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.aware.EncryptRuleAware;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.QueryAssistedEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.OnDuplicateUpdateContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
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
public final class EncryptInsertOnDuplicateKeyUpdateValueParameterRewriter implements ParameterRewriter<InsertStatementContext>, EncryptRuleAware, SchemaNameAware {
    
    private EncryptRule encryptRule;
    
    private String schemaName;
    
    @Override
    public boolean isNeedRewrite(final SQLStatementContext<?> sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext
                && InsertStatementHandler.getOnDuplicateKeyColumnsSegment(((InsertStatementContext) sqlStatementContext).getSqlStatement()).isPresent();
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void rewrite(final ParameterBuilder parameterBuilder, final InsertStatementContext insertStatementContext, final List<Object> parameters) {
        String tableName = insertStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        GroupedParameterBuilder groupedParameterBuilder = (GroupedParameterBuilder) parameterBuilder;
        OnDuplicateUpdateContext onDuplicateKeyUpdateValueContext = insertStatementContext.getOnDuplicateKeyUpdateValueContext();
        for (int index = 0; index < onDuplicateKeyUpdateValueContext.getValueExpressions().size(); index++) {
            int columnIndex = index;
            String encryptLogicColumnName = onDuplicateKeyUpdateValueContext.getColumn(columnIndex).getIdentifier().getValue();
            EncryptContext encryptContext = EncryptContextBuilder.build(schemaName, tableName, encryptLogicColumnName, encryptRule);
            Optional<EncryptAlgorithm> encryptor = encryptRule.findEncryptor(tableName, encryptLogicColumnName);
            encryptor.ifPresent(optional -> {
                Object plainColumnValue = onDuplicateKeyUpdateValueContext.getValue(columnIndex);
                if (plainColumnValue instanceof FunctionSegment && "VALUES".equalsIgnoreCase(((FunctionSegment) plainColumnValue).getFunctionName())) {
                    return;
                }
                Object cipherColumnValue = encryptor.get().encrypt(plainColumnValue, encryptContext);
                groupedParameterBuilder.getGenericParameterBuilder().addReplacedParameters(columnIndex, cipherColumnValue);
                Collection<Object> addedParameters = new LinkedList<>();
                if (optional instanceof QueryAssistedEncryptAlgorithm) {
                    Optional<String> assistedColumnName = encryptRule.findAssistedQueryColumn(tableName, encryptLogicColumnName);
                    Preconditions.checkArgument(assistedColumnName.isPresent(), "Can not find assisted query Column Name");
                    addedParameters.add(((QueryAssistedEncryptAlgorithm) optional).queryAssistedEncrypt(plainColumnValue, encryptContext));
                }
                if (encryptRule.findPlainColumn(tableName, encryptLogicColumnName).isPresent()) {
                    addedParameters.add(plainColumnValue);
                }
                if (!addedParameters.isEmpty()) {
                    if (!groupedParameterBuilder.getGenericParameterBuilder().getAddedIndexAndParameters().containsKey(columnIndex + 1)) {
                        groupedParameterBuilder.getGenericParameterBuilder().getAddedIndexAndParameters().put(columnIndex + 1, new LinkedList<>());
                    }
                    groupedParameterBuilder.getGenericParameterBuilder().getAddedIndexAndParameters().get(columnIndex + 1).addAll(addedParameters);
                }
            });
        }
    }
}
