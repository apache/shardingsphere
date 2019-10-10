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

package org.apache.shardingsphere.core.rewrite.feature.encrypt.parameter.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.segment.insert.InsertValueContext;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.spi.encrypt.ShardingQueryAssistedEncryptor;

import java.util.Iterator;
import java.util.List;

/**
 * Insert value parameter rewriter for encrypt.
 *
 * @author zhangliang
 */
@Setter
public final class EncryptInsertValueParameterRewriter implements ParameterRewriter, EncryptRuleAware {
    
    private EncryptRule encryptRule;
    
    @Override
    public void rewrite(final ParameterBuilder parameterBuilder, final SQLStatementContext sqlStatementContext, final List<Object> parameters) {
        if (!(sqlStatementContext instanceof InsertSQLStatementContext) || parameters.isEmpty()) {
            return;
        }
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
        if (!encryptTable.isPresent()) {
            return;
        }
        for (String each : encryptTable.get().getLogicColumns()) {
            Optional<ShardingEncryptor> shardingEncryptor = encryptRule.findShardingEncryptor(tableName, each);
            if (shardingEncryptor.isPresent()) {
                encryptInsertValues((GroupedParameterBuilder) parameterBuilder, (InsertSQLStatementContext) sqlStatementContext, encryptRule, shardingEncryptor.get(), tableName, each);
            }
        }
    }
    
    private void encryptInsertValues(final GroupedParameterBuilder parameterBuilder, final InsertSQLStatementContext sqlStatementContext, 
                                     final EncryptRule encryptRule, final ShardingEncryptor shardingEncryptor, final String tableName, final String encryptLogicColumnName) {
        int columnIndex = sqlStatementContext.getColumnNames().indexOf(encryptLogicColumnName);
        Iterator<InsertValueContext> insertValueContexts = sqlStatementContext.getInsertValueContexts().iterator();
        int count = 0;
        for (List<Object> each : sqlStatementContext.getGroupedParameters()) {
            if (!each.isEmpty()) {
                encryptInsertValue(encryptRule, shardingEncryptor, tableName, columnIndex, each.size(), 
                        insertValueContexts.next().getValue(columnIndex), parameterBuilder.getParameterBuilders().get(count), encryptLogicColumnName);
            }
            count++;
        }
    }
    
    private void encryptInsertValue(final EncryptRule encryptRule, final ShardingEncryptor shardingEncryptor, final String tableName, final int columnIndex, final int parameterSize,
                                    final Object originalValue, final StandardParameterBuilder parameterBuilder, final String encryptLogicColumnName) {
        // FIXME: can process all part of insert value is ? or literal, can not process mix ? and literal
        // For example: values (?, ?), (1, 1) can process
        // For example: values (?, 1), (?, 2) can not process
        parameterBuilder.getReplacedIndexAndParameters().put(columnIndex, shardingEncryptor.encrypt(originalValue));
        if (shardingEncryptor instanceof ShardingQueryAssistedEncryptor) {
            Optional<String> assistedColumnName = encryptRule.findAssistedQueryColumn(tableName, encryptLogicColumnName);
            Preconditions.checkArgument(assistedColumnName.isPresent(), "Can not find assisted query Column Name");
            parameterBuilder.getAddedIndexAndParameters().put(parameterSize + parameterBuilder.getAddedIndexAndParameters().size(), 
                    ((ShardingQueryAssistedEncryptor) shardingEncryptor).queryAssistedEncrypt(originalValue.toString()));
        }
        if (encryptRule.findPlainColumn(tableName, encryptLogicColumnName).isPresent()) {
            parameterBuilder.getAddedIndexAndParameters().put(parameterSize + parameterBuilder.getAddedIndexAndParameters().size(), originalValue);
        }
    }
}
