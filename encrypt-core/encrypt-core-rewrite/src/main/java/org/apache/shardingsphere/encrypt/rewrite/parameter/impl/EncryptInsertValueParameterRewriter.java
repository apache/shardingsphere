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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.encrypt.rewrite.parameter.EncryptParameterRewriter;
import org.apache.shardingsphere.encrypt.strategy.spi.Encryptor;
import org.apache.shardingsphere.encrypt.strategy.spi.QueryAssistedEncryptor;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.underlying.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.underlying.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.underlying.rewrite.parameter.builder.impl.StandardParameterBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert value parameter rewriter for encrypt.
 *
 * @author zhangliang
 */
public final class EncryptInsertValueParameterRewriter extends EncryptParameterRewriter {
    
    @Override
    protected boolean isNeedRewriteForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertSQLStatementContext && !((InsertStatement) sqlStatementContext.getSqlStatement()).getSetAssignment().isPresent();
    }
    
    @Override
    public void rewrite(final ParameterBuilder parameterBuilder, final SQLStatementContext sqlStatementContext, final List<Object> parameters) {
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        Iterator<String> descendingColumnNames = ((InsertSQLStatementContext) sqlStatementContext).getDescendingColumnNames();
        while (descendingColumnNames.hasNext()) {
            String columnName = descendingColumnNames.next();
            Optional<Encryptor> encryptor = getEncryptRule().findEncryptor(tableName, columnName);
            if (encryptor.isPresent()) {
                encryptInsertValues((GroupedParameterBuilder) parameterBuilder, (InsertSQLStatementContext) sqlStatementContext, encryptor.get(), tableName, columnName);
            }
        }
    }
    
    private void encryptInsertValues(final GroupedParameterBuilder parameterBuilder,
                                     final InsertSQLStatementContext sqlStatementContext, final Encryptor encryptor, final String tableName, final String encryptLogicColumnName) {
        int columnIndex = getColumnIndex(parameterBuilder, sqlStatementContext, encryptLogicColumnName);
        int count = 0;
        for (List<Object> each : sqlStatementContext.getGroupedParameters()) {
            if (!each.isEmpty()) {
                StandardParameterBuilder standardParameterBuilder = parameterBuilder.getParameterBuilders().get(count);
                encryptInsertValue(
                        encryptor, tableName, columnIndex, sqlStatementContext.getInsertValueContexts().get(count).getValue(columnIndex), standardParameterBuilder, encryptLogicColumnName);
            }
            count++;
        }
    }
    
    private int getColumnIndex(final GroupedParameterBuilder parameterBuilder, final InsertSQLStatementContext sqlStatementContext, final String encryptLogicColumnName) {
        List<String> columnNames;
        if (parameterBuilder.getDerivedColumnName().isPresent()) {
            columnNames = new ArrayList<>(sqlStatementContext.getColumnNames());
            columnNames.remove(parameterBuilder.getDerivedColumnName().get());
        } else {
            columnNames = sqlStatementContext.getColumnNames();
        }
        return columnNames.indexOf(encryptLogicColumnName);
    }
    
    private void encryptInsertValue(final Encryptor encryptor, final String tableName, final int columnIndex,
                                    final Object originalValue, final StandardParameterBuilder parameterBuilder, final String encryptLogicColumnName) {
        // FIXME: can process all part of insert value is ? or literal, can not process mix ? and literal
        // For example: values (?, ?), (1, 1) can process
        // For example: values (?, 1), (?, 2) can not process
        parameterBuilder.addReplacedParameters(columnIndex, encryptor.encrypt(originalValue));
        Collection<Object> addedParameters = new LinkedList<>();
        if (encryptor instanceof QueryAssistedEncryptor) {
            Optional<String> assistedColumnName = getEncryptRule().findAssistedQueryColumn(tableName, encryptLogicColumnName);
            Preconditions.checkArgument(assistedColumnName.isPresent(), "Can not find assisted query Column Name");
            addedParameters.add(((QueryAssistedEncryptor) encryptor).queryAssistedEncrypt(originalValue.toString()));
        }
        if (getEncryptRule().findPlainColumn(tableName, encryptLogicColumnName).isPresent()) {
            addedParameters.add(originalValue);
        }
        if (!addedParameters.isEmpty()) {
            if (!parameterBuilder.getAddedIndexAndParameters().containsKey(columnIndex + 1)) {
                parameterBuilder.getAddedIndexAndParameters().put(columnIndex + 1, new LinkedList<>());
            }
            parameterBuilder.getAddedIndexAndParameters().get(columnIndex + 1).addAll(addedParameters);
        }
    }
}
